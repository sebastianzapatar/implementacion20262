package com.nomelestar.repaso.chef.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.service.ChefService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChefController.class)
@DisplayName("Chef — Pruebas Unitarias (Controller)")
class ChefControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    // En Spring Boot 4.1.0 usamos @MockitoBean en vez de @MockBean
    @MockitoBean
    private ChefService chefService;

    @Test
    @DisplayName("POST /api/chefs → Llama al servicio y retorna 201")
    void crearChef() throws Exception {
        ChefRequest request = new ChefRequest("Test Chef");
        ChefResponse response = new ChefResponse(UUID.randomUUID(), "Test Chef", List.of());

        Mockito.when(chefService.crearChef(any(ChefRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/chefs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Test Chef"));

        Mockito.verify(chefService, Mockito.times(1)).crearChef(any(ChefRequest.class));
    }

    @Test
    @DisplayName("GET /api/chefs → Llama al servicio y retorna 200")
    void obtenerTodos() throws Exception {
        ChefResponse response = new ChefResponse(UUID.randomUUID(), "Test Chef", List.of());
        Mockito.when(chefService.obtenerTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/chefs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Test Chef"));

        Mockito.verify(chefService, Mockito.times(1)).obtenerTodos();
    }

    @Test
    @DisplayName("GET /api/chefs/{id} → Llama al servicio y retorna 200")
    void obtenerPorId() throws Exception {
        UUID id = UUID.randomUUID();
        ChefResponse response = new ChefResponse(id, "Test Chef", List.of());
        Mockito.when(chefService.obtenerPorId(id)).thenReturn(response);

        mockMvc.perform(get("/api/chefs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        Mockito.verify(chefService, Mockito.times(1)).obtenerPorId(id);
    }

    @Test
    @DisplayName("PUT /api/chefs/{id} → Llama al servicio y retorna 200")
    void actualizarChef() throws Exception {
        UUID id = UUID.randomUUID();
        ChefRequest request = new ChefRequest("Updated Chef");
        ChefResponse response = new ChefResponse(id, "Updated Chef", List.of());

        Mockito.when(chefService.actualizarChef(eq(id), any(ChefRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/chefs/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Updated Chef"));

        Mockito.verify(chefService, Mockito.times(1)).actualizarChef(eq(id), any(ChefRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/chefs/{id} → Llama al servicio y retorna 204")
    void eliminarChef() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doNothing().when(chefService).eliminarChef(id);

        mockMvc.perform(delete("/api/chefs/{id}", id))
                .andExpect(status().isNoContent());

        Mockito.verify(chefService, Mockito.times(1)).eliminarChef(id);
    }
}
