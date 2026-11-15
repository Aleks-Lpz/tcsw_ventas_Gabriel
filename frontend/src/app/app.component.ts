import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CatalogoComponent, SeleccionProductoEvento } from './components/catalogo/catalogo.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, CatalogoComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'Cliente Angular - Sistema de Ventas';
  ultimoAgregado: SeleccionProductoEvento | null = null;

  alAgregarAlCarrito(evento: SeleccionProductoEvento): void {
    this.ultimoAgregado = evento;
  }
}