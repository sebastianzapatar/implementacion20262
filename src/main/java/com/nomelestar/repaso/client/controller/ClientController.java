package com.nomelestar.repaso.client.controller;

import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.client.dto.ClientRequest;
import com.nomelestar.repaso.client.dto.ClientResponse;
import com.nomelestar.repaso.client.service.ClientService;
import com.nomelestar.repaso.dish.dto.DishResponse;
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
 * Controller para la gestión de Clientes y sus relaciones Muchos a Muchos con Platos.
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clients (Clientes)", description = "Endpoints para la gestión de Clientes y sus consumos de platos")
public class ClientController {

    private final ClientService clientService;

    @Operation(summary = "Crear un nuevo cliente")
    @PostMapping
    public ResponseEntity<ClientResponse> crearCliente(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.crearCliente(request));
    }

    @Operation(summary = "Obtener todos los clientes")
    @GetMapping
    public ResponseEntity<List<ClientResponse>> obtenerTodos() {
        return ResponseEntity.ok(clientService.obtenerTodos());
    }

    @Operation(summary = "Obtener un cliente por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(clientService.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar un cliente")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> actualizarCliente(@PathVariable UUID id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.actualizarCliente(id, request));
    }

    @Operation(summary = "Eliminar un cliente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable UUID id) {
        clientService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Registrar consumo de un plato por un cliente",
            description = "Agrega un plato a la lista de platos consumidos por el cliente (relación muchos a muchos).")
    @PostMapping("/{clientId}/dishes/{dishId}")
    public ResponseEntity<ClientResponse> consumirPlato(@PathVariable UUID clientId, @PathVariable UUID dishId) {
        return ResponseEntity.ok(clientService.consumirPlato(clientId, dishId));
    }

    @Operation(summary = "Chef con más platos vendidos",
            description = "Retorna el chef cuyos platos han sido más comprados por clientes (JOIN en tabla intermedia).")
    @GetMapping("/stats/chef-mas-vendido")
    public ResponseEntity<ChefResponse> chefConMasVentas() {
        return ResponseEntity.ok(clientService.obtenerChefConMasVentas());
    }

    @Operation(summary = "Platos preferidos de un cliente por chef",
            description = "Retorna los platos que un cliente específico ha consumido de un chef específico.")
    @GetMapping("/{clientId}/chefs/{chefId}/platos")
    public ResponseEntity<List<DishResponse>> platosDeClientePorChef(
            @PathVariable UUID clientId, @PathVariable UUID chefId) {
        return ResponseEntity.ok(clientService.obtenerPlatosDeClientePorChef(clientId, chefId));
    }
}
