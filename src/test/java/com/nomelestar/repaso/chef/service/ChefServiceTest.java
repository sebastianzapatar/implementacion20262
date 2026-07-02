package com.nomelestar.repaso.chef.service;

import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import com.nomelestar.repaso.common.exception.BadRequestException;
import com.nomelestar.repaso.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ChefService.
 * 
 * Se usa @ExtendWith(MockitoExtension.class) para NO levantar el contexto de Spring.
 * Los repositorios se simulan (mockean) con @Mock y se inyectan automáticamente
 * en el servicio con @InjectMocks.
 * 
 * Patrón AAA: Arrange → Act → Assert
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChefService — Pruebas Unitarias")
class ChefServiceTest {

    @Mock
    private ChefRepository chefRepository;

    @InjectMocks
    private ChefService chefService;

    // =========================================================================
    // CREAR CHEF
    // =========================================================================
    @Nested
    @DisplayName("crearChef()")
    class CrearChef {

        @Test
        @DisplayName("✅ Con nombre válido → retorna ChefResponse con ID generado")
        void crearChef_conNombreValido_retornaChefResponse() {
            // Arrange
            ChefRequest request = new ChefRequest("Gordon Ramsay");
            UUID idGenerado = UUID.randomUUID();
            Chef chefGuardado = Chef.builder()
                    .id(idGenerado)
                    .nombre("Gordon Ramsay")
                    .platos(new ArrayList<>())
                    .build();

            when(chefRepository.save(any(Chef.class))).thenReturn(chefGuardado);

            // Act
            ChefResponse response = chefService.crearChef(request);

            // Assert
            assertNotNull(response);
            assertEquals(idGenerado, response.id());
            assertEquals("Gordon Ramsay", response.nombre());
            assertTrue(response.platos().isEmpty());

            // Verificar que el repositorio fue invocado exactamente una vez
            verify(chefRepository, times(1)).save(any(Chef.class));
        }

        @Test
        @DisplayName("❌ Con nombre vacío → lanza BadRequestException")
        void crearChef_conNombreVacio_lanzaBadRequestException() {
            // Arrange
            ChefRequest request = new ChefRequest("   ");

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> chefService.crearChef(request)
            );

            assertTrue(exception.getMessage().contains("vacío"));
            // Verificar que NUNCA se llamó al repositorio
            verify(chefRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Con nombre nulo → lanza BadRequestException")
        void crearChef_conNombreNulo_lanzaBadRequestException() {
            // Arrange
            ChefRequest request = new ChefRequest(null);

            // Act & Assert
            assertThrows(BadRequestException.class, () -> chefService.crearChef(request));
            verify(chefRepository, never()).save(any());
        }
    }

    // =========================================================================
    // OBTENER TODOS
    // =========================================================================
    @Nested
    @DisplayName("obtenerTodos()")
    class ObtenerTodos {

