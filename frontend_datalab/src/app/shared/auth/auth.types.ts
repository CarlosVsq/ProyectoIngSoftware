export interface PermisosDTO {
  puedeCrudCrf: boolean;
  puedeExportar: boolean;
  puedeReclutar: boolean;
  puedeAdministrarUsuarios: boolean;
  puedeVerAuditoria: boolean;
}

export interface UsuarioDTO {
  idUsuario: number;
  nombreCompleto: string;
  correo: string;
  rol: string;
  estado: string;
  permisos: PermisosDTO;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  usuario: UsuarioDTO;
}
