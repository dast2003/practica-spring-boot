# 3. Guía paso a paso: creación de carpetas, archivos y su funcionalidad

Orden recomendado: **Paso 0 → Paso 1 (vehiculo-service completo) → probarlo → Paso 2 (pago-service) → Paso 3 (Postman) → Paso 4 (Git)**.
Se construye de **adentro hacia afuera**: primero el dominio, luego la aplicación y al final la infraestructura.

---

## Paso 0 · Requisitos y estructura del repositorio

**Instalar:** JDK 21 (verificar con `java -version`), un IDE (IntelliJ IDEA, VS Code con *Extension Pack for Java* o STS), Postman y Git.
Maven no hace falta instalarlo porque cada proyecto trae su propio *wrapper* (`mvnw`).

Estructura final del repositorio:

```
PRUEBAS PROGRAMACION/            ← raíz del repo Git
├── README.md                    ← instalación y ejecución (entregable)
├── .gitignore
├── docs/                        ← esta documentación y los diagramas
├── postman/
│   └── Prueba Tecnica - Parqueadero.postman_collection.json
├── vehiculo-service/            ← microservicio 1 (puerto 8081)
└── pago-service/                ← microservicio 2 (puerto 8082)
```

---

## Paso 1 · vehiculo-service

### 1.1 Generar el proyecto en Spring Initializr

Entra a <https://start.spring.io/> y configura lo siguiente:

| Campo | Valor |
|---|---|
| Project | **Maven** |
| Language | **Java** |
| Spring Boot | La versión estable más reciente (sin `SNAPSHOT`, `M` ni `RC`) |
| Group | `com.parqueadero` |
| Artifact | `vehiculo-service` |
| **Package name** | `com.parqueadero.vehiculo` ⚠️ cámbialo a mano: por defecto sale `com.parqueadero.vehiculoservice` |
| Packaging | **Jar** |
| Java | **21** |
| Dependencies | **Spring Web**, **Spring Data JPA**, **Validation**, **H2 Database** (opcional: DevTools) |

Haz clic en **Generate** y descomprime el ZIP dentro de la carpeta del repositorio. Luego abre la carpeta `vehiculo-service` en el IDE y espera a que Maven descargue las dependencias.

> La clase principal se llama `VehiculoServiceApplication`. Puedes renombrarla a `VehiculoApplication` como en el enunciado con *Refactor → Rename* en el IDE. Las dos opciones son válidas.

### 1.2 Crear las carpetas (paquetes)

Desde PowerShell, en la raíz del repositorio:

```powershell
cd vehiculo-service\src\main\java\com\parqueadero\vehiculo
mkdir domain\model, domain\port, domain\exception, `
      application\usecase, application\service, `
      infrastructure\adapter\in, infrastructure\adapter\out, `
      infrastructure\entity, infrastructure\dto, infrastructure\mapper, `
      infrastructure\exception, infrastructure\config
```

> En IntelliJ también puedes hacer clic derecho sobre el paquete → *New → Package*.

Resultado:

```
vehiculo-service/src/main/java/com/parqueadero/vehiculo/
├── domain/                          💎 Núcleo de negocio. Java puro, sin Spring
│   ├── model/                       Vehiculo (abstracta), Carro, Moto, TipoVehiculo, VehiculoFactory, EstadisticasVehiculos
│   ├── port/                        VehiculoRepositoryPort  (lo que el dominio NECESITA de afuera)
│   └── exception/                   VehiculoNoEncontradoException, PlacaDuplicadaException, ReglaNegocioException
├── application/                     ⚙️ Casos de uso
│   ├── usecase/                     VehiculoUseCase  (lo que el sistema OFRECE: puerto de entrada)
│   └── service/                     VehiculoService  (implementa el caso de uso)
├── infrastructure/                  🔌 Detalles técnicos
│   ├── adapter/in/                  VehiculoController  (REST → caso de uso)
│   ├── adapter/out/                 VehiculoJpaRepository, VehiculoPersistenceAdapter  (puerto → BD)
│   ├── entity/                      VehiculoEntity  (tabla JPA)
│   ├── dto/                         VehiculoRequest, VehiculoUpdateRequest, VehiculoResponse, EstadisticasResponse
│   ├── mapper/                      VehiculoMapper  (DTO ↔ dominio ↔ entidad)
│   ├── exception/                   GlobalExceptionHandler, ErrorResponse
│   └── config/                      BeanConfig  (conecta las piezas sin ensuciar el dominio)
└── VehiculoApplication.java
```

### 1.3 `application.properties`

`src/main/resources/application.properties`:

```properties
spring.application.name=vehiculo-service
server.port=8081

# H2 en ARCHIVO: los datos sobreviven a un reinicio, útil para "verificar persistencia" en la sustentación.
# Para usar H2 en memoria: spring.datasource.url=jdbc:h2:mem:parqueaderodb
spring.datasource.url=jdbc:h2:file:./data/parqueaderodb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

> Consola H2: <http://localhost:8081/h2-console> con JDBC URL `jdbc:h2:file:./data/parqueaderodb`, usuario `sa` y sin contraseña.
> Si usas Spring Boot 4.x y la consola no aparece, agrega la dependencia `org.springframework.boot:spring-boot-h2console`.

---

### 1.4 Capa `domain/model`: las clases de negocio (POO)

**Funcionalidad:** representa los vehículos con **herencia** (`Carro` y `Moto` extienden `Vehiculo`), **encapsulamiento** (atributos `private`, sin setters y con validación en el constructor) y **polimorfismo** (`getTipo()` es abstracto y cada subclase lo implementa).

`domain/model/TipoVehiculo.java`
```java
package com.parqueadero.vehiculo.domain.model;

public enum TipoVehiculo {
    CARRO,
    MOTO
}
```

`domain/model/Vehiculo.java`
```java
package com.parqueadero.vehiculo.domain.model;

