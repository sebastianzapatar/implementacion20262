package com.nomelestar.repaso.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de INTEGRACIÓN para Chef.
 * 
 * @SpringBootTest levanta TODO el contexto de Spring Boot (servicio, repositorio, controller).
 * @ActiveProfiles("test") apunta a H2 en memoria (NO necesita PostgreSQL).
 * @AutoConfigureMockMvc inyecta MockMvc para simular peticiones HTTP sin servidor real.
 * 
 * La diferencia clave con las pruebas unitarias:
 * - Aquí NO se mockea nada → se usa la BD real (H2).
 * - Se valida la integración completa: Controller → Service → Repository → BD.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("Chef — Pruebas de Integración (H2)")
class ChefIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChefRepository chefRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        chefRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/chefs → 201 CREATED con chef válido")
    void crearChef_retorna201() throws Exception {
        // Arrange
        ChefRequest request = new ChefRequest("Andrés Carne de Res");

        // Act & Assert
        mockMvc.perform(post("/api/chefs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Andrés Carne de Res"))
                .andExpect(jsonPath("$.platos").isArray())
                .andExpect(jsonPath("$.platos").isEmpty());
    }

    @Test
    @DisplayName("GET /api/chefs → 200 OK con lista de chefs")
    void obtenerTodos_retorna200ConLista() throws Exception {
        // Arrange — insertar directamente en la BD
        chefRepository.save(Chef.builder().nombre("Chef A").build());
        chefRepository.save(Chef.builder().nombre("Chef B").build());

        // Act & Assert
        mockMvc.perform(get("/api/chefs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombre").value("Chef A"))
                .andExpect(jsonPath("$[1].nombre").value("Chef B"));
    }

    @Test
    @DisplayName("GET /api/chefs/{id} → 200 OK con chef existente")
    void obtenerPorId_conIdExistente_retorna200() throws Exception {
        // Arrange
        Chef chef = chefRepository.save(Chef.builder().nombre("Ferran Adrià").build());

        // Act & Assert
        mockMvc.perform(get("/api/chefs/{id}", chef.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chef.getId().toString()))
                .andExpect(jsonPath("$.nombre").value("Ferran Adrià"));
    }

    @Test
    @DisplayName("GET /api/chefs/{id} → 404 NOT FOUND con ID inexistente")
    void obtenerPorId_conIdInexistente_retorna404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/chefs/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /api/chefs/{id} → 200 OK actualiza nombre correctamente")
    void actualizarChef_retorna200() throws Exception {
        // Arrange
        Chef chef = chefRepository.save(Chef.builder().nombre("Nombre Viejo").build());
        ChefRequest updateRequest = new ChefRequest("Nombre Nuevo");

        // Act & Assert
        mockMvc.perform(put("/api/chefs/{id}", chef.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre Nuevo"));
    }

    @Test
    @DisplayName("DELETE /api/chefs/{id} → 204 NO CONTENT elimina el chef")
    void eliminarChef_retorna204() throws Exception {
        // Arrange
        Chef chef = chefRepository.save(Chef.builder().nombre("Chef a borrar").build());

        // Act & Assert
        mockMvc.perform(delete("/api/chefs/{id}", chef.getId()))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe en la BD
        mockMvc.perform(get("/api/chefs/{id}", chef.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/chefs con nombre vacío → 400 BAD REQUEST")
    void crearChef_conNombreVacio_retorna400() throws Exception {
        // Arrange
        ChefRequest request = new ChefRequest("");

        // Act & Assert
        mockMvc.perform(post("/api/chefs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(400));
    }
}
