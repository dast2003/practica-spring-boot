# 2. Diagramas de flujo

Cada diagrama sigue una petición desde que llega hasta la respuesta HTTP. Las cajas rojas son errores
que atiende el `GlobalExceptionHandler`.

---

## 2.1 RF-01 · Registrar vehículo (`POST /api/vehiculos`)

```mermaid
flowchart TD
    A([POST /api/vehiculos<br/>JSON]) --> B{¿JSON bien formado<br/>y tipo CARRO/MOTO?}
    B -- No --> E1[400 Bad Request]
    B -- Sí --> C{"¿Pasa @Valid?<br/>placa, marca, modelo, tipo"}
    C -- No --> E1
    C -- Sí --> D[VehiculoMapper → VehiculoFactory<br/>crea Carro o Moto]
    D --> F{"¿CARRO con numeroPuertas > 0?<br/>¿MOTO con cilindraje > 0?"}
    F -- No --> E1
    F -- Sí --> G[VehiculoService.registrar]
    G --> H{¿existePorPlaca?}
    H -- Sí --> E2[409 Conflict<br/>PlacaDuplicadaException]
    H -- No --> I[PersistenceAdapter.guardar<br/>Vehiculo → VehiculoEntity → H2]
    I --> J([201 Created<br/>VehiculoResponse])

    style E1 fill:#fdd,stroke:#c00
    style E2 fill:#fdd,stroke:#c00
    style J fill:#dfd,stroke:#090
```

---

## 2.2 RF-02 / RF-03 · Listar y buscar por placa

```mermaid
flowchart TD
    A([GET /api/vehiculos]) --> B[VehiculoService.listar]
    B --> C[JpaRepository.findAll]
    C --> D([200 OK · lista JSON<br/>vacía si no hay datos])

    A2(["GET /api/vehiculos/{placa}"]) --> B2[VehiculoService.buscarPorPlaca<br/>placa en mayúsculas]
    B2 --> C2{¿Existe?}
    C2 -- No --> E[404 Not Found]
    C2 -- Sí --> D2([200 OK · VehiculoResponse])

    style E fill:#fdd,stroke:#c00
    style D fill:#dfd,stroke:#090
    style D2 fill:#dfd,stroke:#090
```

---

## 2.3 RF-04 · Actualizar (`PUT /api/vehiculos/{placa}`)

La placa **sale de la URL** y no se puede cambiar. Tampoco se permite convertir un carro en moto.

```mermaid
flowchart TD
    A(["PUT /api/vehiculos/{placa}<br/>marca, modelo, tipo, puertas/cilindraje"]) --> B{¿Pasa @Valid?}
    B -- No --> E1[400 Bad Request]
    B -- Sí --> C[Mapper crea el Vehiculo<br/>con la placa de la URL]
    C --> D{¿Existe la placa?}
    D -- No --> E2[404 Not Found]
    D -- Sí --> F{¿Mismo tipo que el registrado?}
    F -- No --> E1
    F -- Sí --> G[guardar → UPDATE en H2]
    G --> H([200 OK · VehiculoResponse])

    style E1 fill:#fdd,stroke:#c00
    style E2 fill:#fdd,stroke:#c00
    style H fill:#dfd,stroke:#090
```

---

## 2.4 RF-05 · Eliminar (`DELETE /api/vehiculos/{placa}`)

```mermaid
flowchart TD
    A(["DELETE /api/vehiculos/{placa}"]) --> B{¿Existe la placa?}
    B -- No --> E[404 Not Found]
    B -- Sí --> C[eliminarPorPlaca]
    C --> D([204 No Content<br/>sin cuerpo])

    style E fill:#fdd,stroke:#c00
    style D fill:#dfd,stroke:#090
```

---

## 2.5 RF-07 · Estadísticas (`GET /api/vehiculos/estadisticas`)

```mermaid
flowchart TD
    A([GET /api/vehiculos/estadisticas]) --> B[VehiculoService.obtenerEstadisticas]
    B --> C[contarTodos → count]
    B --> D[contarPorTipo CARRO → countByTipo]
    B --> F[contarPorTipo MOTO → countByTipo]
    C & D & F --> G[EstadisticasVehiculos]
    G --> H(["200 OK<br/>{ totalVehiculos, totalCarros, totalMotos }"])

    style H fill:#dfd,stroke:#090
```

> Spring da prioridad a las rutas literales sobre las variables, así que `/estadisticas` **no** se
> confunde con `/{placa}`.

---

## 2.6 RF-06 · Calcular pago (`POST /api/pagos/calcular`): flujo principal

