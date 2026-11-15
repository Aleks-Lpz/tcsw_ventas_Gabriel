export interface Producto {
  codigo: string;
  nombre: string;
  precio: number;
  existencia: number;
}

export interface ProductoRequest {
  codigo: string;
  nombre: string;
  precio: number;
  existencia: number;
}