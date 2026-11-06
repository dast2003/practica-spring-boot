package com.parqueadero.vehiculo.infrastructure.dto;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record VehiculoRequest(
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^[A-Za-z]{3}[0-9]{2}[A-Za-z0-9]$",
                message = "Formato de placa inválido (ej: ABC123 o XYZ45A)")
        String placa,

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
