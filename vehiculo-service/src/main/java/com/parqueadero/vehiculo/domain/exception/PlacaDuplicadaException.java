package com.parqueadero.vehiculo.domain.exception;

public class PlacaDuplicadaException extends RuntimeException {

    public PlacaDuplicadaException(String placa) {
        super("Ya existe un vehículo registrado con placa " + placa);
    }
}
