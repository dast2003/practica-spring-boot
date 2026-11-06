package com.parqueadero.vehiculo.domain.model;

import com.parqueadero.vehiculo.domain.exception.ReglaNegocioException;

/**
 * Clase base abstracta del dominio. No depende de Spring ni de JPA.
 * Atributos privados e inmutables (encapsulamiento); cada subclase define su tipo (polimorfismo).
 */
public abstract class Vehiculo {

    private final String placa;
    private final String marca;
    private final String modelo;

    protected Vehiculo(String placa, String marca, String modelo) {
        this.placa = requerido(placa, "placa").trim().toUpperCase();
        this.marca = requerido(marca, "marca").trim();
        this.modelo = requerido(modelo, "modelo").trim();
    }

    /** Polimorfismo: cada subclase indica qué tipo de vehículo es. */
    public abstract TipoVehiculo getTipo();

    protected static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio");
        }
        return valor;
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
}
