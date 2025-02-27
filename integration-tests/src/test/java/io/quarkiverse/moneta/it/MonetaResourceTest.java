package io.quarkiverse.moneta.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MonetaResourceTest {

    @Test
    public void testFormat() {
        given()
                .when().get("/moneta/format")
                .then()
                .statusCode(200)
                .body(is("34,95 USD"));
    }
}