Es el flujo más importante porque muestra la **comunicación entre microservicios**.

```mermaid
flowchart TD
    A(["POST :8082/api/pagos/calcular<br/>{ placa, horas }"]) --> B{"¿Pasa @Valid?<br/>placa no vacía, horas > 0"}
    B -- No --> E1[400 Bad Request]
    B -- Sí --> C[PagoService.calcular]
    C --> D[VehiculoClientPort.buscarPorPlaca]
    D --> H["VehiculoHttpAdapter<br/>RestClient GET :8081/api/vehiculos/{placa}"]
    H --> I{Respuesta de vehiculo-service}
    I -- "Sin conexión / timeout / 5xx" --> E3[503 Service Unavailable]
    I -- 404 --> E2[404 Not Found<br/>placa inexistente]
    I -- 200 --> J{¿Hay calculadora<br/>para el tipo?}
    J -- No --> E4[422 Tipo no soportado]
    J -- Sí --> K["Polimorfismo:<br/>calculadora.calcular(horas)"]
    K --> K1["CARRO → 5.000 × horas"]
    K --> K2["MOTO → 3.000 × horas"]
    K1 & K2 --> L(["200 OK · PagoResponse<br/>placa, tipoVehiculo, horas,<br/>valorHora, totalPagar, COP"])

    style E1 fill:#fdd,stroke:#c00
    style E2 fill:#fdd,stroke:#c00
    style E3 fill:#fdd,stroke:#c00
    style E4 fill:#fdd,stroke:#c00
    style L fill:#dfd,stroke:#090
```

---

## 2.7 Diagrama de secuencia: cálculo de pago exitoso

```mermaid
sequenceDiagram
    autonumber
    actor U as Postman
    participant PC as PagoController<br/>(:8082)
    participant PS as PagoService
    participant HA as VehiculoHttpAdapter
    participant VC as VehiculoController<br/>(:8081)
    participant VS as VehiculoService
    participant DB as H2

    U->>PC: POST /api/pagos/calcular {placa:"ABC123", horas:3}
    PC->>PC: @Valid PagoRequest
    PC->>PS: calcular("ABC123", 3)
    PS->>HA: buscarPorPlaca("ABC123")
    HA->>VC: GET /api/vehiculos/ABC123
    VC->>VS: buscarPorPlaca("ABC123")
    VS->>DB: SELECT ... WHERE placa='ABC123'
    DB-->>VS: fila
    VS-->>VC: Carro
    VC-->>HA: 200 {placa, tipo:"CARRO", ...}
    HA-->>PS: VehiculoInfo(ABC123, CARRO)
    PS->>PS: calculadoras.get(CARRO) → CarroPagoCalculator
    PS->>PS: 5000 × 3 = 15000
    PS-->>PC: Pago
    PC-->>U: 200 {placa, tipoVehiculo:"CARRO", horas:3,<br/>valorHora:5000, totalPagar:15000, moneda:"COP"}
```

## 2.8 Diagrama de secuencia: errores en el cálculo

```mermaid
sequenceDiagram
    autonumber
    actor U as Postman
    participant P as pago-service
    participant V as vehiculo-service

    Note over U,P: Caso 1 · horas = -2
    U->>P: POST {placa:"ABC123", horas:-2}
    P-->>U: 400 Bad Request (no se llama a vehiculo-service)

    Note over U,V: Caso 2 · placa inexistente
    U->>P: POST {placa:"NOP999", horas:3}
    P->>V: GET /api/vehiculos/NOP999
    V-->>P: 404
    P-->>U: 404 Not Found

    Note over U,V: Caso 3 · vehiculo-service apagado (bonus)
    U->>P: POST {placa:"ABC123", horas:3}
    P-xV: GET /api/vehiculos/ABC123 (conexión rechazada)
    P-->>U: 503 Service Unavailable
```

---

## 2.9 Tabla resumen de códigos HTTP

| Situación | Excepción | Código |
|---|---|---|
| Registro exitoso | — | 201 |
| Consulta / actualización / cálculo exitoso | — | 200 |
| Eliminación exitosa | — | 204 |
| JSON inválido, `@Valid` falla, horas ≤ 0, datos del tipo inválidos | `MethodArgumentNotValidException`, `ReglaNegocioException`, `HorasInvalidasException` | 400 |
| Placa no existe | `VehiculoNoEncontradoException` | 404 |
| Placa duplicada | `PlacaDuplicadaException` | 409 |
| Tipo sin calculadora | `TipoNoSoportadoException` | 422 |
| vehiculo-service caído | `ServicioNoDisponibleException` | 503 |
