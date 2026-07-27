package com.nomelestar.repaso.dish.entity;

import com.nomelestar.repaso.chef.entity.Chef;
import com.nomelestar.repaso.client.entity.Client;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa la tabla "dishes" (platos) en la base de datos PostgreSQL.
 */
@Entity
@Table(name = "dishes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @jakarta.persistence.Column(nullable = false, length = 100)
    private String nombre;

    @jakarta.persistence.Column(length = 255)
    private String descripcion;

    /**
     * precision = 10, scale = 2 -> hasta 99.999.999,99 con 2 decimales exactos.
     * Sin esto Hibernate crea la columna con el default (19,2) y el tamaño real
     * queda "por accidente" en vez de ser una decisión.
     * Nunca usar double/float para dinero: pierden precisión al redondear.
     */
    @jakarta.persistence.Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * Relación Muchos a Uno (Muchos platos pertenecen a un Chef).
     * @JoinColumn indica el nombre de la columna en la tabla "dishes" que funcionará como llave foránea.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_id", nullable = false)
    private Chef chef;

    /**
     * Relación Muchos a Muchos inversa con Client.
     * "mappedBy" indica que Client es el dueño de la relación (ahí está el @JoinTable).
     */
    @ManyToMany(mappedBy = "platosConsumidos", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Client> clientes = new ArrayList<>();

    /**
     * equals/hashCode basados en el id.
     *
     * <p>¿Por qué hacen falta? Porque el código hace cosas como
     * {@code client.getPlatosConsumidos().contains(dish)} o {@code .remove(dish)}.
     * Sin estos métodos, Java compara por referencia de memoria: funciona de
     * casualidad mientras las dos instancias vengan de la misma transacción,
     * y falla en cuanto vienen de consultas distintas.
     *
     * <p>Detalles importantes del patrón:
     * <ul>
     *   <li>{@code instanceof} en vez de {@code getClass() != o.getClass()}:
     *       Hibernate entrega proxies (subclases generadas) para las relaciones
     *       LAZY, y con getClass() esos proxies nunca serían iguales.</li>
     *   <li>{@code id != null &&}: dos entidades nuevas sin guardar (id null)
     *       NO son iguales entre sí.</li>
     *   <li>hashCode constante: el id lo asigna la BD al guardar, así que si el
     *       hash dependiera del id, la entidad cambiaría de hash estando dentro
     *       de un HashSet y se "perdería".</li>
     * </ul>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dish other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Dish.class.hashCode();
    }
}

