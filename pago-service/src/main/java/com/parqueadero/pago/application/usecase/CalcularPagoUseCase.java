package com.parqueadero.pago.application.usecase;

import com.parqueadero.pago.domain.model.Pago;

public interface CalcularPagoUseCase {

    Pago calcular(String placa, int horas);
}
