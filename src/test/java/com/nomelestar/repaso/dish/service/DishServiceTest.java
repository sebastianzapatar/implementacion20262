package com.nomelestar.repaso.dish.service;

import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import com.nomelestar.repaso.common.exception.BadRequestException;
import com.nomelestar.repaso.common.exception.ResourceNotFoundException;
import com.nomelestar.repaso.dish.dto.DishRequest;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.entity.Dish;
import com.nomelestar.repaso.dish.repository.DishRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

/**
 * Pruebas unitarias para DishService.
 * 
 * Se mockean tanto DishRepository como ChefRepository porque el servicio
 * depende de ambos (para validar que el Chef exista al crear/actualizar un plato).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DishService — Pruebas Unitarias")
class DishServiceTest {

    @Mock
    private DishRepository dishRepository;

    @Mock
    private ChefRepository chefRepository;

    @InjectMocks
    private DishService dishService;

    // Helper para crear un Chef de prueba
    private Chef crearChefDePrueba() {
        return Chef.builder()
                .id(UUID.randomUUID())
                .nombre("Chef de Prueba")
                .platos(new ArrayList<>())
                .build();
    }

    // Helper para crear un Dish de prueba
    private Dish crearDishDePrueba(Chef chef) {
        return Dish.builder()
                .id(UUID.randomUUID())
                .nombre("Bandeja Paisa")
                .descripcion("Plato típico colombiano")
                .precio(new BigDecimal("35000"))
                .chef(chef)
                .build();
    }

    // =========================================================================
    // CREAR PLATO
    // =========================================================================
    @Nested
    @DisplayName("crearPlato()")
    class CrearPlato {

        @Test
        @DisplayName("✅ Con datos válidos → retorna DishResponse")
        void crearPlato_conDatosValidos_retornaDishResponse() {
            // Arrange
            Chef chef = crearChefDePrueba();
            DishRequest request = new DishRequest("Bandeja Paisa", "Plato típico", new BigDecimal("35000"), chef.getId());
            Dish dishGuardado = crearDishDePrueba(chef);

            when(chefRepository.findById(chef.getId())).thenReturn(Optional.of(chef));
            when(dishRepository.save(any(Dish.class))).thenReturn(dishGuardado);

            // Act
            DishResponse response = dishService.crearPlato(request);

            // Assert
            assertNotNull(response);
            assertEquals("Bandeja Paisa", response.nombre());
            assertEquals(chef.getId(), response.chefId());
            assertEquals("Chef de Prueba", response.nombreChef());
            verify(chefRepository).findById(chef.getId());
            verify(dishRepository).save(any(Dish.class));
        }

        @Test
        @DisplayName("❌ Con nombre vacío → lanza BadRequestException")
        void crearPlato_conNombreVacio_lanzaBadRequestException() {
            // Arrange
            DishRequest request = new DishRequest("", "desc", new BigDecimal("10000"), UUID.randomUUID());

            // Act & Assert
            assertThrows(BadRequestException.class, () -> dishService.crearPlato(request));
            verify(dishRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Con precio negativo → lanza BadRequestException")
        void crearPlato_conPrecioNegativo_lanzaBadRequestException() {
            // Arrange
            DishRequest request = new DishRequest("Plato", "desc", new BigDecimal("-5000"), UUID.randomUUID());

            // Act & Assert
            assertThrows(BadRequestException.class, () -> dishService.crearPlato(request));
            verify(dishRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Con chefId nulo → lanza BadRequestException")
        void crearPlato_conChefIdNulo_lanzaBadRequestException() {
            // Arrange
            DishRequest request = new DishRequest("Plato", "desc", new BigDecimal("10000"), null);

            // Act & Assert
            assertThrows(BadRequestException.class, () -> dishService.crearPlato(request));
        }

        @Test
        @DisplayName("❌ Con chef inexistente → lanza ResourceNotFoundException")
        void crearPlato_conChefInexistente_lanzaResourceNotFoundException() {
            // Arrange
            UUID chefIdFalso = UUID.randomUUID();
            DishRequest request = new DishRequest("Plato", "desc", new BigDecimal("10000"), chefIdFalso);
            when(chefRepository.findById(chefIdFalso)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> dishService.crearPlato(request)
            );
            assertTrue(exception.getMessage().contains(chefIdFalso.toString()));
        }
    }

    // =========================================================================
    // OBTENER TODOS
    // =========================================================================
    @Nested
    @DisplayName("obtenerTodos()")
    class ObtenerTodos {

        @Test
        @DisplayName("✅ Con platos existentes → retorna lista")
        void obtenerTodos_retornaListaDePlatos() {
            // Arrange
            Chef chef = crearChefDePrueba();
            Dish dish1 = crearDishDePrueba(chef);
            Dish dish2 = Dish.builder().id(UUID.randomUUID()).nombre("Ajiaco").descripcion("Sopa bogotana")
                    .precio(new BigDecimal("25000")).chef(chef).build();
            when(dishRepository.findAll()).thenReturn(List.of(dish1, dish2));

            // Act
            List<DishResponse> resultado = dishService.obtenerTodos();

            // Assert
            assertEquals(2, resultado.size());
        }
    }

    // =========================================================================
    // OBTENER POR ID
    // =========================================================================
    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorId {

        @Test
        @DisplayName("✅ Con ID existente → retorna plato")
        void obtenerPorId_conIdExistente_retornaPlato() {
            // Arrange
            Chef chef = crearChefDePrueba();
            Dish dish = crearDishDePrueba(chef);
            when(dishRepository.findById(dish.getId())).thenReturn(Optional.of(dish));

            // Act
            DishResponse response = dishService.obtenerPorId(dish.getId());

            // Assert
            assertEquals(dish.getId(), response.id());
            assertEquals("Bandeja Paisa", response.nombre());
        }

        @Test
        @DisplayName("❌ Con ID inexistente → lanza ResourceNotFoundException")
        void obtenerPorId_conIdInexistente_lanzaResourceNotFoundException() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(dishRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> dishService.obtenerPorId(id));
        }
    }

    // =========================================================================
    // ACTUALIZAR PLATO
    // =========================================================================
    @Nested
    @DisplayName("actualizarPlato()")
    class ActualizarPlato {

        @Test
        @DisplayName("✅ Con datos válidos → retorna plato actualizado")
        void actualizarPlato_conDatosValidos_retornaPlatoActualizado() {
            // Arrange
            Chef chef = crearChefDePrueba();
            Dish platoExistente = crearDishDePrueba(chef);
            UUID platoId = platoExistente.getId();

            DishRequest request = new DishRequest("Ajiaco Santafereño", "Receta actualizada",
                    new BigDecimal("28000"), chef.getId());

            when(dishRepository.findById(platoId)).thenReturn(Optional.of(platoExistente));
            when(dishRepository.save(any(Dish.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            DishResponse response = dishService.actualizarPlato(platoId, request);

            // Assert
            assertEquals("Ajiaco Santafereño", response.nombre());
            assertEquals("Receta actualizada", response.descripcion());
        }

        @Test
        @DisplayName("❌ Con chef inexistente → lanza ResourceNotFoundException")
        void actualizarPlato_conChefInexistente_lanzaResourceNotFoundException() {
            // Arrange
            Chef chef = crearChefDePrueba();
            Dish platoExistente = crearDishDePrueba(chef);
            UUID chefIdFalso = UUID.randomUUID();
            DishRequest request = new DishRequest("Plato", "desc", new BigDecimal("10000"), chefIdFalso);

            when(dishRepository.findById(platoExistente.getId())).thenReturn(Optional.of(platoExistente));
            when(chefRepository.findById(chefIdFalso)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class,
                    () -> dishService.actualizarPlato(platoExistente.getId(), request));
        }
    }

    // =========================================================================
    // ELIMINAR PLATO
    // =========================================================================
    @Nested
    @DisplayName("eliminarPlato()")
    class EliminarPlato {

        @Test
        @DisplayName("✅ Con ID existente → elimina correctamente")
        void eliminarPlato_conIdExistente_eliminaCorrectamente() {
            // Arrange
            Chef chef = crearChefDePrueba();
            Dish dish = crearDishDePrueba(chef);
            when(dishRepository.findById(dish.getId())).thenReturn(Optional.of(dish));

            // Act
            dishService.eliminarPlato(dish.getId());

            // Assert
            verify(dishRepository).delete(dish);
        }

        @Test
        @DisplayName("❌ Con ID inexistente → lanza ResourceNotFoundException")
        void eliminarPlato_conIdInexistente_lanzaResourceNotFoundException() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(dishRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> dishService.eliminarPlato(id));
            verify(dishRepository, never()).delete(any());
        }
    }
}
