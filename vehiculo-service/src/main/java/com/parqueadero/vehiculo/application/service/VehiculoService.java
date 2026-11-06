package com.parqueadero.vehiculo.application.service;

import com.parqueadero.vehiculo.application.usecase.VehiculoUseCase;
import com.parqueadero.vehiculo.domain.exception.PlacaDuplicadaException;
import com.parqueadero.vehiculo.domain.exception.ReglaNegocioException;
import com.parqueadero.vehiculo.domain.exception.VehiculoNoEncontradoException;
import com.parqueadero.vehiculo.domain.model.EstadisticasVehiculos;
import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import com.parqueadero.vehiculo.domain.port.VehiculoRepositoryPort;
import java.util.List;

/** Implementa los casos de uso. Aquí viven las reglas de negocio (no en el controlador). */
public class VehiculoService implements VehiculoUseCase {

    private final VehiculoRepositoryPort repository;

    public VehiculoService(VehiculoRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public Vehiculo registrar(Vehiculo vehiculo) {
        if (repository.existePorPlaca(vehiculo.getPlaca())) {
            throw new PlacaDuplicadaException(vehiculo.getPlaca());
        }
        return repository.guardar(vehiculo);
    }

    @Override
    public List<Vehiculo> listar() {
        return repository.listarTodos();
    }

    @Override
    public Vehiculo buscarPorPlaca(String placa) {
        String normalizada = placa.trim().toUpperCase();
        return repository.buscarPorPlaca(normalizada)
                .orElseThrow(() -> new VehiculoNoEncontradoException(normalizada));
    }

    @Override
    public Vehiculo actualizar(String placa, Vehiculo datosNuevos) {
        Vehiculo existente = buscarPorPlaca(placa);
        if (existente.getTipo() != datosNuevos.getTipo()) {
            throw new ReglaNegocioException("No se permite cambiar el tipo de vehículo ("
                    + existente.getTipo() + " -> " + datosNuevos.getTipo() + ")");
        }
        return repository.guardar(datosNuevos);
    }

    @Override
    public void eliminar(String placa) {
        Vehiculo existente = buscarPorPlaca(placa);
        repository.eliminarPorPlaca(existente.getPlaca());
    }

    @Override
    public EstadisticasVehiculos obtenerEstadisticas() {
        return new EstadisticasVehiculos(
                repository.contarTodos(),
                repository.contarPorTipo(TipoVehiculo.CARRO),
                repository.contarPorTipo(TipoVehiculo.MOTO));
    }
}
