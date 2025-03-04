/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.moneta.it;

import java.util.Locale;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.convert.ConversionQueryBuilder;
import javax.money.convert.MonetaryConversions;
import javax.money.convert.RateType;
import javax.money.format.MonetaryFormats;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.javamoney.moneta.Money;

@Path("/moneta")
@ApplicationScoped
public class MonetaResource {

    @GET
    @Path("/format")
    public String format() {
        var money = (MonetaryAmount) Money.of(100, "EUR")
                .divide(3);

        money = Monetary.getDefaultRounding()
                .apply(money);

        var rateProvider = MonetaryConversions.getExchangeRateProvider("IDENT", "ECB-HIST");

        var query = ConversionQueryBuilder.of()
                .setBaseCurrency(money.getCurrency())
                .setTermCurrency(Monetary.getCurrency("USD"))
                .setRateTypes(RateType.DEFERRED)
                .build();

        money = rateProvider.getCurrencyConversion(query)
                .apply(money);

        query = ConversionQueryBuilder.of()
                .setBaseCurrency(money.getCurrency())
                .setTermCurrency(Monetary.getCurrency("EUR"))
                .setRateTypes(RateType.DEFERRED)
                .build();

        money = rateProvider.getCurrencyConversion(query)
                .apply(money);

        return MonetaryFormats.getAmountFormat(Locale.US)
                .format(money);
    }
}
