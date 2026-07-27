package com.nomelestar.repaso.dish.controller;

import com.nomelestar.repaso.dish.dto.DishRequest;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.service.DishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
@Tag(name = "Dishes (Platos)", description = "Endpoints para la gestión de Platos creados por los Chefs")
public class DishController {

    private final DishService dishService;

    @Operation(summary = "Crear un nuevo plato", description = "Crea un plato y lo asocia a un Chef existente mediante su chefId.")
    @PostMapping
    public ResponseEntity<DishResponse> crearPlato(@Valid @RequestBody DishRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dishService.crearPlato(request));
    }

    @Operation(summary = "Obtener todos los platos", description = "Devuelve una lista con todos los platos registrados en el sistema.")
    @GetMapping
    public ResponseEntity<List<DishResponse>> obtenerTodos() {
        return ResponseEntity.ok(dishService.obtenerTodos());
    }

    @Operation(summary = "Obtener un plato por ID", description = "Busca y devuelve la información detallada de un plato específico.")
    @GetMapping("/{id}")
    public ResponseEntity<DishResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(dishService.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar un plato", description = "Actualiza los datos de un plato existente. También requiere un chefId válido.")
    @PutMapping("/{id}")
    public ResponseEntity<DishResponse> actualizarPlato(@PathVariable UUID id, @Valid @RequestBody DishRequest request) {
        return ResponseEntity.ok(dishService.actualizarPlato(id, request));
    }

    @Operation(summary = "Eliminar un plato", description = "Elimina de forma permanente un plato usando su identificador.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPlato(@PathVariable UUID id) {
        dishService.eliminarPlato(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Endpoints que demuestran JOINs @ManyToMany SIN @Query
    // -------------------------------------------------------------------------

    @Operation(summary = "Platos consumidos por un cliente (JOIN sin @Query)",
            description = "Usa el método derivado findByClientes_Id: Spring Data arma solo el JOIN "
                    + "con la tabla intermedia dish_clients, sin escribir JPQL.")
    @GetMapping("/by-client/{clientId}")
    public ResponseEntity<List<DishResponse>> platosPorCliente(@PathVariable UUID clientId) {
        return ResponseEntity.ok(dishService.obtenerPlatosConsumidosPorCliente(clientId));
    }

    @Operation(summary = "Platos de un cliente filtrados por chef (doble JOIN sin @Query)",
            description = "Usa findByClientes_IdAndChef_IdOrderByPrecioDesc. Devuelve lo mismo que "
                    + "GET /api/clients/{clientId}/chefs/{chefId}/platos, que lo resuelve con @Query.")
    @GetMapping("/by-client/{clientId}/chef/{chefId}")
    public ResponseEntity<List<DishResponse>> platosPorClienteYChef(
            @PathVariable UUID clientId, @PathVariable UUID chefId) {
        return ResponseEntity.ok(dishService.obtenerPlatosDeClientePorChefSinQuery(clientId, chefId));
    }

    @Operation(summary = "Cantidad de platos consumidos por un cliente (COUNT con JOIN sin @Query)",
            description = "Usa countByClientes_Id: hace un SELECT COUNT en la BD sin traer las filas.")
    @GetMapping("/by-client/{clientId}/count")
    public ResponseEntity<Long> contarPlatosPorCliente(@PathVariable UUID clientId) {
        return ResponseEntity.ok(dishService.contarPlatosConsumidosPorCliente(clientId));
    }
}
