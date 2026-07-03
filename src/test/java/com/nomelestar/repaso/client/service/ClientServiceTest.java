package com.nomelestar.repaso.client.service;

import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.client.dto.ClientRequest;
import com.nomelestar.repaso.client.dto.ClientResponse;
import com.nomelestar.repaso.client.entity.Client;
import com.nomelestar.repaso.client.repository.ClientRepository;
import com.nomelestar.repaso.common.exception.BadRequestException;
import com.nomelestar.repaso.common.exception.ResourceNotFoundException;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.entity.Dish;
import com.nomelestar.repaso.dish.repository.DishRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Client — Pruebas Unitarias (Service)")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DishRepository dishRepository;

    @InjectMocks
    private ClientService clientService;

    // --- CRUD Tests ---

    @Test
    @DisplayName("crearCliente() guarda y retorna el cliente correctamente")
    void crearCliente() {
        ClientRequest request = new ClientRequest("Juan", "juan@email.com");
        Client saved = Client.builder().id(UUID.randomUUID()).nombre("Juan").email("juan@email.com").build();
        when(clientRepository.save(any(Client.class))).thenReturn(saved);

        ClientResponse response = clientService.crearCliente(request);

        assertNotNull(response);
        assertEquals("Juan", response.nombre());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("crearCliente() lanza excepción si el nombre está vacío")
    void crearClienteNombreVacio() {
        ClientRequest request = new ClientRequest("", "email@test.com");
        assertThrows(BadRequestException.class, () -> clientService.crearCliente(request));
    }

    @Test
    @DisplayName("crearCliente() lanza excepción si el email está vacío")
    void crearClienteEmailVacio() {
        ClientRequest request = new ClientRequest("Juan", "");
        assertThrows(BadRequestException.class, () -> clientService.crearCliente(request));
    }

    @Test
    @DisplayName("obtenerTodos() retorna la lista de clientes")
    void obtenerTodos() {
        Client c1 = Client.builder().id(UUID.randomUUID()).nombre("Juan").email("j@e.com").build();
        Client c2 = Client.builder().id(UUID.randomUUID()).nombre("Ana").email("a@e.com").build();
        when(clientRepository.findAll()).thenReturn(List.of(c1, c2));

        List<ClientResponse> result = clientService.obtenerTodos();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("obtenerPorId() retorna el cliente correcto")
    void obtenerPorId() {
        UUID id = UUID.randomUUID();
        Client client = Client.builder().id(id).nombre("Juan").email("j@e.com").build();
        when(clientRepository.findById(id)).thenReturn(Optional.of(client));

        ClientResponse response = clientService.obtenerPorId(id);

        assertEquals(id, response.id());
    }

    @Test
    @DisplayName("obtenerPorId() lanza excepción si no existe")
    void obtenerPorIdNoExiste() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clientService.obtenerPorId(id));
    }

    @Test
    @DisplayName("actualizarCliente() modifica y retorna el cliente actualizado")
    void actualizarCliente() {
        UUID id = UUID.randomUUID();
        Client existing = Client.builder().id(id).nombre("Juan").email("j@e.com").build();
        when(clientRepository.findById(id)).thenReturn(Optional.of(existing));
        when(clientRepository.save(any(Client.class))).thenReturn(existing);

        ClientResponse response = clientService.actualizarCliente(id, new ClientRequest("Ana", "ana@e.com"));

        assertEquals("Ana", response.nombre());
    }

    @Test
    @DisplayName("eliminarCliente() elimina correctamente")
    void eliminarCliente() {
        UUID id = UUID.randomUUID();
        Client client = Client.builder().id(id).nombre("Juan").email("j@e.com").build();
        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        doNothing().when(clientRepository).delete(client);

        assertDoesNotThrow(() -> clientService.eliminarCliente(id));
        verify(clientRepository, times(1)).delete(client);
    }

    // --- ManyToMany Tests ---

    @Test
    @DisplayName("consumirPlato() agrega un plato al cliente")
    void consumirPlato() {
        UUID clientId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();
        Chef chef = Chef.builder().id(UUID.randomUUID()).nombre("Chef A").build();
        Dish dish = Dish.builder().id(dishId).nombre("Pizza").chef(chef).build();
        Client client = Client.builder().id(clientId).nombre("Juan").email("j@e.com")
                .platosConsumidos(new ArrayList<>()).build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(dishRepository.findById(dishId)).thenReturn(Optional.of(dish));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        ClientResponse response = clientService.consumirPlato(clientId, dishId);

        assertNotNull(response);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("consumirPlato() lanza excepción si el plato no existe")
    void consumirPlatoNoExiste() {
        UUID clientId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();
        Client client = Client.builder().id(clientId).nombre("Juan").email("j@e.com").build();
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(dishRepository.findById(dishId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientService.consumirPlato(clientId, dishId));
    }

    // --- JOIN Query Tests ---

    @Test
    @DisplayName("obtenerChefConMasVentas() retorna el chef correcto")
    void obtenerChefConMasVentas() {
        Chef chef = Chef.builder().id(UUID.randomUUID()).nombre("Chef Estrella").build();
        when(clientRepository.findChefConMasVentas()).thenReturn(Optional.of(chef));

        ChefResponse response = clientService.obtenerChefConMasVentas();

        assertEquals("Chef Estrella", response.nombre());
    }

    @Test
    @DisplayName("obtenerChefConMasVentas() lanza excepción si no hay datos")
    void obtenerChefConMasVentasSinDatos() {
        when(clientRepository.findChefConMasVentas()).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clientService.obtenerChefConMasVentas());
    }

    @Test
    @DisplayName("obtenerPlatosDeClientePorChef() retorna los platos correctos")
    void obtenerPlatosDeClientePorChef() {
        UUID clientId = UUID.randomUUID();
        UUID chefId = UUID.randomUUID();
        Client client = Client.builder().id(clientId).nombre("Juan").email("j@e.com").build();
        Chef chef = Chef.builder().id(chefId).nombre("Chef A").build();
        Dish d1 = Dish.builder().id(UUID.randomUUID()).nombre("Pizza").precio(BigDecimal.TEN).chef(chef).build();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.findPlatosDeClientePorChef(clientId, chefId)).thenReturn(List.of(d1));

        List<DishResponse> result = clientService.obtenerPlatosDeClientePorChef(clientId, chefId);

        assertEquals(1, result.size());
        assertEquals("Pizza", result.get(0).nombre());
    }
}
