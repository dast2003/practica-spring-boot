package com.parqueadero.pago.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PagoRequest(
        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotNull(message = "Las horas son obligatorias")
        @Positive(message = "Las horas deben ser mayores a cero")
        Integer horas
) {
}
