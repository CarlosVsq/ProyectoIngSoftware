package com.proyecto.datalab.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.datalab.web.dto.common.ApiResponse;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@RestController
@RequestMapping("/api/password")
public class PasswordController {

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<String>> forgot(@RequestBody ForgotRequest request) {
        // Placeholder: aquí se generaría token y se enviaría email
        return ResponseEntity.ok(ApiResponse.success("Si el correo existe, recibirás un enlace de recuperación."));
    }

    @Data
    public static class ForgotRequest {
        @NotBlank
        @Email
        private String correo;
    }
}
