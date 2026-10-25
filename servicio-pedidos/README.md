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
| `GET` | `/api/pedidos` | Listar todos los pedidos (filtros opcionales por `clienteId` y `estado`) |
| `GET` | `/api/pedidos/{id}` | Obtener pedido por ID |
| `POST` | `/api/pedidos` | Crear nuevo pedido |
| `PUT` | `/api/pedidos/{id}` | Actualizar pedido existente |
| `DELETE` | `/api/pedidos/{id}` | Eliminar pedido por ID |

## Migraciones de Base de Datos

Las migraciones son gestionadas automáticamente con Flyway al iniciar la aplicación (`src/main/resources/db/migration`):

| Versión | Descripción |
|---------|-------------|
| `V1` | Creación de la tabla `pedidos` |

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
