package com.parqueadero.pago.domain.port;

import com.parqueadero.pago.domain.model.VehiculoInfo;
import java.util.Optional;

/** Puerto de salida: cómo obtener un vehículo, sin saber que es por HTTP. */
public interface VehiculoClientPort {

    Optional<VehiculoInfo> buscarPorPlaca(String placa);
}
