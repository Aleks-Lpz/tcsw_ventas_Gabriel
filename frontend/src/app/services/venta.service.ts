import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NuevaVentaPeticion, VentaRespuesta } from '../models/venta.model';
import { extraerMensajeError } from '../models/problema-detalle.model';

@Injectable({
  providedIn: 'root'
})
export class VentaService {
  private readonly apiUrl = 'http://localhost:8080/ventas';

  constructor(private readonly http: HttpClient) {}

  registrarVenta(peticion: NuevaVentaPeticion): Observable<VentaRespuesta> {
    return this.http.post<VentaRespuesta>(this.apiUrl, peticion).pipe(
      catchError((error: HttpErrorResponse) =>
        throwError(() => new Error(extraerMensajeError(error, 'Error al registrar la operación de venta.')))
      )
    );
  }

  /**
   * Convierte códigos de catálogo ('P001' -> 1, '101' -> 101) al productoId numérico
   * requerido por el esquema OpenAPI / VentaRestMapper del backend.
   */
  extraerProductoId(codigo: string): number {
    const soloDigitos = codigo.replace(/\D+/g, '');
    const id = Number.parseInt(soloDigitos, 10);
    if (Number.isNaN(id)) {
      throw new Error(`El código de producto '${codigo}' no contiene un identificador numérico válido.`);
    }
    return id;
  }
}