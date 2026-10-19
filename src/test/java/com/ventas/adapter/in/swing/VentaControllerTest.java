package com.ventas.adapter.in.swing;

import com.ventas.adapter.in.swing.controller.VentaController;
import com.ventas.adapter.in.swing.DTO.CatalogoDTO;
import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.model.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.SwingWorker;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class VentaControllerTest {

    private CatalogoDTO productoTeclado;
    private CatalogoDTO productoMouse;
    private RegistrarVentaUseCase registrarVentaMock;

    @BeforeEach
    void setUp() {
        // Ahora usamos DTOs para simular lo que manda la vista
        productoTeclado = new CatalogoDTO("P001", "Teclado", 500.0, 10);
        productoMouse = new CatalogoDTO("P002", "Mouse", 200.0, 5);
        registrarVentaMock = v -> v;
    }

    @Test
    void instanciarConDependenciasNulasLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new VentaController(null));
    }

    @Test
    void agregarProductoValidoActualizaPartidasYSubtotal() {
        VentaController controller = new VentaController(registrarVentaMock);
        controller.agregarProducto(productoTeclado, 2);
        
        // Verificamos usando el modelo de la tabla
        assertEquals(1, controller.getModeloPartidas().getRowCount());
        assertEquals("P001", controller.getModeloPartidas().getValueAt(0, 0)); 
        assertEquals(2, controller.getModeloPartidas().getValueAt(0, 3)); // Cantidad está en columna 3
        assertEquals(1000.0, controller.calcularSubtotal());
        assertEquals(2, controller.getCantidadTotalArticulos());
    }

    @Test
    void agregarMismoProductoIncrementaCantidadEnMismaPartida() {
        VentaController controller = new VentaController(registrarVentaMock);
        controller.agregarProducto(productoTeclado, 2);
        controller.agregarProducto(productoTeclado, 3);
        
        assertEquals(1, controller.getModeloPartidas().getRowCount());
        assertEquals(5, controller.getModeloPartidas().getValueAt(0, 3)); 
        assertEquals(2500.0, controller.calcularSubtotal());
        assertEquals(5, controller.getCantidadTotalArticulos());
    }

    @Test
    void agregarProductoNuloLanzaExcepcion() {
        VentaController controller = new VentaController(registrarVentaMock);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.agregarProducto(null, 1));
        assertEquals("Debe seleccionar un producto válido.", ex.getMessage());
    }

    @Test
    void agregarProductoConCantidadInvalidaLanzaExcepcion() {
        VentaController controller = new VentaController(registrarVentaMock);
        assertThrows(IllegalArgumentException.class, () -> controller.agregarProducto(productoTeclado, 0));
        assertThrows(IllegalArgumentException.class, () -> controller.agregarProducto(productoTeclado, -3));
    }

    @Test
    void agregarProductoSuperandoExistenciaLanzaExcepcion() {
        VentaController controller = new VentaController(registrarVentaMock);
        // productoMouse tiene existencia 5
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.agregarProducto(productoMouse, 6));
        assertTrue(ex.getMessage().contains("Existencia insuficiente"));

        // Agregar 3 primero
        controller.agregarProducto(productoMouse, 3);
        // Intentar agregar 3 más (superar a 5)
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> controller.agregarProducto(productoMouse, 3));
        assertTrue(ex2.getMessage().contains("Existencia insuficiente"));
    }

    @Test
    void eliminarPartidaActualizaCarrito() {
        VentaController controller = new VentaController(registrarVentaMock);
        controller.agregarProducto(productoTeclado, 1);
        controller.agregarProducto(productoMouse, 1);
        assertEquals(2, controller.getModeloPartidas().getRowCount());
        
        controller.eliminarPartida(0);
        
        assertEquals(1, controller.getModeloPartidas().getRowCount());
        assertEquals("P002", controller.getModeloPartidas().getValueAt(0, 0));
        assertEquals(200.0, controller.calcularSubtotal());
    }

    @Test
    void limpiarVentaReiniciaElCarrito() {
        VentaController controller = new VentaController(registrarVentaMock);
        controller.agregarProducto(productoTeclado, 2);
        assertEquals(1, controller.getModeloPartidas().getRowCount());
        
        controller.limpiarVenta();
        
        assertEquals(0, controller.getModeloPartidas().getRowCount());
        assertEquals(0.0, controller.calcularSubtotal());
        assertEquals(0, controller.getCantidadTotalArticulos());
    }

    @Test
    void registrarVentaConCarritoVacioReportaError() {
        VentaController controller = new VentaController(registrarVentaMock);
        AtomicReference<String> mensajeError = new AtomicReference<>();
        SwingWorker<Double, Void> worker = controller.registrarVenta(
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
        VentaController controller = new VentaController(registrar);
        controller.agregarProducto(productoTeclado, 2);
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Double> totalRegistrado = new AtomicReference<>();
        
        SwingWorker<Double, Void> worker = controller.registrarVenta(
                total -> {
                    totalRegistrado.set(total);
                    latch.countDown();
                },
                error -> latch.countDown()
        );
        
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertTrue(casoDeUsoLlamado.get());
        assertNotNull(totalRegistrado.get());
        assertEquals(1000.0, totalRegistrado.get());
        
        // El carrito debe haber sido vaciado tras el registro exitoso
        assertEquals(0, controller.getModeloPartidas().getRowCount());
    }
}