import com.parqueadero.vehiculo.domain.exception.ReglaNegocioException;

public abstract class Vehiculo {

    private final String placa;
    private final String marca;
    private final String modelo;

    protected Vehiculo(String placa, String marca, String modelo) {
        this.placa = requerido(placa, "placa").trim().toUpperCase();
        this.marca = requerido(marca, "marca").trim();
        this.modelo = requerido(modelo, "modelo").trim();
    }

    /** Polimorfismo: cada subclase dice qué tipo es. */
    public abstract TipoVehiculo getTipo();

    protected static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio");
        }
        return valor;
    }

    public String getPlaca()  { return placa; }
    public String getMarca()  { return marca; }
    public String getModelo() { return modelo; }
}
```

`domain/model/Carro.java`
```java
package com.parqueadero.vehiculo.domain.model;

import com.parqueadero.vehiculo.domain.exception.ReglaNegocioException;

public class Carro extends Vehiculo {

    private final int numeroPuertas;

    public Carro(String placa, String marca, String modelo, Integer numeroPuertas) {
        super(placa, marca, modelo);
        if (numeroPuertas == null || numeroPuertas <= 0) {
            throw new ReglaNegocioException("Un CARRO requiere 'numeroPuertas' mayor a 0");
        }
        this.numeroPuertas = numeroPuertas;
    }

    @Override
    public TipoVehiculo getTipo() { return TipoVehiculo.CARRO; }

    public int getNumeroPuertas() { return numeroPuertas; }
}
```

`domain/model/Moto.java`
```java
package com.parqueadero.vehiculo.domain.model;

import com.parqueadero.vehiculo.domain.exception.ReglaNegocioException;

public class Moto extends Vehiculo {

    private final int cilindraje;

    public Moto(String placa, String marca, String modelo, Integer cilindraje) {
        super(placa, marca, modelo);
        if (cilindraje == null || cilindraje <= 0) {
            throw new ReglaNegocioException("Una MOTO requiere 'cilindraje' mayor a 0");
        }
        this.cilindraje = cilindraje;
    }

    @Override
    public TipoVehiculo getTipo() { return TipoVehiculo.MOTO; }

    public int getCilindraje() { return cilindraje; }
}
```

`domain/model/VehiculoFactory.java`: concentra en **un solo lugar** la decisión de qué subclase crear.
```java
package com.parqueadero.vehiculo.domain.model;

public final class VehiculoFactory {

    private VehiculoFactory() { }

    public static Vehiculo crear(String placa, String marca, String modelo, TipoVehiculo tipo,
                                 Integer numeroPuertas, Integer cilindraje) {
        return switch (tipo) {
            case CARRO -> new Carro(placa, marca, modelo, numeroPuertas);
            case MOTO  -> new Moto(placa, marca, modelo, cilindraje);
        };
    }
}
```

`domain/model/EstadisticasVehiculos.java`
```java
package com.parqueadero.vehiculo.domain.model;

public record EstadisticasVehiculos(long totalVehiculos, long totalCarros, long totalMotos) { }
```

### 1.5 Capa `domain/exception`: errores de negocio

**Funcionalidad:** son excepciones propias del negocio. El dominio las lanza sin saber nada de HTTP; después el `GlobalExceptionHandler` las traduce a 404, 409 o 400.

```java
// domain/exception/VehiculoNoEncontradoException.java
package com.parqueadero.vehiculo.domain.exception;

public class VehiculoNoEncontradoException extends RuntimeException {
    public VehiculoNoEncontradoException(String placa) {
        super("No existe un vehículo con placa " + placa);
    }
}
```
```java
// domain/exception/PlacaDuplicadaException.java
package com.parqueadero.vehiculo.domain.exception;

public class PlacaDuplicadaException extends RuntimeException {
    public PlacaDuplicadaException(String placa) {
        super("Ya existe un vehículo registrado con placa " + placa);
    }
}
```
```java
// domain/exception/ReglaNegocioException.java
package com.parqueadero.vehiculo.domain.exception;

public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) { super(mensaje); }
}
```

### 1.6 Capa `domain/port`: puerto de salida

**Funcionalidad:** es el *contrato* que el dominio necesita para guardar datos. **No sabe** si detrás hay H2, MySQL o un archivo, y eso es justamente lo que permite cambiar la base de datos sin tocar el negocio.

`domain/port/VehiculoRepositoryPort.java`
```java
package com.parqueadero.vehiculo.domain.port;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import java.util.List;
import java.util.Optional;

public interface VehiculoRepositoryPort {
    Vehiculo guardar(Vehiculo vehiculo);
    Optional<Vehiculo> buscarPorPlaca(String placa);
    List<Vehiculo> listarTodos();
    boolean existePorPlaca(String placa);
    void eliminarPorPlaca(String placa);
    long contarTodos();
    long contarPorTipo(TipoVehiculo tipo);
}
```

### 1.7 Capa `application/usecase`: puerto de entrada

**Funcionalidad:** define **qué puede hacer** el sistema. El controlador depende de esta interfaz y no de la implementación.

`application/usecase/VehiculoUseCase.java`
```java
package com.parqueadero.vehiculo.application.usecase;

import com.parqueadero.vehiculo.domain.model.EstadisticasVehiculos;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import java.util.List;

public interface VehiculoUseCase {
    Vehiculo registrar(Vehiculo vehiculo);
    List<Vehiculo> listar();
    Vehiculo buscarPorPlaca(String placa);
    Vehiculo actualizar(String placa, Vehiculo datosNuevos);
    void eliminar(String placa);
    EstadisticasVehiculos obtenerEstadisticas();
}
```

### 1.8 Capa `application/service`: reglas de negocio

**Funcionalidad:** aquí viven **todas las reglas**: no permitir placas duplicadas, lanzar 404 si la placa no existe, no cambiar el tipo al actualizar y calcular las estadísticas. **No lleva anotaciones de Spring**; el `BeanConfig` (paso 1.13) se encarga de registrarla.

`application/service/VehiculoService.java`
```java
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

