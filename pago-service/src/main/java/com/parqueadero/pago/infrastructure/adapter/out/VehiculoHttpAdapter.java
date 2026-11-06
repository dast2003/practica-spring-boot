package com.parqueadero.pago.infrastructure.adapter.out;

import com.parqueadero.pago.domain.exception.ServicioNoDisponibleException;
import com.parqueadero.pago.domain.exception.TipoNoSoportadoException;
import com.parqueadero.pago.domain.model.TipoVehiculo;
import com.parqueadero.pago.domain.model.VehiculoInfo;
import com.parqueadero.pago.domain.port.VehiculoClientPort;
import com.parqueadero.pago.infrastructure.dto.VehiculoRemotoResponse;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/** Adaptador de salida: consulta a vehiculo-service por HTTP con RestClient. */
@Component
public class VehiculoHttpAdapter implements VehiculoClientPort {

    private final RestClient restClient;

    public VehiculoHttpAdapter(RestClient vehiculoRestClient) {
        this.restClient = vehiculoRestClient;
    }

    @Override
    public Optional<VehiculoInfo> buscarPorPlaca(String placa) {
        try {
            VehiculoRemotoResponse respuesta = restClient.get()
                    .uri("/api/vehiculos/{placa}", placa)
                    .retrieve()
                    .body(VehiculoRemotoResponse.class);

            if (respuesta == null) {
                return Optional.empty();
            }
            return Optional.of(new VehiculoInfo(respuesta.placa(), convertirTipo(respuesta.tipo())));

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new ServicioNoDisponibleException("vehiculo-service no está disponible en este momento", e);
        }
    }

    private TipoVehiculo convertirTipo(String tipo) {
        try {
            return TipoVehiculo.valueOf(tipo);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new TipoNoSoportadoException(String.valueOf(tipo));
        }
    }
}
