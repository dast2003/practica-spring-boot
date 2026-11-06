package com.parqueadero.vehiculo.application.usecase;

import com.parqueadero.vehiculo.domain.model.EstadisticasVehiculos;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import java.util.List;

/** Puerto de entrada: operaciones que ofrece el sistema. */
public interface VehiculoUseCase {

    Vehiculo registrar(Vehiculo vehiculo);

    List<Vehiculo> listar();

    Vehiculo buscarPorPlaca(String placa);

    Vehiculo actualizar(String placa, Vehiculo datosNuevos);

    void eliminar(String placa);

    EstadisticasVehiculos obtenerEstadisticas();
}
