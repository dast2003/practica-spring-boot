package com.parqueadero.pago.infrastructure.adapter.in;

import com.parqueadero.pago.application.usecase.CalcularPagoUseCase;
import com.parqueadero.pago.infrastructure.dto.PagoRequest;
import com.parqueadero.pago.infrastructure.dto.PagoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final CalcularPagoUseCase useCase;

    public PagoController(CalcularPagoUseCase useCase) {
        this.useCase = useCase;
    }

    /** RF-06: cálculo remoto -> 200 OK. */
    @PostMapping("/calcular")
    public PagoResponse calcular(@Valid @RequestBody PagoRequest request) {
        return PagoResponse.from(useCase.calcular(request.placa(), request.horas()));
    }
}
