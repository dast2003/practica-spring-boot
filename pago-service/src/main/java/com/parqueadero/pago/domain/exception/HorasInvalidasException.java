package com.parqueadero.pago.domain.exception;

public class HorasInvalidasException extends RuntimeException {

    public HorasInvalidasException(int horas) {
        super("Las horas deben ser mayores a cero. Recibido: " + horas);
    }
}
