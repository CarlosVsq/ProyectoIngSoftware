# Análisis de Patrones de Diseño - Datalab

Este documento identifica y describe los patrones de diseño implementados en el proyecto Datalab para la investigación de cáncer de páncreas.

## Resumen Ejecutivo

El proyecto implementa una arquitectura basada en **Spring Boot** (backend) y **Angular** (frontend), siguiendo principios SOLID y diversos patrones de diseño que garantizan la mantenibilidad, escalabilidad y calidad del código.

---

## Patrones de Diseño Implementados

### 1. MVC (Model-View-Controller) - Arquitectónico

**Ubicación**: Arquitectura general del proyecto

**Descripción**: El proyecto separa claramente las responsabilidades en tres capas:

- **Model (Modelo)**:
  - Entidades JPA: `Usuario`, `Participante`, `Variable`, `Respuesta`, `Auditoria`, `Rol`
  - Ubicación: `backend_datalab/src/main/java/com/proyecto/datalab/entity/`

- **View (Vista)**:
  - Componentes Angular en el frontend
  - Ubicación: `frontend_datalab/src/app/features/`
  - Ejemplos: `login.html`, `dashboard.html`, `reclutamiento.html`

- **Controller (Controlador)**:
  - Controladores REST: `UsuarioController`, `ParticipanteController`, `VariableController`, `RespuestaController`, `AuthController`
  - Ubicación: `backend_datalab/src/main/java/com/proyecto/datalab/controller/`

**Beneficios**:
- Separación clara de responsabilidades
- Facilita el testing unitario
- Permite desarrollo paralelo de frontend y backend

---

### 2. Repository Pattern - Persistencia

**Ubicación**:
- `backend_datalab/src/main/java/com/proyecto/datalab/repository/`

**Clases implementadas**:
- `UsuarioRepository`
- `ParticipanteRepository`
- `VariableRepository`
- `RespuestaRepository`
- `AuditoriaRepository`
- `RolRepository`

**Descripción**:
- Abstrae el acceso a datos mediante interfaces que extienden `JpaRepository`
- Separa la lógica de negocio de la lógica de persistencia
- Utiliza Spring Data JPA para implementación automática

**Ejemplo**:
```java
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCorreo(String correo);
}
```

**Beneficios**:
- Centraliza la lógica de acceso a datos
- Facilita el cambio de tecnología de persistencia
- Simplifica el testing con mocks

---

### 3. Service Layer Pattern - Lógica de Negocio

**Ubicación**:
- `backend_datalab/src/main/java/com/proyecto/datalab/service/`

**Clases implementadas**:
- `UsuarioService`
- `ParticipanteService`
- `VariableService`
- `RespuestaService`
- `AuditoriaService`
- `JwtService`
- `AuthService`

**Descripción**:
- Capa intermedia entre controladores y repositorios
- Contiene la lógica de negocio de la aplicación
- Maneja transacciones con `@Transactional`
- Orquesta llamadas a múltiples repositorios

**Ejemplo**:
```java
@Service
public class UsuarioService {
    @Transactional
    public Usuario crearUsuario(String nombre, String correo, String contrasena, Integer rolId) {
        // Validación de negocio: verificar correo duplicado
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }
        // Lógica de negocio adicional...
    }
}
```

**Beneficios**:
- Centraliza lógica de negocio reutilizable
- Facilita pruebas unitarias aisladas
- Mejora la legibilidad del código

---

### 4. DTO (Data Transfer Object) - Transferencia de Datos

**Ubicación**:
- `backend_datalab/src/main/java/com/proyecto/datalab/dto/`
- `backend_datalab/src/main/java/com/proyecto/datalab/web/dto/`

**Clases implementadas**:
- Request DTOs: `UsuarioCreateRequest`, `UsuarioUpdateRequest`, `ParticipanteCreateRequest`, `VariableCreateRequest`, `LoginRequest`, `RegisterRequest`
- Response DTOs: `AuthResponse`, `UsuarioDTO`, `PermisosDTO`, `ApiResponse`

**Descripción**:
- Objetos especializados para transferencia de datos entre capas
- Evita exponer entidades del dominio directamente
- Permite validación de entrada con `@Valid`

**Ejemplo**:
```java
public class UsuarioCreateRequest {
    private String nombre;
    private String correo;
    private String contrasena;
    private Integer rolId;
}
```

**Beneficios**:
- Desacopla la capa de presentación del modelo de dominio
- Permite validaciones específicas por operación
- Mejora la seguridad (evita mass assignment)

---

### 5. Dependency Injection (DI) - Inversión de Control

