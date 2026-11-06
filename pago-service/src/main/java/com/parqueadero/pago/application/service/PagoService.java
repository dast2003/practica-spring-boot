package com.parqueadero.pago.application.service;

import com.parqueadero.pago.application.usecase.CalcularPagoUseCase;
import com.parqueadero.pago.domain.calculator.PagoCalculator;
import com.parqueadero.pago.domain.exception.HorasInvalidasException;
import com.parqueadero.pago.domain.exception.TipoNoSoportadoException;
import com.parqueadero.pago.domain.exception.VehiculoNoEncontradoException;
import com.parqueadero.pago.domain.model.Pago;
import com.parqueadero.pago.domain.model.TipoVehiculo;
import com.parqueadero.pago.domain.model.VehiculoInfo;
import com.parqueadero.pago.domain.port.VehiculoClientPort;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Caso de uso: valida horas, consulta el vehículo y aplica la calculadora de su tipo (sin if/else). */
public class PagoService implements CalcularPagoUseCase {

    private static final String MONEDA = "COP";

    private final VehiculoClientPort vehiculoClient;
    private final Map<TipoVehiculo, PagoCalculator> calculadoras;

    public PagoService(VehiculoClientPort vehiculoClient, List<PagoCalculator> calculadoras) {
        this.vehiculoClient = vehiculoClient;
        this.calculadoras = calculadoras.stream()
                .collect(Collectors.toMap(PagoCalculator::getTipo, Function.identity()));
    }

    @Override
    public Pago calcular(String placa, int horas) {
        if (horas <= 0) {
            throw new HorasInvalidasException(horas);
        }
        String normalizada = placa.trim().toUpperCase();
        VehiculoInfo vehiculo = vehiculoClient.buscarPorPlaca(normalizada)
                .orElseThrow(() -> new VehiculoNoEncontradoException(normalizada));

        PagoCalculator calculadora = calculadoras.get(vehiculo.tipo());
        if (calculadora == null) {
            throw new TipoNoSoportadoException(vehiculo.tipo().name());
        }

        return new Pago(vehiculo.placa(), vehiculo.tipo(), horas,
                calculadora.getValorHora(), calculadora.calcular(horas), MONEDA);
    }
}
