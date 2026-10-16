package com.ventas.adapter.in.swing;

import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class VentaControllerTest {

    private Producto productoTeclado;
    private Producto productoMouse;

    @BeforeEach
    void setUp() {
        productoTeclado = new Producto("P001", "Teclado", 500.0, 10);
        productoMouse = new Producto("P002", "Mouse", 200.0, 5);
    }

    @Test
    void instanciarConDependenciasNulasLanzaExcepcion() {
        ConsultarProductosUseCase consultar = () -> List.of();
        RegistrarVentaUseCase registrar = v -> v;

        assertThrows(IllegalArgumentException.class, () -> new VentaController(null, registrar));
        assertThrows(IllegalArgumentException.class, () -> new VentaController(consultar, null));
    }

    @Test
    void agregarProductoValidoActualizaPartidasYSubtotal() {
        VentaController controller = new VentaController(() -> List.of(), v -> v);

        controller.agregarProducto(productoTeclado, 2);

        List<Partida> partidas = controller.getPartidas();
        assertEquals(1, partidas.size());
        assertEquals("P001", partidas.get(0).getProducto().getCodigo());
        assertEquals(2, partidas.get(0).getCantidad());
        assertEquals(1000.0, controller.calcularSubtotal());
        assertEquals(2, controller.getCantidadTotalArticulos());
    }

    @Test
    void agregarMismoProductoIncrementaCantidadEnMismaPartida() {
        VentaController controller = new VentaController(() -> List.of(), v -> v);

        controller.agregarProducto(productoTeclado, 2);
        controller.agregarProducto(productoTeclado, 3);

        List<Partida> partidas = controller.getPartidas();
        assertEquals(1, partidas.size());
        assertEquals(5, partidas.get(0).getCantidad());
        assertEquals(2500.0, controller.calcularSubtotal());
        assertEquals(5, controller.getCantidadTotalArticulos());
    }

    @Test
    void agregarProductoNuloLanzaExcepcion() {
        VentaController controller = new VentaController(() -> List.of(), v -> v);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.agregarProducto(null, 1));
        assertEquals("El producto no puede ser nulo.", ex.getMessage());
    }

    @Test
    void agregarProductoConCantidadInvalidaLanzaExcepcion() {
        VentaController controller = new VentaController(() -> List.of(), v -> v);

        assertThrows(IllegalArgumentException.class, () -> controller.agregarProducto(productoTeclado, 0));
        assertThrows(IllegalArgumentException.class, () -> controller.agregarProducto(productoTeclado, -3));
    }

    @Test
    void agregarProductoSuperandoExistenciaLanzaExcepcion() {
        VentaController controller = new VentaController(() -> List.of(), v -> v);

        // productoMouse tiene existencia 5
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.agregarProducto(productoMouse, 6));
        assertTrue(ex.getMessage().contains("Existencia insuficiente"));

        // Agregar 3 primero
        controller.agregarProducto(productoMouse, 3);
        // Intentar agregar 3 más (superaría 5)
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> controller.agregarProducto(productoMouse, 3));
        assertTrue(ex2.getMessage().contains("Existencia insuficiente"));
    }

    @Test
    void eliminarPartidaActualizaCarrito() {
        VentaController controller = new VentaController(() -> List.of(), v -> v);
        controller.agregarProducto(productoTeclado, 1);
        controller.agregarProducto(productoMouse, 1);
        assertEquals(2, controller.getPartidas().size());

        controller.eliminarPartida(0);

        assertEquals(1, controller.getPartidas().size());
        assertEquals("P002", controller.getPartidas().get(0).getProducto().getCodigo());
        assertEquals(200.0, controller.calcularSubtotal());
    }

    @Test
    void eliminarPartidaIndiceInvalidoLanzaExcepcion() {
        VentaController controller = new VentaController(() -> List.of(), v -> v);

        assertThrows(IndexOutOfBoundsException.class, () -> controller.eliminarPartida(0));
        assertThrows(IndexOutOfBoundsException.class, () -> controller.eliminarPartida(-1));
    }

    @Test
    void limpiarVentaReiniciaElCarrito() {
        VentaController controller = new VentaController(() -> List.of(), v -> v);
        controller.agregarProducto(productoTeclado, 2);
        assertEquals(1, controller.getPartidas().size());

        controller.limpiarVenta();

        assertTrue(controller.getPartidas().isEmpty());
        assertEquals(0.0, controller.calcularSubtotal());
        assertEquals(0, controller.getCantidadTotalArticulos());
    }

    @Test
    void cargarProductosExitosoInvocaOnSuccess() throws Exception {
        List<Producto> listaSimulada = List.of(productoTeclado, productoMouse);
        ConsultarProductosUseCase consultar = () -> listaSimulada;
        VentaController controller = new VentaController(consultar, v -> v);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<Producto>> resultado = new AtomicReference<>();

        SwingWorker<List<Producto>, Void> worker = controller.cargarProductos(
                productos -> {
                    resultado.set(productos);
                    latch.countDown();
                },
                error -> latch.countDown()
        );

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertNotNull(resultado.get());
        assertEquals(2, resultado.get().size());
        assertEquals("P001", resultado.get().get(0).getCodigo());
    }

    @Test
    void cargarProductosConErrorInvocaOnError() throws Exception {
        ConsultarProductosUseCase consultar = () -> {
            throw new RuntimeException("Fallo de conexión con el repositorio.");
        };
        VentaController controller = new VentaController(consultar, v -> v);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> mensajeError = new AtomicReference<>();

        SwingWorker<List<Producto>, Void> worker = controller.cargarProductos(
                productos -> latch.countDown(),
                error -> {
                    mensajeError.set(error);
                    latch.countDown();
                }
        );

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertNotNull(mensajeError.get());
        assertTrue(mensajeError.get().contains("Fallo de conexión"));
    }

    @Test
    void registrarVentaConCarritoVacioReportaError() {
        VentaController controller = new VentaController(() -> List.of(), v -> v);

        AtomicReference<String> mensajeError = new AtomicReference<>();
        SwingWorker<Venta, Void> worker = controller.registrarVenta(
                venta -> {},
                error -> mensajeError.set(error)
        );

        assertNull(worker);
        assertNotNull(mensajeError.get());
        assertEquals("No se puede registrar una venta sin partidas.", mensajeError.get());
    }

    @Test
    void registrarVentaExitosoInvocaCasoDeUsoYCallback() throws Exception {
        AtomicBoolean casoDeUsoLlamado = new AtomicBoolean(false);
        RegistrarVentaUseCase registrar = venta -> {
            casoDeUsoLlamado.set(true);
            return venta;
        };
        VentaController controller = new VentaController(() -> List.of(), registrar);
        controller.agregarProducto(productoTeclado, 2);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Venta> ventaRegistrada = new AtomicReference<>();

        SwingWorker<Venta, Void> worker = controller.registrarVenta(
                venta -> {
                    ventaRegistrada.set(venta);
                    latch.countDown();
                },
                error -> latch.countDown()
        );

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertTrue(casoDeUsoLlamado.get());
        assertNotNull(ventaRegistrada.get());
        assertEquals(1000.0, ventaRegistrada.get().calcularTotal());
        // El carrito debe haber sido vaciado tras el registro exitoso
        assertTrue(controller.getPartidas().isEmpty());
    }

    @Test
    void registrarVentaCuandoCasoDeUsoFallaReportaError() throws Exception {
        RegistrarVentaUseCase registrar = venta -> {
            throw new IllegalStateException("Error al persistir la venta.");
        };
        VentaController controller = new VentaController(() -> List.of(), registrar);
        controller.agregarProducto(productoTeclado, 1);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorCapturado = new AtomicReference<>();

        SwingWorker<Venta, Void> worker = controller.registrarVenta(
                venta -> latch.countDown(),
                error -> {
                    errorCapturado.set(error);
                    latch.countDown();
                }
        );

        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertNotNull(errorCapturado.get());
        assertTrue(errorCapturado.get().contains("Error al persistir la venta."));
        // El carrito no se pierde si falla la persistencia
        assertEquals(1, controller.getPartidas().size());
    }
}
