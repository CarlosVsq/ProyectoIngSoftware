package com.proyecto.datalab.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.datalab.entity.Rol;
import com.proyecto.datalab.entity.Usuario;
import com.proyecto.datalab.enums.EstadoUsuario;
import com.proyecto.datalab.repository.RolRepository;
import com.proyecto.datalab.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
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
        //String contrasenaHasheada = passwordEncoder.encode(contrasena);

        // 4. Crear la entidad
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(nombre);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setContrasenia(passwordEncoder.encode(contrasena));
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setEstado(EstadoUsuario.ACTIVO);

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


