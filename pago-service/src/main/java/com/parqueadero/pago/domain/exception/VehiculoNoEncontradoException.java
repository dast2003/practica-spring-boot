package com.parqueadero.pago.domain.exception;

public class VehiculoNoEncontradoException extends RuntimeException {

    public VehiculoNoEncontradoException(String placa) {
        super("No existe un vehículo con placa " + placa);
    }
}
