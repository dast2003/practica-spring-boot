package com.parqueadero.pago.domain.model;

public record Pago(
        String placa,
        TipoVehiculo tipoVehiculo,
        int horas,
        long valorHora,
        long totalPagar,
        String moneda
) {
}
