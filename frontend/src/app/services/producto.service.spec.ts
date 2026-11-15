import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductoService } from './producto.service';
import { Producto, ProductoRequest } from '../models/producto.model';
import { ProblemaDetalle } from '../models/problema-detalle.model';

describe('ProductoService', () => {
  let service: ProductoService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/productos';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProductoService]
    });
    service = TestBed.inject(ProductoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe listar el catálogo de productos correctamente (200 OK)', () => {
    const mockCatalogo: Producto[] = [
      { codigo: 'P001', nombre: 'Jabón Roma', precio: 25.5, existencia: 40 },
      { codigo: 'P002', nombre: 'Leche Lala +Proteína', precio: 42.8, existencia: 23 }
    ];

    service.listarProductos().subscribe((productos) => {
      expect(productos.length).toBe(2);
      expect(productos).toEqual(mockCatalogo);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockCatalogo, { status: 200, statusText: 'OK' });
  });

  it('debe registrar un producto válido y devolverlo (201 Created)', () => {
    const nuevoProducto: ProductoRequest = {
      codigo: 'P006',
      nombre: 'Café Molido',
      precio: 125.75,
      existencia: 15
    };

    service.registrarProducto(nuevoProducto).subscribe((respuesta) => {
      expect(respuesta).toEqual(nuevoProducto);
      expect(respuesta.codigo).toBe('P006');
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nuevoProducto);
    req.flush(nuevoProducto, { status: 201, statusText: 'Created' });
  });

  it('debe extraer error.error.detail cuando POST /productos devuelve 400 Bad Request (ProblemaDetalle)', () => {
    const peticionInvalida: ProductoRequest = {
      codigo: '',
      nombre: '',
      precio: -10,
      existencia: -1
    };

    const problema: ProblemaDetalle = {
      type: 'https://ejemplo.invalid/problemas/entrada-invalida',
      title: 'Entrada inválida',
      status: 400,
      detail: 'precio: debe ser mayor que 0; nombre: no debe estar vacío'
    };

    let mensajeCapturado = '';
    service.registrarProducto(peticionInvalida).subscribe({
      next: () => fail('Se esperaba un error 400'),
      error: (err: Error) => {
        mensajeCapturado = err.message;
      }
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(problema, { status: 400, statusText: 'Bad Request' });

    expect(mensajeCapturado).toBe('precio: debe ser mayor que 0; nombre: no debe estar vacío');
  });
});