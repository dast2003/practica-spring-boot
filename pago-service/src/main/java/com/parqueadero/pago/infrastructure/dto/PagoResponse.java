package com.parqueadero.pago.infrastructure.dto;

import com.parqueadero.pago.domain.model.Pago;

public record PagoResponse(
        String placa,
        String tipoVehiculo,
        int horas,
        long valorHora,
        long totalPagar,
        String moneda
) {

    public static PagoResponse from(Pago p) {
        return new PagoResponse(p.placa(), p.tipoVehiculo().name(), p.horas(),
                p.valorHora(), p.totalPagar(), p.moneda());
    }
}
