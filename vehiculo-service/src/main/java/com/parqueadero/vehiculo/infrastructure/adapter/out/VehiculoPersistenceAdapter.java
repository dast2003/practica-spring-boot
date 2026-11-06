package com.parqueadero.vehiculo.infrastructure.adapter.out;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import com.parqueadero.vehiculo.domain.port.VehiculoRepositoryPort;
import com.parqueadero.vehiculo.infrastructure.mapper.VehiculoMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Adaptador de salida: implementa el puerto del dominio usando Spring Data JPA. */
@Component
public class VehiculoPersistenceAdapter implements VehiculoRepositoryPort {

    private final VehiculoJpaRepository jpa;

    public VehiculoPersistenceAdapter(VehiculoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Vehiculo guardar(Vehiculo vehiculo) {
        return VehiculoMapper.toDomain(jpa.save(VehiculoMapper.toEntity(vehiculo)));
    }

    @Override
    public Optional<Vehiculo> buscarPorPlaca(String placa) {
        return jpa.findById(placa).map(VehiculoMapper::toDomain);
    }

    @Override
    public List<Vehiculo> listarTodos() {
        return jpa.findAll().stream().map(VehiculoMapper::toDomain).toList();
    }

    @Override
    public boolean existePorPlaca(String placa) {
        return jpa.existsById(placa);
    }

    @Override
    public void eliminarPorPlaca(String placa) {
        jpa.deleteById(placa);
    }

    @Override
    public long contarTodos() {
        return jpa.count();
    }

    @Override
    public long contarPorTipo(TipoVehiculo tipo) {
        return jpa.countByTipo(tipo);
    }
}
