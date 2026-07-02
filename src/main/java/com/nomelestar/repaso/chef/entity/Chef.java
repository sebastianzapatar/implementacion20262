package com.nomelestar.repaso.chef.entity;

import com.nomelestar.repaso.dish.entity.Dish;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa la tabla "chefs" en la base de datos PostgreSQL.
 * Utilizamos Lombok para autogenerar getters, setters, constructores y el patrón Builder.
 */
@Entity
@Table(name = "chefs") // Nombre explícito para la tabla en la base de datos
@Getter
@Setter
@NoArgsConstructor // Constructor vacío requerido por JPA
@AllArgsConstructor // Constructor con todos los argumentos (útil con Builder)
@Builder // Patrón de diseño para construir objetos de forma más legible
public class Chef {

    /**
     * Identificador único del Chef.
     * Se genera automáticamente utilizando UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Nombre del Chef.
     */
    private String nombre;

    /**
     * Lista de platos creados por este chef.
     * Relación Uno a Muchos (Un Chef -> Muchos Platos).
     * cascade = ALL indica que si se borra un chef, se borran sus platos.
     */
    @OneToMany(mappedBy = "chef", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Dish> platos = new ArrayList<>();

}
