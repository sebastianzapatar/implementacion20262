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

    // Dependencias inyectadas por Spring gracias a @RequiredArgsConstructor
    private final ClientRepository clientRepository;
    private final DishRepository dishRepository;

    /**
     * Crea un nuevo cliente validando previamente los datos de entrada.
     * @param request Datos del cliente a crear (DTO).
     * @return El cliente creado convertido en DTO.
     */
    public ClientResponse crearCliente(ClientRequest request) {
        // 1. Validamos que los campos obligatorios vengan en la petición
        validarRequest(request);
        
        // 2. Convertimos el DTO (Request) a la Entidad (Client) para guardarlo
        Client nuevoCliente = ClientMapper.toEntity(request);
        
        // 3. Persistimos el cliente en la base de datos usando el repositorio
        Client clienteGuardado = clientRepository.save(nuevoCliente);
        
        // 4. Retornamos la respuesta mapeando la entidad guardada de vuelta a DTO (Response)
        return ClientMapper.toResponse(clienteGuardado);
    }

    /**
     * Obtiene una lista con todos los clientes registrados.
     */
    public List<ClientResponse> obtenerTodos() {
        // Obtenemos todos los registros, los convertimos a un Stream para mapearlos,
        // y por cada Entidad, llamamos al mapper para convertirla en DTO de respuesta.
        return clientRepository.findAll().stream()
                .map(ClientMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca un cliente por su ID y lo retorna.
     */
    public ClientResponse obtenerPorId(UUID id) {
        // Delegamos la búsqueda y el manejo del error (404) a un método privado
        Client client = buscarPorIdOGenerarExcepcion(id);
        return ClientMapper.toResponse(client);
    }

    /**
     * Actualiza los datos de un cliente existente.
     */
    public ClientResponse actualizarCliente(UUID id, ClientRequest request) {
        // 1. Validamos los datos entrantes
        validarRequest(request);
        
        // 2. Buscamos el cliente actual en base de datos. Si no existe, lanza excepción
        Client clienteExistente = buscarPorIdOGenerarExcepcion(id);
        
        // 3. Actualizamos las propiedades de la entidad
        clienteExistente.setNombre(request.nombre());
        clienteExistente.setEmail(request.email());
        
        // 4. Al hacer save(), JPA (Hibernate) hace un UPDATE SQL
        Client clienteActualizado = clientRepository.save(clienteExistente);
        
        return ClientMapper.toResponse(clienteActualizado);
    }

    /**
     * Elimina físicamente un cliente de la base de datos.
     */
    public void eliminarCliente(UUID id) {
        Client client = buscarPorIdOGenerarExcepcion(id);
        clientRepository.delete(client);
    }

    /**
     * Registra que un cliente consumió un plato (agrega a la tabla intermedia).
     * @Transactional asegura que si ocurre un error, no se guarden cambios parciales.
     */
    @Transactional
    public ClientResponse consumirPlato(UUID clientId, UUID dishId) {
        // 1. Buscamos el cliente
        Client client = buscarPorIdOGenerarExcepcion(clientId);
        
        // 2. Buscamos el plato, y si no existe, lanzamos 404
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el plato con ID: " + dishId));

        // 3. Verificamos que no se agregue de manera duplicada en la colección
        if (!client.getPlatosConsumidos().contains(dish)) {
            // Se añade a la lista mapeada con @ManyToMany
            client.getPlatosConsumidos().add(dish);
            // JPA se encargará de insertar un registro en la tabla intermedia "dish_clients"
            clientRepository.save(client);
        }

        return ClientMapper.toResponse(client);
    }

    /**
     * Obtiene el Chef con más platos vendidos (comprados por clientes).
     */
    public ChefResponse obtenerChefConMasVentas() {
        // Llama a la consulta @Query (JPQL) definida en ClientRepository
        Chef chef = clientRepository.findChefConMasVentas()
                .orElseThrow(() -> new ResourceNotFoundException("No hay datos de ventas disponibles"));
        return ChefMapper.toResponse(chef);
    }

    /**
     * Obtiene los platos que un cliente ha consumido de un chef específico.
     */
    public List<DishResponse> obtenerPlatosDeClientePorChef(UUID clientId, UUID chefId) {
        // 1. Validar que el cliente exista antes de buscar sus consumos
        buscarPorIdOGenerarExcepcion(clientId);

        // 2. Obtiene la lista de platos usando la consulta JPQL con JOIN
        List<Dish> platos = clientRepository.findPlatosDeClientePorChef(clientId, chefId);
        
        // 3. Convierte cada entidad Dish a DishResponse
        return platos.stream()
                .map(DishMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Método auxiliar privado para centralizar las validaciones.
     */
    private void validarRequest(ClientRequest request) {
        if (request.nombre() == null || request.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del cliente no puede estar vacío");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new BadRequestException("El email del cliente no puede estar vacío");
        }
    }

    /**
     * Método auxiliar privado para buscar por ID y lanzar 404 (Not Found) centralizado.
     */
    private Client buscarPorIdOGenerarExcepcion(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún cliente con el ID: " + id));
    }
}
