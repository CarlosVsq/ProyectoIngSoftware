# Perfiles y secretos en Spring Boot

## Archivos incluidos
- `backend_datalab/src/main/resources/application.properties`: configuración mínima y segura. Activa el perfil `${SPRING_PROFILES_ACTIVE:prod}` por defecto.
- `backend_datalab/src/main/resources/application-local.properties.example`: plantilla para desarrollo local. **No la subas**; copia a `application-local.properties`.

## Ejecución local
```bash
cp backend_datalab/src/main/resources/application-local.properties.example backend_datalab/src/main/resources/application-local.properties
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# o
java -jar target/app.jar --spring.profiles.active=local
```

## Docker/Compose
Con Docker Compose, las credenciales van en variables de entorno (`.env`). No necesitas guardarlas en propiedades.
