# ProyectoIngSoftware DataLAB - Sistema de Trazabilidad de Genes Polimorfos del Cáncer Gástrico

Sistema integral para la gestión y trazabilidad de participantes en estudios de investigación sobre cáncer gástrico en la región de Ñuble. Permite la administración de casos y controles, levantamiento de encuestas CRF (Case Report Form) digitales, y seguimiento del proceso de reclutamiento.

## 📋 Descripción

Este proyecto es una solución de software completa que incluye backend, frontend y base de datos contenerizada. Su objetivo principal es facilitar la recolección de datos clínicos y epidemiológicos, asegurando la integridad y trazabilidad de la información.

### Funcionalidades Principales
- **Gestión de Usuarios:** Roles de Administrador, Reclutador, Editor, etc.
- **Gestión de Participantes:** Registro y seguimiento de participantes (Casos y Controles).
- **CRF Digital:** Formularios dinámicos para la recolección de datos.
- **Tableros de Control:** Visualización de métricas de reclutamiento en tiempo real.
- **Auditoría:** Registro detallado de acciones realizadas en el sistema.
- **Seguridad:** Autenticación mediante JWT.

## 🛠️ Tecnologías Utilizadas

- **Backend:** Java 17, Spring Boot 3, Maven
- **Frontend:** Angular 16+, TypeScript, Tailwind CSS
- **Base de Datos:** MySQL 8.0
- **DevOps:** Docker, Docker Compose

## 🔧 Estructura del Proyecto

```
ProyectoIngSoftware/
├── backend_datalab/    # API REST con Spring Boot
├── frontend_datalab/   # Cliente Web Angular
├── BDD/                # Scripts SQL
├── docker-compose.yml  # Orquestación
└── ...
```

## ✅ Ejecución de Pruebas

### Backend
```bash
cd backend_datalab
./mvnw test
```

## 📚 Documentación de API

Se incluye una colección de **Postman** en la raíz del proyecto: `Datalab_API_Tests.postman_collection.json`.

## 👥 Credenciales por Defecto (Entorno Local)

- **Admin por defecto:** `admin@datalab.com` / `admin123`
- **Base de Datos:** `datalab_user` / `password_datalab_user`

## 📄 Licencia

Este proyecto es parte de un trabajo universitario de Ingeniería de Software.
