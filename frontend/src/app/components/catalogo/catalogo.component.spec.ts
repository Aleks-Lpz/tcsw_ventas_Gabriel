import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { CatalogoComponent, SeleccionProductoEvento } from './catalogo.component';
import { ProductoService } from '../../services/producto.service';
import { Producto } from '../../models/producto.model';

describe('CatalogoComponent', () => {
  let component: CatalogoComponent;
  let fixture: ComponentFixture<CatalogoComponent>;
  let productoServiceSpy: jasmine.SpyObj<ProductoService>;

  const mockProductos: Producto[] = [
    { codigo: 'P001', nombre: 'Jabón Roma', precio: 25.5, existencia: 40 },
    { codigo: 'P002', nombre: 'Producto Agotado', precio: 50.0, existencia: 0 }
  ];

  beforeEach(async () => {
    productoServiceSpy = jasmine.createSpyObj<ProductoService>('ProductoService', [
      'listarProductos',
      'registrarProducto'
    ]);
    productoServiceSpy.listarProductos.and.returnValue(of(mockProductos));

    await TestBed.configureTestingModule({
      imports: [CatalogoComponent],
      providers: [{ provide: ProductoService, useValue: productoServiceSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(CatalogoComponent);
    component = fixture.componentInstance;
  });

  it('debe mostrar estado de Cargando mientras espera respuesta de ProductoService', () => {
    const subject = new Subject<Producto[]>();
    productoServiceSpy.listarProductos.and.returnValue(subject.asObservable());

    fixture.detectChanges(); // dispara ngOnInit()

    expect(component.cargandoCatalogo).toBeTrue();
    const loadingEl = fixture.nativeElement.querySelector('#estado-cargando-catalogo');
    expect(loadingEl).toBeTruthy();

    subject.next(mockProductos);
    subject.complete();
    fixture.detectChanges();

    expect(component.cargandoCatalogo).toBeFalse();
    expect(fixture.nativeElement.querySelector('#tabla-catalogo')).toBeTruthy();
  });

  it('debe renderizar la tabla de productos en estado de Éxito y deshabilitar botón si existencia === 0', () => {
    fixture.detectChanges();

    const filas = fixture.nativeElement.querySelectorAll('#tabla-catalogo tbody tr');
    expect(filas.length).toBe(2);

    const btnDisponible: HTMLButtonElement = filas[0].querySelector('.btn-agregar-carrito');
    const btnAgotado: HTMLButtonElement = filas[1].querySelector('.btn-agregar-carrito');

    expect(btnDisponible.disabled).toBeFalse();
    expect(btnAgotado.disabled).toBeTrue();
  });

  it('debe mostrar alerta visible en estado de Error cuando falla listarProductos()', () => {
    productoServiceSpy.listarProductos.and.returnValue(
      throwError(() => new Error('No se pudo conectar con la API local'))
    );

    fixture.detectChanges();

    expect(component.errorCatalogo).toBe('No se pudo conectar con la API local');
    const alertaError = fixture.nativeElement.querySelector('#alerta-error-catalogo');
    expect(alertaError).toBeTruthy();
    expect(alertaError.textContent).toContain('No se pudo conectar con la API local');
  });

  it('debe validar el formulario reactivo y bloquear el envío si los campos son inválidos', () => {
    fixture.detectChanges();

    component.formularioProducto.setValue({
      codigo: '',
      nombre: '',
      precio: 0,
      existencia: -5
    });

    component.registrarNuevoProducto();

    expect(component.formularioProducto.invalid).toBeTrue();
    expect(productoServiceSpy.registrarProducto).not.toHaveBeenCalled();
  });

  it('debe registrar un producto válido mediante ProductoService y recargar el catálogo', () => {
    const productoCreado: Producto = {
      codigo: 'P006',
      nombre: 'Café Molido',
      precio: 120.5,
      existencia: 15
    };
    productoServiceSpy.registrarProducto.and.returnValue(of(productoCreado));

    fixture.detectChanges();
    productoServiceSpy.listarProductos.calls.reset();

    component.formularioProducto.setValue({
      codigo: 'P006',
      nombre: 'Café Molido',
      precio: 120.5,
      existencia: 15
    });

    component.registrarNuevoProducto();
    fixture.detectChanges();

    expect(productoServiceSpy.registrarProducto).toHaveBeenCalledWith({
      codigo: 'P006',
      nombre: 'Café Molido',
      precio: 120.5,
      existencia: 15
    });
    expect(productoServiceSpy.listarProductos).toHaveBeenCalledTimes(1);
    expect(component.mensajeExitoRegistro).toContain('Café Molido');
  });

  it('debe emitir el evento agregarAlCarrito con el producto y cantidad seleccionada', () => {
    fixture.detectChanges();

    let eventoEmitido: SeleccionProductoEvento | undefined;
    component.agregarAlCarrito.subscribe((evento) => {
      eventoEmitido = evento;
    });

    component.actualizarCantidad(mockProductos[0], 3);
    component.seleccionarParaCarrito(mockProductos[0]);

    expect(eventoEmitido).toEqual({
      producto: mockProductos[0],
      cantidad: 3
    });
  });
});