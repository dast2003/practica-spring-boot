package com.parqueadero.pago.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** JSON que devuelve vehiculo-service; solo se toman los campos necesarios. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VehiculoRemotoResponse(String placa, String tipo) {
}