public class VehiculoService implements VehiculoUseCase {

    private final VehiculoRepositoryPort repository;

    public VehiculoService(VehiculoRepositoryPort repository) {   // inyección por constructor
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
                    + existente.getTipo() + " → " + datosNuevos.getTipo() + ")");
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
```

### 1.9 Capa `infrastructure/entity`: la tabla JPA

**Funcionalidad:** es la clase que Hibernate convierte en la tabla `vehiculos`. **No es el dominio**: es su representación en la base de datos (ver la pregunta 7 de la sustentación).

`infrastructure/entity/VehiculoEntity.java`
```java
package com.parqueadero.vehiculo.infrastructure.entity;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import jakarta.persistence.*;

@Entity
@Table(name = "vehiculos")
public class VehiculoEntity {

    @Id
    @Column(length = 10)
    private String placa;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoVehiculo tipo;

    private Integer numeroPuertas;   // solo CARRO
    private Integer cilindraje;      // solo MOTO

    protected VehiculoEntity() { }   // requerido por JPA

    public VehiculoEntity(String placa, String marca, String modelo, TipoVehiculo tipo,
                          Integer numeroPuertas, Integer cilindraje) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.tipo = tipo;
        this.numeroPuertas = numeroPuertas;
        this.cilindraje = cilindraje;
    }

    public String getPlaca() { return placa; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public TipoVehiculo getTipo() { return tipo; }
    public Integer getNumeroPuertas() { return numeroPuertas; }
    public Integer getCilindraje() { return cilindraje; }
}
```

### 1.10 Capa `infrastructure/dto`: contratos JSON

**Funcionalidad:** define la forma del JSON que entra y sale, y valida la entrada con **Bean Validation** (`@NotBlank`, `@Pattern`, etc.). Están hechos con `record` (Java 16+) porque son inmutables y no necesitan *boilerplate*.

```java
// infrastructure/dto/VehiculoRequest.java  (POST)
package com.parqueadero.vehiculo.infrastructure.dto;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import jakarta.validation.constraints.*;

public record VehiculoRequest(
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^[A-Za-z]{3}[0-9]{2}[A-Za-z0-9]$",
                 message = "Formato de placa inválido (ej: ABC123 o XYZ45A)")
        String placa,
        @NotBlank(message = "La marca es obligatoria") String marca,
        @NotBlank(message = "El modelo es obligatorio") String modelo,
        @NotNull(message = "El tipo es obligatorio (CARRO o MOTO)") TipoVehiculo tipo,
        @Positive(message = "numeroPuertas debe ser mayor a 0") Integer numeroPuertas,
        @Positive(message = "cilindraje debe ser mayor a 0") Integer cilindraje
) { }
```
```java
// infrastructure/dto/VehiculoUpdateRequest.java  (PUT: sin placa, porque no se puede cambiar)
package com.parqueadero.vehiculo.infrastructure.dto;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import jakarta.validation.constraints.*;

public record VehiculoUpdateRequest(
        @NotBlank(message = "La marca es obligatoria") String marca,
        @NotBlank(message = "El modelo es obligatorio") String modelo,
        @NotNull(message = "El tipo es obligatorio (CARRO o MOTO)") TipoVehiculo tipo,
        @Positive Integer numeroPuertas,
        @Positive Integer cilindraje
) { }
```
```java
// infrastructure/dto/VehiculoResponse.java
package com.parqueadero.vehiculo.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)   // no muestra "cilindraje": null en un carro
public record VehiculoResponse(String placa, String marca, String modelo, String tipo,
                               Integer numeroPuertas, Integer cilindraje) { }
```
```java
// infrastructure/dto/EstadisticasResponse.java
package com.parqueadero.vehiculo.infrastructure.dto;

public record EstadisticasResponse(long totalVehiculos, long totalCarros, long totalMotos) { }
```

### 1.11 Capa `infrastructure/mapper`: traducciones

**Funcionalidad:** convierte entre los tres "idiomas" del sistema (**DTO ↔ dominio ↔ entidad**), de modo que ninguna capa tiene que conocer las clases de otra.

`infrastructure/mapper/VehiculoMapper.java`
```java
package com.parqueadero.vehiculo.infrastructure.mapper;

import com.parqueadero.vehiculo.domain.model.*;
import com.parqueadero.vehiculo.infrastructure.dto.*;
import com.parqueadero.vehiculo.infrastructure.entity.VehiculoEntity;

public final class VehiculoMapper {

    private VehiculoMapper() { }

    // ---------- DTO → dominio ----------
    public static Vehiculo toDomain(VehiculoRequest r) {
        return VehiculoFactory.crear(r.placa(), r.marca(), r.modelo(), r.tipo(),
                r.numeroPuertas(), r.cilindraje());
    }

    public static Vehiculo toDomain(String placa, VehiculoUpdateRequest r) {
        return VehiculoFactory.crear(placa, r.marca(), r.modelo(), r.tipo(),
                r.numeroPuertas(), r.cilindraje());
    }

    // ---------- dominio ↔ entidad ----------
    public static VehiculoEntity toEntity(Vehiculo v) {
        return new VehiculoEntity(v.getPlaca(), v.getMarca(), v.getModelo(), v.getTipo(),
                numeroPuertas(v), cilindraje(v));
    }

    public static Vehiculo toDomain(VehiculoEntity e) {
        return VehiculoFactory.crear(e.getPlaca(), e.getMarca(), e.getModelo(), e.getTipo(),
                e.getNumeroPuertas(), e.getCilindraje());
    }

