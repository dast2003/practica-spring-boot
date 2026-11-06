package com.parqueadero.vehiculo.infrastructure.mapper;

import com.parqueadero.vehiculo.domain.model.Carro;
import com.parqueadero.vehiculo.domain.model.EstadisticasVehiculos;
import com.parqueadero.vehiculo.domain.model.Moto;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import com.parqueadero.vehiculo.domain.model.VehiculoFactory;
import com.parqueadero.vehiculo.infrastructure.dto.EstadisticasResponse;
import com.parqueadero.vehiculo.infrastructure.dto.VehiculoRequest;
import com.parqueadero.vehiculo.infrastructure.dto.VehiculoResponse;
import com.parqueadero.vehiculo.infrastructure.dto.VehiculoUpdateRequest;
import com.parqueadero.vehiculo.infrastructure.entity.VehiculoEntity;

/** Traduce entre DTO <-> dominio <-> entidad JPA. */
public final class VehiculoMapper {

    private VehiculoMapper() {
    }

    // ---------- DTO -> dominio ----------

    public static Vehiculo toDomain(VehiculoRequest r) {
        return VehiculoFactory.crear(r.placa(), r.marca(), r.modelo(), r.tipo(),
                r.numeroPuertas(), r.cilindraje());
    }

    public static Vehiculo toDomain(String placa, VehiculoUpdateRequest r) {
        return VehiculoFactory.crear(placa, r.marca(), r.modelo(), r.tipo(),
                r.numeroPuertas(), r.cilindraje());
    }

    // ---------- dominio <-> entidad ----------

    public static VehiculoEntity toEntity(Vehiculo v) {
        return new VehiculoEntity(v.getPlaca(), v.getMarca(), v.getModelo(), v.getTipo(),
                numeroPuertas(v), cilindraje(v));
    }

    public static Vehiculo toDomain(VehiculoEntity e) {
        return VehiculoFactory.crear(e.getPlaca(), e.getMarca(), e.getModelo(), e.getTipo(),
                e.getNumeroPuertas(), e.getCilindraje());
    }

    // ---------- dominio -> DTO ----------

    public static VehiculoResponse toResponse(Vehiculo v) {
        return new VehiculoResponse(v.getPlaca(), v.getMarca(), v.getModelo(), v.getTipo().name(),
                numeroPuertas(v), cilindraje(v));
    }

    public static EstadisticasResponse toResponse(EstadisticasVehiculos e) {
        return new EstadisticasResponse(e.totalVehiculos(), e.totalCarros(), e.totalMotos());
    }

    private static Integer numeroPuertas(Vehiculo v) {
        return (v instanceof Carro c) ? c.getNumeroPuertas() : null;
    }

    private static Integer cilindraje(Vehiculo v) {
        return (v instanceof Moto m) ? m.getCilindraje() : null;
    }
}
