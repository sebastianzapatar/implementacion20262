package com.nomelestar.repaso.dish.service;

import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import com.nomelestar.repaso.common.exception.BadRequestException;
import com.nomelestar.repaso.common.exception.ResourceNotFoundException;
import com.nomelestar.repaso.dish.dto.DishRequest;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.entity.Dish;
import com.nomelestar.repaso.dish.mapper.DishMapper;
import com.nomelestar.repaso.dish.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishService {

    // Dependencias inyectadas (repositorios para acceder a BD)
    private final DishRepository dishRepository;
    private final ChefRepository chefRepository;

    /**
     * Crea un nuevo plato asociado a un chef existente.
     * @param request Datos del plato a crear.
     * @return El plato creado en formato Response.
     */
    public DishResponse crearPlato(DishRequest request) {
        // 1. Validar que el nombre no esté vacío
        validarRequest(request);

        // 2. Buscar al chef asociado, si no existe lanza excepción
        Chef chefAsociado = chefRepository.findById(request.chefId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró al Chef con ID: " + request.chefId()));

        // 3. Crear entidad y establecer la relación bidireccional ManyToOne
        Dish nuevoPlato = DishMapper.toEntity(request, chefAsociado);
        
        // 4. Guardar en BD
        Dish platoGuardado = dishRepository.save(nuevoPlato);
        return DishMapper.toResponse(platoGuardado);
    }

    /**
     * Obtiene todos los platos almacenados.
     */
    public List<DishResponse> obtenerTodos() {
        return dishRepository.findAll().stream()
                .map(DishMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un plato específico por su ID.
     */
    public DishResponse obtenerPorId(UUID id) {
        Dish dish = buscarPorIdOGenerarExcepcion(id);
        return DishMapper.toResponse(dish);
    }

    /**
     * Actualiza los datos de un plato. Permite cambiar el chef asociado.
     */
    public DishResponse actualizarPlato(UUID id, DishRequest request) {
        // 1. Validar request
        validarRequest(request);
        
        // 2. Buscar plato a actualizar
        Dish platoExistente = buscarPorIdOGenerarExcepcion(id);

        // 3. Si el request envía un chefId distinto, validamos que el nuevo chef exista
        if (!platoExistente.getChef().getId().equals(request.chefId())) {
            Chef nuevoChef = chefRepository.findById(request.chefId())
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontró el nuevo Chef con ID: " + request.chefId()));
            platoExistente.setChef(nuevoChef);
        }

        // 4. Actualizar las demás propiedades
        platoExistente.setNombre(request.nombre());
        platoExistente.setDescripcion(request.descripcion());
        platoExistente.setPrecio(request.precio());

        // 5. Guardar cambios en BD (Hibernate detecta que ya existe e internamente hace UPDATE)
        Dish platoActualizado = dishRepository.save(platoExistente);
        return DishMapper.toResponse(platoActualizado);
    }

    /**
     * Elimina físicamente un plato de la base de datos.
     */
    public void eliminarPlato(UUID id) {
        Dish dish = buscarPorIdOGenerarExcepcion(id);
        dishRepository.delete(dish);
    }

    /**
     * Valida reglas de negocio básicas del DishRequest.
     */
    private void validarRequest(DishRequest request) {
        if (request.nombre() == null || request.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del plato no puede estar vacío");
        }
        if (request.precio() == null || request.precio().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("El precio no puede ser negativo o nulo");
        }
        if (request.chefId() == null) {
            throw new BadRequestException("El ID del Chef es requerido para crear/actualizar un plato");
        }
    }

    private Dish buscarPorIdOGenerarExcepcion(UUID id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró ningún plato con el ID: " + id));
    }
}
