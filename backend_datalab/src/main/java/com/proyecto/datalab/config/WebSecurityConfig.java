package com.proyecto.datalab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // <--- CAMBIO 2
    }

    // Inicializa usuarios ficticios
    @Bean
    public UserDetailsService userDetailsService() {
        
        UserDetails user = User.builder()
            .username("user")
            // Spring tomará "12345", la hasheará con Bcrypt, 
            // y guardará ese hash en memoria.
            .password(passwordEncoder().encode("12345")) // <--- CAMBIO 3
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123")) // <--- CAMBIO 4
            .roles("ADMIN", "USER")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    //acceso a paginas con roles
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                //.requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()//.authenticated()//
            )
            .formLogin(form -> form
                .permitAll() 
            )
            .logout(logout -> logout
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}