    // ---------- dominio → DTO ----------
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
```

### 1.12 Capa `infrastructure/adapter/out`: persistencia

**Funcionalidad:** `VehiculoJpaRepository` es Spring Data JPA, que genera el SQL a partir del nombre de cada método. `VehiculoPersistenceAdapter` **implementa el puerto del dominio** apoyándose en ese repositorio.

`infrastructure/adapter/out/VehiculoJpaRepository.java`
```java
package com.parqueadero.vehiculo.infrastructure.adapter.out;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import com.parqueadero.vehiculo.infrastructure.entity.VehiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoJpaRepository extends JpaRepository<VehiculoEntity, String> {
    long countByTipo(TipoVehiculo tipo);   // SELECT COUNT(*) FROM vehiculos WHERE tipo = ?
}
```

`infrastructure/adapter/out/VehiculoPersistenceAdapter.java`
```java
package com.parqueadero.vehiculo.infrastructure.adapter.out;

import com.parqueadero.vehiculo.domain.model.TipoVehiculo;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import com.parqueadero.vehiculo.domain.port.VehiculoRepositoryPort;
import com.parqueadero.vehiculo.infrastructure.mapper.VehiculoMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

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
    public boolean existePorPlaca(String placa) { return jpa.existsById(placa); }

    @Override
    public void eliminarPorPlaca(String placa) { jpa.deleteById(placa); }

    @Override
    public long contarTodos() { return jpa.count(); }

    @Override
    public long contarPorTipo(TipoVehiculo tipo) { return jpa.countByTipo(tipo); }
}
```

### 1.13 Capa `infrastructure/config`: ensamblado de beans

**Funcionalidad:** le indica a Spring cómo construir el `VehiculoService` y le inyecta el adaptador. Con esto el servicio no necesita `@Service` y queda libre de Spring (esto es **inyección de dependencias**).

`infrastructure/config/BeanConfig.java`
```java
package com.parqueadero.vehiculo.infrastructure.config;

import com.parqueadero.vehiculo.application.service.VehiculoService;
import com.parqueadero.vehiculo.application.usecase.VehiculoUseCase;
import com.parqueadero.vehiculo.domain.port.VehiculoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public VehiculoUseCase vehiculoUseCase(VehiculoRepositoryPort repositoryPort) {
        return new VehiculoService(repositoryPort);
    }
}
```

### 1.14 Capa `infrastructure/adapter/in`: controlador REST

**Funcionalidad:** **solo coordina**: recibe el HTTP, valida con `@Valid`, mapea, llama al caso de uso y devuelve el código HTTP. No contiene reglas de negocio.

`infrastructure/adapter/in/VehiculoController.java`
```java
package com.parqueadero.vehiculo.infrastructure.adapter.in;

import com.parqueadero.vehiculo.application.usecase.VehiculoUseCase;
import com.parqueadero.vehiculo.domain.model.Vehiculo;
import com.parqueadero.vehiculo.infrastructure.dto.*;
import com.parqueadero.vehiculo.infrastructure.mapper.VehiculoMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoUseCase useCase;

    public VehiculoController(VehiculoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping                                                    // RF-01 → 201
    public ResponseEntity<VehiculoResponse> registrar(@Valid @RequestBody VehiculoRequest request) {
        Vehiculo creado = useCase.registrar(VehiculoMapper.toDomain(request));
        return ResponseEntity.created(URI.create("/api/vehiculos/" + creado.getPlaca()))
                .body(VehiculoMapper.toResponse(creado));
    }

    @GetMapping                                                     // RF-02 → 200
    public List<VehiculoResponse> listar() {
        return useCase.listar().stream().map(VehiculoMapper::toResponse).toList();
    }

    @GetMapping("/estadisticas")                                    // RF-07 → 200
    public EstadisticasResponse estadisticas() {
        return VehiculoMapper.toResponse(useCase.obtenerEstadisticas());
    }

    @GetMapping("/{placa}")                                         // RF-03 → 200 / 404
    public VehiculoResponse buscar(@PathVariable String placa) {
        return VehiculoMapper.toResponse(useCase.buscarPorPlaca(placa));
    }

    @PutMapping("/{placa}")                                         // RF-04 → 200
    public VehiculoResponse actualizar(@PathVariable String placa,
                                       @Valid @RequestBody VehiculoUpdateRequest request) {
        Vehiculo datos = VehiculoMapper.toDomain(placa, request);
        return VehiculoMapper.toResponse(useCase.actualizar(placa, datos));
    }

    @DeleteMapping("/{placa}")                                      // RF-05 → 204
    public ResponseEntity<Void> eliminar(@PathVariable String placa) {
        useCase.eliminar(placa);
        return ResponseEntity.noContent().build();
    }
}
```

### 1.15 Capa `infrastructure/exception`: manejo centralizado de errores

**Funcionalidad:** un solo lugar para convertir excepciones en respuestas JSON uniformes. Gracias a esto el controlador no necesita ningún `try/catch`.

`infrastructure/exception/ErrorResponse.java`
```java
package com.parqueadero.vehiculo.infrastructure.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(LocalDateTime timestamp, int status, String error,
                            String mensaje, String ruta, List<String> detalles) { }
```

`infrastructure/exception/GlobalExceptionHandler.java`
```java
package com.parqueadero.vehiculo.infrastructure.exception;

