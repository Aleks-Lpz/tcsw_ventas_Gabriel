package com.ventas.adapter.in.swing;

import com.ventas.model.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClienteVentasSwingFrame extends JFrame {

    private final ClienteVentasPanel panelVentas;

    public ClienteVentasSwingFrame(VentaController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("El controlador de ventas no puede ser nulo.");
        }

        setTitle("Sistema de Ventas - Cliente Swing");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 600);
        setMinimumSize(new Dimension(800, 500));

        this.panelVentas = new ClienteVentasPanel(controller);
        setContentPane(panelVentas);
    }

    public ClienteVentasPanel getPanelVentas() {
        return panelVentas;
    }

    public void cargarCatalogo() {
        panelVentas.cargarCatalogo();
    }

    public void actualizarTablaProductos(List<Producto> productos) {
        panelVentas.actualizarTablaProductos(productos);
    }

    public void actualizarTablaPartidas() {
        panelVentas.actualizarTablaPartidas();
    }

    public VentaController getController() {
        return panelVentas.getController();
    }

    public JTable getTablaProductos() {
        return panelVentas.getTablaProductos();
    }

    public JTable getTablaPartidas() {
        return panelVentas.getTablaPartidas();
    }

    public DefaultTableModel getModeloProductos() {
        return panelVentas.getModeloProductos();
    }

    public DefaultTableModel getModeloPartidas() {
        return panelVentas.getModeloPartidas();
    }

    public JButton getBtnAgregarProducto() {
        return panelVentas.getBtnAgregarProducto();
    }

    public JButton getBtnActualizarCatalogo() {
        return panelVentas.getBtnActualizarCatalogo();
    }

    public JButton getBtnQuitarPartida() {
        return panelVentas.getBtnQuitarPartida();
    }

    public JButton getBtnLimpiarVenta() {
        return panelVentas.getBtnLimpiarVenta();
    }

    public JButton getBtnRegistrarVenta() {
        return panelVentas.getBtnRegistrarVenta();
    }

    public JSpinner getSpinnerCantidad() {
        return panelVentas.getSpinnerCantidad();
    }

    public JLabel getLblTotal() {
        return panelVentas.getLblTotal();
    }

    public JLabel getLblSubtotal() {
        return panelVentas.getLblSubtotal();
    }

    public JLabel getLblTotalArticulos() {
        return panelVentas.getLblTotalArticulos();
    }

    public JLabel getLblEstado() {
        return panelVentas.getLblEstado();
    }

    public JProgressBar getBarraProgreso() {
        return panelVentas.getBarraProgreso();
    }
}
