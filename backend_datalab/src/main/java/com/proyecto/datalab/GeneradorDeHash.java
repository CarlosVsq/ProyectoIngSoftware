package com.proyecto.datalab;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneradorDeHash {
    
    public static void main(String[] args) {
        // --- CAMBIA ESTA CONTRASEÑA ---
        String contraseniaPlana = "administradorEstudiantesDAtalab"; 
        
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hash = passwordEncoder.encode(contraseniaPlana);
        
        System.out.println("Tu hash es:");
        System.out.println(hash);
    }
}