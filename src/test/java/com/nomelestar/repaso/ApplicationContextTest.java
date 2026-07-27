package com.nomelestar.repaso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de humo ("smoke test") del arranque completo de la aplicación.
 *
 * <p>Levanta TODO el contexto de Spring (controllers, services, repositories,
 * el manejador global de excepciones y la config de OpenAPI) contra una base H2
 * en memoria, para no depender de que Postgres esté corriendo.
 *
 * <p>{@code @ActiveProfiles("test")} hace que Spring cargue
 * {@code application.yml} + {@code application-test.yml} (el de
 * src/test/resources, con H2), en vez del perfil "dev" que apunta a Postgres.
 *
 * <p>Qué detecta este test: dependencias que no se pueden inyectar, dos beans con
 * el mismo nombre, un @Query con error de sintaxis, un @EntityGraph que apunta a
 * un atributo que no existe, o un application.yml mal escrito. Todo eso hace
 * fallar el arranque, y es mejor enterarse acá que al desplegar.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Arranque de la aplicación")
class ApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("El contexto de Spring levanta con todos los beans")
    void contextLoads() {
        assertThat(context).isNotNull();

        // Las tres capas quedaron registradas correctamente
        assertThat(context.getBeansOfType(org.springframework.web.bind.annotation.RestController.class))
                .isNotNull();
        assertThat(context.containsBean("chefController")).isTrue();
        assertThat(context.containsBean("dishController")).isTrue();
        assertThat(context.containsBean("clientController")).isTrue();
        assertThat(context.containsBean("globalExceptionHandler")).isTrue();
    }
}
