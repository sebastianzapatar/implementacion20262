package com.nomelestar.repaso.client.entity;

import com.nomelestar.repaso.dish.entity.Dish;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
 * Entidad que representa la tabla "clients" (clientes) en la base de datos.
 * Tiene una relación Muchos a Muchos con Dish: un cliente puede consumir muchos platos
 * y un plato puede ser consumido por muchos clientes.
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nombre;

    private String email;

    /**
     * Relación Muchos a Muchos con Dish.
     * La tabla intermedia "dish_clients" contiene las llaves foráneas.
     * El lado "dueño" de la relación es Client (aquí se define @JoinTable).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "dish_clients",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "dish_id")
    )
    @Builder.Default
    private List<Dish> platosConsumidos = new ArrayList<>();

}
