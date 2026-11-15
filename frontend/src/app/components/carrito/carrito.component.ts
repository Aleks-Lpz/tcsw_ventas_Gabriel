import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { CarritoService, PartidaCarrito } from '../../services/carrito.service';
import { VentaService } from '../../services/venta.service';
import { VentaRespuesta } from '../../models/venta.model';

@Component({
  selector: 'app-carrito',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './carrito.component.html',
  styleUrl: './carrito.component.css'
})
export class CarritoComponent implements OnInit, OnDestroy {
  partidas: PartidaCarrito[] = [];
  subtotal = 0;
  totalArticulos = 0;

  // Estados explícitos de la operación de venta (M13)
  procesandoVenta = false;
  errorVenta: string | null = null;
  reciboVenta: VentaRespuesta | null = null;

  @Output() ventaConfirmada = new EventEmitter<VentaRespuesta>();

  private suscripcion?: Subscription;

  constructor(
    private readonly carritoService: CarritoService,
    private readonly ventaService: VentaService
  ) {}

  ngOnInit(): void {
    this.suscripcion = this.carritoService.partidas$.subscribe((lista) => {
      this.partidas = lista;
      this.subtotal = this.carritoService.calcularSubtotal();
      this.totalArticulos = this.carritoService.obtenerCantidadTotalArticulos();
    });
  }

  ngOnDestroy(): void {
    this.suscripcion?.unsubscribe();
  }

  cambiarCantidad(codigo: string, valor: number | string): void {
    this.errorVenta = null;
    this.carritoService.actualizarCantidad(codigo, Number(valor));
  }

  quitarPartida(codigo: string): void {
    this.errorVenta = null;
    this.carritoService.eliminarPartida(codigo);
  }

  limpiarCarrito(): void {
    this.errorVenta = null;
    this.carritoService.vaciarCarrito();
  }

  confirmarVenta(): void {
    this.errorVenta = null;
    this.reciboVenta = null;

    if (this.partidas.length === 0) {
      this.errorVenta = 'No se puede registrar una venta sin partidas en el carrito.';
      return;
    }

    const peticion = this.carritoService.construirPeticionVenta();
    this.procesandoVenta = true;

    this.ventaService.registrarVenta(peticion).subscribe({
      next: (respuesta) => {
        this.procesandoVenta = false;
        this.reciboVenta = respuesta;
        this.carritoService.vaciarCarrito();
        this.ventaConfirmada.emit(respuesta);
      },
      error: (err: Error) => {
        this.procesandoVenta = false;
        // Conserva las partidas del carrito intactas y hace visible ProblemaDetalle.detail
        this.errorVenta = err.message || 'Ocurrió un error al registrar la venta.';
      }
    });
  }
}