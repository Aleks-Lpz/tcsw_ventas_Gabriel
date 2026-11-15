import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CarritoService } from './carrito.service';
import { Producto } from '../models/producto.model';

describe('CarritoService', () => {
  let service: CarritoService;

  const producto1: Producto = {
    codigo: 'P001',
    nombre: 'Jabón Roma',
    precio: 25.5,
    existencia: 40
  };

  const producto2: Producto = {
    codigo: 'P002',
    nombre: 'Leche Lala +Proteína',
    precio: 42.8,
    existencia: 23
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CarritoService]
    });
    service = TestBed.inject(CarritoService);
  });

  it('debe iniciar con el carrito vacío y totales en cero', () => {
    expect(service.obtenerPartidas().length).toBe(0);
    expect(service.calcularSubtotal()).toBe(0);
    expect(service.obtenerCantidadTotalArticulos()).toBe(0);
  });

  it('debe agregar productos y consolidar cantidad si se agrega el mismo código', () => {
    service.agregarProducto(producto1, 2);
    service.agregarProducto(producto1, 3);
    service.agregarProducto(producto2, 1);

    const partidas = service.obtenerPartidas();
    expect(partidas.length).toBe(2);
    expect(partidas[0].cantidad).toBe(5);
    expect(partidas[0].subtotal).toBe(127.5);
    expect(service.obtenerCantidadTotalArticulos()).toBe(6);
    expect(service.calcularSubtotal()).toBe(170.3);
  });

  it('debe eliminar una partida por su código y recalcular el subtotal', () => {
    service.agregarProducto(producto1, 2);
    service.agregarProducto(producto2, 1);

    service.eliminarPartida('P001');

    expect(service.obtenerPartidas().length).toBe(1);
    expect(service.obtenerPartidas()[0].producto.codigo).toBe('P002');
    expect(service.calcularSubtotal()).toBe(42.8);
  });

  it('debe vaciar el carrito completamente', () => {
    service.agregarProducto(producto1, 4);
    service.vaciarCarrito();

    expect(service.obtenerPartidas().length).toBe(0);
    expect(service.calcularSubtotal()).toBe(0);
    expect(service.obtenerCantidadTotalArticulos()).toBe(0);
  });

  it('debe construir NuevaVentaPeticion convirtiendo códigos a productoId numérico', () => {
    service.agregarProducto(producto1, 2);
    service.agregarProducto(producto2, 3);

    const peticion = service.construirPeticionVenta();
    expect(peticion).toEqual({
      partidas: [
        { productoId: 1, cantidad: 2 },
        { productoId: 2, cantidad: 3 }
      ]
    });
  });
});