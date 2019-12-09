package org.eclipse.che.incubator.workspace.telemetry;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.is;

public class TelemetryResourceIT {
    @Test
    public void TestEvent() {
        given()
                .when().post("/telemetry/event")
                .then()
                .statusCode(200)
                .body(is("event"));
    }
}
