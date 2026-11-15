import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { VentaService } from './venta.service';
import { NuevaVentaPeticion, VentaRespuesta } from '../models/venta.model';
import { ProblemaDetalle } from '../models/problema-detalle.model';

describe('VentaService', () => {
  let service: VentaService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/ventas';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [VentaService]
    });
    service = TestBed.inject(VentaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe registrar una venta válida y retornar VentaRespuesta (201 Created)', () => {
    const peticion: NuevaVentaPeticion = {
      partidas: [
        { productoId: 1, cantidad: 2 },
        { productoId: 2, cantidad: 1 }
      ]
    };

    const mockRespuesta: VentaRespuesta = {
      id: 10,
      fecha: '2026-11-15T15:00:00Z',
      descuento: 0,
      total: 93.8,
      partidas: [
        { productoId: 1, nombre: 'Jabón Roma', cantidad: 2, precioUnitario: 25.5, subtotal: 51.0 },
        { productoId: 2, nombre: 'Leche Lala +Proteína', cantidad: 1, precioUnitario: 42.8, subtotal: 42.8 }
      ]
    };

    service.registrarVenta(peticion).subscribe((respuesta) => {
      expect(respuesta.id).toBe(10);
      expect(respuesta.total).toBe(93.8);
      expect(respuesta.partidas.length).toBe(2);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(peticion);
    req.flush(mockRespuesta, { status: 201, statusText: 'Created' });
  });

  it('debe extraer error.error.detail cuando ocurre un 400 Bad Request en POST /ventas', () => {
    const peticionVacia: NuevaVentaPeticion = { partidas: [] };
    const problema400: ProblemaDetalle = {
      type: 'https://ejemplo.invalid/problemas/entrada-invalida',
      title: 'Entrada inválida',
      status: 400,
      detail: 'partidas: la venta debe tener al menos una partida'
    };

    let errorRecibido = '';
    service.registrarVenta(peticionVacia).subscribe({
      next: () => fail('Debería fallar con 400 Bad Request'),
      error: (err: Error) => {
        errorRecibido = err.message;
      }
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(problema400, { status: 400, statusText: 'Bad Request' });

    expect(errorRecibido).toBe('partidas: la venta debe tener al menos una partida');
  });

  it('debe extraer error.error.detail cuando ocurre un 409 Conflict por existencia insuficiente', () => {
    const peticionExcedida: NuevaVentaPeticion = {
      partidas: [{ productoId: 1, cantidad: 999 }]
    };

    const problema409: ProblemaDetalle = {
      type: 'https://ejemplo.invalid/problemas/existencia-insuficiente',
      title: 'Existencia insuficiente',
      status: 409,
      detail: "Existencia insuficiente para el producto 'P001'. Solicitado: 999, disponible: 40. La venta no se registro."
    };

    let errorRecibido = '';
    service.registrarVenta(peticionExcedida).subscribe({
      next: () => fail('Debería fallar con 409 Conflict'),
      error: (err: Error) => {
        errorRecibido = err.message;
      }
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(problema409, { status: 409, statusText: 'Conflict' });

    expect(errorRecibido).toContain("Existencia insuficiente para el producto 'P001'");
    expect(errorRecibido).toContain('Solicitado: 999, disponible: 40');
  });

  it('debe convertir códigos con prefijo P001 o numéricos 101 a productoId numérico', () => {
    expect(service.extraerProductoId('P001')).toBe(1);
    expect(service.extraerProductoId('P005')).toBe(5);
    expect(service.extraerProductoId('101')).toBe(101);
  });
});