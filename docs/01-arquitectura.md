# 1. Arquitectura del sistema de parqueadero

> Arquitectura elegida: **hexagonal (puertos y adaptadores)** en los dos microservicios.
> Los diagramas están en Mermaid. GitHub los muestra directamente. En VS Code hay que instalar la extensión
> *Markdown Preview Mermaid Support* y abrir la vista previa con `Ctrl+Shift+V`.

---

## 1.1 Vista general (contexto)

Hay dos microservicios **independientes**. Cada uno tiene su propio puerto y su propio proceso.
`pago-service` **nunca** lee la base de datos de vehículos: los consulta por HTTP.

```mermaid
flowchart LR
    U([Usuario / Postman])

    subgraph VS["vehiculo-service · puerto 8081"]
        V[API /api/vehiculos]
    end

    subgraph PS["pago-service · puerto 8082"]
        P[API /api/pagos/calcular]
    end

    DB[(H2<br/>parqueaderodb<br/>tabla vehiculos)]

    U -->|"CRUD + estadísticas (JSON)"| V
    U -->|"placa + horas (JSON)"| P
    P -->|"GET /api/vehiculos/{placa}<br/>RestClient"| V
    V -->|Spring Data JPA| DB

    P -. "PROHIBIDO acceso directo" .-> DB
    linkStyle 4 stroke:#c00,stroke-width:2px
```

| Servicio | Responsabilidad | Persistencia | Puerto |
|---|---|---|---|
| `vehiculo-service` | Alta, consulta, actualización, eliminación y estadísticas de vehículos | H2 con Spring Data JPA | 8081 |
| `pago-service` | Recibir placa y horas, consultar el vehículo por HTTP, aplicar la tarifa | Ninguna (el historial es opcional) | 8082 |

---

## 1.2 Arquitectura hexagonal: las capas

La regla de oro es que **las dependencias siempre apuntan hacia adentro**. El dominio no conoce
Spring, JPA ni HTTP. La infraestructura sí conoce al dominio.

```mermaid
flowchart TB
    subgraph INFRA["🔌 infrastructure — Spring, JPA, HTTP, JSON"]
        direction TB
        IN["adapter/in<br/>Controllers REST"]
        OUT["adapter/out<br/>JPA / RestClient"]
        CFG["config · dto · mapper · entity · exception"]

        subgraph APP["⚙️ application — casos de uso"]
            direction TB
            UC["usecase/<br/>interfaces (puertos de entrada)"]
            SV["service/<br/>implementación de los casos de uso"]

            subgraph DOM["💎 domain — Java puro"]
                M["model/<br/>Vehiculo, Carro, Moto"]
                PO["port/<br/>interfaces (puertos de salida)"]
                EX["exception/<br/>errores de negocio"]
            end
        end
    end

    IN -->|llama| UC
    SV -.->|implementa| UC
    SV -->|usa| M
    SV -->|usa| PO
    OUT -.->|implementa| PO
```

| Capa | ¿Qué contiene? | ¿De qué puede depender? |
|---|---|---|
| **domain** | Entidades de negocio, reglas, puertos de salida y excepciones | De nada externo (solo Java) |
| **application** | Casos de uso: orquesta el dominio | Solo del dominio |
| **infrastructure** | Controladores, JPA, cliente HTTP, DTOs, mappers, configuración | Del dominio, de la aplicación y de Spring |

---

## 1.3 vehiculo-service: componentes

```mermaid
flowchart LR
    C([Postman / pago-service])

    subgraph IN["infrastructure/adapter/in"]
        VC[VehiculoController]
    end

    subgraph DTO["infrastructure/dto + mapper"]
        RQ[VehiculoRequest<br/>VehiculoUpdateRequest]
        RS[VehiculoResponse<br/>EstadisticasResponse]
        MP[VehiculoMapper]
    end

    subgraph APP["application"]
        UC[[VehiculoUseCase]]
        SV[VehiculoService]
    end

    subgraph DOM["domain"]
        VH["Vehiculo «abstract»"]
        CA[Carro]
        MO[Moto]
        PORT[[VehiculoRepositoryPort]]
    end

    subgraph OUT["infrastructure/adapter/out"]
        PA[VehiculoPersistenceAdapter]
        JR[VehiculoJpaRepository]
        EN[VehiculoEntity]
    end

    DB[(H2)]
    GEH[GlobalExceptionHandler]

    C -->|HTTP JSON| VC
    VC --> RQ --> MP
    VC --> UC
    SV -. implementa .-> UC
    SV --> PORT
    SV --> VH
    CA --> VH
    MO --> VH
    PA -. implementa .-> PORT
    PA --> MP
    PA --> JR --> EN --> DB
    MP --> RS
    VC -. errores .-> GEH
```

