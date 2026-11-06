package com.parqueadero.vehiculo.infrastructure.entity;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Representación JPA (tabla) del vehículo. No es el modelo de dominio. */
@Entity
@Table(name = "vehiculos")
public class VehiculoEntity {

    @Id
    @Column(length = 10)
    private String placa;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoVehiculo tipo;

    private Integer numeroPuertas;

    private Integer cilindraje;

    protected VehiculoEntity() {
        // requerido por JPA
    }

    public VehiculoEntity(String placa, String marca, String modelo, TipoVehiculo tipo,
                          Integer numeroPuertas, Integer cilindraje) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.tipo = tipo;
        this.numeroPuertas = numeroPuertas;
        this.cilindraje = cilindraje;
    }

    public String getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    public Integer getNumeroPuertas() {
        return numeroPuertas;
    }

    public Integer getCilindraje() {
        return cilindraje;
    }
}
