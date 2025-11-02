import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.scss'],
})
export class SidebarComponent {
  /** true: visible (off-canvas); false: oculto */
  @Input({ required: true }) open = true;
  /** true: modo mini (icon-only) en ≥lg; ignorado en móvil */
  @Input() mini = false;

  @Output() openChange = new EventEmitter<boolean>();
  @Output() miniChange = new EventEmitter<boolean>();

  close() { this.openChange.emit(false); }
  openMenu(){ this.openChange.emit(true); }
  toggle(){ this.openChange.emit(!this.open); }

  toggleMini() { this.miniChange.emit(!this.mini); }
}
