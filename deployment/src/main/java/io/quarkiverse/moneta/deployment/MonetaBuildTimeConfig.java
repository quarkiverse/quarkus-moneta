package io.quarkiverse.moneta.deployment;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "quarkus.moneta")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface MonetaBuildTimeConfig {

    /**
     * If true, current exchange rates from the IMF and the ECB are downloaded and processed during the build and regularly at
     * runtime.
     */
    @WithDefault("true")
    @WithName("conversion.enabled")
    boolean conversionEnabled();

    /**
     * The default order of exchange rate providers.
     * Read more:
     * https://github.com/JavaMoney/jsr354-ri/blob/master/moneta-core/src/main/asciidoc/userguide.adoc#32-exchange-rate-providers
     */
    @WithDefault("IDENT,ECB,IMF,ECB-HIST,ECB-HIST90")
    @WithName("conversion.default-chain")
    String conversionDefaultChain();
}
