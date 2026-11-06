package com.parqueadero.vehiculo.domain.model;

/** Único lugar donde se decide qué subclase de Vehiculo crear según el tipo. */
public final class VehiculoFactory {

    private VehiculoFactory() {
    }

    public static Vehiculo crear(String placa, String marca, String modelo, TipoVehiculo tipo,
                                 Integer numeroPuertas, Integer cilindraje) {
        return switch (tipo) {
            case CARRO -> new Carro(placa, marca, modelo, numeroPuertas);
            case MOTO -> new Moto(placa, marca, modelo, cilindraje);
        };
    }
}
