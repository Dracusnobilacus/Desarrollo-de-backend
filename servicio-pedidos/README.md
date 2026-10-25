# Servicio Pedidos

Microservicio de gestión de pedidos desarrollado con Spring Boot 3.3.

## Tecnologías

- Java 17
- Spring Boot 3.3.0
- Spring Data JPA
- Spring Web
- Flyway (Core + PostgreSQL)
- PostgreSQL
- Lombok
- Gradle

## Configuración del Servidor

- **Puerto:** `8082` (`server.port=8082`)
- **Base de datos:** PostgreSQL (`jdbc:postgresql://localhost:5433/pedidosdb`)

## Estructura del Proyecto

```
com.fube.pedidos
├── controladores    # Controladores REST (/api/pedidos)
├── dto              # Objetos de transferencia de datos (PedidoDTO)
├── mapper           # Conversión entre entidades y DTOs (PedidoMapper)
├── modelos          # Entidades JPA (Pedido, EstadoPedido)
├── repositorio      # Repositorios Spring Data JPA (PedidoRepositorio)
└── servicios        # Lógica de negocio (PedidoServicio, PedidoServicioImpl)
```

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| `POST` | `/api/pedidos` | Crear nuevo pedido (valida cliente y reserva stock) |
| `GET` | `/api/pedidos/{id}` | Obtener pedido completo por ID |
| `GET` | `/api/pedidos/cliente/{clienteId}` | Listar pedidos de un cliente |
| `PUT` | `/api/pedidos/{id}/cancelar` | Cancelar pedido (en estado CREADO o CONFIRMADO) y liberar stock |

## Documentación OpenAPI (Swagger)

Con la aplicación en ejecución, accedé a la documentación interactiva en:
```
http://localhost:8082/swagger-ui.html
```
O al documento OpenAPI JSON en:
```
http://localhost:8082/v3/api-docs
```

## Migraciones de Base de Datos

Las migraciones son gestionadas automáticamente con Flyway al iniciar la aplicación (`src/main/resources/db/migration`):

| Versión | Descripción |
|---------|-------------|
| `V1` | Creación de las tablas `pedidos` e `item_pedidos` con claves foráneas e índices |

## Cómo correr el proyecto

```bash
./gradlew bootRun
```

O en Windows:

```cmd
gradlew.bat bootRun
```

Para ejecutar los tests:

```bash
./gradlew test
```
