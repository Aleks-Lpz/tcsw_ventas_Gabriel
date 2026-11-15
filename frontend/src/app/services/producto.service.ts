import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Producto, ProductoRequest } from '../models/producto.model';
import { extraerMensajeError } from '../models/problema-detalle.model';

@Injectable({
  providedIn: 'root'
})
export class ProductoService {
  private readonly apiUrl = 'http://localhost:8080/productos';

  constructor(private readonly http: HttpClient) {}

  listarProductos(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.apiUrl).pipe(
      catchError((error: HttpErrorResponse) =>
        throwError(() => new Error(extraerMensajeError(error, 'Error al consultar el catálogo de productos.')))
      )
    );
  }

  registrarProducto(req: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.apiUrl, req).pipe(
      catchError((error: HttpErrorResponse) =>
        throwError(() => new Error(extraerMensajeError(error, 'Error al registrar el producto.')))
      )
    );
  }
}