package com.nomelestar.repaso.dish.mapper;

import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.dish.dto.DishRequest;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.entity.Dish;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para DishMapper.
 */
@DisplayName("DishMapper — Pruebas Unitarias")
class DishMapperTest {

    @Test
    @DisplayName("toEntity() — Request válido → retorna Dish con campos correctos")
    void toEntity_conRequestValido_retornaDish() {
        // Arrange
        Chef chef = Chef.builder().id(UUID.randomUUID()).nombre("Chef Test").platos(new ArrayList<>()).build();
        DishRequest request = new DishRequest("Empanadas", "Crujientes y doradas", new BigDecimal("5000"), chef.getId());

        // Act
        Dish result = DishMapper.toEntity(request, chef);

        // Assert
        assertNotNull(result);
        assertEquals("Empanadas", result.getNombre());
        assertEquals("Crujientes y doradas", result.getDescripcion());
        assertEquals(new BigDecimal("5000"), result.getPrecio());
        assertEquals(chef, result.getChef());
        assertNull(result.getId()); // ID se genera al persistir
    }

    @Test
    @DisplayName("toEntity() — Request nulo → retorna null")
    void toEntity_conRequestNulo_retornaNull() {
        assertNull(DishMapper.toEntity(null, null));
    }

    @Test
    @DisplayName("toResponse() — Entidad con chef → retorna DishResponse completo")
    void toResponse_conEntidadValida_retornaDishResponse() {
        // Arrange
        UUID chefId = UUID.randomUUID();
        UUID dishId = UUID.randomUUID();
        Chef chef = Chef.builder().id(chefId).nombre("Massimo Bottura").platos(new ArrayList<>()).build();
        Dish dish = Dish.builder()
                .id(dishId)
                .nombre("Tortellini")
                .descripcion("Pasta rellena")
                .precio(new BigDecimal("65000"))
                .chef(chef)
                .build();

        // Act
        DishResponse response = DishMapper.toResponse(dish);

        // Assert
        assertNotNull(response);
        assertEquals(dishId, response.id());
        assertEquals("Tortellini", response.nombre());
        assertEquals("Pasta rellena", response.descripcion());
        assertEquals(new BigDecimal("65000"), response.precio());
        assertEquals(chefId, response.chefId());
        assertEquals("Massimo Bottura", response.nombreChef());
    }

    @Test
    @DisplayName("toResponse() — Entidad nula → retorna null")
    void toResponse_conEntidadNula_retornaNull() {
        assertNull(DishMapper.toResponse(null));
    }

    @Test
    @DisplayName("toResponse() — Dish sin chef → retorna nulls en campos del chef")
    void toResponse_conChefNulo_retornaNulls() {
        // Arrange
        Dish dish = Dish.builder()
                .id(UUID.randomUUID())
                .nombre("Plato huérfano")
                .descripcion("Sin chef")
                .precio(new BigDecimal("10000"))
                .chef(null)
                .build();

        // Act
        DishResponse response = DishMapper.toResponse(dish);

        // Assert
        assertNotNull(response);
        assertNull(response.chefId());
        assertNull(response.nombreChef());
    }
}
