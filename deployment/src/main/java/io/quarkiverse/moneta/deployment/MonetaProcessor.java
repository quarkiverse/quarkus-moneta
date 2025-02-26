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

import org.javamoney.moneta.spi.MonetaryAmountProducer;
import org.javamoney.moneta.spi.MonetaryConfigProvider;
import org.javamoney.moneta.spi.loader.LoaderService;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.deployment.pkg.builditem.UberJarMergedResourceBuildItem;
import org.javamoney.moneta.spi.loader.okhttp.OkHttpLoaderService;

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
        producer.produce(spiBuildItem(MonetaryConfigProvider.class));
        producer.produce(spiBuildItem(MonetaryConversionsSingletonSpi.class));
        producer.produce(spiBuildItem(ExchangeRateProvider.class));
        producer.produce(spiBuildItem(MonetaryFormatsSingletonSpi.class));
        producer.produce(spiBuildItem(MonetaryAmountProducer.class));
        producer.produce(spiBuildItem(MonetaryRoundingsSingletonSpi.class));

        producer.produce(spiBuildItem(LoaderService.class, OkHttpLoaderService.class));
    }

    @BuildStep
    void indexDependencies(BuildProducer<IndexDependencyBuildItem> producer) {
        producer.produce(new IndexDependencyBuildItem("javax.money", "money-api"));
        producer.produce(new IndexDependencyBuildItem("org.javamoney", "moneta-core"));
        producer.produce(new IndexDependencyBuildItem("org.javamoney", "moneta-convert"));
        producer.produce(new IndexDependencyBuildItem("org.javamoney", "moneta-ecb"));
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

    @BuildStep(onlyIf = IsNormal.class)
    void uberJarFiles(BuildProducer<UberJarMergedResourceBuildItem> uberJarMergedProducer) {
        uberJarMergedProducer.produce(new UberJarMergedResourceBuildItem("javamoney.properties"));
    }

    @BuildStep
    void javaMoneyProperties(BuildProducer<NativeImageResourceBuildItem> resourceProducer,
                           BuildProducer<GeneratedResourceBuildItem> generatedResourceProducer) {
        var properties = "load.ECBHistoricRateProvider.type=SCHEDULED\n" +
                "load.ECBHistoricRateProvider.period=24:00\n" +
                "load.ECBHistoricRateProvider.delay=01:00\n" +
                "load.ECBHistoricRateProvider.at=07:00\n" +
                "load.ECBHistoricRateProvider.resource=org/javamoney/moneta/convert/ecb/defaults/eurofxref-hist.xml\n" +
                "load.ECBHistoricRateProvider.urls=https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/ecb-historic.xml\n" +
                "load.ECBHistoric90RateProvider.type=SCHEDULED\n" +
                "load.ECBHistoric90RateProvider.period=03:00\n" +
                "load.ECBHistoric90RateProvider.resource=org/javamoney/moneta/convert/ecb/defaults/eurofxref-hist-90d.xml\n" +
                "load.ECBHistoric90RateProvider.urls=https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/ecb-historic-90d.xml\n" +
                "load.ECBCurrentRateProvider.type=SCHEDULED\n" +
                "load.ECBCurrentRateProvider.period=03:00\n" +
                "load.ECBCurrentRateProvider.resource=org/javamoney/moneta/convert/ecb/defaults/eurofxref-daily.xml\n" +
                "load.ECBCurrentRateProvider.urls=https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/ecb-daily.xml\n" +
                "load.IMFRateProvider.type=SCHEDULED\n" +
                "load.IMFRateProvider.period=06:00\n" +
                "load.IMFRateProvider.resource=org/javamoney/moneta/convert/imf/defaults/rms_five.tsv\n" +
                "load.IMFRateProvider.urls=https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/imf.tsv";

        generatedResourceProducer.produce(new GeneratedResourceBuildItem("javamoney.properties", properties.getBytes()));
        resourceProducer.produce(new NativeImageResourceBuildItem("javamoney.properties"));
    }

    private ServiceProviderBuildItem spiBuildItem(Class<?> clazz) {
        return ServiceProviderBuildItem.allProvidersFromClassPath(clazz.getName());
    }

    @SafeVarargs
    private <C, I extends C> ServiceProviderBuildItem spiBuildItem(Class<C> serviceInterface, Class<I>... providers) {
        var p = Arrays.stream(providers)
                .map(Class::getName)
                .collect(Collectors.toUnmodifiableList());

        return new ServiceProviderBuildItem(serviceInterface.getName(), p);
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