**Ubicación**: Todo el proyecto (Spring Framework)

**Descripción**:
- Spring gestiona automáticamente la creación y ciclo de vida de los beans
- Inyección mediante constructores (práctica recomendada)

**Ejemplos**:
```java
@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    // Constructor injection
    public UsuarioService(UsuarioRepository usuarioRepository,
                         RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }
}
```

```java
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;
}
```

**Beneficios**:
- Reduce acoplamiento entre clases
- Facilita testing con mocks
- Mejora la mantenibilidad

---

### 6. Builder Pattern - Construcción de Objetos

**Ubicación**:
- Entidades con Lombok: `Usuario`, `Rol`
- DTOs: `UsuarioDTO`, `PermisosDTO`, `AuthResponse`

**Descripción**:
- Utiliza anotación `@Builder` de Lombok
- Facilita la creación de objetos complejos

**Ejemplo**:
```java
@Entity
@Builder
public class Usuario implements UserDetails {
    // ...
}

// Uso
Usuario usuario = Usuario.builder()
    .nombreCompleto("Dr. Juan Pérez")
    .correo("juan@hospital.com")
    .rol(rol)
    .estado(EstadoUsuario.ACTIVO)
    .build();
```

**Beneficios**:
- Código más legible
- Permite construcción paso a paso
- Inmutabilidad opcional

---

### 7. Strategy Pattern - Algoritmos Intercambiables

**Ubicación**:
- `SecurityConfig` - Configuración de seguridad
- Manejo de diferentes roles y permisos

**Descripción**:
- Spring Security permite definir diferentes estrategias de autenticación
- Los métodos de `Usuario` (`puedeCrudCrf()`, `puedeExportar()`, etc.) implementan estrategias de permisos

**Ejemplo**:
```java
public class Usuario implements UserDetails {
    public boolean puedeCrudCrf() {
        return rol != null && rol.puedeCrudCrf();
    }

    public boolean puedeExportar() {
        return rol != null && rol.puedeExportar();
    }
}
```

**Beneficios**:
- Flexibilidad para cambiar comportamiento en tiempo de ejecución
- Facilita agregar nuevos roles/permisos

---

### 8. Template Method Pattern - Flujo Algorítmico

**Ubicación**:
- Callbacks de JPA: `@PrePersist` en entidades

**Descripción**:
- Define esqueleto de algoritmo en clase base
- JPA llama automáticamente a métodos anotados

**Ejemplo**:
```java
@Entity
public class Usuario {
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoUsuario.ACTIVO;
        }
    }
}
```

**Beneficios**:
- Garantiza inicialización correcta de entidades
- Centraliza lógica común

---

### 9. Observer Pattern - Eventos y Notificaciones

**Ubicación**:
- Sistema de auditoría: `AuditoriaService`

**Descripción**:
- El servicio de auditoría "observa" cambios en el sistema
- Se invoca cuando ocurren eventos importantes (crear, actualizar, borrar)

**Ejemplo**:
```java
@Service
public class ParticipanteService {
    public Participante crearParticipante(...) {
        Participante participanteGuardado = participanteRepository.save(p);

        // Notificar al observador (AuditoriaService)
        auditoriaService.registrarAccion(
            reclutador,
            participanteGuardado,
            "CREAR",
            "Participante",
            "Se creó el participante ID: " + participanteGuardado.getIdParticipante()
        );

        return participanteRepository.save(participanteGuardado);
    }
}
```

**Beneficios**:
- Cumple con HU-11: Bitácora de cambios
- Desacopla lógica de auditoría de lógica de negocio

---

### 10. Singleton Pattern - Instancia Única

**Ubicación**:
- Todos los beans de Spring (`@Service`, `@Controller`, `@Repository`)

**Descripción**:
- Por defecto, Spring crea una única instancia de cada bean
- Gestionado automáticamente por el contenedor IoC

**Ejemplo**:
```java
@Service // Singleton por defecto
public class UsuarioService {
    // Spring garantiza una única instancia
}
```

**Beneficios**:
- Ahorro de memoria
- Estado compartido cuando es necesario
- Thread-safe gestionado por Spring

---

### 11. Chain of Responsibility - Cadena de Filtros

**Ubicación**:
- `JwtAuthenticationFilter` - Filtros de seguridad
- Spring Security filter chain

**Descripción**:
- Solicitudes HTTP pasan por una cadena de filtros
- Cada filtro puede procesar o pasar al siguiente

