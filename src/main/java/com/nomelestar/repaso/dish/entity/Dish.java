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

    private String nombre;

    private String descripcion;

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

}

