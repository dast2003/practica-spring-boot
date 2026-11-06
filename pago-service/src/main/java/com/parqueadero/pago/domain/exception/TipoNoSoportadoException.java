package com.parqueadero.pago.domain.exception;

public class TipoNoSoportadoException extends RuntimeException {

    public TipoNoSoportadoException(String tipo) {
        super("No hay tarifa configurada para el tipo de vehículo: " + tipo);
    }
}
