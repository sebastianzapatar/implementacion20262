package com.nomelestar.repaso.e2e;

import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import com.nomelestar.repaso.dish.dto.DishRequest;
import com.nomelestar.repaso.dish.dto.DishResponse;
import com.nomelestar.repaso.dish.repository.DishRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas E2E (End-to-End) para Dish.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Dish — Pruebas E2E")
class DishE2ETest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private ChefRepository chefRepository;

    private String getDishUrl() {
        return "http://localhost:" + port + "/api/dishes";
    }

    private String getChefUrl() {
        return "http://localhost:" + port + "/api/chefs";
    }

    @BeforeEach
    void limpiarBaseDeDatos() {
        dishRepository.deleteAll();
        chefRepository.deleteAll();
    }

    @Test
    @DisplayName("E2E: Flujo completo de un Plato (CRUD) asociado a un Chef")
    void flujoCompletoDish() {
        // 1. Crear Chef para poder asociarle el plato
        ChefRequest chefReq = new ChefRequest("Chef E2E Dish");
        ResponseEntity<ChefResponse> chefRes = restTemplate.postForEntity(getChefUrl(), chefReq, ChefResponse.class);
        assertEquals(HttpStatus.CREATED, chefRes.getStatusCode());
        UUID chefId = chefRes.getBody().id();

        // 2. Crear Plato (POST)
        DishRequest createReq = new DishRequest("Pizza", "Con queso", new BigDecimal("25000"), chefId);
        ResponseEntity<DishResponse> createRes = restTemplate.postForEntity(getDishUrl(), createReq, DishResponse.class);
        
        assertEquals(HttpStatus.CREATED, createRes.getStatusCode());
        assertNotNull(createRes.getBody());
        assertEquals("Pizza", createRes.getBody().nombre());
        assertEquals(chefId, createRes.getBody().chefId());
        
        String dishId = createRes.getBody().id().toString();

        // 3. Obtener por ID (GET)
        ResponseEntity<DishResponse> getRes = restTemplate.getForEntity(getDishUrl() + "/" + dishId, DishResponse.class);
        
        assertEquals(HttpStatus.OK, getRes.getStatusCode());
        assertEquals("Pizza", getRes.getBody().nombre());

        // 4. Actualizar (PUT)
        DishRequest updateReq = new DishRequest("Pizza Modificada", "Con extra queso", new BigDecimal("30000"), chefId);
        HttpEntity<DishRequest> requestEntity = new HttpEntity<>(updateReq);
        ResponseEntity<DishResponse> updateRes = restTemplate.exchange(
                getDishUrl() + "/" + dishId,
                HttpMethod.PUT,
                requestEntity,
                DishResponse.class
        );
        
        assertEquals(HttpStatus.OK, updateRes.getStatusCode());
        assertEquals("Pizza Modificada", updateRes.getBody().nombre());
        assertEquals(new BigDecimal("30000"), updateRes.getBody().precio());

        // 5. Eliminar (DELETE)
        ResponseEntity<Void> deleteRes = restTemplate.exchange(
                getDishUrl() + "/" + dishId,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        
        assertEquals(HttpStatus.NO_CONTENT, deleteRes.getStatusCode());

        // 6. Verificar eliminación (GET debe dar 404)
        org.springframework.web.client.HttpClientErrorException exception = assertThrows(
                org.springframework.web.client.HttpClientErrorException.class,
                () -> restTemplate.getForEntity(getDishUrl() + "/" + dishId, String.class)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
