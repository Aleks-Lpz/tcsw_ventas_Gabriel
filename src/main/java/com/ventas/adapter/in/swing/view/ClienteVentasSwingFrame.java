package com.ventas.adapter.in.swing.view;

import com.ventas.adapter.in.swing.controller.CatalogoController;
import com.ventas.adapter.in.swing.controller.VentaController;
import com.ventas.adapter.in.swing.model.PartidaTableModel;
import com.ventas.adapter.in.swing.model.ProductoTableModel;

import javax.swing.*;
import java.awt.*;

public class ClienteVentasSwingFrame extends JFrame {

    private final ClienteVentasPanel panelVentas;

    public ClienteVentasSwingFrame(CatalogoController catalogoController, VentaController ventaController) {
        if (catalogoController == null || ventaController == null) {
            throw new IllegalArgumentException("Los controladores no pueden ser nulos.");
        }
        setTitle("Sistema de Ventas - Cliente Swing");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 600);
        setMinimumSize(new Dimension(800, 500));

        this.panelVentas = new ClienteVentasPanel(catalogoController, ventaController);
        setContentPane(panelVentas);
    }

    public ClienteVentasPanel getPanelVentas() {
        return panelVentas;
    }

    public void cargarCatalogo() {
        panelVentas.cargarCatalogo();
    }

    public ProductoTableModel getModeloProductos() {
        return panelVentas.getModeloProductos();
    }

    public PartidaTableModel getModeloPartidas() {
        return panelVentas.getModeloPartidas();
    }
}