package com.nomelestar.repaso.chef.controller;

import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.service.ChefService;
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

/**
 * Capa de Presentación (Controller).
 * Esta clase expone los endpoints HTTP para interactuar con la entidad Chef desde el exterior.
 * La anotación @RestController combina @Controller y @ResponseBody.
 */
@RestController
@RequestMapping("/api/chefs") // Ruta base para todos los métodos de este controlador
@RequiredArgsConstructor // Genera constructor con dependencias 'final' (inyecta el ChefService)
@Tag(name = "Chefs", description = "Endpoints para la gestión de Chefs en el restaurante") // Etiqueta de Swagger
public class ChefController {

    private final ChefService chefService;

    /**
     * Endpoint POST: Crear un nuevo chef.
     * URL: http://localhost:8080/api/chefs
     */
    @Operation(summary = "Crear un nuevo chef", description = "Crea un chef con el nombre proporcionado y lo guarda en la base de datos.")
    @PostMapping
    public ResponseEntity<ChefResponse> crearChef(@Valid @RequestBody ChefRequest request) {
        ChefResponse response = chefService.crearChef(request);
        // Devuelve un HTTP Status 201 (CREATED)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint GET: Obtener la lista de todos los chefs.
     * URL: http://localhost:8080/api/chefs
     */
    @Operation(summary = "Obtener todos los chefs", description = "Devuelve una lista con todos los chefs registrados, incluyendo los nombres de sus platos creados.")
    @GetMapping
    public ResponseEntity<List<ChefResponse>> obtenerTodos() {
        List<ChefResponse> listaChefs = chefService.obtenerTodos();
        return ResponseEntity.ok(listaChefs);
    }

    /**
     * Endpoint GET con path variable: Obtener un chef por su ID UUID.
     * URL ejemplo: http://localhost:8080/api/chefs/550e8400-e29b-41d4-a716-446655440000
     */
    @Operation(summary = "Obtener un chef por ID", description = "Busca y devuelve la información de un chef específico mediante su identificador único (UUID).")
    @GetMapping("/{id}")
    public ResponseEntity<ChefResponse> obtenerPorId(@PathVariable UUID id) {
        ChefResponse response = chefService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint PUT: Actualizar completamente un chef.
     * URL ejemplo: http://localhost:8080/api/chefs/550e8400-e29b-41d4-a716-446655440000
     */
    @Operation(summary = "Actualizar los datos de un chef", description = "Permite modificar el nombre de un chef por su ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ChefResponse> actualizarChef(@PathVariable UUID id, @Valid @RequestBody ChefRequest request) {
        ChefResponse response = chefService.actualizarChef(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint DELETE: Eliminar un chef por su ID.
     * URL ejemplo: http://localhost:8080/api/chefs/550e8400-e29b-41d4-a716-446655440000
     */
    @Operation(summary = "Eliminar un chef", description = "Elimina un chef de la base de datos. Por efecto Cascada (CascadeType.ALL), también eliminará todos sus platos asociados.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarChef(@PathVariable UUID id) {
        chefService.eliminarChef(id);
        // Devuelve HTTP Status 204 (NO CONTENT) indicando que la operación fue exitosa pero no hay cuerpo de respuesta
        return ResponseEntity.noContent().build();
    }
}
