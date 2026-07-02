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
     */
    public ChefResponse crearChef(ChefRequest request) {
        // Validación de ejemplo (simulando un error del usuario si el nombre viene vacío)
        if (request.nombre() == null || request.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del Chef no puede estar vacío");
        }

        // 1. Mapeamos el DTO de entrada a Entidad
        Chef nuevoChef = ChefMapper.toEntity(request);
        
        // 2. Guardamos en la base de datos
        Chef chefGuardado = chefRepository.save(nuevoChef);
        
        // 3. Mapeamos la Entidad guardada a DTO de respuesta
        return ChefMapper.toResponse(chefGuardado);
    }

    /**
     * Obtiene todos los Chefs.
     */
    public List<ChefResponse> obtenerTodos() {
        return chefRepository.findAll().stream()
                .map(ChefMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un Chef por su ID.
     */
    public ChefResponse obtenerPorId(UUID id) {
        Chef chef = buscarPorIdOGenerarExcepcion(id);
        return ChefMapper.toResponse(chef);
    }

    /**
     * Actualiza un Chef existente.
     */
    public ChefResponse actualizarChef(UUID id, ChefRequest request) {
        if (request.nombre() == null || request.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del Chef no puede estar vacío para actualizar");
        }

        // Buscamos el chef en la BD, si no existe arrojará 404
        Chef chefExistente = buscarPorIdOGenerarExcepcion(id);
        
        // Actualizamos los campos de la entidad
        chefExistente.setNombre(request.nombre());
        
        // Guardamos los cambios
        Chef chefActualizado = chefRepository.save(chefExistente);
        
        return ChefMapper.toResponse(chefActualizado);
    }

    /**
     * Elimina un Chef por su ID.
     */
    public void eliminarChef(UUID id) {
        Chef chef = buscarPorIdOGenerarExcepcion(id);
        chefRepository.delete(chef);
    }

    /**
     * Método auxiliar privado para reutilizar la lógica de buscar un Chef o lanzar excepción 404.
     */
    private Chef buscarPorIdOGenerarExcepcion(UUID id) {
        return chefRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún Chef con el ID: " + id));
    }
}
