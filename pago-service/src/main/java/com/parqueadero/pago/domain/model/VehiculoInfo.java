package com.parqueadero.pago.domain.model;

/** Lo único que pago-service necesita saber de un vehículo. */
public record VehiculoInfo(String placa, TipoVehiculo tipo) {
}
