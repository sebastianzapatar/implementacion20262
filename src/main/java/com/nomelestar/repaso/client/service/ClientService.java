package com.nomelestar.repaso.client.service;

import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.mapper.ChefMapper;
import com.nomelestar.repaso.client.dto.ClientRequest;
import com.nomelestar.repaso.client.dto.ClientResponse;
import com.nomelestar.repaso.client.entity.Client;
import com.nomelestar.repaso.client.mapper.ClientMapper;
import com.nomelestar.repaso.client.repository.ClientRepository;
import com.nomelestar.repaso.common.exception.BadRequestException;
import com.nomelestar.repaso.common.exception.ResourceNotFoundException;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.entity.Dish;
import com.nomelestar.repaso.dish.mapper.DishMapper;
import com.nomelestar.repaso.dish.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de Clientes.
 * Incluye CRUD y operaciones de relación muchos a muchos con platos.
 */
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final DishRepository dishRepository;

    public ClientResponse crearCliente(ClientRequest request) {
        validarRequest(request);
        Client nuevoCliente = ClientMapper.toEntity(request);
        Client clienteGuardado = clientRepository.save(nuevoCliente);
        return ClientMapper.toResponse(clienteGuardado);
    }

    public List<ClientResponse> obtenerTodos() {
        return clientRepository.findAll().stream()
                .map(ClientMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ClientResponse obtenerPorId(UUID id) {
        Client client = buscarPorIdOGenerarExcepcion(id);
        return ClientMapper.toResponse(client);
    }

    public ClientResponse actualizarCliente(UUID id, ClientRequest request) {
        validarRequest(request);
        Client clienteExistente = buscarPorIdOGenerarExcepcion(id);
        clienteExistente.setNombre(request.nombre());
        clienteExistente.setEmail(request.email());
        Client clienteActualizado = clientRepository.save(clienteExistente);
        return ClientMapper.toResponse(clienteActualizado);
    }

    public void eliminarCliente(UUID id) {
        Client client = buscarPorIdOGenerarExcepcion(id);
        clientRepository.delete(client);
    }

    /**
     * Registra que un cliente consumió un plato (agrega a la tabla intermedia).
     */
    @Transactional
    public ClientResponse consumirPlato(UUID clientId, UUID dishId) {
        Client client = buscarPorIdOGenerarExcepcion(clientId);
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el plato con ID: " + dishId));

        // Evitar duplicados
        if (!client.getPlatosConsumidos().contains(dish)) {
            client.getPlatosConsumidos().add(dish);
            clientRepository.save(client);
        }

        return ClientMapper.toResponse(client);
    }

    /**
     * Obtiene el Chef con más platos vendidos (comprados por clientes).
     */
    public ChefResponse obtenerChefConMasVentas() {
        Chef chef = clientRepository.findChefConMasVentas()
                .orElseThrow(() -> new ResourceNotFoundException("No hay datos de ventas disponibles"));
        return ChefMapper.toResponse(chef);
    }

    /**
     * Obtiene los platos que un cliente ha consumido de un chef específico.
     */
    public List<DishResponse> obtenerPlatosDeClientePorChef(UUID clientId, UUID chefId) {
        // Validar que el cliente exista
        buscarPorIdOGenerarExcepcion(clientId);

        List<Dish> platos = clientRepository.findPlatosDeClientePorChef(clientId, chefId);
        return platos.stream()
                .map(DishMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validarRequest(ClientRequest request) {
        if (request.nombre() == null || request.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del cliente no puede estar vacío");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new BadRequestException("El email del cliente no puede estar vacío");
        }
    }

    private Client buscarPorIdOGenerarExcepcion(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún cliente con el ID: " + id));
    }
}
