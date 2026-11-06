package com.parqueadero.pago.infrastructure.config;

import com.parqueadero.pago.application.service.PagoService;
import com.parqueadero.pago.application.usecase.CalcularPagoUseCase;
import com.parqueadero.pago.domain.calculator.CarroPagoCalculator;
import com.parqueadero.pago.domain.calculator.MotoPagoCalculator;
import com.parqueadero.pago.domain.calculator.PagoCalculator;
import com.parqueadero.pago.domain.port.VehiculoClientPort;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public PagoCalculator carroPagoCalculator() {
        return new CarroPagoCalculator();
    }

    @Bean
    public PagoCalculator motoPagoCalculator() {
        return new MotoPagoCalculator();
    }

    /** Spring inyecta en la lista todas las PagoCalculator registradas. */
    @Bean
    public CalcularPagoUseCase calcularPagoUseCase(VehiculoClientPort client, List<PagoCalculator> calculadoras) {
        return new PagoService(client, calculadoras);
    }
}
