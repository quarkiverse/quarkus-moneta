package io.quarkiverse.moneta;

import java.security.KeyStore;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.quarkus.tls.BaseTlsConfiguration;
import io.quarkus.tls.TlsConfiguration;

public class CertificateSupplier implements Supplier<TlsConfiguration> {

    private static final Logger logger = Logger.getLogger(CertificateSupplier.class.getName());

    @Override
    public TlsConfiguration get() {
        return new BaseTlsConfiguration() {
            @Override
            public KeyStore getTrustStore() {
                KeyStore trustStore;
                try (var stream = getClass().getResourceAsStream("ecb.europa.eu.jks")) {
                    trustStore = KeyStore.getInstance("JKS");
                    trustStore.load(stream, "ecb-truststore".toCharArray());
                    logger.info("Loaded truststore for ECB.");
                    return trustStore;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to load truststore", e);
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
