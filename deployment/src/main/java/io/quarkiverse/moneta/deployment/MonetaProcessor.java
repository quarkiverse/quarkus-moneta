package io.quarkiverse.moneta.deployment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.money.convert.ExchangeRateProvider;
import javax.money.spi.*;

import org.eclipse.transformer.action.ActionContext;
import org.eclipse.transformer.action.ByteData;
import org.eclipse.transformer.action.impl.*;
import org.eclipse.transformer.util.FileUtils;
import org.javamoney.moneta.spi.MonetaryAmountProducer;
import org.javamoney.moneta.spi.MonetaryConfigProvider;
import org.javamoney.moneta.spi.loader.LoaderService;
import org.objectweb.asm.ClassReader;
import org.slf4j.LoggerFactory;

import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.BytecodeTransformerBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;

class MonetaProcessor {

    private static final String feature = "io.quarkiverse.quarkus-moneta";
    private static final List<String> classesNeedingTransformation = List.of(
            "org.javamoney.moneta.spi.PriorityServiceComparator", "org.javamoney.moneta.spi.MoneyAmountFactoryProvider",
            "org.javamoney.moneta.spi.PriorityAwareServiceProvider", "org.javamoney.moneta.internal.OSGIServiceComparator");

    private static final Logger logger = Logger.getLogger("MonetaProcessor");

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(feature);
    }

    @BuildStep
    void spiRegistration(BuildProducer<ServiceProviderBuildItem> producer) {
        producer.produce(spiBuildItem(MonetaryAmountsSingletonSpi.class));
        producer.produce(spiBuildItem(CurrencyProviderSpi.class));
        producer.produce(spiBuildItem(MonetaryAmountFactoryProviderSpi.class));
        producer.produce(spiBuildItem(MonetaryAmountFormatProviderSpi.class));
        producer.produce(spiBuildItem(MonetaryAmountsSingletonQuerySpi.class));
        producer.produce(spiBuildItem(MonetaryAmountsSingletonSpi.class));
        producer.produce(spiBuildItem(MonetaryCurrenciesSingletonSpi.class));
        producer.produce(spiBuildItem(RoundingProviderSpi.class));
        producer.produce(spiBuildItem(ServiceProvider.class));
        producer.produce(spiBuildItem(LoaderService.class));
        producer.produce(spiBuildItem(MonetaryConfigProvider.class));
        producer.produce(spiBuildItem(MonetaryConversionsSingletonSpi.class));
        producer.produce(spiBuildItem(ExchangeRateProvider.class));
        producer.produce(spiBuildItem(MonetaryFormatsSingletonSpi.class));
        producer.produce(spiBuildItem(MonetaryAmountProducer.class));
        producer.produce(spiBuildItem(MonetaryRoundingsSingletonSpi.class));
    }

    @BuildStep
    void exchangeRateResources(BuildProducer<NativeImageResourceBuildItem> resourceProducer,
            BuildProducer<GeneratedResourceBuildItem> generatedResourceProducer) {
        registerResource("org/javamoney/moneta/convert/ecb/defaults/eurofxref-daily.xml",
                "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml", resourceProducer, generatedResourceProducer);
        registerResource("org/javamoney/moneta/convert/ecb/defaults/eurofxref-hist-90d.xml",
                "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml", resourceProducer,
                generatedResourceProducer);
        registerResource("org/javamoney/moneta/convert/ecb/defaults/eurofxref-hist.xml",
                "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist.xml", resourceProducer, generatedResourceProducer);
        registerResource("org/javamoney/moneta/convert/imf/defaults/rms_five.tsv", "https://www.imf.org/external/np/fin/data/rms_five.aspx?tsvflag=Y", resourceProducer, generatedResourceProducer);
    }

    @BuildStep
    void transformToJakarta(BuildProducer<BytecodeTransformerBuildItem> producer) {
        if (QuarkusClassLoader.isClassPresentAtRuntime("jakarta.annotation.Priority")) {
            var transformer = new JakartaTransformer();

            classesNeedingTransformation.stream()
                    .map(s -> new BytecodeTransformerBuildItem.Builder().setCacheable(true).setContinueOnFailure(false)
                            .setClassToTransform(s).setClassReaderOptions(ClassReader.SKIP_DEBUG)
                            .setInputTransformer(transformer::transform).build())
                    .forEach(producer::produce);
        }
    }

    private ServiceProviderBuildItem spiBuildItem(Class<?> clazz) {
        return ServiceProviderBuildItem.allProvidersFromClassPath(clazz.getName());
    }

    private void registerResource(String resourcePath, String url, BuildProducer<NativeImageResourceBuildItem> resourceProducer,
            BuildProducer<GeneratedResourceBuildItem> generatedResourceProducer) {
        try {
            logger.info("Downloading exchange rates from " + url);
            var data = downloadFile(url);
            generatedResourceProducer.produce(new GeneratedResourceBuildItem(resourcePath, data));
        } catch (Exception e) {
            logger.log(Level.WARNING,
                    "Failed to download exchange rates from " + url + ". Using java money resource " + resourcePath, e);
            resourceProducer.produce(new NativeImageResourceBuildItem(resourcePath));
        }
    }

    private byte[] downloadFile(String url) throws IOException {
        try (var stream = new BufferedInputStream(new URL(url).openStream())) {
            return stream.readAllBytes();
        }
    }

    private static class JakartaTransformer {

        private final org.slf4j.Logger logger;
        private final ActionContext ctx;
        // We need to prevent the Eclipse Transformer to adjust the "javax" packages.
        // Thus why we split the strings.
        private static final Map<String, String> renames = Map.of("javax" + ".annotation", "jakarta.annotation");

        JakartaTransformer() {
            logger = LoggerFactory.getLogger("JakartaTransformer");
            //N.B. we enable only this single transformation of package renames, not the full set of capabilities of Eclipse Transformer;
            //this might need tailoring if the same idea gets applied to a different context.
            ctx = new ActionContextImpl(logger, new SelectionRuleImpl(logger, Collections.emptyMap(), Collections.emptyMap()),
                    new SignatureRuleImpl(logger, renames, null, null, null, null, null, Collections.emptyMap()));
        }

        byte[] transform(final String name, final byte[] bytes) {
            logger.debug("Jakarta EE compatibility enhancer for Quarkus: transforming " + name);
            final ClassActionImpl classTransformer = new ClassActionImpl(ctx);
            final ByteBuffer input = ByteBuffer.wrap(bytes);
            final ByteData inputData = new ByteDataImpl(name, input, FileUtils.DEFAULT_CHARSET);
            final ByteData outputData = classTransformer.apply(inputData);
            return outputData.buffer().array();
        }
    }
}
