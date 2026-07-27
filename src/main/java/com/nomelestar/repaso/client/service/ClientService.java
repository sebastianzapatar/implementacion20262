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
import com.nomelestar.repaso.dish.dto.DishPopularityResponse;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.entity.Dish;
import com.nomelestar.repaso.dish.mapper.DishMapper;
import com.nomelestar.repaso.dish.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    @Transactional
    public ClientResponse crearCliente(ClientRequest request) {
        // 1. Validamos que los campos obligatorios vengan en la petición
        validarRequest(request);
        
        // 2. Convertimos el DTO (Request) a la Entidad (Client) para guardarlo
        Client nuevoCliente = ClientMapper.toEntity(request);
        
        if (request.dishIds() != null && !request.dishIds().isEmpty()) {
            List<Dish> dishes = dishRepository.findAllById(request.dishIds());
            if (dishes.size() != request.dishIds().size()) {
                throw new ResourceNotFoundException("Algunos platos no fueron encontrados");
            }
            nuevoCliente.getPlatosConsumidos().addAll(dishes);
        }

        // 3. Persistimos el cliente en la base de datos usando el repositorio
        Client clienteGuardado = clientRepository.save(nuevoCliente);
        
        // 4. Retornamos la respuesta mapeando la entidad guardada de vuelta a DTO (Response)
        return ClientMapper.toResponse(clienteGuardado);
    }

    /**
     * Obtiene una lista con todos los clientes registrados.
     */
    @Transactional(readOnly = true)
    public List<ClientResponse> obtenerTodos() {
        // Usamos la consulta con JOIN FETCH para traer clientes + platos + chef
        // en UNA sola consulta. Con findAll() el mapper disparaba una consulta
        // extra por cada cliente al leer getPlatosConsumidos() (problema N+1).
        return clientRepository.findAllConPlatosYChef().stream()
                .map(ClientMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ranking de los platos más consumidos del restaurante.
     *
     * <p>Delega en la consulta JPQL compleja {@code rankingPlatosMasConsumidos}
     * (JOIN sobre la @ManyToMany + JOIN con chef + GROUP BY + HAVING + agregados).
     * El repositorio ya devuelve DTOs, así que aquí no hay que mapear nada.
     *
     * @param precioMinimo   precio mínimo del plato a considerar
     * @param minimoClientes cuántos clientes distintos debe tener como mínimo
     */
    @Transactional(readOnly = true)
    public List<DishPopularityResponse> obtenerRankingPlatos(BigDecimal precioMinimo, long minimoClientes) {
        return clientRepository.rankingPlatosMasConsumidos(precioMinimo, minimoClientes);
    }

    /**
     * Busca un cliente por su ID y lo retorna.
     */
    @Transactional(readOnly = true)
    public ClientResponse obtenerPorId(UUID id) {
        // Delegamos la búsqueda y el manejo del error (404) a un método privado
        Client client = buscarPorIdOGenerarExcepcion(id);
        return ClientMapper.toResponse(client);
    }

    /**
     * Actualiza los datos de un cliente existente.
     */
    @Transactional
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
    @Transactional
    public void eliminarCliente(UUID id) {
        // Acá no hace falta limpiar dish_clients a mano: el cliente es el DUEÑO
        // de la relación, así que Hibernate borra solo sus filas de la tabla
        // intermedia. El problema aparece al borrar del otro lado (Dish/Chef).
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
    @Transactional(readOnly = true)
    public ChefResponse obtenerChefConMasVentas() {
        // Llama a la consulta @Query (JPQL) definida en ClientRepository
        Chef chef = clientRepository.findChefConMasVentas()
                .orElseThrow(() -> new ResourceNotFoundException("No hay datos de ventas disponibles"));
        return ChefMapper.toResponse(chef);
    }

    /**
     * Obtiene los platos que un cliente ha consumido de un chef específico.
     */
    @Transactional(readOnly = true)
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
