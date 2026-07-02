package com.nomelestar.repaso.chef.repository;

import com.nomelestar.repaso.chef.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de Spring Data JPA para la entidad Chef.
 * Extender JpaRepository ya nos provee métodos como save(), findById(), findAll(), deleteById(), etc.
 */
public interface ChefRepository extends JpaRepository<Chef, UUID> {

    // -------------------------------------------------------------------------
    // EJEMPLOS DE CONSULTAS PERSONALIZADAS PARA EXPLICACIÓN
    // -------------------------------------------------------------------------

    /**
     * 1. Query Method (Derivado del nombre del método).
     * Spring Data JPA genera automáticamente la consulta SQL por debajo:
     * SELECT * FROM chefs WHERE nombre = ?
     */
    Optional<Chef> findByNombre(String nombre);

    /**
     * 2. Consulta personalizada usando @Query con JPQL (Java Persistence Query Language).
     * Aquí consultamos usando el nombre de la Entidad y de sus campos en Java, NO de la tabla de la BD.
     */
    @Query("SELECT c FROM Chef c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Chef> buscarPorNombreContieneJPQL(@Param("nombre") String nombre);

    /**
     * 3. Consulta personalizada usando @Query con SQL Nativo (nativeQuery = true).
     * Aquí la consulta se escribe usando SQL estándar y apunta al nombre real de la tabla en BD ("chefs").
     */
    @Query(value = "SELECT * FROM chefs WHERE nombre = :nombre LIMIT 1", nativeQuery = true)
    Optional<Chef> buscarPorNombreExactoSQLNativo(@Param("nombre") String nombre);

}
