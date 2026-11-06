package com.parqueadero.vehiculo.infrastructure.dto;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Cuerpo del PUT: no incluye placa porque la placa no se puede cambiar (viene en la URL). */
public record VehiculoUpdateRequest(
        @NotBlank(message = "La marca es obligatoria")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        String modelo,

        @NotNull(message = "El tipo es obligatorio (CARRO o MOTO)")
        TipoVehiculo tipo,

        @Positive(message = "numeroPuertas debe ser mayor a 0")
        Integer numeroPuertas,

        @Positive(message = "cilindraje debe ser mayor a 0")
        Integer cilindraje
) {
}
