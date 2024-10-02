package io.quarkiverse.moneta.deployment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.convert.ExchangeRateProvider;
import javax.money.spi.*;

import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import org.javamoney.moneta.spi.MonetaryAmountProducer;
import org.javamoney.moneta.spi.MonetaryConfigProvider;
import org.javamoney.moneta.spi.loader.LoaderService;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;

class MonetaProcessor {

    private static final String feature = "io.quarkiverse.quarkus-moneta";
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
    AdditionalIndexedClassesBuildItem index() {
        return new AdditionalIndexedClassesBuildItem(
                MonetaryAmount.class.getName(),
                CurrencyUnit.class.getName()
        );
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
        registerResource("org/javamoney/moneta/convert/imf/defaults/rms_five.tsv",
                "https://www.imf.org/external/np/fin/data/rms_five.aspx?tsvflag=Y", resourceProducer,
                generatedResourceProducer);
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
        }

        resourceProducer.produce(new NativeImageResourceBuildItem(resourcePath));
    }

    private byte[] downloadFile(String url) throws IOException {
        try (var stream = new BufferedInputStream(new URL(url).openStream())) {
            return stream.readAllBytes();
        }
    }
}
