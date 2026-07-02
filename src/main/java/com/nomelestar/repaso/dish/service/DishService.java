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

    private final DishRepository dishRepository;
    private final ChefRepository chefRepository; // Inyectamos el repositorio del chef para buscarlo

    public DishResponse crearPlato(DishRequest request) {
        validarRequest(request);

        // Validar que el Chef existe
        Chef chef = chefRepository.findById(request.chefId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el Chef con ID: " + request.chefId()));

        Dish nuevoPlato = DishMapper.toEntity(request, chef);
        Dish platoGuardado = dishRepository.save(nuevoPlato);
        
        return DishMapper.toResponse(platoGuardado);
    }

    public List<DishResponse> obtenerTodos() {
        return dishRepository.findAll().stream()
                .map(DishMapper::toResponse)
                .collect(Collectors.toList());
    }

    public DishResponse obtenerPorId(UUID id) {
        Dish dish = buscarPorIdOGenerarExcepcion(id);
        return DishMapper.toResponse(dish);
    }

    public DishResponse actualizarPlato(UUID id, DishRequest request) {
        validarRequest(request);

        Dish platoExistente = buscarPorIdOGenerarExcepcion(id);

        Chef chef = chefRepository.findById(request.chefId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el Chef con ID: " + request.chefId()));

        platoExistente.setNombre(request.nombre());
        platoExistente.setDescripcion(request.descripcion());
        platoExistente.setPrecio(request.precio());
        platoExistente.setChef(chef);

        Dish platoActualizado = dishRepository.save(platoExistente);
        return DishMapper.toResponse(platoActualizado);
    }

    public void eliminarPlato(UUID id) {
        Dish dish = buscarPorIdOGenerarExcepcion(id);
        dishRepository.delete(dish);
    }

    private void validarRequest(DishRequest request) {
        if (request.nombre() == null || request.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del plato no puede estar vacío");
        }
        if (request.precio() == null || request.precio().doubleValue() < 0) {
            throw new BadRequestException("El precio del plato no es válido");
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
