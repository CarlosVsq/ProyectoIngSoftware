import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
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
  isSubmitting = false;

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
    this.rolService.listarRoles().subscribe(roles => this.roles = roles);
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
    this.isSubmitting = true;
    this.usuarioService.crearUsuario(this.userForm.value).subscribe({
      next: () => {
        alert('Usuario creado exitosamente');
        this.isSubmitting = false;
        this.cerrarModalUsuario();
        this.loadData();
      },
      error: (err) => {
        alert('Error al crear usuario');
        this.isSubmitting = false;
      }
    });
  }

  eliminarUsuario(id: number) {
    if (!confirm('¿Estás seguro de eliminar este usuario?')) return;
    this.usuarioService.borrarUsuario(id).subscribe(() => this.loadData());
  }

  // Permissions Management
  togglePermiso(rol: Rol, permiso: keyof Rol) {
    // Because simple boolean toggle might not trigger change detection deeply or reference update
    // We update the rol object and send it
    // @ts-ignore
    rol[permiso] = !rol[permiso];

    this.rolService.actualizarPermisos(rol).subscribe({
      next: (updatedRol) => {
        console.log('Rol actualizado', updatedRol);
      },
      error: () => {
        alert('No se pudo actualizar el permiso');
        // Revert?
        // @ts-ignore
        rol[permiso] = !rol[permiso];
      }
    });
  }
}

