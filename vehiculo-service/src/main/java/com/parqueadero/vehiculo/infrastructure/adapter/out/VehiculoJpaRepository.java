package com.parqueadero.vehiculo.infrastructure.adapter.out;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import com.parqueadero.vehiculo.infrastructure.entity.VehiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoJpaRepository extends JpaRepository<VehiculoEntity, String> {

    /** SELECT COUNT(*) FROM vehiculos WHERE tipo = ? */
    long countByTipo(TipoVehiculo tipo);
}
