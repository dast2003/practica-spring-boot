package com.parqueadero.pago.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.parqueadero.pago.domain.calculator.CarroPagoCalculator;
import com.parqueadero.pago.domain.calculator.MotoPagoCalculator;
import com.parqueadero.pago.domain.exception.HorasInvalidasException;
import com.parqueadero.pago.domain.exception.VehiculoNoEncontradoException;
import com.parqueadero.pago.domain.model.Pago;
import com.parqueadero.pago.domain.model.TipoVehiculo;
import com.parqueadero.pago.domain.model.VehiculoInfo;
import com.parqueadero.pago.domain.port.VehiculoClientPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PagoServiceTest {

    private VehiculoClientPort client;
    private PagoService service;

    @BeforeEach
    void setUp() {
        client = mock(VehiculoClientPort.class);
        service = new PagoService(client, List.of(new CarroPagoCalculator(), new MotoPagoCalculator()));
    }

    @Test
    void carro3Horas_15000() {
        when(client.buscarPorPlaca("ABC123"))
                .thenReturn(Optional.of(new VehiculoInfo("ABC123", TipoVehiculo.CARRO)));

        Pago pago = service.calcular("ABC123", 3);

        assertEquals(5_000, pago.valorHora());
        assertEquals(15_000, pago.totalPagar());
        assertEquals("COP", pago.moneda());
    }

    @Test
    void moto3Horas_9000() {
        when(client.buscarPorPlaca("XYZ45A"))
                .thenReturn(Optional.of(new VehiculoInfo("XYZ45A", TipoVehiculo.MOTO)));

        assertEquals(9_000, service.calcular("XYZ45A", 3).totalPagar());
    }

    @Test
    void horasNegativas_lanzaExcepcion_sinLlamarAlOtroServicio() {
        assertThrows(HorasInvalidasException.class, () -> service.calcular("ABC123", -1));
        verifyNoInteractions(client);
    }

    @Test
    void horasCero_lanzaExcepcion() {
        assertThrows(HorasInvalidasException.class, () -> service.calcular("ABC123", 0));
    }

    @Test
    void placaInexistente_lanzaExcepcion() {
        when(client.buscarPorPlaca("NOP999")).thenReturn(Optional.empty());

        assertThrows(VehiculoNoEncontradoException.class, () -> service.calcular("NOP999", 3));
    }
}
