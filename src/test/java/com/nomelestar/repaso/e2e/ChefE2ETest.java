package com.nomelestar.repaso.e2e;

import com.nomelestar.repaso.chef.dto.ChefRequest;
import com.nomelestar.repaso.chef.dto.ChefResponse;
import com.nomelestar.repaso.chef.repository.ChefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas E2E (End-to-End) para Chef.
 * 
 * SpringBootTest.WebEnvironment.RANDOM_PORT levanta un servidor Tomcat real 
 * en un puerto aleatorio para evitar colisiones.
 * TestRestTemplate hace peticiones HTTP reales a ese servidor.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Chef — Pruebas E2E")
class ChefE2ETest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ChefRepository chefRepository;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/chefs";
    }

    @BeforeEach
    void limpiarBaseDeDatos() {
        chefRepository.deleteAll();
    }

    @Test
    @DisplayName("E2E: Flujo completo de un Chef (CRUD)")
    void flujoCompletoChef() {
        // 1. Crear Chef (POST)
        ChefRequest createReq = new ChefRequest("Chef E2E");
        ResponseEntity<ChefResponse> createRes = restTemplate.postForEntity(getBaseUrl(), createReq, ChefResponse.class);
        
        assertEquals(HttpStatus.CREATED, createRes.getStatusCode());
        assertNotNull(createRes.getBody());
        assertNotNull(createRes.getBody().id());
        assertEquals("Chef E2E", createRes.getBody().nombre());
        
        String chefId = createRes.getBody().id().toString();

        // 2. Obtener por ID (GET)
        ResponseEntity<ChefResponse> getRes = restTemplate.getForEntity(getBaseUrl() + "/" + chefId, ChefResponse.class);
        
        assertEquals(HttpStatus.OK, getRes.getStatusCode());
        assertNotNull(getRes.getBody());
        assertEquals("Chef E2E", getRes.getBody().nombre());

        // 3. Listar todos (GET)
        ResponseEntity<List<ChefResponse>> listRes = restTemplate.exchange(
                getBaseUrl(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ChefResponse>>() {}
        );
        
        assertEquals(HttpStatus.OK, listRes.getStatusCode());
        assertNotNull(listRes.getBody());
        assertEquals(1, listRes.getBody().size());

        // 4. Actualizar (PUT)
        ChefRequest updateReq = new ChefRequest("Chef E2E Modificado");
        HttpEntity<ChefRequest> requestEntity = new HttpEntity<>(updateReq);
        ResponseEntity<ChefResponse> updateRes = restTemplate.exchange(
                getBaseUrl() + "/" + chefId,
                HttpMethod.PUT,
                requestEntity,
                ChefResponse.class
        );
        
        assertEquals(HttpStatus.OK, updateRes.getStatusCode());
        assertNotNull(updateRes.getBody());
        assertEquals("Chef E2E Modificado", updateRes.getBody().nombre());

        // 5. Eliminar (DELETE)
        ResponseEntity<Void> deleteRes = restTemplate.exchange(
                getBaseUrl() + "/" + chefId,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        
        assertEquals(HttpStatus.NO_CONTENT, deleteRes.getStatusCode());

        // 6. Verificar eliminación (GET debe dar 404)
        org.springframework.web.client.HttpClientErrorException exception = assertThrows(
                org.springframework.web.client.HttpClientErrorException.class,
                () -> restTemplate.getForEntity(getBaseUrl() + "/" + chefId, String.class)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
