export interface ItemVentaPeticion {
  productoId: number;
  cantidad: number;
}

export interface NuevaVentaPeticion {
  partidas: ItemVentaPeticion[];
}

export interface PartidaRespuesta {
  productoId: number;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface VentaRespuesta {
  id: number;
  fecha: string;
  descuento: number;
  total: number;
  partidas: PartidaRespuesta[];
}