package com.parqueadero.vehiculo.domain.model;

import com.parqueadero.vehiculo.domain.exception.ReglaNegocioException;

public class Moto extends Vehiculo {

    private final int cilindraje;

    public Moto(String placa, String marca, String modelo, Integer cilindraje) {
        super(placa, marca, modelo);
        if (cilindraje == null || cilindraje <= 0) {
            throw new ReglaNegocioException("Una MOTO requiere 'cilindraje' mayor a 0");
        }
        this.cilindraje = cilindraje;
    }

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.MOTO;
    }

    public int getCilindraje() {
        return cilindraje;
    }
}