---

## 1.4 pago-service: componentes

```mermaid
flowchart LR
    C([Postman])

    subgraph IN["infrastructure/adapter/in"]
        PC[PagoController]
        RQ[PagoRequest]
        RS[PagoResponse]
    end

    subgraph APP["application"]
        UC[[CalcularPagoUseCase]]
        SV[PagoService]
    end

    subgraph DOM["domain"]
        CALC[["PagoCalculator «interface»"]]
        CC["CarroPagoCalculator<br/>$5.000/h"]
        MC["MotoPagoCalculator<br/>$3.000/h"]
        PORT[[VehiculoClientPort]]
        PG[Pago]
    end

    subgraph OUT["infrastructure/adapter/out"]
        HA[VehiculoHttpAdapter<br/>RestClient]
    end

    VS[(vehiculo-service<br/>:8081)]

    C -->|POST /api/pagos/calcular| PC
    PC --> RQ
    PC --> UC
    SV -. implementa .-> UC
    SV --> PORT
    SV --> CALC
    CC -. implementa .-> CALC
    MC -. implementa .-> CALC
    SV --> PG --> RS
    HA -. implementa .-> PORT
    HA -->|"GET /api/vehiculos/{placa}"| VS
```

**Polimorfismo:** `PagoService` no tiene ningún `if (tipo == CARRO)`. Busca en un `Map<TipoVehiculo, PagoCalculator>`
la calculadora que corresponde y llama a `calcular(horas)`. Para agregar un **Camión** basta con crear
`CamionPagoCalculator`; `Carro` y `Moto` no se tocan (principio abierto/cerrado).

---

## 1.5 Modelo de clases (POO)

```mermaid
classDiagram
    class Vehiculo {
        <<abstract>>
        -String placa
        -String marca
        -String modelo
        +getTipo() TipoVehiculo*
        +getPlaca() String
        +getMarca() String
        +getModelo() String
    }
    class Carro {
        -int numeroPuertas
        +getTipo() TipoVehiculo
        +getNumeroPuertas() int
    }
    class Moto {
        -int cilindraje
        +getTipo() TipoVehiculo
        +getCilindraje() int
    }
    class TipoVehiculo {
        <<enumeration>>
        CARRO
        MOTO
    }
    class VehiculoRepositoryPort {
        <<interface>>
        +guardar(Vehiculo) Vehiculo
        +buscarPorPlaca(String) Optional~Vehiculo~
        +listarTodos() List~Vehiculo~
        +existePorPlaca(String) boolean
        +eliminarPorPlaca(String) void
        +contarTodos() long
        +contarPorTipo(TipoVehiculo) long
    }
    class PagoCalculator {
        <<interface>>
        +getTipo() TipoVehiculo
        +getValorHora() long
        +calcular(int horas) long
    }
    class CarroPagoCalculator {
        +getValorHora() 5000
    }
    class MotoPagoCalculator {
        +getValorHora() 3000
    }

    Vehiculo <|-- Carro
    Vehiculo <|-- Moto
    Vehiculo --> TipoVehiculo
    VehiculoRepositoryPort ..> Vehiculo
    PagoCalculator <|.. CarroPagoCalculator
    PagoCalculator <|.. MotoPagoCalculator
```

---

## 1.6 Modelo de datos (vehiculo-service)

Se usa **una sola tabla**: la columna `tipo` distingue carros de motos y los campos propios de cada
tipo admiten `NULL`.

```mermaid
erDiagram
    VEHICULOS {
        varchar placa PK "ej. ABC123"
        varchar marca "NOT NULL"
        varchar modelo "NOT NULL"
        varchar tipo "CARRO o MOTO"
        int numero_puertas "solo CARRO"
        int cilindraje "solo MOTO"
    }
```

---

## 1.7 ¿Por qué hexagonal y no MVC? (para la sustentación)

| Aspecto | MVC por capas | Hexagonal |
|---|---|---|
| Dependencia del dominio | El modelo suele ser la entidad JPA | El dominio es Java puro y la entidad JPA es aparte |
| Cambiar H2 por MySQL | Se toca el modelo | Solo cambia el adaptador `out` |
| Cambiar REST por otra entrada | Se toca el controlador y el servicio | Se agrega otro adaptador `in` |
| Pruebas unitarias | Hay que simular JPA | Se simula una interfaz (puerto) con Mockito |
| Complejidad | Menor | Algo mayor (más clases) |

**Justificación:** el enunciado la recomienda y separa con claridad las reglas de negocio (tarifas,
validaciones, duplicados) de la tecnología. Además, el bonus del **Camión** sale casi gratis gracias a los
puertos y al patrón *Strategy* de las calculadoras.
