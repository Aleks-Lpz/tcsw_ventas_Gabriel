import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CarritoComponent } from './carrito.component';
import { CarritoService } from '../../services/carrito.service';
import { VentaService } from '../../services/venta.service';
import { Producto } from '../../models/producto.model';
import { VentaRespuesta } from '../../models/venta.model';

describe('CarritoComponent', () => {
  let component: CarritoComponent;
  let fixture: ComponentFixture<CarritoComponent>;
  let carritoService: CarritoService;
  let ventaServiceSpy: jasmine.SpyObj<VentaService>;

  const productoMock: Producto = {
    codigo: 'P001',
    nombre: 'Jabón Roma',
    precio: 25.5,
    existencia: 40
  };

  beforeEach(async () => {
    ventaServiceSpy = jasmine.createSpyObj<VentaService>('VentaService', [
      'registrarVenta',
      'extraerProductoId'
    ]);
    ventaServiceSpy.extraerProductoId.and.returnValue(1);

    await TestBed.configureTestingModule({
      imports: [CarritoComponent],
      providers: [
        CarritoService,
        { provide: VentaService, useValue: ventaServiceSpy }
      ]
    }).compileComponents();

    carritoService = TestBed.inject(CarritoService);
    fixture = TestBed.createComponent(CarritoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe reflejar las partidas agregadas en CarritoService y mostrar el subtotal', () => {
    carritoService.agregarProducto(productoMock, 2);
    fixture.detectChanges();

    expect(component.partidas.length).toBe(1);
    expect(component.subtotal).toBe(51.0);
    expect(component.totalArticulos).toBe(2);

    const tabla = fixture.nativeElement.querySelector('#tabla-carrito');
    expect(tabla).toBeTruthy();
  });

  it('al confirmar venta exitosa (201 Created) debe mostrar el recibo, vaciar el carrito y emitir ventaConfirmada', () => {
    carritoService.agregarProducto(productoMock, 2);
    fixture.detectChanges();

    const respuestaMock: VentaRespuesta = {
      id: 15,
      fecha: '2026-11-15T18:30:00Z',
      descuento: 0,
      total: 51.0,
      partidas: [
        { productoId: 1, nombre: 'Jabón Roma', cantidad: 2, precioUnitario: 25.5, subtotal: 51.0 }
      ]
    };
    ventaServiceSpy.registrarVenta.and.returnValue(of(respuestaMock));

    let ventaEmitida: VentaRespuesta | undefined;
    component.ventaConfirmada.subscribe((v) => {
      ventaEmitida = v;
    });

    component.confirmarVenta();
    fixture.detectChanges();

    expect(ventaServiceSpy.registrarVenta).toHaveBeenCalledWith({
      partidas: [{ productoId: 1, cantidad: 2 }]
    });
    expect(component.partidas.length).toBe(0);
    expect(component.reciboVenta).toEqual(respuestaMock);
    expect(ventaEmitida).toEqual(respuestaMock);

    const reciboEl = fixture.nativeElement.querySelector('#recibo-venta-exitosa');
    expect(reciboEl).toBeTruthy();
    expect(reciboEl.textContent).toContain('#15');
    expect(reciboEl.textContent).toContain('51.00');
  });

  it('al ocurrir un error 409 Conflict por sobreventa debe conservar el carrito y mostrar ProblemaDetalle.detail', () => {
    carritoService.agregarProducto(productoMock, 999);
    fixture.detectChanges();

    const detalleError =
      "Existencia insuficiente para el producto 'P001'. Solicitado: 999, disponible: 40. La venta no se registro.";
    ventaServiceSpy.registrarVenta.and.returnValue(throwError(() => new Error(detalleError)));

    component.confirmarVenta();
    fixture.detectChanges();

    // El carrito NO se vacía y conserva su estado
    expect(component.partidas.length).toBe(1);
    expect(component.partidas[0].cantidad).toBe(999);
    expect(component.reciboVenta).toBeNull();
    expect(component.errorVenta).toBe(detalleError);

    const alertaError = fixture.nativeElement.querySelector('#alerta-error-venta');
    expect(alertaError).toBeTruthy();
    expect(alertaError.textContent).toContain("Existencia insuficiente para el producto 'P001'");
  });
});