# Dockerización rápida (multi-contenedor)

Servicios:
- `db`: MySQL 8 con volumen persistente
- `app`: Spring Boot (Java 17) empaquetado con Maven
- `adminer`: GUI liviana para consultar la base (`http://localhost:8081`)

## Requisitos
- Docker y Docker Compose v2 instalados

## Pasos
```bash
# 1) Estar en la raíz del proyecto
cd ProyectoIngSoftware-main

# 2) (Opcional) crear un archivo .env con tus variables
cp .env.example .env

# 3) Levantar el stack
docker compose up --build -d

# 4) Ver logs del backend
docker compose logs -f app

# 5) Abrir Adminer
#   servidor: db | usuario: MYSQL_USER | pass: MYSQL_PASSWORD | base: MYSQL_DATABASE
http://localhost:8081

# 6) Detener
docker compose down

# 7) Borrar datos (¡destructivo!)
docker compose down -v
```

> La app se expone en `http://localhost:${SERVER_PORT:-8080}`.
> Ajusta variables en `.env` o directamente en `docker-compose.yml`.
