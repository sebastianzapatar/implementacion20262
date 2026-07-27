package com.nomelestar.repaso.chef.service;

import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.mapper.ChefMapper;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import com.nomelestar.repaso.client.entity.Client;
import com.nomelestar.repaso.common.exception.BadRequestException;
import com.nomelestar.repaso.common.exception.ResourceNotFoundException;
import com.nomelestar.repaso.dish.entity.Dish;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Capa de Lógica de Negocio (Service).
 * Aquí se validan los datos y se interactúa con el repositorio.
 * La anotación @Service le indica a Spring que esta clase es un Bean y debe inyectarse
 * en donde sea requerida (por ejemplo en el Controller).
 */
@Service
@RequiredArgsConstructor // Genera un constructor con los campos 'final' para inyección de dependencias
public class ChefService {

    private final ChefRepository chefRepository;

    /**
     * Crea un nuevo Chef.
     * @param request Datos del Chef a crear.
     * @return ChefResponse con los datos del Chef creado.
     */
    @Transactional
    public ChefResponse crearChef(ChefRequest request) {
        // 1. Validamos que el nombre no venga nulo ni vacío
        validarRequest(request);
        
        // 2. Convertimos el DTO de entrada a Entidad JPA
        Chef nuevoChef = ChefMapper.toEntity(request);
        
        // 3. Persistimos en base de datos
        Chef chefGuardado = chefRepository.save(nuevoChef);
        
        // 4. Retornamos el DTO de salida
        return ChefMapper.toResponse(chefGuardado);
    }

    /**
     * Obtiene todos los Chefs registrados.
     */
    @Transactional(readOnly = true)
    public List<ChefResponse> obtenerTodos() {
        // findAllBy() usa @EntityGraph: trae los chefs CON sus platos en una
        // sola consulta. Con findAll() se lanzaba una consulta extra por chef
        // al leer getPlatos() en el mapper (problema N+1).
        return chefRepository.findAllBy().stream()
                .map(ChefMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un Chef por su ID.
     */
    @Transactional(readOnly = true)
    public ChefResponse obtenerPorId(UUID id) {
        // Versión con @EntityGraph para traer también los platos que el mapper necesita
        Chef chef = chefRepository.findWithPlatosById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún chef con el ID: " + id));
        return ChefMapper.toResponse(chef);
    }

    /**
     * Actualiza los datos de un Chef existente.
     */
    @Transactional
    public ChefResponse actualizarChef(UUID id, ChefRequest request) {
        // 1. Validamos los datos entrantes
        validarRequest(request);
        
        // 2. Obtenemos el Chef actual de la BD
        Chef chefExistente = buscarPorIdOGenerarExcepcion(id);
        
        // 3. Modificamos los atributos necesarios
        chefExistente.setNombre(request.nombre());
        
        // 4. Guardamos los cambios (Hibernate detecta que ya existe y hace un UPDATE)
        Chef chefActualizado = chefRepository.save(chefExistente);
        return ChefMapper.toResponse(chefActualizado);
    }

    /**
     * Elimina un Chef y, por CascadeType.ALL, todos sus platos.
     *
     * <p>ANTES fallaba: al borrar los platos, las filas de la tabla intermedia
     * {@code dish_clients} seguían apuntando a esos platos y Postgres rechazaba
     * el DELETE con un error de llave foránea (que salía como HTTP 500).
     *
     * <p>La cascada de JPA solo viaja por la relación Chef -> Dish; no sabe nada
     * de la Muchos-a-Muchos Dish <-> Client. Como el dueño de esa relación es
     * Client (ahí está el @JoinTable), hay que sacar el plato de la lista de
     * cada cliente ANTES de borrar. Eso genera los DELETE en dish_clients.
     */
    @Transactional
    public void eliminarChef(UUID id) {
        // Traemos el chef junto con sus platos para poder recorrerlos
        Chef chef = chefRepository.findWithPlatosById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún chef con el ID: " + id));

        for (Dish plato : chef.getPlatos()) {
            desvincularPlatoDeSusClientes(plato);
        }

        chefRepository.delete(chef);
    }

    /**
     * Rompe la relación Muchos-a-Muchos de un plato con todos sus clientes.
     * Hay que tocar el lado DUEÑO (Client.platosConsumidos) para que Hibernate
     * genere realmente los DELETE sobre la tabla intermedia.
     */
    private void desvincularPlatoDeSusClientes(Dish plato) {
        for (Client cliente : plato.getClientes()) {
            cliente.getPlatosConsumidos().remove(plato);
        }
        plato.getClientes().clear();
    }

    /**
     * Método auxiliar privado para validar las reglas de negocio básicas.
     * Si no se cumplen, lanza una excepción de tipo BadRequest (HTTP 400).
     */
    private void validarRequest(ChefRequest request) {
        if (request.nombre() == null || request.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del chef no puede estar vacío");
        }
    }

    /**
     * Centraliza la búsqueda por ID y el lanzamiento de la excepción ResourceNotFoundException (HTTP 404).
     */
    private Chef buscarPorIdOGenerarExcepcion(UUID id) {
        return chefRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún chef con el ID: " + id));
    }
}
