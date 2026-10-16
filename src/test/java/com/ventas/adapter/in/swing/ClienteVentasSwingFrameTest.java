package com.ventas.adapter.in.swing;

import com.ventas.model.Producto;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClienteVentasSwingFrameTest {

    @Test
    void inicializacionDeComponentesYModeloEnPanel() throws Exception {
        VentaController controller = new VentaController(() -> List.of(), v -> v);

        final ClienteVentasPanel[] holder = new ClienteVentasPanel[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasPanel(controller));

        ClienteVentasPanel panel = holder[0];
        assertNotNull(panel);
        assertNotNull(panel.getTablaProductos());
        assertNotNull(panel.getTablaPartidas());
        assertNotNull(panel.getBtnAgregarProducto());
        assertNotNull(panel.getBtnRegistrarVenta());
        assertEquals(4, panel.getModeloProductos().getColumnCount());
        assertEquals(5, panel.getModeloPartidas().getColumnCount());
    }

    @Test
    void actualizarTablaProductosReflejaDatosCorrectamente() throws Exception {
        VentaController controller = new VentaController(() -> List.of(), v -> v);
        final ClienteVentasPanel[] holder = new ClienteVentasPanel[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasPanel(controller));
        ClienteVentasPanel panel = holder[0];

        List<Producto> productos = List.of(
                new Producto("P001", "Laptop", 15000.0, 5),
                new Producto("P002", "Mouse", 300.0, 20)
        );

        ejecutarEnEDT(() -> panel.actualizarTablaProductos(productos));

        assertEquals(2, panel.getModeloProductos().getRowCount());
        assertEquals("P001", panel.getModeloProductos().getValueAt(0, 0));
        assertEquals("Laptop", panel.getModeloProductos().getValueAt(0, 1));
        assertEquals("P002", panel.getModeloProductos().getValueAt(1, 0));
    }

    @Test
    void actualizarTablaPartidasCalculaYMuestraTotales() throws Exception {
        VentaController controller = new VentaController(() -> List.of(), v -> v);
        final ClienteVentasPanel[] holder = new ClienteVentasPanel[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasPanel(controller));
        ClienteVentasPanel panel = holder[0];

        Producto producto = new Producto("P001", "Monitor", 4000.0, 10);
        controller.agregarProducto(producto, 2);

        ejecutarEnEDT(() -> panel.actualizarTablaPartidas());

        assertEquals(1, panel.getModeloPartidas().getRowCount());
        assertEquals("P001", panel.getModeloPartidas().getValueAt(0, 0));
        assertEquals(2, panel.getModeloPartidas().getValueAt(0, 3));
        assertTrue(panel.getLblTotalArticulos().getText().contains("2"));
        assertTrue(panel.getLblTotal().getText().contains("8,000"));
    }

    @Test
    void agregarSinSeleccionarProductoMuestraEstadoDeError() throws Exception {
        VentaController controller = new VentaController(() -> List.of(), v -> v);
        final ClienteVentasPanel[] holder = new ClienteVentasPanel[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasPanel(controller));
        ClienteVentasPanel panel = holder[0];

        ejecutarEnEDT(() -> panel.getBtnAgregarProducto().doClick());

        assertTrue(panel.getLblEstado().getText().contains("Error"));
    }

    @Test
    void quitarSinSeleccionDePartidaMuestraEstadoDeError() throws Exception {
        VentaController controller = new VentaController(() -> List.of(), v -> v);
        final ClienteVentasPanel[] holder = new ClienteVentasPanel[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasPanel(controller));
        ClienteVentasPanel panel = holder[0];

        ejecutarEnEDT(() -> panel.getBtnQuitarPartida().doClick());

        assertTrue(panel.getLblEstado().getText().contains("Error"));
    }

    @Test
    void inicializacionDeFrameSiEntornoGraficoEstaDisponible() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        VentaController controller = new VentaController(() -> List.of(), v -> v);
        final ClienteVentasSwingFrame[] holder = new ClienteVentasSwingFrame[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasSwingFrame(controller));

        ClienteVentasSwingFrame frame = holder[0];
        try {
            assertNotNull(frame);
            assertEquals("Sistema de Ventas - Cliente Swing", frame.getTitle());
            assertNotNull(frame.getPanelVentas());
        } finally {
            ejecutarEnEDT(frame::dispose);
        }
    }

    private void ejecutarEnEDT(Runnable runnable) throws InterruptedException, InvocationTargetException {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeAndWait(runnable);
        }
    }
}
