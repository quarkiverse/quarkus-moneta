package io.quarkiverse.moneta.deployment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.money.convert.ExchangeRateProvider;
import javax.money.spi.CurrencyProviderSpi;
import javax.money.spi.MonetaryAmountFactoryProviderSpi;
import javax.money.spi.MonetaryAmountFormatProviderSpi;
import javax.money.spi.MonetaryAmountsSingletonQuerySpi;
import javax.money.spi.MonetaryAmountsSingletonSpi;
import javax.money.spi.MonetaryConversionsSingletonSpi;
import javax.money.spi.MonetaryCurrenciesSingletonSpi;
import javax.money.spi.MonetaryFormatsSingletonSpi;
import javax.money.spi.MonetaryRoundingsSingletonSpi;
import javax.money.spi.RoundingProviderSpi;
import javax.money.spi.ServiceProvider;

import org.javamoney.moneta.convert.IdentityRateProvider;
import org.javamoney.moneta.convert.imf.IMFHistoricRateProvider;
import org.javamoney.moneta.convert.imf.IMFRateProvider;
import org.javamoney.moneta.spi.*;
import org.javamoney.moneta.spi.loader.LoaderService;

import io.quarkiverse.moneta.QuarkusConfigProvider;
import io.quarkiverse.moneta.convert.ECBCurrentRateProvider;
import io.quarkiverse.moneta.convert.ECBHistoric90RateProvider;
import io.quarkiverse.moneta.convert.ECBHistoricRateProvider;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
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
    public ExtensionSslNativeSupportBuildItem build() {
        return new ExtensionSslNativeSupportBuildItem(feature);
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
        producer.produce(spiBuildItem(MonetaryConversionsSingletonSpi.class));
        producer.produce(spiBuildItem(MonetaryFormatsSingletonSpi.class));
        producer.produce(spiBuildItem(MonetaryAmountProducer.class));
        producer.produce(spiBuildItem(MonetaryRoundingsSingletonSpi.class));

        producer.produce(spiProviders(MonetaryConfigProvider.class, QuarkusConfigProvider.class));
        producer.produce(spiProviders(ExchangeRateProvider.class, IdentityRateProvider.class, IMFRateProvider.class,
                IMFHistoricRateProvider.class, ECBHistoric90RateProvider.class, ECBCurrentRateProvider.class,
                ECBHistoricRateProvider.class));
    }

    @BuildStep
    void indexDependencies(BuildProducer<IndexDependencyBuildItem> producer) {
        producer.produce(new IndexDependencyBuildItem("javax.money", "money-api"));
        producer.produce(new IndexDependencyBuildItem("org.javamoney", "moneta-core"));
        producer.produce(new IndexDependencyBuildItem("org.javamoney", "moneta-convert"));
        producer.produce(new IndexDependencyBuildItem("org.javamoney", "moneta-imf"));
    }

    @BuildStep
    void exchangeRateResources(BuildProducer<NativeImageResourceBuildItem> resourceProducer,
            BuildProducer<GeneratedResourceBuildItem> generatedResourceProducer) {
        registerResource("org/javamoney/moneta/convert/ecb/defaults/eurofxref-daily.xml",
                "https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/ecb-daily.xml",
                resourceProducer, generatedResourceProducer);
        registerResource("org/javamoney/moneta/convert/ecb/defaults/eurofxref-hist-90d.xml",
                "https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/ecb-historic-90d.xml",
                resourceProducer,
                generatedResourceProducer);
        registerResource("org/javamoney/moneta/convert/ecb/defaults/eurofxref-hist.xml",
                "https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/ecb-historic.xml",
                resourceProducer, generatedResourceProducer);
        registerResource("org/javamoney/moneta/convert/imf/defaults/rms_five.tsv",
                "https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/imf.tsv",
                resourceProducer,
                generatedResourceProducer);
    }

    @BuildStep
    NativeImageResourceBuildItem registerServices() {
        return new NativeImageResourceBuildItem(
                "META-INF/services/javax.money.convert.ExchangeRateProvider",
                "META-INF/services/org.javamoney.moneta.spi.MonetaryConfigProvider");
    }

    @SafeVarargs
    private <P, I extends P> ServiceProviderBuildItem spiProviders(Class<P> provider, Class<? extends I>... implementations) {
        var providerName = provider.getName();
        var implementationNames = Arrays.stream(implementations)
                .map(Class::getName)
                .collect(Collectors.toUnmodifiableList());

        return new ServiceProviderBuildItem(providerName, implementationNames);
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
