package com.ventas.adapter.in.swing;

import com.ventas.adapter.in.swing.controller.CatalogoController;
import com.ventas.adapter.in.swing.controller.VentaController;
import com.ventas.adapter.in.swing.DTO.CatalogoDTO;
import com.ventas.adapter.in.swing.view.ClienteVentasPanel;
import com.ventas.adapter.in.swing.view.ClienteVentasSwingFrame;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClienteVentasSwingFrameTest {

    private CatalogoController crearCatalogoSimulado() {
        return new CatalogoController(() -> List.of());
    }

    private VentaController crearVentaSimulada() {
        return new VentaController(v -> v);
    }

    @Test
    void inicializacionDeComponentesYModeloEnPanel() throws Exception {
        final ClienteVentasPanel[] holder = new ClienteVentasPanel[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasPanel(crearCatalogoSimulado(), crearVentaSimulada()));
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
    void actualizarTablaPartidasCalculaYMuestraTotales() throws Exception {
        VentaController ventaController = crearVentaSimulada();
        final ClienteVentasPanel[] holder = new ClienteVentasPanel[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasPanel(crearCatalogoSimulado(), ventaController));
        ClienteVentasPanel panel = holder[0];

        CatalogoDTO productoSimulado = new CatalogoDTO("P001", "Monitor", 4000.0, 10);
        ventaController.agregarProducto(productoSimulado, 2);
        
        ejecutarEnEDT(() -> panel.actualizarEtiquetasTotales());

        assertEquals(1, panel.getModeloPartidas().getRowCount());
        assertEquals("P001", panel.getModeloPartidas().getValueAt(0, 0));
        assertEquals(2, panel.getModeloPartidas().getValueAt(0, 3));
        assertTrue(panel.getLblTotalArticulos().getText().contains("2"));
        assertTrue(panel.getLblTotal().getText().contains("8,000"));
    }

    @Test
    void agregarSinSeleccionarProductoMuestraEstadoDeError() throws Exception {
        final ClienteVentasPanel[] holder = new ClienteVentasPanel[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasPanel(crearCatalogoSimulado(), crearVentaSimulada()));
        ClienteVentasPanel panel = holder[0];

        ejecutarEnEDT(() -> panel.getBtnAgregarProducto().doClick());
        assertTrue(panel.getLblEstado().getText().contains("Error"));
    }

    @Test
    void inicializacionDeFrameSiEntornoGraficoEstaDisponible() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        final ClienteVentasSwingFrame[] holder = new ClienteVentasSwingFrame[1];
        ejecutarEnEDT(() -> holder[0] = new ClienteVentasSwingFrame(crearCatalogoSimulado(), crearVentaSimulada()));
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
