package com.parqueadero.vehiculo.infrastructure.adapter.in;

import com.parqueadero.vehiculo.application.usecase.VehiculoUseCase;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import com.parqueadero.vehiculo.infrastructure.dto.EstadisticasResponse;
import com.parqueadero.vehiculo.infrastructure.dto.VehiculoRequest;
import com.parqueadero.vehiculo.infrastructure.dto.VehiculoResponse;
import com.parqueadero.vehiculo.infrastructure.dto.VehiculoUpdateRequest;
import com.parqueadero.vehiculo.infrastructure.mapper.VehiculoMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST: solo coordina, no tiene reglas de negocio. */
@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoUseCase useCase;

    public VehiculoController(VehiculoUseCase useCase) {
        this.useCase = useCase;
    }

    /** RF-01: registrar carro o moto -> 201 Created. */
    @PostMapping
    public ResponseEntity<VehiculoResponse> registrar(@Valid @RequestBody VehiculoRequest request) {
        Vehiculo creado = useCase.registrar(VehiculoMapper.toDomain(request));
        return ResponseEntity.created(URI.create("/api/vehiculos/" + creado.getPlaca()))
                .body(VehiculoMapper.toResponse(creado));
    }

    /** RF-02: listar todos -> 200 OK. */
    @GetMapping
    public List<VehiculoResponse> listar() {
        return useCase.listar().stream().map(VehiculoMapper::toResponse).toList();
    }

    /** RF-07: total de vehículos por tipo -> 200 OK. */
    @GetMapping("/estadisticas")
    public EstadisticasResponse estadisticas() {
        return VehiculoMapper.toResponse(useCase.obtenerEstadisticas());
    }

    /** RF-03: buscar por placa -> 200 / 404. */
    @GetMapping("/{placa}")
    public VehiculoResponse buscar(@PathVariable String placa) {
        return VehiculoMapper.toResponse(useCase.buscarPorPlaca(placa));
    }

    /** RF-04: actualizar sin cambiar la placa -> 200 OK. */
    @PutMapping("/{placa}")
    public VehiculoResponse actualizar(@PathVariable String placa,
                                       @Valid @RequestBody VehiculoUpdateRequest request) {
        Vehiculo datos = VehiculoMapper.toDomain(placa, request);
        return VehiculoMapper.toResponse(useCase.actualizar(placa, datos));
    }

    /** RF-05: eliminar -> 204 No Content. */
    @DeleteMapping("/{placa}")
    public ResponseEntity<Void> eliminar(@PathVariable String placa) {
        useCase.eliminar(placa);
        return ResponseEntity.noContent().build();
    }
}