**Ejemplo**:
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
        // Procesar JWT
        // ...

        // Pasar al siguiente filtro
        filterChain.doFilter(request, response);
    }
}
```

**Beneficios**:
- Flexibilidad para agregar/quitar filtros
- Separación de responsabilidades

---

### 12. Factory Pattern - Creación de Objetos

**Ubicación**:
- Spring Data JPA genera implementaciones de repositorios automáticamente
- BCrypt para encriptación de contraseñas

**Descripción**:
- Spring actúa como factory para crear instancias de interfaces
- BCryptPasswordEncoder crea hashes de contraseñas

**Ejemplo**:
```java
@Service
public class UsuarioService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Usuario crearUsuario(...) {
        usuario.setContrasenia(passwordEncoder.encode(contrasena));
        // ...
    }
}
```

**Beneficios**:
- Oculta complejidad de creación
- Permite cambiar implementación fácilmente

---

### 13. Adapter Pattern - Adaptación de Interfaces

**Ubicación**:
- `Usuario implements UserDetails` - Adaptador para Spring Security

**Descripción**:
- La entidad `Usuario` adapta la interfaz `UserDetails` requerida por Spring Security

**Ejemplo**:
```java
@Entity
public class Usuario implements UserDetails {
    @Override
    public String getUsername() {
        return this.correo; // Adapta correo a username
    }

    @Override
    public String getPassword() {
        return this.contrasenia; // Adapta contrasenia a password
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.getNombreRol()));
    }
}
```

**Beneficios**:
- Integración con frameworks externos
- Mantiene el modelo de dominio limpio

---

### 14. REST API Pattern - Arquitectura de Servicios

**Ubicación**:
- Todos los controladores REST

**Descripción**:
- Endpoints RESTful con verbos HTTP apropiados
- Recursos claramente definidos
- Códigos de estado HTTP semánticos

**Ejemplo**:
```java
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @GetMapping          // GET - Listar
    @GetMapping("/{id}") // GET - Obtener uno
    @PostMapping         // POST - Crear (201 Created)
    @PutMapping("/{id}") // PUT - Actualizar (200 OK)
    @DeleteMapping("/{id}") // DELETE - Borrar (204 No Content)
}
```

**Beneficios**:
- Interfaz uniforme y predecible
- Facilita consumo desde frontend
- Escalabilidad y cacheo

---

### 15. Global Exception Handling - Manejo Centralizado de Errores

**Ubicación**:
- `GlobalExceptionHandler` con `@RestControllerAdvice`

**Descripción**:
- Maneja excepciones de forma centralizada
- Retorna respuestas consistentes con `ApiResponse`

**Ejemplo**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }
}
```

**Beneficios**:
- Respuestas de error consistentes
- Código de controllers más limpio
- Logging centralizado

---

## Principios SOLID Aplicados

### Single Responsibility Principle (SRP)
- Cada clase tiene una única responsabilidad
- Ejemplo: `UsuarioService` solo gestiona usuarios, `AuditoriaService` solo auditoría

### Open/Closed Principle (OCP)
- Abierto para extensión, cerrado para modificación
- Ejemplo: Nuevos roles se pueden agregar sin modificar código existente

### Liskov Substitution Principle (LSP)
- Subtipos pueden sustituir a tipos base
- Ejemplo: `Usuario implements UserDetails` puede usarse donde se espera `UserDetails`

### Interface Segregation Principle (ISP)
- Interfaces específicas en lugar de generales
- Ejemplo: Repositorios extienden `JpaRepository` con métodos específicos

### Dependency Inversion Principle (DIP)
- Dependencias hacia abstracciones, no implementaciones concretas
- Ejemplo: Controllers dependen de Service (abstracción), no de Repository directamente

---

## Conclusiones

El proyecto Datalab implementa una arquitectura robusta basada en patrones de diseño bien establecidos:

1. **Arquitectura en capas** (MVC + Service + Repository) garantiza separación de responsabilidades
2. **Patrones de creación** (Builder, Factory, DI) facilitan la construcción de objetos
3. **Patrones estructurales** (Adapter, DTO) permiten integración con frameworks
4. **Patrones de comportamiento** (Strategy, Observer, Chain of Responsibility) proporcionan flexibilidad
5. **Principios SOLID** aseguran código mantenible y extensible

Esta arquitectura es apropiada para un proyecto de investigación médica que requiere:
- **Auditoría completa** (HU-11) - Observer Pattern
- **Control de acceso** (HU-03) - Strategy Pattern
- **Escalabilidad** - Repository Pattern
- **Mantenibilidad** - Separación de capas
- **Testing** - Dependency Injection

---

**Documento generado para**: Proyecto Universitario de Ingeniería de Software
**Rama**: testing
**Fecha**: 2024