import com.parqueadero.vehiculo.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VehiculoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(VehiculoNoEncontradoException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(PlacaDuplicadaException.class)
    public ResponseEntity<ErrorResponse> duplicada(PlacaDuplicadaException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", req, detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST,
                "JSON mal formado o valor no permitido (tipo debe ser CARRO o MOTO)", req, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String mensaje,
                                                HttpServletRequest req, List<String> detalles) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), mensaje, req.getRequestURI(), detalles);
        return ResponseEntity.status(status).body(body);
    }
}
```

### 1.16 Probar vehiculo-service

```powershell
cd vehiculo-service
.\mvnw.cmd spring-boot:run
```

Busca en la consola el mensaje `Tomcat started on port 8081`. Después prueba `GET http://localhost:8081/api/vehiculos` en Postman: la respuesta debe ser `[]`.

---

## Paso 2 · pago-service

### 2.1 Generar el proyecto en Spring Initializr

| Campo | Valor |
|---|---|
| Group / Artifact | `com.parqueadero` / `pago-service` |
| **Package name** | `com.parqueadero.pago` |
| Java / Packaging | 21 / Jar |
| Dependencies | **Spring Web**, **Validation** (opcional: DevTools) |

> No agregamos JPA porque no se guarda historial (es opcional). Si más adelante quieres el bonus del historial, agrega **Spring Data JPA + H2** con **su propia** base `pagodb`.

### 2.2 Crear las carpetas

```powershell
cd pago-service\src\main\java\com\parqueadero\pago
mkdir domain\model, domain\calculator, domain\port, domain\exception, `
      application\usecase, application\service, `
      infrastructure\adapter\in, infrastructure\adapter\out, `
      infrastructure\dto, infrastructure\config, infrastructure\exception
```

```
pago-service/src/main/java/com/parqueadero/pago/
├── domain/
│   ├── model/          TipoVehiculo, VehiculoInfo, Pago
│   ├── calculator/     PagoCalculator (interface), CarroPagoCalculator, MotoPagoCalculator   ← POLIMORFISMO
│   ├── port/           VehiculoClientPort  (cómo obtengo un vehículo, sin saber que es HTTP)
│   └── exception/      VehiculoNoEncontradoException, HorasInvalidasException,
│                       TipoNoSoportadoException, ServicioNoDisponibleException
├── application/
│   ├── usecase/        CalcularPagoUseCase
│   └── service/        PagoService
├── infrastructure/
│   ├── adapter/in/     PagoController
│   ├── adapter/out/    VehiculoHttpAdapter  (RestClient → vehiculo-service)
│   ├── dto/            PagoRequest, PagoResponse, VehiculoRemotoResponse
│   ├── config/         BeanConfig, RestClientConfig
│   └── exception/      GlobalExceptionHandler, ErrorResponse
└── PagoApplication.java
```

### 2.3 `application.properties`

```properties
spring.application.name=pago-service
server.port=8082

# URL del otro microservicio (no se escribe "quemada" en el código)
vehiculo-service.url=http://localhost:8081
vehiculo-service.connect-timeout=2s
vehiculo-service.read-timeout=3s
```

### 2.4 `domain/model`

> `pago-service` tiene **su propio** `TipoVehiculo`. Los microservicios no comparten clases ni tablas, solo contratos JSON.

```java
// domain/model/TipoVehiculo.java
package com.parqueadero.pago.domain.model;

public enum TipoVehiculo { CARRO, MOTO }
```
```java
// domain/model/VehiculoInfo.java: lo único que pago-service necesita saber del vehículo
package com.parqueadero.pago.domain.model;

public record VehiculoInfo(String placa, TipoVehiculo tipo) { }
```
```java
// domain/model/Pago.java: resultado del cálculo
package com.parqueadero.pago.domain.model;

public record Pago(String placa, TipoVehiculo tipoVehiculo, int horas,
                   long valorHora, long totalPagar, String moneda) { }
```

### 2.5 `domain/calculator`: polimorfismo (patrón Strategy)

**Funcionalidad:** cada tipo de vehículo tiene **su propia** calculadora con su tarifa, y el servicio las trata a todas por igual a través de la interfaz.

```java
// domain/calculator/PagoCalculator.java
package com.parqueadero.pago.domain.calculator;

import com.parqueadero.pago.domain.model.TipoVehiculo;

public interface PagoCalculator {
    TipoVehiculo getTipo();
    long getValorHora();

    default long calcular(int horas) {
        return getValorHora() * horas;
    }
}
```
```java
// domain/calculator/CarroPagoCalculator.java
package com.parqueadero.pago.domain.calculator;

import com.parqueadero.pago.domain.model.TipoVehiculo;

public class CarroPagoCalculator implements PagoCalculator {
    private static final long VALOR_HORA = 5_000L;

    @Override public TipoVehiculo getTipo() { return TipoVehiculo.CARRO; }
    @Override public long getValorHora()    { return VALOR_HORA; }
}
```
```java
// domain/calculator/MotoPagoCalculator.java
package com.parqueadero.pago.domain.calculator;

import com.parqueadero.pago.domain.model.TipoVehiculo;

public class MotoPagoCalculator implements PagoCalculator {
    private static final long VALOR_HORA = 3_000L;

    @Override public TipoVehiculo getTipo() { return TipoVehiculo.MOTO; }
    @Override public long getValorHora()    { return VALOR_HORA; }
}
```

### 2.6 `domain/port` y `domain/exception`

```java
// domain/port/VehiculoClientPort.java
package com.parqueadero.pago.domain.port;

import com.parqueadero.pago.domain.model.VehiculoInfo;
import java.util.Optional;

public interface VehiculoClientPort {
    Optional<VehiculoInfo> buscarPorPlaca(String placa);
}
```
```java
// domain/exception/VehiculoNoEncontradoException.java
package com.parqueadero.pago.domain.exception;

public class VehiculoNoEncontradoException extends RuntimeException {
    public VehiculoNoEncontradoException(String placa) {
        super("No existe un vehículo con placa " + placa);
    }
}
```
```java
// domain/exception/HorasInvalidasException.java
package com.parqueadero.pago.domain.exception;

