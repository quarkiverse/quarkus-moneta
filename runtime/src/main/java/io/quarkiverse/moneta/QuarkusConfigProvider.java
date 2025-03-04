package io.quarkiverse.moneta;

import java.util.Map;

import org.javamoney.moneta.spi.MonetaryConfigProvider;

public class QuarkusConfigProvider implements MonetaryConfigProvider {

    @Override
    public String getProperty(String key) {
        return getProperties().get(key);
    }

    @Override
    public Map<String, String> getProperties() {
        return BuildTimeConfigRecorder.getProperties();
    }
}
