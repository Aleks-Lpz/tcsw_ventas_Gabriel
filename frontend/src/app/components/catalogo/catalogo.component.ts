import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Producto, ProductoRequest } from '../../models/producto.model';
import { ProductoService } from '../../services/producto.service';

export interface SeleccionProductoEvento {
  producto: Producto;
  cantidad: number;
}

@Component({
  selector: 'app-catalogo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './catalogo.component.html',
  styleUrl: './catalogo.component.css'
})
export class CatalogoComponent implements OnInit {
  productos: Producto[] = [];
  cantidadesSeleccionadas: Record<string, number> = {};

  // Estados explícitos de la consulta del catálogo (M13)
  cargandoCatalogo = false;
  errorCatalogo: string | null = null;

  // Estados explícitos del formulario de registro de producto
  formularioProducto: FormGroup;
  registrandoProducto = false;
  errorRegistro: string | null = null;
  mensajeExitoRegistro: string | null = null;

  @Output() agregarAlCarrito = new EventEmitter<SeleccionProductoEvento>();

  constructor(
    private readonly productoService: ProductoService,
    private readonly fb: FormBuilder
  ) {
    this.formularioProducto = this.fb.group({
      codigo: ['', [Validators.required, Validators.pattern(/\S+/)]],
      nombre: ['', [Validators.required, Validators.pattern(/\S+/)]],
      precio: [null, [Validators.required, Validators.min(0.01)]],
      existencia: [0, [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.cargarCatalogo();
  }

  cargarCatalogo(): void {
    this.cargandoCatalogo = true;
    this.errorCatalogo = null;

    this.productoService.listarProductos().subscribe({
      next: (lista) => {
        this.productos = lista;
        this.inicializarCantidades(lista);
        this.cargandoCatalogo = false;
      },
      error: (err: Error) => {
        this.errorCatalogo = err.message || 'Error al cargar el catálogo de productos.';
        this.cargandoCatalogo = false;
      }
    });
  }

  private inicializarCantidades(lista: Producto[]): void {
    for (const prod of lista) {
      if (this.cantidadesSeleccionadas[prod.codigo] === undefined) {
        this.cantidadesSeleccionadas[prod.codigo] = prod.existencia > 0 ? 1 : 0;
      } else if (prod.existencia === 0) {
        this.cantidadesSeleccionadas[prod.codigo] = 0;
      } else if (this.cantidadesSeleccionadas[prod.codigo] > prod.existencia) {
        this.cantidadesSeleccionadas[prod.codigo] = prod.existencia;
      }
    }
  }

  obtenerCantidad(codigo: string): number {
    return this.cantidadesSeleccionadas[codigo] ?? 1;
  }

  actualizarCantidad(producto: Producto, valor: string | number): void {
    const numero = Number(valor);
    if (Number.isNaN(numero) || numero < 1) {
      this.cantidadesSeleccionadas[producto.codigo] = 1;
      return;
    }
    if (producto.existencia > 0 && numero > producto.existencia) {
      this.cantidadesSeleccionadas[producto.codigo] = producto.existencia;
      return;
    }
    this.cantidadesSeleccionadas[producto.codigo] = Math.floor(numero);
  }

  seleccionarParaCarrito(producto: Producto): void {
    if (producto.existencia <= 0) {
      return;
    }
    const cantidad = this.obtenerCantidad(producto.codigo);
    if (cantidad < 1 || cantidad > producto.existencia) {
      return;
    }
    this.agregarAlCarrito.emit({ producto, cantidad });
  }

  registrarNuevoProducto(): void {
    this.errorRegistro = null;
    this.mensajeExitoRegistro = null;

    if (this.formularioProducto.invalid) {
      this.formularioProducto.markAllAsTouched();
      return;
    }

    const valores = this.formularioProducto.value;
    const peticion: ProductoRequest = {
      codigo: String(valores.codigo).trim(),
      nombre: String(valores.nombre).trim(),
      precio: Number(valores.precio),
      existencia: Number(valores.existencia)
    };

    this.registrandoProducto = true;
    this.productoService.registrarProducto(peticion).subscribe({
      next: (creado) => {
        this.registrandoProducto = false;
        this.mensajeExitoRegistro = `Producto '${creado.nombre}' (${creado.codigo}) registrado con éxito.`;
        this.formularioProducto.reset({
          codigo: '',
          nombre: '',
          precio: null,
          existencia: 0
        });
        this.cargarCatalogo();
      },
      error: (err: Error) => {
        this.registrandoProducto = false;
        this.errorRegistro = err.message || 'No se pudo registrar el producto.';
      }
    });
  }

  campoInvalido(campo: string): boolean {
    const control = this.formularioProducto.get(campo);
    return Boolean(control && control.invalid && (control.dirty || control.touched));
  }
}