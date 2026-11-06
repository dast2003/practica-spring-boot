package com.parqueadero.vehiculo.domain.port;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: lo que el dominio necesita para persistir, sin saber qué tecnología hay detrás. */
public interface VehiculoRepositoryPort {

    Vehiculo guardar(Vehiculo vehiculo);

    Optional<Vehiculo> buscarPorPlaca(String placa);

    List<Vehiculo> listarTodos();

    boolean existePorPlaca(String placa);

    void eliminarPorPlaca(String placa);

    long contarTodos();

    long contarPorTipo(TipoVehiculo tipo);
}
