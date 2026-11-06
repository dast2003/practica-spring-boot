package com.parqueadero.vehiculo.infrastructure.config;

import com.parqueadero.vehiculo.application.service.VehiculoService;
import com.parqueadero.vehiculo.application.usecase.VehiculoUseCase;
import com.parqueadero.vehiculo.domain.port.VehiculoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Ensambla el caso de uso inyectándole el adaptador, sin poner anotaciones de Spring en la aplicación. */
@Configuration
public class BeanConfig {

    @Bean
    public VehiculoUseCase vehiculoUseCase(VehiculoRepositoryPort repositoryPort) {
        return new VehiculoService(repositoryPort);
    }
}
