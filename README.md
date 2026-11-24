Desarrollo de Backend

Sistema backend basado en microservicios desarrollado con Java y Spring Boot para la gestion de clientes, inventario y pedidos.

Microservicios

- Clientes: ubicado en la raiz del proyecto, corre en el puerto 8080 y usa la base de datos "clientesdb". Se encarga de la gestion de clientes y del proceso batch de normalizacion de datos.
- Inventario: ubicado en la carpeta "servicio-inventario", corre en el puerto 8081 y usa la base de datos "inventariodb". Se encarga de la gestion de productos, stock y reservas.
- Pedidos: ubicado en la carpeta "servicio-pedidos", corre en el puerto 8082 y usa la base de datos "pedidosdb". Se encarga de la creacion y seguimiento de pedidos, la integracion con Clientes e Inventario, y el proceso batch de expiracion.

Tecnologias

- Java 17
- Spring Boot 3.3
- Spring Data JPA
- Spring Batch
- Flyway
- PostgreSQL
- Gradle
- Lombok
- Springdoc OpenAPI (Swagger)

Requisitos previos

- Java 17
- PostgreSQL corriendo en localhost puerto 5432 con usuario "postgres" y contrasena "postgres"

Base de datos

Antes de iniciar los servicios por primera vez, ejecutar el script ubicado en "scripts/init-databases.sql" sobre PostgreSQL para crear las tres bases de datos: "clientesdb", "inventariodb" y "pedidosdb".

Las tablas y los datos iniciales se crean automaticamente con Flyway al levantar cada microservicio.

Como ejecutar el proyecto

Cada microservicio se levanta de forma independiente desde su carpeta usando Gradle:

1. Servicio de Clientes (puerto 8080): ejecutar "./gradlew bootRun" desde la raiz del proyecto.
2. Servicio de Inventario (puerto 8081): entrar en "servicio-inventario" y ejecutar "./gradlew bootRun".
3. Servicio de Pedidos (puerto 8082): entrar en "servicio-pedidos" y ejecutar "./gradlew bootRun".

En Windows se puede usar ".\gradlew.bat bootRun".

Tests

Para ejecutar los tests de cada microservicio, correr "./gradlew test" dentro de su carpeta correspondiente.

Documentacion de la API

Con los servicios en ejecucion, la documentacion de Swagger queda disponible en:

- Clientes: http://localhost:8080/swagger-ui.html
- Inventario: http://localhost:8081/swagger-ui.html
- Pedidos: http://localhost:8082/swagger-ui.html
