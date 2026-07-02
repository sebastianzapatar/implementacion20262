package com.nomelestar.repaso.dish.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomelestar.repaso.dish.dto.DishRequest;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.service.DishService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DishController.class)
@DisplayName("Dish — Pruebas Unitarias (Controller)")
class DishControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DishService dishService;

    @Test
    @DisplayName("POST /api/dishes → Llama al servicio y retorna 201")
    void crearPlato() throws Exception {
        UUID chefId = UUID.randomUUID();
        DishRequest request = new DishRequest("Pizza", "Desc", new BigDecimal("10.0"), chefId);
        DishResponse response = new DishResponse(UUID.randomUUID(), "Pizza", "Desc", new BigDecimal("10.0"), chefId, "ChefName");

        Mockito.when(dishService.crearPlato(any(DishRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/dishes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pizza"));

        Mockito.verify(dishService, Mockito.times(1)).crearPlato(any(DishRequest.class));
    }

    @Test
    @DisplayName("GET /api/dishes → Llama al servicio y retorna 200")
    void obtenerTodos() throws Exception {
        UUID chefId = UUID.randomUUID();
        DishResponse response = new DishResponse(UUID.randomUUID(), "Pizza", "Desc", new BigDecimal("10.0"), chefId, "ChefName");
        Mockito.when(dishService.obtenerTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/dishes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Pizza"));

        Mockito.verify(dishService, Mockito.times(1)).obtenerTodos();
    }

    @Test
    @DisplayName("GET /api/dishes/{id} → Llama al servicio y retorna 200")
    void obtenerPorId() throws Exception {
        UUID id = UUID.randomUUID();
        UUID chefId = UUID.randomUUID();
        DishResponse response = new DishResponse(id, "Pizza", "Desc", new BigDecimal("10.0"), chefId, "ChefName");
        Mockito.when(dishService.obtenerPorId(id)).thenReturn(response);

        mockMvc.perform(get("/api/dishes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        Mockito.verify(dishService, Mockito.times(1)).obtenerPorId(id);
    }

    @Test
    @DisplayName("PUT /api/dishes/{id} → Llama al servicio y retorna 200")
    void actualizarPlato() throws Exception {
        UUID id = UUID.randomUUID();
        UUID chefId = UUID.randomUUID();
        DishRequest request = new DishRequest("Updated Pizza", "Desc", new BigDecimal("10.0"), chefId);
        DishResponse response = new DishResponse(id, "Updated Pizza", "Desc", new BigDecimal("10.0"), chefId, "ChefName");

        Mockito.when(dishService.actualizarPlato(eq(id), any(DishRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/dishes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Updated Pizza"));

        Mockito.verify(dishService, Mockito.times(1)).actualizarPlato(eq(id), any(DishRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/dishes/{id} → Llama al servicio y retorna 204")
    void eliminarPlato() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doNothing().when(dishService).eliminarPlato(id);

        mockMvc.perform(delete("/api/dishes/{id}", id))
                .andExpect(status().isNoContent());

        Mockito.verify(dishService, Mockito.times(1)).eliminarPlato(id);
    }
}
