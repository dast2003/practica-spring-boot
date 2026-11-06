package com.parqueadero.vehiculo.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VehiculoResponse(
        String placa,
        String marca,
        String modelo,
        String tipo,
        Integer numeroPuertas,
        Integer cilindraje
) {
}
