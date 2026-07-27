package com.nomelestar.repaso.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Interceptor global de excepciones. Atrapa las excepciones lanzadas en cualquier
 * controlador y devuelve una respuesta estructurada al cliente en lugar del error
 * por defecto de Spring Boot.
 *
 * <p>Se usa @RestControllerAdvice (= @ControllerAdvice + @ResponseBody), que es
 * la anotación correcta cuando todos los métodos devuelven JSON.
 *
 * <p>Regla de oro: los errores que son culpa del CLIENTE (datos mal enviados)
 * devuelven 4xx con un mensaje útil; los que son culpa del SERVIDOR devuelven
 * 500 con un mensaje genérico y el detalle real queda solo en los logs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja la excepción cuando un recurso no es encontrado. -> 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Maneja la excepción cuando la solicitud del cliente es incorrecta. -> 400
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Maneja las validaciones de Bean Validation (@Valid en el @RequestBody). -> 400
     *
     * <p>Además del mensaje resumido, devuelve un mapa campo -> error, mucho más
     * cómodo de consumir desde un frontend que un texto todo concatenado.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        // Errores que no pertenecen a un campo puntual (validaciones de clase completa)
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errores.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        ErrorResponse error = new ErrorResponse(
                "Error de validación en los datos enviados",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                errores
        );
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * El cliente mandó un tipo de dato que no corresponde en la URL. -> 400
     *
     * <p>Caso típico: {@code GET /api/chefs/hola} cuando se espera un UUID.
     * Sin este handler caía en el catch-all de abajo y devolvía un 500, como si
     * fuera un fallo del servidor, cuando en realidad es culpa del cliente.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String tipoEsperado = ex.getRequiredType() == null ? "válido" : ex.getRequiredType().getSimpleName();
        return construir(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' debe ser un " + tipoEsperado
                        + " válido. Valor recibido: " + ex.getValue());
    }

    /**
     * El body no es JSON válido o no se puede convertir. -> 400
     * Antes también terminaba como 500.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonInvalido(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición no es un JSON válido o tiene un tipo de dato incorrecto");
    }

    /**
     * Se violó una restricción de la base de datos. -> 409 Conflict
     *
     * <p>Caso típico: crear dos clientes con el mismo email, que tiene
     * {@code @Column(unique = true)}. Antes devolvía 500 con el mensaje crudo de
     * Postgres (incluyendo nombres de tablas y constraints, información que no
     * conviene exponer). 409 es el código correcto para "choca con algo que ya existe".
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad en base de datos", ex);
        return construir(HttpStatus.CONFLICT,
                "La operación viola una restricción de la base de datos. "
                        + "Verificá que no estés duplicando un valor único (por ejemplo, el email).");
    }

    /**
     * Cualquier otra excepción no contemplada. -> 500
     *
     * <p>IMPORTANTE: acá NO se devuelve {@code ex.getMessage()}. Ese mensaje puede
     * contener consultas SQL, rutas internas o nombres de tablas, y filtrarlo al
     * cliente es una fuga de información. El detalle completo (con stack trace)
     * se registra en el log del servidor, que es donde hay que mirarlo.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Error no controlado en la aplicación", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor. Revisá los logs para más detalle.");
    }

    /**
     * Arma la respuesta de error. Evita repetir el mismo bloque en cada handler.
     */
    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje) {
        ErrorResponse error = new ErrorResponse(mensaje, status.value(), LocalDateTime.now());
        return ResponseEntity.status(status).body(error);
    }
}
