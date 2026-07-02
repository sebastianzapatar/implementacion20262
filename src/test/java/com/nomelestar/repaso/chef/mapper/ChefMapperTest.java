package com.nomelestar.repaso.chef.mapper;

import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.dish.entity.Dish;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para ChefMapper.
 * 
 * No se necesita Mockito ni Spring porque los métodos son estáticos y puros.
 */
@DisplayName("ChefMapper — Pruebas Unitarias")
class ChefMapperTest {

    @Test
    @DisplayName("toEntity() — Request válido → retorna Chef con nombre correcto")
    void toEntity_conRequestValido_retornaChef() {
        // Arrange
        ChefRequest request = new ChefRequest("Jamie Oliver");

        // Act
        Chef result = ChefMapper.toEntity(request);

        // Assert
        assertNotNull(result);
        assertEquals("Jamie Oliver", result.getNombre());
        assertNull(result.getId()); // El ID lo genera JPA, aquí no existe todavía
    }

    @Test
    @DisplayName("toEntity() — Request nulo → retorna null")
    void toEntity_conRequestNulo_retornaNull() {
        assertNull(ChefMapper.toEntity(null));
    }

    @Test
    @DisplayName("toResponse() — Entidad con platos → retorna ChefResponse con nombres de platos")
    void toResponse_conEntidadValida_retornaChefResponse() {
        // Arrange
        UUID id = UUID.randomUUID();
        Chef chef = Chef.builder().id(id).nombre("Leonor Espinosa").platos(new ArrayList<>()).build();

        Dish dish1 = Dish.builder().nombre("Ceviche de coco").chef(chef).precio(new BigDecimal("42000")).build();
        Dish dish2 = Dish.builder().nombre("Arroz de lisa").chef(chef).precio(new BigDecimal("38000")).build();
        chef.getPlatos().addAll(List.of(dish1, dish2));

        // Act
        ChefResponse response = ChefMapper.toResponse(chef);

        // Assert
        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("Leonor Espinosa", response.nombre());
        assertEquals(2, response.platos().size());
        assertTrue(response.platos().contains("Ceviche de coco"));
        assertTrue(response.platos().contains("Arroz de lisa"));
    }

    @Test
    @DisplayName("toResponse() — Entidad nula → retorna null")
    void toResponse_conEntidadNula_retornaNull() {
        assertNull(ChefMapper.toResponse(null));
    }

    @Test
    @DisplayName("toResponse() — Chef sin platos → retorna lista vacía (no null)")
    void toResponse_conPlatosVacios_retornaListaVacia() {
        // Arrange
        Chef chef = Chef.builder().id(UUID.randomUUID()).nombre("Chef nuevo").platos(new ArrayList<>()).build();

        // Act
        ChefResponse response = ChefMapper.toResponse(chef);

        // Assert
        assertNotNull(response.platos());
        assertTrue(response.platos().isEmpty());
    }
}