public class HorasInvalidasException extends RuntimeException {
    public HorasInvalidasException(int horas) {
        super("Las horas deben ser mayores a cero. Recibido: " + horas);
    }
}
```
```java
// domain/exception/TipoNoSoportadoException.java
package com.parqueadero.pago.domain.exception;

public class TipoNoSoportadoException extends RuntimeException {
    public TipoNoSoportadoException(String tipo) {
        super("No hay tarifa configurada para el tipo de vehículo: " + tipo);
    }
}
```
```java
// domain/exception/ServicioNoDisponibleException.java
package com.parqueadero.pago.domain.exception;

public class ServicioNoDisponibleException extends RuntimeException {
    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
```

### 2.7 `application`: caso de uso y servicio

```java
// application/usecase/CalcularPagoUseCase.java
package com.parqueadero.pago.application.usecase;

import com.parqueadero.pago.domain.model.Pago;

public interface CalcularPagoUseCase {
    Pago calcular(String placa, int horas);
}
```

**Funcionalidad de `PagoService`:** valida las horas, consulta el vehículo mediante el puerto, elige la calculadora según el tipo **sin usar `if/else`** y arma el `Pago`.

```java
// application/service/PagoService.java
package com.parqueadero.pago.application.service;

import com.parqueadero.pago.application.usecase.CalcularPagoUseCase;
import com.parqueadero.pago.domain.calculator.PagoCalculator;
import com.parqueadero.pago.domain.exception.*;
import com.parqueadero.pago.domain.model.*;
import com.parqueadero.pago.domain.port.VehiculoClientPort;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PagoService implements CalcularPagoUseCase {

    private static final String MONEDA = "COP";

    private final VehiculoClientPort vehiculoClient;
    private final Map<TipoVehiculo, PagoCalculator> calculadoras;

    public PagoService(VehiculoClientPort vehiculoClient, List<PagoCalculator> calculadoras) {
        this.vehiculoClient = vehiculoClient;
        this.calculadoras = calculadoras.stream()
                .collect(Collectors.toMap(PagoCalculator::getTipo, Function.identity()));
    }

    @Override
    public Pago calcular(String placa, int horas) {
        if (horas <= 0) {
            throw new HorasInvalidasException(horas);
        }
        VehiculoInfo vehiculo = vehiculoClient.buscarPorPlaca(placa.trim().toUpperCase())
                .orElseThrow(() -> new VehiculoNoEncontradoException(placa));

        PagoCalculator calculadora = calculadoras.get(vehiculo.tipo());
        if (calculadora == null) {
            throw new TipoNoSoportadoException(vehiculo.tipo().name());
        }

        return new Pago(vehiculo.placa(), vehiculo.tipo(), horas,
                calculadora.getValorHora(), calculadora.calcular(horas), MONEDA);
    }
}
```

### 2.8 `infrastructure/dto`

```java
// infrastructure/dto/PagoRequest.java
package com.parqueadero.pago.infrastructure.dto;

import jakarta.validation.constraints.*;

public record PagoRequest(
        @NotBlank(message = "La placa es obligatoria") String placa,
        @NotNull(message = "Las horas son obligatorias")
        @Positive(message = "Las horas deben ser mayores a cero") Integer horas
) { }
```
```java
// infrastructure/dto/PagoResponse.java
package com.parqueadero.pago.infrastructure.dto;

import com.parqueadero.pago.domain.model.Pago;

public record PagoResponse(String placa, String tipoVehiculo, int horas,
                           long valorHora, long totalPagar, String moneda) {

    public static PagoResponse from(Pago p) {
        return new PagoResponse(p.placa(), p.tipoVehiculo().name(), p.horas(),
                p.valorHora(), p.totalPagar(), p.moneda());
    }
}
```
```java
// infrastructure/dto/VehiculoRemotoResponse.java: el JSON que devuelve vehiculo-service
package com.parqueadero.pago.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)   // ignora marca, modelo, etc.
public record VehiculoRemotoResponse(String placa, String tipo) { }
```

### 2.9 `infrastructure/config`

```java
// infrastructure/config/RestClientConfig.java
package com.parqueadero.pago.infrastructure.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient vehiculoRestClient(@Value("${vehiculo-service.url}") String baseUrl,
                                         @Value("${vehiculo-service.connect-timeout}") Duration connectTimeout,
                                         @Value("${vehiculo-service.read-timeout}") Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
```
```java
// infrastructure/config/BeanConfig.java
package com.parqueadero.pago.infrastructure.config;

import com.parqueadero.pago.application.service.PagoService;
import com.parqueadero.pago.application.usecase.CalcularPagoUseCase;
import com.parqueadero.pago.domain.calculator.*;
import com.parqueadero.pago.domain.port.VehiculoClientPort;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public PagoCalculator carroPagoCalculator() { return new CarroPagoCalculator(); }

    @Bean
    public PagoCalculator motoPagoCalculator() { return new MotoPagoCalculator(); }

    @Bean   // Spring inyecta TODAS las PagoCalculator registradas en la lista
    public CalcularPagoUseCase calcularPagoUseCase(VehiculoClientPort client, List<PagoCalculator> calculadoras) {
        return new PagoService(client, calculadoras);
    }
}
```

### 2.10 `infrastructure/adapter/out`: cliente HTTP

**Funcionalidad:** implementa el puerto `VehiculoClientPort` llamando por HTTP a `vehiculo-service`. Aquí vive la **comunicación entre microservicios** y la traducción de los errores de red a errores del dominio.

`infrastructure/adapter/out/VehiculoHttpAdapter.java`
```java
package com.parqueadero.pago.infrastructure.adapter.out;

