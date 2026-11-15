import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CatalogoComponent, SeleccionProductoEvento } from './components/catalogo/catalogo.component';
import { CarritoComponent } from './components/carrito/carrito.component';
import { CarritoService } from './services/carrito.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, CatalogoComponent, CarritoComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'Cliente Angular - Sistema de Ventas';

  @ViewChild(CatalogoComponent) catalogoComponent?: CatalogoComponent;

  constructor(private readonly carritoService: CarritoService) {}

  alAgregarAlCarrito(evento: SeleccionProductoEvento): void {
    this.carritoService.agregarProducto(evento.producto, evento.cantidad);
  }

  alConfirmarVenta(): void {
    this.catalogoComponent?.cargarCatalogo();
  }
}