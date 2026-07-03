package com.nomelestar.repaso.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import com.nomelestar.repaso.client.repository.ClientRepository;
import com.nomelestar.repaso.dish.dto.DishRequest;
import com.nomelestar.repaso.dish.entity.Dish;
import com.nomelestar.repaso.dish.repository.DishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de INTEGRACIÓN para Dish.
 * 
 * Valida la cadena completa: Controller → Service → Repository → BD H2.
 * Los platos dependen de un Chef existente, por eso se crea un chef primero.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("Dish — Pruebas de Integración (H2)")
class DishIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ChefRepository chefRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Chef chefPersistido;

    @BeforeEach
    void limpiarYPrepararDatos() {
        clientRepository.deleteAll(); // Limpiar clientes primero (FK a dish_clients)
        dishRepository.deleteAll();
        chefRepository.deleteAll();
        // Crear un chef de apoyo para asociar platos
        chefPersistido = chefRepository.save(Chef.builder().nombre("Chef de Integración").build());
    }

    @Test
    @DisplayName("POST /api/dishes → 201 CREATED con plato válido")
    void crearPlato_retorna201() throws Exception {
        // Arrange
        DishRequest request = new DishRequest("Arepas", "Con queso", new BigDecimal("8000"), chefPersistido.getId());

        // Act & Assert
        mockMvc.perform(post("/api/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Arepas"))
                .andExpect(jsonPath("$.descripcion").value("Con queso"))
                .andExpect(jsonPath("$.precio").value(8000))
                .andExpect(jsonPath("$.chefId").value(chefPersistido.getId().toString()))
                .andExpect(jsonPath("$.nombreChef").value("Chef de Integración"));
    }

    @Test
    @DisplayName("GET /api/dishes → 200 OK con lista de platos")
    void obtenerTodos_retorna200ConLista() throws Exception {
        // Arrange
        dishRepository.save(Dish.builder().nombre("Plato 1").descripcion("d1")
                .precio(new BigDecimal("10000")).chef(chefPersistido).build());
        dishRepository.save(Dish.builder().nombre("Plato 2").descripcion("d2")
                .precio(new BigDecimal("20000")).chef(chefPersistido).build());

        // Act & Assert
        mockMvc.perform(get("/api/dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/dishes/{id} → 200 OK con plato existente")
    void obtenerPorId_retorna200() throws Exception {
        // Arrange
        Dish dish = dishRepository.save(Dish.builder().nombre("Sancocho").descripcion("Sopa")
                .precio(new BigDecimal("18000")).chef(chefPersistido).build());

        // Act & Assert
        mockMvc.perform(get("/api/dishes/{id}", dish.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Sancocho"))
                .andExpect(jsonPath("$.nombreChef").value("Chef de Integración"));
    }

    @Test
    @DisplayName("PUT /api/dishes/{id} → 200 OK actualiza plato")
    void actualizarPlato_retorna200() throws Exception {
        // Arrange
        Dish dish = dishRepository.save(Dish.builder().nombre("Viejo").descripcion("desc")
                .precio(new BigDecimal("15000")).chef(chefPersistido).build());
        DishRequest updateRequest = new DishRequest("Nuevo", "nueva desc",
                new BigDecimal("22000"), chefPersistido.getId());

        // Act & Assert
        mockMvc.perform(put("/api/dishes/{id}", dish.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo"))
                .andExpect(jsonPath("$.precio").value(22000));
    }

    @Test
    @DisplayName("DELETE /api/dishes/{id} → 204 NO CONTENT elimina plato")
    void eliminarPlato_retorna204() throws Exception {
        // Arrange
        Dish dish = dishRepository.save(Dish.builder().nombre("A borrar").descripcion("d")
                .precio(new BigDecimal("5000")).chef(chefPersistido).build());

        // Act & Assert
        mockMvc.perform(delete("/api/dishes/{id}", dish.getId()))
                .andExpect(status().isNoContent());

        // Verificar que fue eliminado
        mockMvc.perform(get("/api/dishes/{id}", dish.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/dishes con chef inexistente → 404 NOT FOUND")
    void crearPlato_conChefInexistente_retorna404() throws Exception {
        // Arrange
        UUID chefIdFalso = UUID.randomUUID();
        DishRequest request = new DishRequest("Plato", "desc", new BigDecimal("10000"), chefIdFalso);

        // Act & Assert
        mockMvc.perform(post("/api/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