import com.parqueadero.pago.domain.exception.ServicioNoDisponibleException;
import com.parqueadero.pago.domain.exception.TipoNoSoportadoException;
import com.parqueadero.pago.domain.model.TipoVehiculo;
import com.parqueadero.pago.domain.model.VehiculoInfo;
import com.parqueadero.pago.domain.port.VehiculoClientPort;
import com.parqueadero.pago.infrastructure.dto.VehiculoRemotoResponse;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class VehiculoHttpAdapter implements VehiculoClientPort {

    private final RestClient restClient;

    public VehiculoHttpAdapter(RestClient vehiculoRestClient) {
        this.restClient = vehiculoRestClient;
    }

    @Override
    public Optional<VehiculoInfo> buscarPorPlaca(String placa) {
        try {
            VehiculoRemotoResponse respuesta = restClient.get()
                    .uri("/api/vehiculos/{placa}", placa)
                    .retrieve()
                    .body(VehiculoRemotoResponse.class);

            if (respuesta == null) {
                return Optional.empty();
            }
            return Optional.of(new VehiculoInfo(respuesta.placa(), convertirTipo(respuesta.tipo())));

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();                                      // → 404
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw new ServicioNoDisponibleException(
                    "vehiculo-service no está disponible en este momento", e); // → 503
        }
    }

    private TipoVehiculo convertirTipo(String tipo) {
        try {
            return TipoVehiculo.valueOf(tipo);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new TipoNoSoportadoException(String.valueOf(tipo));
        }
    }
}
```

### 2.11 `infrastructure/adapter/in`: controlador

```java
// infrastructure/adapter/in/PagoController.java
package com.parqueadero.pago.infrastructure.adapter.in;

import com.parqueadero.pago.application.usecase.CalcularPagoUseCase;
import com.parqueadero.pago.infrastructure.dto.PagoRequest;
import com.parqueadero.pago.infrastructure.dto.PagoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final CalcularPagoUseCase useCase;

    public PagoController(CalcularPagoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/calcular")                                        // RF-06 → 200
    public PagoResponse calcular(@Valid @RequestBody PagoRequest request) {
        return PagoResponse.from(useCase.calcular(request.placa(), request.horas()));
    }
}
```

### 2.12 `infrastructure/exception`

`ErrorResponse.java` es igual al de vehiculo-service; solo cambia el paquete a `com.parqueadero.pago.infrastructure.exception`.

```java
// infrastructure/exception/GlobalExceptionHandler.java
package com.parqueadero.pago.infrastructure.exception;

import com.parqueadero.pago.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VehiculoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(HorasInvalidasException.class)
    public ResponseEntity<ErrorResponse> horas(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(TipoNoSoportadoException.class)
    public ResponseEntity<ErrorResponse> tipo(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> noDisponible(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", req, detalles);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> json(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "JSON mal formado", req, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String mensaje,
                                                HttpServletRequest req, List<String> detalles) {
        return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(),
                status.value(), status.getReasonPhrase(), mensaje, req.getRequestURI(), detalles));
    }
}
```

### 2.13 Ejecutar los dos servicios

Abre **dos terminales**:

```powershell
# Terminal 1
cd vehiculo-service; .\mvnw.cmd spring-boot:run
# Terminal 2
cd pago-service; .\mvnw.cmd spring-boot:run
```

---

## Paso 3 · Colección Postman "Prueba Tecnica - Parqueadero"

En Postman crea una colección con ese nombre y define dos variables de colección: `vehiculos = http://localhost:8081` y `pagos = http://localhost:8082`. Agrega las siguientes peticiones **en este orden**:

| # | Nombre | Método y URL | Body | Esperado |
|---|---|---|---|---|
| 1 | Registrar carro 1 | `POST {{vehiculos}}/api/vehiculos` | `{"placa":"ABC123","marca":"Chevrolet","modelo":"2020","tipo":"CARRO","numeroPuertas":4}` | 201 |
| 2 | Registrar carro 2 | `POST {{vehiculos}}/api/vehiculos` | `{"placa":"DEF456","marca":"Mazda","modelo":"2022","tipo":"CARRO","numeroPuertas":5}` | 201 |
| 3 | Registrar moto 1 | `POST {{vehiculos}}/api/vehiculos` | `{"placa":"XYZ45A","marca":"Yamaha","modelo":"2023","tipo":"MOTO","cilindraje":150}` | 201 |
| 4 | Registrar moto 2 | `POST {{vehiculos}}/api/vehiculos` | `{"placa":"QWE12B","marca":"Honda","modelo":"2021","tipo":"MOTO","cilindraje":125}` | 201 |
| 5 | Placa duplicada | `POST {{vehiculos}}/api/vehiculos` | igual al #1 | **409** |
| 6 | Listar | `GET {{vehiculos}}/api/vehiculos` | — | 200 |
| 7 | Buscar existente | `GET {{vehiculos}}/api/vehiculos/ABC123` | — | 200 |
| 8 | Buscar inexistente | `GET {{vehiculos}}/api/vehiculos/NOP999` | — | **404** |
| 9 | Estadísticas | `GET {{vehiculos}}/api/vehiculos/estadisticas` | — | 200 `{4,2,2}` |
| 10 | Pago carro 3h | `POST {{pagos}}/api/pagos/calcular` | `{"placa":"ABC123","horas":3}` | 200 · **15000** |
| 11 | Pago moto 3h | `POST {{pagos}}/api/pagos/calcular` | `{"placa":"XYZ45A","horas":3}` | 200 · **9000** |
| 12 | Pago horas negativas | `POST {{pagos}}/api/pagos/calcular` | `{"placa":"ABC123","horas":-2}` | **400** |
| 13 | Pago placa inexistente | `POST {{pagos}}/api/pagos/calcular` | `{"placa":"NOP999","horas":3}` | **404** |
| 14 | Actualizar | `PUT {{vehiculos}}/api/vehiculos/DEF456` | `{"marca":"Mazda","modelo":"2024","tipo":"CARRO","numeroPuertas":4}` | 200 |
| 15 | Eliminar | `DELETE {{vehiculos}}/api/vehiculos/QWE12B` | — | **204** |
| 16 | *(Bonus)* Servicio caído | detén vehiculo-service y repite el #10 | — | **503** |

