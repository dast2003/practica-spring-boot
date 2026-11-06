# Sistema de Parqueadero · Microservicios Spring Boot

Prueba técnica Java Backend Junior: dos microservicios independientes con **arquitectura hexagonal**.

| Servicio | Puerto | Función |
|---|---|---|
| `vehiculo-service` | 8081 | CRUD y estadísticas de vehículos (Spring Data JPA + H2) |
| `pago-service` | 8082 | Calcula el valor a pagar consultando a `vehiculo-service` por HTTP (RestClient) |

Tarifas: **Carro $5.000 COP/h** · **Moto $3.000 COP/h**

## Documentación

1. [Arquitectura](docs/01-arquitectura.md): contexto, capas hexagonales, componentes, clases y modelo de datos
2. [Diagramas de flujo](docs/02-diagramas-de-flujo.md): un flujo por requerimiento y diagramas de secuencia
3. [Guía paso a paso](docs/03-guia-paso-a-paso.md): creación de carpetas, función de cada archivo y código

**Stack:** Java 21 · Spring Boot 4.1.2 · Maven · Spring Data JPA · H2 · Bean Validation · RestClient · JUnit 5 + Mockito

## Requisitos

- JDK 21
- Postman
- No hace falta instalar Maven: cada proyecto trae `mvnw`

## Ejecución

```powershell
# Terminal 1
cd vehiculo-service
.\mvnw.cmd spring-boot:run

# Terminal 2
cd pago-service
.\mvnw.cmd spring-boot:run
```

- API de vehículos: <http://localhost:8081/api/vehiculos>
- Consola H2: <http://localhost:8081/h2-console> (JDBC URL `jdbc:h2:file:./data/parqueaderodb`, usuario `sa`, sin contraseña)
- API de pagos: `POST http://localhost:8082/api/pagos/calcular`

## Endpoints

| Código | Método | Ruta | Respuesta |
|---|---|---|---|
| RF-01 | POST | `/api/vehiculos` | 201 / 400 / 409 |
| RF-02 | GET | `/api/vehiculos` | 200 |
| RF-03 | GET | `/api/vehiculos/{placa}` | 200 / 404 |
| RF-04 | PUT | `/api/vehiculos/{placa}` | 200 / 400 / 404 |
| RF-05 | DELETE | `/api/vehiculos/{placa}` | 204 / 404 |
| RF-06 | POST | `/api/pagos/calcular` | 200 / 400 / 404 / 503 |
| RF-07 | GET | `/api/vehiculos/estadisticas` | 200 |

## Pruebas

- Unitarias: `.\mvnw.cmd test` en cada servicio
- Postman: importar `postman/Prueba Tecnica - Parqueadero.postman_collection.json` (*Import* → archivo) y ejecutar las peticiones en orden. Cada petición trae tests que validan el código HTTP y los totales.
- Bonus 503: detener `vehiculo-service` y ejecutar la última petición de la carpeta *pago-service*.
