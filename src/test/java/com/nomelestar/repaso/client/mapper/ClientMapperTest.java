package com.nomelestar.repaso.client.mapper;

import com.nomelestar.repaso.client.dto.ClientRequest;
import com.nomelestar.repaso.client.dto.ClientResponse;
import com.nomelestar.repaso.client.entity.Client;
import com.nomelestar.repaso.dish.entity.Dish;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Client — Pruebas Unitarias (Mapper)")
class ClientMapperTest {

    @Test
    @DisplayName("toEntity() convierte un ClientRequest a Client correctamente")
    void toEntity() {
        ClientRequest request = new ClientRequest("Juan", "juan@email.com");

        Client entity = ClientMapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("Juan", entity.getNombre());
        assertEquals("juan@email.com", entity.getEmail());
        assertNull(entity.getId()); // No se genera hasta persistir
    }

    @Test
    @DisplayName("toEntity() retorna null si el request es null")
    void toEntityNull() {
        assertNull(ClientMapper.toEntity(null));
    }

    @Test
    @DisplayName("toResponse() convierte un Client a ClientResponse correctamente")
    void toResponse() {
        Dish plato = Dish.builder().id(UUID.randomUUID()).nombre("Pizza").build();
        Client entity = Client.builder()
                .id(UUID.randomUUID())
                .nombre("Juan")
                .email("juan@email.com")
                .platosConsumidos(List.of(plato))
                .build();

        ClientResponse response = ClientMapper.toResponse(entity);

        assertNotNull(response);
        assertEquals(entity.getId(), response.id());
        assertEquals("Juan", response.nombre());
        assertEquals("juan@email.com", response.email());
        assertEquals(1, response.platosConsumidos().size());
        assertEquals("Pizza", response.platosConsumidos().get(0));
    }

    @Test
    @DisplayName("toResponse() retorna null si la entidad es null")
    void toResponseNull() {
        assertNull(ClientMapper.toResponse(null));
    }

    @Test
    @DisplayName("toResponse() retorna lista vacía si el cliente no tiene platos")
    void toResponseSinPlatos() {
        Client entity = Client.builder()
                .id(UUID.randomUUID())
                .nombre("Ana")
                .email("ana@email.com")
                .build();

        ClientResponse response = ClientMapper.toResponse(entity);

        assertNotNull(response);
        assertTrue(response.platosConsumidos().isEmpty());
    }
}
