package com.parqueadero.pago.domain.calculator;

import com.parqueadero.pago.domain.model.TipoVehiculo;

/** Estrategia de tarifa: cada tipo de vehículo tiene su implementación (polimorfismo). */
public interface PagoCalculator {

    TipoVehiculo getTipo();

    long getValorHora();

    default long calcular(int horas) {
        return getValorHora() * horas;
    }
}
