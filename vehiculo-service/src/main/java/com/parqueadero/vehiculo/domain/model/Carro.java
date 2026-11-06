package com.parqueadero.vehiculo.domain.model;

import com.parqueadero.vehiculo.domain.exception.ReglaNegocioException;

public class Carro extends Vehiculo {

    private final int numeroPuertas;

    public Carro(String placa, String marca, String modelo, Integer numeroPuertas) {
        super(placa, marca, modelo);
        if (numeroPuertas == null || numeroPuertas <= 0) {
            throw new ReglaNegocioException("Un CARRO requiere 'numeroPuertas' mayor a 0");
        }
        this.numeroPuertas = numeroPuertas;
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.CARRO;
    }

    public int getNumeroPuertas() {
        return numeroPuertas;
    }
}
