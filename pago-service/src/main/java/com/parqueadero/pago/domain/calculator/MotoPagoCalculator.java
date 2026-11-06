package com.parqueadero.pago.domain.calculator;

import com.parqueadero.pago.domain.model.TipoVehiculo;

public class MotoPagoCalculator implements PagoCalculator {

    private static final long VALOR_HORA = 3_000L;

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.MOTO;
    }

    @Override
    public long getValorHora() {
        return VALOR_HORA;
    }
}
