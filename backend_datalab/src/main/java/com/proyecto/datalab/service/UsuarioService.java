package com.proyecto.datalab.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.proyecto.datalab.dto.UsuarioUpdateRequest;
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

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional
    public Usuario crearUsuario(String nombre, String correo, String contrasena, Long rolId) {
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        //String contrasenaHasheada = passwordEncoder.encode(contrasena);

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(nombre);
        nuevoUsuario.setCorreo(correo);
        nuevoUsuario.setContrasenia(passwordEncoder.encode(contrasena));
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setEstado(EstadoUsuario.ACTIVO);

        return usuarioRepository.save(nuevoUsuario);
    }

    
    @Transactional
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional
    public void borrarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public Usuario actualizarUsuario(Long id, @RequestBody UsuarioUpdateRequest datos) {
        
        //Buscar al usuario existente
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        if (datos.getCorreo() != null && !datos.getCorreo().isEmpty()) {
            // Verifica que el nuevo correo no esté en uso por OTRO usuario
            Optional<Usuario> existenteConCorreo = usuarioRepository.findByCorreo(datos.getCorreo());
            if (existenteConCorreo.isPresent() && !existenteConCorreo.get().getIdUsuario().equals(id)) {
                throw new RuntimeException("El correo ya está en uso por otro usuario");
            }
            usuario.setCorreo(datos.getCorreo());
        }

        if (datos.getNombre() != null && !datos.getNombre().isEmpty()) {
        System.out.println(datos.getNombre());
            usuario.setNombreCompleto(datos.getNombre());
        }

        if (datos.getRolId() != null) {
            Rol rol = rolRepository.findById(datos.getRolId())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
            usuario.setRol(rol);
        }

        if (datos.getContrasena() != null && !datos.getContrasena().isEmpty()) {
            // Siempre hashear la nueva contraseña
            usuario.setContrasenia(passwordEncoder.encode(datos.getContrasena()));
        }
        return usuarioRepository.save(usuario);
    }
}