        @Test
        @DisplayName("✅ Con chefs existentes → retorna lista con elementos")
        void obtenerTodos_retornaListaDeChefs() {
            // Arrange
            Chef chef1 = Chef.builder().id(UUID.randomUUID()).nombre("Chef 1").platos(new ArrayList<>()).build();
            Chef chef2 = Chef.builder().id(UUID.randomUUID()).nombre("Chef 2").platos(new ArrayList<>()).build();
            when(chefRepository.findAll()).thenReturn(List.of(chef1, chef2));

            // Act
            List<ChefResponse> resultado = chefService.obtenerTodos();

            // Assert
            assertEquals(2, resultado.size());
            assertEquals("Chef 1", resultado.get(0).nombre());
            assertEquals("Chef 2", resultado.get(1).nombre());
            verify(chefRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("✅ Sin chefs → retorna lista vacía")
        void obtenerTodos_sinChefs_retornaListaVacia() {
            // Arrange
            when(chefRepository.findAll()).thenReturn(List.of());

            // Act
            List<ChefResponse> resultado = chefService.obtenerTodos();

            // Assert
            assertTrue(resultado.isEmpty());
        }
    }

    // =========================================================================
    // OBTENER POR ID
    // =========================================================================
    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorId {

        @Test
        @DisplayName("✅ Con ID existente → retorna el chef")
        void obtenerPorId_conIdExistente_retornaChef() {
            // Arrange
            UUID id = UUID.randomUUID();
            Chef chef = Chef.builder().id(id).nombre("Gastón Acurio").platos(new ArrayList<>()).build();
            when(chefRepository.findById(id)).thenReturn(Optional.of(chef));

            // Act
            ChefResponse response = chefService.obtenerPorId(id);

            // Assert
            assertEquals(id, response.id());
            assertEquals("Gastón Acurio", response.nombre());
        }

        @Test
        @DisplayName("❌ Con ID inexistente → lanza ResourceNotFoundException")
        void obtenerPorId_conIdInexistente_lanzaResourceNotFoundException() {
            // Arrange
            UUID idInexistente = UUID.randomUUID();
            when(chefRepository.findById(idInexistente)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> chefService.obtenerPorId(idInexistente)
            );

            assertTrue(exception.getMessage().contains(idInexistente.toString()));
        }
    }

    // =========================================================================
    // ACTUALIZAR CHEF
    // =========================================================================
    @Nested
    @DisplayName("actualizarChef()")
    class ActualizarChef {

        @Test
        @DisplayName("✅ Con datos válidos → retorna chef actualizado")
        void actualizarChef_conDatosValidos_retornaChefActualizado() {
            // Arrange
            UUID id = UUID.randomUUID();
            Chef chefExistente = Chef.builder().id(id).nombre("Nombre viejo").platos(new ArrayList<>()).build();
            ChefRequest request = new ChefRequest("Nombre nuevo");

            when(chefRepository.findById(id)).thenReturn(Optional.of(chefExistente));
            when(chefRepository.save(any(Chef.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ChefResponse response = chefService.actualizarChef(id, request);

            // Assert
            assertEquals("Nombre nuevo", response.nombre());
            verify(chefRepository).findById(id);
            verify(chefRepository).save(any(Chef.class));
        }

        @Test
        @DisplayName("❌ Con ID inexistente → lanza ResourceNotFoundException")
        void actualizarChef_conIdInexistente_lanzaResourceNotFoundException() {
            // Arrange
            UUID id = UUID.randomUUID();
            ChefRequest request = new ChefRequest("Nombre");
            when(chefRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> chefService.actualizarChef(id, request));
        }

        @Test
        @DisplayName("❌ Con nombre vacío → lanza BadRequestException (sin tocar BD)")
        void actualizarChef_conNombreVacio_lanzaBadRequestException() {
            // Arrange
            UUID id = UUID.randomUUID();
            ChefRequest request = new ChefRequest("");

            // Act & Assert
            assertThrows(BadRequestException.class, () -> chefService.actualizarChef(id, request));
            verify(chefRepository, never()).findById(any());
            verify(chefRepository, never()).save(any());
        }
    }

    // =========================================================================
    // ELIMINAR CHEF
    // =========================================================================
    @Nested
    @DisplayName("eliminarChef()")
    class EliminarChef {

        @Test
        @DisplayName("✅ Con ID existente → elimina correctamente")
        void eliminarChef_conIdExistente_eliminaCorrectamente() {
            // Arrange
            UUID id = UUID.randomUUID();
            Chef chef = Chef.builder().id(id).nombre("Chef a borrar").platos(new ArrayList<>()).build();
            when(chefRepository.findById(id)).thenReturn(Optional.of(chef));

            // Act
            chefService.eliminarChef(id);

            // Assert — verificar que delete fue invocado
            verify(chefRepository, times(1)).delete(chef);
        }

        @Test
        @DisplayName("❌ Con ID inexistente → lanza ResourceNotFoundException")
        void eliminarChef_conIdInexistente_lanzaResourceNotFoundException() {
            // Arrange
            UUID id = UUID.randomUUID();
            when(chefRepository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(ResourceNotFoundException.class, () -> chefService.eliminarChef(id));
            verify(chefRepository, never()).delete(any());
        }
    }
}
