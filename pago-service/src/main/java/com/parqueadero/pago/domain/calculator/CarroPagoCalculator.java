package com.parqueadero.pago.domain.calculator;

import com.parqueadero.pago.domain.model.TipoVehiculo;

public class CarroPagoCalculator implements PagoCalculator {

    private static final long VALOR_HORA = 5_000L;

    @Override
    public TipoVehiculo getTipo() {
        return TipoVehiculo.CARRO;
    }

    @Override
    public long getValorHora() {
        return VALOR_HORA;
    }
}
