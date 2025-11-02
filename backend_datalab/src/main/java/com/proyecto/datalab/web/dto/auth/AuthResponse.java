package com.proyecto.datalab.web.dto.auth;

import com.proyecto.datalab.web.dto.user.UsuarioDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UsuarioDTO usuario;
    
    @Builder.Default
    private String type = "Bearer";
}
