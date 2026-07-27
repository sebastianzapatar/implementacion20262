package com.nomelestar.repaso.dish.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de PROYECCIÓN (no es una entidad, no se guarda en BD).
 *
 * <p>Se usa como resultado de la consulta JPQL {@code rankingPlatosMasConsumidos}
 * del {@code ClientRepository}. En JPQL esto se llama "constructor expression":
 *
 * <pre>
 *   SELECT new com.nomelestar.repaso.dish.dto.DishPopularityResponse(d.id, d.nombre, ...)
 * </pre>
 *
 * <p>Ventaja frente a traer entidades completas: la base de datos solo devuelve
 * las columnas que realmente necesitamos, no filas enteras de {@code dishes} +
 * {@code chefs} + {@code clients}. Es más rápido y no arrastra colecciones LAZY.
 *
 * <p>IMPORTANTE: el orden y el tipo de los parámetros de este record deben
 * coincidir EXACTAMENTE con el orden y tipo de las expresiones del SELECT,
 * si no, Hibernate lanza un error al arrancar la aplicación.
 *
 * @param dishId          identificador del plato
 * @param nombrePlato     nombre del plato
 * @param nombreChef      nombre del chef que lo creó (viene del JOIN con chefs)
 * @param precio          precio unitario del plato
 * @param totalClientes   cuántos clientes distintos lo han consumido (COUNT DISTINCT)
 * @param ingresoEstimado precio * totalClientes, calculado con SUM en la BD
 */
public record DishPopularityResponse(
        UUID dishId,
        String nombrePlato,
        String nombreChef,
        BigDecimal precio,
        Long totalClientes,
        BigDecimal ingresoEstimado
) {
}