Para no revisar cada respuesta a ojo, agrega un *test* en la pestaña **Scripts → Post-response**:

```javascript
pm.test("Status 201", () => pm.response.to.have.status(201));
// para el pago del carro:
pm.test("Total 15000", () => pm.expect(pm.response.json().totalPagar).to.eql(15000));
```

Para exportarla: clic derecho en la colección → **Export** → *Collection v2.1*, y guárdala en `postman/`.

---

## Paso 4 · Pruebas unitarias (bonus JUnit 5 + Mockito)

`spring-boot-starter-test` ya trae JUnit 5 y Mockito. Crea los tests en `src/test/java/...` con la misma estructura de paquetes.

`vehiculo-service/src/test/java/com/parqueadero/vehiculo/application/service/VehiculoServiceTest.java`
```java
package com.parqueadero.vehiculo.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.parqueadero.vehiculo.domain.exception.*;
import com.parqueadero.vehiculo.domain.model.*;
import com.parqueadero.vehiculo.domain.port.VehiculoRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock VehiculoRepositoryPort repository;
    @InjectMocks VehiculoService service;

    @Test
    void registrar_placaDuplicada_lanza409() {
        Carro carro = new Carro("ABC123", "Chevrolet", "2020", 4);
        when(repository.existePorPlaca("ABC123")).thenReturn(true);

        assertThrows(PlacaDuplicadaException.class, () -> service.registrar(carro));
        verify(repository, never()).guardar(any());
    }

    @Test
    void buscar_inexistente_lanza404() {
        when(repository.buscarPorPlaca("NOP999")).thenReturn(Optional.empty());
        assertThrows(VehiculoNoEncontradoException.class, () -> service.buscarPorPlaca("nop999"));
    }

    @Test
    void actualizar_cambiandoTipo_lanzaReglaNegocio() {
        when(repository.buscarPorPlaca("ABC123"))
                .thenReturn(Optional.of(new Carro("ABC123", "Chevrolet", "2020", 4)));
        Moto comoMoto = new Moto("ABC123", "Yamaha", "2023", 150);

        assertThrows(ReglaNegocioException.class, () -> service.actualizar("ABC123", comoMoto));
    }
}
```

`pago-service/src/test/java/com/parqueadero/pago/application/service/PagoServiceTest.java`
```java
package com.parqueadero.pago.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.parqueadero.pago.domain.calculator.*;
import com.parqueadero.pago.domain.exception.*;
import com.parqueadero.pago.domain.model.*;
import com.parqueadero.pago.domain.port.VehiculoClientPort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PagoServiceTest {

    private VehiculoClientPort client;
    private PagoService service;

    @BeforeEach
    void setUp() {
        client = mock(VehiculoClientPort.class);
        service = new PagoService(client, List.of(new CarroPagoCalculator(), new MotoPagoCalculator()));
    }

    @Test
    void carro3Horas_15000() {
        when(client.buscarPorPlaca("ABC123")).thenReturn(Optional.of(new VehiculoInfo("ABC123", TipoVehiculo.CARRO)));
        assertEquals(15_000, service.calcular("ABC123", 3).totalPagar());
    }

    @Test
    void moto3Horas_9000() {
        when(client.buscarPorPlaca("XYZ45A")).thenReturn(Optional.of(new VehiculoInfo("XYZ45A", TipoVehiculo.MOTO)));
        assertEquals(9_000, service.calcular("XYZ45A", 3).totalPagar());
    }

    @Test
    void horasNegativas_lanza400_sinLlamarAlOtroServicio() {
        assertThrows(HorasInvalidasException.class, () -> service.calcular("ABC123", -1));
        verifyNoInteractions(client);
    }

    @Test
    void placaInexistente_lanza404() {
        when(client.buscarPorPlaca("NOP999")).thenReturn(Optional.empty());
        assertThrows(VehiculoNoEncontradoException.class, () -> service.calcular("NOP999", 3));
    }
}
```

Ejecuta las pruebas con `.\mvnw.cmd test` en cada servicio.

---

## Paso 5 · Bonus: agregar el tipo Camión sin modificar Carro ni Moto

1. **vehiculo-service:** agrega `CAMION` a `TipoVehiculo`, crea `domain/model/Camion.java` (por ejemplo con `capacidadToneladas`), agrega un `case CAMION` en `VehiculoFactory` y crea la columna correspondiente en la entidad.
2. **pago-service:** agrega `CAMION` a su enum, crea `CamionPagoCalculator` (por ejemplo a $10.000/h) y regístrala con un `@Bean` en `BeanConfig`.
3. `PagoService`, `CarroPagoCalculator` y `MotoPagoCalculator` quedan **igual**. Esto es el principio abierto/cerrado en la práctica.

---

## Paso 6 · Git y entregables

`.gitignore` en la raíz:

```gitignore
target/
data/
*.mv.db
*.trace.db
.idea/
*.iml
.vscode/
.DS_Store
```

```powershell
git init
git add .
git commit -m "Prueba tecnica: vehiculo-service y pago-service con arquitectura hexagonal"
```

**Checklist final**
- [ ] Los dos `pom.xml` usan Java 21.
- [ ] Hay un `application.properties` por servicio, con los puertos 8081 y 8082.
- [ ] El `README.md` explica la instalación y la ejecución.
- [ ] La colección Postman está exportada en `postman/`.
- [ ] Están registrados 2 carros y 2 motos, y todos los escenarios devuelven el código esperado.
- [ ] No hay credenciales reales en el repositorio.
