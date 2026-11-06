package com.parqueadero.vehiculo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parqueadero.vehiculo.domain.exception.PlacaDuplicadaException;
import com.parqueadero.vehiculo.domain.exception.ReglaNegocioException;
import com.parqueadero.vehiculo.domain.exception.VehiculoNoEncontradoException;
import com.parqueadero.vehiculo.domain.model.Carro;
import com.parqueadero.vehiculo.domain.model.EstadisticasVehiculos;
import com.parqueadero.vehiculo.domain.model.Moto;
import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import com.parqueadero.vehiculo.domain.port.VehiculoRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    VehiculoRepositoryPort repository;

    @InjectMocks
    VehiculoService service;

    @Test
    void registrar_placaNueva_guarda() {
        Carro carro = new Carro("ABC123", "Chevrolet", "2020", 4);
        when(repository.existePorPlaca("ABC123")).thenReturn(false);
        when(repository.guardar(carro)).thenReturn(carro);

        assertEquals("ABC123", service.registrar(carro).getPlaca());
    }

    @Test
    void registrar_placaDuplicada_lanzaExcepcion() {
        Carro carro = new Carro("ABC123", "Chevrolet", "2020", 4);
        when(repository.existePorPlaca("ABC123")).thenReturn(true);

        assertThrows(PlacaDuplicadaException.class, () -> service.registrar(carro));
        verify(repository, never()).guardar(any());
    }

    @Test
    void buscar_inexistente_lanzaExcepcion() {
        when(repository.buscarPorPlaca("NOP999")).thenReturn(Optional.empty());

        assertThrows(VehiculoNoEncontradoException.class, () -> service.buscarPorPlaca("nop999"));
    }

    @Test
    void actualizar_cambiandoTipo_lanzaReglaNegocio() {
        when(repository.buscarPorPlaca("ABC123"))
                .thenReturn(Optional.of(new Carro("ABC123", "Chevrolet", "2020", 4)));
        Moto comoMoto = new Moto("ABC123", "Yamaha", "2023", 150);

        assertThrows(ReglaNegocioException.class, () -> service.actualizar("ABC123", comoMoto));
        verify(repository, never()).guardar(any());
    }

    @Test
    void estadisticas_cuentaPorTipo() {
        when(repository.contarTodos()).thenReturn(4L);
        when(repository.contarPorTipo(TipoVehiculo.CARRO)).thenReturn(2L);
        when(repository.contarPorTipo(TipoVehiculo.MOTO)).thenReturn(2L);

        assertEquals(new EstadisticasVehiculos(4, 2, 2), service.obtenerEstadisticas());
    }

    @Test
    void carroSinPuertas_esInvalido() {
        assertThrows(ReglaNegocioException.class, () -> new Carro("ABC123", "Chevrolet", "2020", null));
    }
}
