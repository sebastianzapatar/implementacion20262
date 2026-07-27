package com.nomelestar.repaso;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que el mecanismo de perfiles funcione de verdad: que Spring encuentre
 * el archivo {@code application-<perfil>.yml} y que sus valores pisen a los de
 * {@code application.yml}.
 *
 * <p>Sin estos tests, un error de nombre de archivo (por ejemplo
 * {@code application_prod.yml} con guion bajo) pasaría desapercibido: Spring no
 * avisa, simplemente ignora el archivo y arranca con la configuración base.
 */
@DisplayName("Perfiles de configuración")
class ProfileConfigTest {

    @Nested
    @SpringBootTest
    @ActiveProfiles("test")
    @DisplayName("Perfil test")
    class PerfilTest {

        @Autowired
        private Environment env;

        @Value("${spring.datasource.url}")
        private String url;

        @Test
        @DisplayName("carga application-test.yml y usa H2 en memoria")
        void cargaPerfilTest() {
            assertThat(env.getActiveProfiles()).containsExactly("test");
            // Este valor solo existe en application-test.yml
            assertThat(url).startsWith("jdbc:h2:mem:");
            // create-drop viene del perfil, no del application.yml base
            assertThat(env.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("create-drop");
        }

        @Test
        @DisplayName("hereda lo definido en application.yml (común a todos)")
        void heredaConfiguracionComun() {
            // open-in-view NO está en application-test.yml: viene del archivo base
            assertThat(env.getProperty("spring.jpa.open-in-view")).isEqualTo("false");
            assertThat(env.getProperty("spring.application.name")).isEqualTo("repaso");
        }
    }

    @Nested
    @SpringBootTest(properties = {
            // El perfil prod exige estas variables; acá se simulan para poder
            // comprobar que el archivo se lee, sin conectarse a nada real.
            "DB_URL=jdbc:h2:mem:falsa",
            "DB_USERNAME=sa",
            "DB_PASSWORD=",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.jpa.hibernate.ddl-auto=create-drop"
    })
    @ActiveProfiles("prod")
    @DisplayName("Perfil prod")
    class PerfilProd {

        @Autowired
        private Environment env;

        @Test
        @DisplayName("carga application-prod.yml y apaga el log de SQL")
        void cargaPerfilProd() {
            assertThat(env.getActiveProfiles()).containsExactly("prod");
            // show-sql: false solo está declarado en application-prod.yml
            assertThat(env.getProperty("spring.jpa.show-sql")).isEqualTo("false");
            assertThat(env.getProperty("logging.level.root")).isEqualTo("info");
        }
    }
}
