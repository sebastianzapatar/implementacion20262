package com.nomelestar.repaso.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.client.dto.ClientRequest;
import com.nomelestar.repaso.client.dto.ClientResponse;
import com.nomelestar.repaso.client.service.ClientService;
import com.nomelestar.repaso.dish.dto.DishResponse;
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

@WebMvcTest(ClientController.class)
@DisplayName("Client — Pruebas Unitarias (Controller)")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ClientService clientService;

    @Test
    @DisplayName("POST /api/clients → Crea un cliente y retorna 201")
    void crearCliente() throws Exception {
        ClientRequest request = new ClientRequest("Juan", "juan@email.com");
        ClientResponse response = new ClientResponse(UUID.randomUUID(), "Juan", "juan@email.com", List.of());
        Mockito.when(clientService.crearCliente(any(ClientRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("GET /api/clients → Retorna lista de clientes")
    void obtenerTodos() throws Exception {
        ClientResponse response = new ClientResponse(UUID.randomUUID(), "Juan", "juan@email.com", List.of());
        Mockito.when(clientService.obtenerTodos()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @DisplayName("GET /api/clients/{id} → Retorna un cliente")
    void obtenerPorId() throws Exception {
        UUID id = UUID.randomUUID();
        ClientResponse response = new ClientResponse(id, "Juan", "juan@email.com", List.of());
        Mockito.when(clientService.obtenerPorId(id)).thenReturn(response);

        mockMvc.perform(get("/api/clients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("PUT /api/clients/{id} → Actualiza un cliente")
    void actualizarCliente() throws Exception {
        UUID id = UUID.randomUUID();
        ClientRequest request = new ClientRequest("Ana", "ana@email.com");
        ClientResponse response = new ClientResponse(id, "Ana", "ana@email.com", List.of());
        Mockito.when(clientService.actualizarCliente(eq(id), any(ClientRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/clients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    @DisplayName("DELETE /api/clients/{id} → Elimina un cliente y retorna 204")
    void eliminarCliente() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doNothing().when(clientService).eliminarCliente(id);

        mockMvc.perform(delete("/api/clients/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/clients/{clientId}/dishes/{dishId} → Registra consumo de plato")
    void consumirPlato() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();
        ClientResponse response = new ClientResponse(clientId, "Juan", "j@e.com", List.of("Pizza"));
        Mockito.when(clientService.consumirPlato(clientId, dishId)).thenReturn(response);

        mockMvc.perform(post("/api/clients/{clientId}/dishes/{dishId}", clientId, dishId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platosConsumidos[0]").value("Pizza"));
    }

    @Test
    @DisplayName("GET /api/clients/stats/chef-mas-vendido → Retorna el chef con más ventas")
    void chefConMasVentas() throws Exception {
        ChefResponse chefResponse = new ChefResponse(UUID.randomUUID(), "Chef Estrella", List.of("Pizza"));
        Mockito.when(clientService.obtenerChefConMasVentas()).thenReturn(chefResponse);

        mockMvc.perform(get("/api/clients/stats/chef-mas-vendido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Chef Estrella"));
    }

    @Test
    @DisplayName("GET /api/clients/{clientId}/chefs/{chefId}/platos → Retorna platos preferidos")
    void platosDeClientePorChef() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID chefId = UUID.randomUUID();
        DishResponse dishResponse = new DishResponse(UUID.randomUUID(), "Pizza", "Rica", BigDecimal.TEN, chefId, "Chef A");
        Mockito.when(clientService.obtenerPlatosDeClientePorChef(clientId, chefId)).thenReturn(List.of(dishResponse));

        mockMvc.perform(get("/api/clients/{clientId}/chefs/{chefId}/platos", clientId, chefId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Pizza"));
    }
}
