import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Producto } from '../models/producto.model';
import { ItemVentaPeticion, NuevaVentaPeticion } from '../models/venta.model';
import { VentaService } from './venta.service';

export interface PartidaCarrito {
  producto: Producto;
  cantidad: number;
  subtotal: number;
}

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private readonly partidasSubject = new BehaviorSubject<PartidaCarrito[]>([]);
  readonly partidas$: Observable<PartidaCarrito[]> = this.partidasSubject.asObservable();

  constructor(private readonly ventaService: VentaService) {}

  obtenerPartidas(): PartidaCarrito[] {
    return this.partidasSubject.getValue();
  }

  agregarProducto(producto: Producto, cantidad: number): void {
    if (!producto) {
      throw new Error('Debe seleccionar un producto válido.');
    }
    const cantidadEntera = Math.floor(Number(cantidad));
    if (Number.isNaN(cantidadEntera) || cantidadEntera <= 0) {
      throw new Error('La cantidad a agregar debe ser mayor a cero.');
    }

    const actuales = [...this.partidasSubject.getValue()];
    const indice = actuales.findIndex((p) => p.producto.codigo === producto.codigo);

    if (indice >= 0) {
      const nuevaCantidad = actuales[indice].cantidad + cantidadEntera;
      actuales[indice] = {
        producto,
        cantidad: nuevaCantidad,
        subtotal: Number((nuevaCantidad * producto.precio).toFixed(2))
      };
    } else {
      actuales.push({
        producto,
        cantidad: cantidadEntera,
        subtotal: Number((cantidadEntera * producto.precio).toFixed(2))
      });
    }

    this.partidasSubject.next(actuales);
  }

  actualizarCantidad(codigo: string, nuevaCantidad: number): void {
    const cantidadEntera = Math.floor(Number(nuevaCantidad));
    if (Number.isNaN(cantidadEntera) || cantidadEntera <= 0) {
      this.eliminarPartida(codigo);
      return;
    }

    const actuales = this.partidasSubject.getValue().map((partida) => {
      if (partida.producto.codigo === codigo) {
        return {
          ...partida,
          cantidad: cantidadEntera,
          subtotal: Number((cantidadEntera * partida.producto.precio).toFixed(2))
        };
      }
      return partida;
    });

    this.partidasSubject.next(actuales);
  }

  eliminarPartida(codigo: string): void {
    const filtradas = this.partidasSubject
      .getValue()
      .filter((p) => p.producto.codigo !== codigo);
    this.partidasSubject.next(filtradas);
  }

  vaciarCarrito(): void {
    this.partidasSubject.next([]);
  }

  calcularSubtotal(): number {
    const suma = this.partidasSubject
      .getValue()
      .reduce((acc, partida) => acc + partida.subtotal, 0);
    return Number(suma.toFixed(2));
  }

  obtenerCantidadTotalArticulos(): number {
    return this.partidasSubject
      .getValue()
      .reduce((acc, partida) => acc + partida.cantidad, 0);
  }

  construirPeticionVenta(): NuevaVentaPeticion {
    const partidas: ItemVentaPeticion[] = this.partidasSubject.getValue().map((p) => ({
      productoId: this.ventaService.extraerProductoId(p.producto.codigo),
      cantidad: p.cantidad
    }));
    return { partidas };
  }
}