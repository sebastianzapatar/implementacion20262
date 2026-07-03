package com.nomelestar.repaso.chef.service;

import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.mapper.ChefMapper;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import com.nomelestar.repaso.common.exception.BadRequestException;
import com.nomelestar.repaso.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public List<ChefResponse> obtenerTodos() {
        // Buscamos todos, mapeamos cada Entidad a DTO y retornamos la lista
        return chefRepository.findAll().stream()
                .map(ChefMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un Chef por su ID.
     */
    public ChefResponse obtenerPorId(UUID id) {
        // Reutilizamos el método privado para buscar o lanzar excepción 404
        Chef chef = buscarPorIdOGenerarExcepcion(id);
        return ChefMapper.toResponse(chef);
    }

    /**
     * Actualiza los datos de un Chef existente.
     */
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
     * Elimina un Chef.
     */
    public void eliminarChef(UUID id) {
        // Si no existe, lanzará 404. Si existe, lo elimina de la BD.
        Chef chef = buscarPorIdOGenerarExcepcion(id);
        chefRepository.delete(chef);
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
