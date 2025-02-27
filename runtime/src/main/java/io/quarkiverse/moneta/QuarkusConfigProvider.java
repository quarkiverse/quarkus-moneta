package io.quarkiverse.moneta;

import java.util.HashMap;
import java.util.Map;

import org.javamoney.moneta.spi.MonetaryConfigProvider;

public class QuarkusConfigProvider implements MonetaryConfigProvider {

    private static final Map<String, String> properties;

    static {
        properties = new HashMap<>();

        properties.put("conversion.default-chain", "IDENT,ECB,IMF,ECB-HIST,ECB-HIST90");
        properties.put("org.javamoney.moneta.Money.defaults.precision", "256");
        properties.put("org.javamoney.moneta.Money.defaults.roundingMode", "HALF_EVEN");

        properties.put("ecb.digit.fraction", "6");
        properties.put("imf.digit.fraction", "6");

        properties.put("load.IMFRateProvider.type", "SCHEDULED");
        properties.put("load.IMFRateProvider.period", "06:00");
        properties.put("load.IMFRateProvider.resource", "org/javamoney/moneta/convert/imf/defaults/rms_five.tsv");
        properties.put("load.IMFRateProvider.urls",
                "https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/imf.tsv");

        properties.put("load.IMFHistoricRateProvider.type", "LAZY");
        properties.put("load.IMFHistoricRateProvider.resource", "org/javamoney/moneta/convert/imf/defaults/rms_five.tsv");
        properties.put("load.IMFHistoricRateProvider.urls",
                "https://raw.githubusercontent.com/instant-solutions/quarkus-moneta-data/refs/heads/main/imf.tsv");
        properties.put("load.IMFHistoricRateProvider.startRemote", "false");
        properties.put("load.IMFHistoricRateProvider.useragent", "Chrome/51.0.2704.103");
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }
}
