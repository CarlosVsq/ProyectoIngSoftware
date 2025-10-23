package com.proyecto.datalab.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.datalab.entidades.Rol;
import com.proyecto.datalab.entidades.Usuario;
import com.proyecto.datalab.enums.EstadoUsuario;
import com.proyecto.datalab.repositorio.RolRepository;
import com.proyecto.datalab.repositorio.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario crearUsuario(String nombre, String correo, String contrasena, Long rolId) {
        // 1. Lógica de negocio: Verificar si el correo ya existe
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 2. Lógica de negocio: Buscar el rol
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        // 3. Lógica de negocio: Hashear la contraseña
        String contrasenaHasheada = passwordEncoder.encode(contrasena);

        // 4. Crear la entidad
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(nombre);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setContrasenia(contrasenaHasheada);
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setEstado(EstadoUsuario.Activo);

        // 5. Guardar en la base de datos (CRUD)
        return usuarioRepository.save(nuevoUsuario);
    }

    
    @Transactional //(readOnly = true)
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional //(readOnly = true)
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public void borrarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }
}


