package io.quarkiverse.moneta.deployment;

import java.time.Duration;
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

import io.quarkiverse.moneta.CertificateSupplier;
import io.quarkiverse.moneta.Constants;
import io.quarkiverse.moneta.loader.JvmHttpLoaderService;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.deployment.pkg.builditem.UberJarMergedResourceBuildItem;
import io.quarkus.tls.TlsCertificateBuildItem;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.mutiny.core.Vertx;

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

        producer.produce(spiBuildItem(LoaderService.class, JvmHttpLoaderService.class));
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
    TlsCertificateBuildItem certificates() {
        return new TlsCertificateBuildItem(Constants.TLS_CONFIGURATION, new CertificateSupplier());
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

    @BuildStep
    void overrideServices(BuildProducer<NativeImageResourceBuildItem> resourceProducer,
            BuildProducer<GeneratedResourceBuildItem> generatedResourceProducer) {
        var resourcePath = "META-INF/services/" + LoaderService.class.getName();
        generatedResourceProducer
                .produce(new GeneratedResourceBuildItem(resourcePath, JvmHttpLoaderService.class.getName().getBytes()));
        resourceProducer.produce(new NativeImageResourceBuildItem(resourcePath));
    }

    @BuildStep(onlyIf = IsNormal.class)
    void uberJarFiles(BuildProducer<UberJarMergedResourceBuildItem> uberJarMergedProducer) {
        uberJarMergedProducer.produce(new UberJarMergedResourceBuildItem("javamoney.properties"));
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

    private byte[] downloadFile(String url) {
        var options = new HttpClientOptions()
                .setVerifyHost(false)
                .setTrustAll(true)
                .setSsl(true);

        var client = Vertx.vertx()
                .createHttpClient(options);

        var requestOptions = new RequestOptions()
                .setMethod(HttpMethod.GET)
                .setTimeout(5000)
                .setConnectTimeout(5000)
                .setIdleTimeout(5000)
                .setAbsoluteURI(url);

        var buffer = client.request(requestOptions)
                .await()
                .atMost(Duration.ofSeconds(5))
                .send()
                .await()
                .atMost(Duration.ofSeconds(5))
                .body()
                .await()
                .atMost(Duration.ofSeconds(5));

        return buffer.getBytes();
    }
}
