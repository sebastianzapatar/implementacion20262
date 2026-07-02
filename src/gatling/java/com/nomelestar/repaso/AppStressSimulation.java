package com.nomelestar.repaso;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Pruebas de Estrés y Carga con Gatling para toda la App.
 * 
 * NOTA IMPORTANTE: La aplicación DEBE estar corriendo (e.g. en el puerto 8080)
 * y conectada a PostgreSQL para poder ejecutar estas pruebas.
 * 
 * Ejecutar con: ./gradlew gatlingRun
 */
public class AppStressSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080/api")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder escenarioGeneral = scenario("Prueba de estrés para Chefs y Platos")
            .exec(http("Obtener todos los Chefs")
                    .get("/chefs")
                    .check(status().is(200))
            )
            .pause(1)
            .exec(http("Crear un Chef")
                    .post("/chefs")
                    .body(StringBody("{\"nombre\": \"Chef Gatling\"}"))
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("chefId"))
            )
            .pause(1)
            .exec(http("Obtener todos los Platos")
                    .get("/dishes")
                    .check(status().is(200))
            )
            .pause(1)
            .exec(http("Crear un Plato asociado al Chef")
                    .post("/dishes")
                    .body(StringBody("{\"nombre\": \"Plato Gatling\", \"chefId\": \"#{chefId}\"}"))
                    .check(status().is(201))
            );

    public AppStressSimulation() {
        setUp(
                escenarioGeneral.injectOpen(
                        // SMOKE TEST: Para verificar que todo el script corre bien sin tumbar el server
                        rampUsers(5).during(10)
                        
                        /* 
                        // LOAD TEST (Descomentar para usar)
                        rampUsers(50).during(30)
                        */
                        
                        /*
                        // STRESS TEST (Descomentar para usar)
                        atOnceUsers(20),
                        rampUsers(80).during(20)
                        */
                )
        ).protocols(httpProtocol);
    }
}
