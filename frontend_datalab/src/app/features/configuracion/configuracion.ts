import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs'; // Import forkJoin
import { AlertPanelComponent } from '../../alert-panel/alert-panel.component';
import { LogoutPanelComponent } from '../../shared/logout-panel/logout-panel.component';
import { AuthService } from '../../shared/auth/auth.service';
import { UsuarioService, Usuario } from '../../shared/usuarios/usuario.service';
import { RolService, Rol } from '../../shared/roles/rol.service';

@Component({
  selector: 'app-configuracion',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, AlertPanelComponent, LogoutPanelComponent],
  templateUrl: './configuracion.html',
  styleUrls: ['./configuracion.scss']
})
export class ConfiguracionComponent implements OnInit {
  usuarioNombre = '';
  usuarioRol = '';

  usuarios: Usuario[] = [];
  roles: Rol[] = [];

  // Modal State
  showUserModal = false;
  userForm: FormGroup;
  savingUser = false;
  savingRoles = false;
  rolesDirty = false;

  @ViewChild(LogoutPanelComponent)
  logoutPanel!: LogoutPanelComponent;

  constructor(
    private auth: AuthService,
    private usuarioService: UsuarioService,
    private rolService: RolService,
    private fb: FormBuilder
  ) {
    this.usuarioNombre = this.auth.getUserName();
    this.usuarioRol = this.auth.getUserRole();

    this.userForm = this.fb.group({
      nombre: ['', Validators.required],
      correo: ['', [Validators.required, Validators.email]],
      contrasena: ['', [Validators.required, Validators.minLength(6)]],
      rolId: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.usuarioService.listarUsuarios().subscribe(users => this.usuarios = users);
    this.rolService.listarRoles().subscribe(roles => {
      this.roles = roles;
      this.rolesDirty = false;
    });
  }

  abrirLogoutPanel() {
    this.logoutPanel.showPanel();
  }

  // User Management
  abrirModalUsuario() {
    this.userForm.reset();
    this.showUserModal = true;
  }

  cerrarModalUsuario() {
    this.showUserModal = false;
  }

  guardarUsuario() {
    if (this.userForm.invalid) return;
    this.savingUser = true;
    this.usuarioService.crearUsuario(this.userForm.value).subscribe({
      next: () => {
        alert('Usuario creado exitosamente');
        this.savingUser = false;
        this.cerrarModalUsuario();
        this.loadData();
      },
      error: (err) => {
        alert('Error al crear usuario');
        this.savingUser = false;
      }
    });
  }

  eliminarUsuario(id: number) {
    if (!confirm('¿Estás seguro de eliminar este usuario?')) return;
    this.usuarioService.borrarUsuario(id).subscribe(() => this.loadData());
  }

  toggleEstadoUsuario(user: Usuario) {
    if (!user.idUsuario) return;
    const action = user.estado === 'ACTIVO' ? 'bloquear' : 'desbloquear';
    if (!confirm(`¿Estás seguro de ${action} a ${user.nombreCompleto}?`)) return;

    this.usuarioService.cambiarEstado(user.idUsuario).subscribe({
      next: () => {
        alert(`Usuario ${action === 'bloquear' ? 'bloqueado' : 'desbloqueado'} exitosamente`);
        this.loadData();
      },
      error: () => alert('Error al cambiar el estado del usuario')
    });
  }

  // Permissions Management
  togglePermiso(rol: Rol, permiso: keyof Rol) {
    // @ts-ignore
    rol[permiso] = !rol[permiso];
    this.rolesDirty = true;
  }

  guardarRoles() {
    if (!this.rolesDirty || this.savingRoles) return;

    this.savingRoles = true;
    forkJoin(this.roles.map(rol => this.rolService.actualizarPermisos(rol))).subscribe({
      next: () => {
        alert('Permisos actualizados exitosamente');
        this.rolesDirty = false;
        this.savingRoles = false;
      },
      error: () => {
        alert('Error al actualizar uno o más permisos');
        this.savingRoles = false;
        this.loadData(); // Revert to server state on error
      }
    });
  }
}
