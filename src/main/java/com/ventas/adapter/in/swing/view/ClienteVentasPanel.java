package com.ventas.adapter.in.swing.view;

import com.ventas.adapter.in.swing.DTO.CatalogoDTO;
import com.ventas.adapter.in.swing.controller.CatalogoController;
import com.ventas.adapter.in.swing.controller.VentaController;
import com.ventas.adapter.in.swing.model.PartidaTableModel;
import com.ventas.adapter.in.swing.model.ProductoTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.Locale;

public class ClienteVentasPanel extends JPanel {

    private final CatalogoController catalogoController;
    private final VentaController ventaController;
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private JTable tablaProductos;
    private JTable tablaPartidas;
    private JSpinner spinnerCantidad;
    private JButton btnAgregarProducto;
    private JButton btnActualizarCatalogo;
    private JButton btnQuitarPartida;
    private JButton btnLimpiarVenta;
    private JButton btnRegistrarVenta;
    private JLabel lblSubtotal;
    private JLabel lblTotalArticulos;
    private JLabel lblTotal;
    private JLabel lblEstado;
    private JProgressBar barraProgreso;

    public ClienteVentasPanel(CatalogoController catalogoController, VentaController ventaController) {
        if (catalogoController == null || ventaController == null) {
            throw new IllegalArgumentException("Los controladores no pueden ser nulos.");
        }
        this.catalogoController = catalogoController;
        this.ventaController = ventaController;
        inicializarComponentes();
        configurarAccesibilidad();
        configurarEventos();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Panel Superior: Título
        JPanel panelEncabezado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTitulo = new JLabel("Módulo de Ventas");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        panelEncabezado.add(lblTitulo);
        add(panelEncabezado, BorderLayout.NORTH);

        // Panel Central: Split con Catálogo (Izquierda) y Venta Actual (Derecha)
        JPanel panelCatalogo = crearPanelCatalogo();
        JPanel panelVenta = crearPanelVenta();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelCatalogo, panelVenta);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        // Panel Inferior: Estado y Acciones
        JPanel panelInferior = crearPanelInferior();
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelCatalogo() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Catálogo de Productos"));

        // Se enlaza directamente al ProductoTableModel separable del controlador
        tablaProductos = new JTable(catalogoController.getModeloProductos());
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setName("tablaProductos");

        JScrollPane scroll = new JScrollPane(tablaProductos);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JLabel lblCantidad = new JLabel("Cantidad:");
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spinnerCantidad.setName("spinnerCantidad");
        lblCantidad.setLabelFor(spinnerCantidad);

        btnAgregarProducto = new JButton("Agregar a la Venta");
        btnAgregarProducto.setName("btnAgregarProducto");

        btnActualizarCatalogo = new JButton("Recargar");
        btnActualizarCatalogo.setName("btnActualizarCatalogo");

        panelControles.add(lblCantidad);
        panelControles.add(spinnerCantidad);
        panelControles.add(btnAgregarProducto);
        panelControles.add(btnActualizarCatalogo);

        panel.add(panelControles, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelVenta() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Venta en Curso"));

        // Se enlaza directamente al PartidaTableModel separable del controlador
        tablaPartidas = new JTable(ventaController.getModeloPartidas());
        tablaPartidas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPartidas.setName("tablaPartidas");

        JScrollPane scroll = new JScrollPane(tablaPartidas);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel panelDerechoSur = new JPanel(new BorderLayout(6, 6));
        JPanel panelBotonesVenta = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        btnQuitarPartida = new JButton("Quitar Selección");
        btnQuitarPartida.setName("btnQuitarPartida");

        btnLimpiarVenta = new JButton("Limpiar Venta");
        btnLimpiarVenta.setName("btnLimpiarVenta");

        panelBotonesVenta.add(btnQuitarPartida);
        panelBotonesVenta.add(btnLimpiarVenta);
        panelDerechoSur.add(panelBotonesVenta, BorderLayout.NORTH);

        JPanel panelTotales = new JPanel(new GridLayout(3, 1, 2, 2));
        panelTotales.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        lblTotalArticulos = new JLabel("Artículos en carrito: 0");
        lblTotalArticulos.setName("lblTotalArticulos");
        lblSubtotal = new JLabel("Subtotal: $0.00");
        lblSubtotal.setName("lblSubtotal");
        lblTotal = new JLabel("Total: $0.00");
        lblTotal.setName("lblTotal");
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 14f));
        lblTotal.setForeground(new Color(0, 102, 0));

        panelTotales.add(lblTotalArticulos);
        panelTotales.add(lblSubtotal);
        panelTotales.add(lblTotal);
        panelDerechoSur.add(panelTotales, BorderLayout.SOUTH);

        panel.add(panelDerechoSur, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(10, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        lblEstado = new JLabel("Listo");
        lblEstado.setName("lblEstado");

        barraProgreso = new JProgressBar();
        barraProgreso.setName("barraProgreso");
        barraProgreso.setIndeterminate(true);
        barraProgreso.setVisible(false);
        barraProgreso.setPreferredSize(new Dimension(120, 16));

        panelEstado.add(lblEstado);
        panelEstado.add(barraProgreso);
        panel.add(panelEstado, BorderLayout.WEST);

        btnRegistrarVenta = new JButton("Registrar Venta");
        btnRegistrarVenta.setName("btnRegistrarVenta");
        btnRegistrarVenta.setFont(btnRegistrarVenta.getFont().deriveFont(Font.BOLD, 13f));
        btnRegistrarVenta.setBackground(new Color(33, 115, 70));
        btnRegistrarVenta.setForeground(Color.BLACK);
        panel.add(btnRegistrarVenta, BorderLayout.EAST);

        return panel;
    }

private void configurarAccesibilidad() {
        btnAgregarProducto.setMnemonic(KeyEvent.VK_A); 
        btnAgregarProducto.setText("Agregar a la Venta"); 
        btnAgregarProducto.getAccessibleContext().setAccessibleName("Agregar producto seleccionado a la venta");

        btnActualizarCatalogo.setMnemonic(KeyEvent.VK_R); 
        btnActualizarCatalogo.getAccessibleContext().setAccessibleName("Recargar catálogo de productos");

        btnQuitarPartida.setMnemonic(KeyEvent.VK_Q);
        btnQuitarPartida.getAccessibleContext().setAccessibleName("Quitar partida seleccionada de la venta");

        btnLimpiarVenta.setMnemonic(KeyEvent.VK_L); 
        btnLimpiarVenta.getAccessibleContext().setAccessibleName("Limpiar todas las partidas de la venta");

        btnRegistrarVenta.setMnemonic(KeyEvent.VK_V); 
        btnRegistrarVenta.getAccessibleContext().setAccessibleName("Confirmar y registrar la venta actual");

        tablaProductos.getAccessibleContext().setAccessibleName("Tabla de catálogo de productos disponibles");
        tablaPartidas.getAccessibleContext().setAccessibleName("Tabla de partidas en la venta actual");
        spinnerCantidad.getAccessibleContext().setAccessibleName("Cantidad de producto a agregar");
    }

    private void configurarEventos() {
        btnActualizarCatalogo.addActionListener(e -> cargarCatalogo());

        btnAgregarProducto.addActionListener(e -> {
            int filaSeleccionada = tablaProductos.getSelectedRow();
            CatalogoDTO productoSeleccionado = catalogoController.obtenerProductoEnFila(filaSeleccionada);
            if (productoSeleccionado == null) {
                mostrarMensajeError("Debe seleccionar un producto del catálogo.");
                return;
            }
            int cantidad = (Integer) spinnerCantidad.getValue();
            try {
                ventaController.agregarProducto(productoSeleccionado, cantidad);
                actualizarEtiquetasTotales();
                setEstado("Producto agregado a la venta: " + productoSeleccionado.getNombre(), false);
            } catch (IllegalArgumentException ex) {
                mostrarMensajeError(ex.getMessage());
            }
        });

        btnQuitarPartida.addActionListener(e -> {
            int filaSeleccionada = tablaPartidas.getSelectedRow();
            if (filaSeleccionada < 0) {
                mostrarMensajeError("Seleccione una partida para quitar.");
                return;
            }
            try {
                ventaController.eliminarPartida(filaSeleccionada);
                actualizarEtiquetasTotales();
                setEstado("Partida eliminada.", false);
            } catch (Exception ex) {
                mostrarMensajeError(ex.getMessage());
            }
        });

        btnLimpiarVenta.addActionListener(e -> {
            ventaController.limpiarVenta();
            actualizarEtiquetasTotales();
            setEstado("Venta reiniciada.", false);
        });

        btnRegistrarVenta.addActionListener(e -> ejecutarRegistroVenta());
    }

    public void cargarCatalogo() {
        setEnProgreso(true, "Consultando catálogo de productos...");
        catalogoController.cargarProductos(
                cantidadProductos -> {
                    setEnProgreso(false, "Catálogo cargado con éxito (" + cantidadProductos + " productos).");
                },
                error -> {
                    setEnProgreso(false, "Error: " + error);
                    mostrarMensajeError(error);
                }
        );
    }

    private void ejecutarRegistroVenta() {
        if (!ventaController.tienePartidas()) {
            mostrarMensajeError("No se puede registrar una venta sin partidas.");
            return;
        }
        setEnProgreso(true, "Registrando venta en el sistema...");
        ventaController.registrarVenta(
                totalCobrado -> {
                    setEnProgreso(false, "Venta registrada con éxito.");
                    actualizarEtiquetasTotales();
                    mostrarMensajeExito("¡Venta registrada con éxito!\nTotal cobrado: "
                            + formatoMoneda.format(totalCobrado));
                    cargarCatalogo();
                },
                error -> {
                    setEnProgreso(false, "Error al registrar: " + error);
                    mostrarMensajeError(error);
                }
        );
    }

    public void actualizarEtiquetasTotales() {
        double subtotal = ventaController.calcularSubtotal();
        int totalArticulos = ventaController.getCantidadTotalArticulos();
        lblTotalArticulos.setText("Artículos en carrito: " + totalArticulos);
        lblSubtotal.setText("Subtotal: " + formatoMoneda.format(subtotal));
        lblTotal.setText("Total: " + formatoMoneda.format(subtotal));
    }

    private void setEnProgreso(boolean enProgreso, String mensaje) {
        barraProgreso.setVisible(enProgreso);
        btnRegistrarVenta.setEnabled(!enProgreso);
        btnAgregarProducto.setEnabled(!enProgreso);
        btnActualizarCatalogo.setEnabled(!enProgreso);
        btnQuitarPartida.setEnabled(!enProgreso);
        btnLimpiarVenta.setEnabled(!enProgreso);
        setEstado(mensaje, false);
    }

    private void setEstado(String mensaje, boolean esError) {
        lblEstado.setText(mensaje);
        lblEstado.setForeground(esError ? Color.RED : Color.DARK_GRAY);
    }

    protected void mostrarMensajeError(String mensaje) {
        setEstado("Error: " + mensaje, true);
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void mostrarMensajeExito(String mensaje) {
        if (!GraphicsEnvironment.isHeadless()) {
            JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public CatalogoController getCatalogoController() {
        return catalogoController;
    }

    public VentaController getVentaController() {
        return ventaController;
    }

    public JTable getTablaProductos() {
        return tablaProductos;
    }

    public JTable getTablaPartidas() {
        return tablaPartidas;
    }

    public ProductoTableModel getModeloProductos() {
        return catalogoController.getModeloProductos();
    }

    public PartidaTableModel getModeloPartidas() {
        return ventaController.getModeloPartidas();
    }

    public JButton getBtnAgregarProducto() {
        return btnAgregarProducto;
    }

    public JButton getBtnActualizarCatalogo() {
        return btnActualizarCatalogo;
    }

    public JButton getBtnQuitarPartida() {
        return btnQuitarPartida;
    }

    public JButton getBtnLimpiarVenta() {
        return btnLimpiarVenta;
    }

    public JButton getBtnRegistrarVenta() {
        return btnRegistrarVenta;
    }

    public JSpinner getSpinnerCantidad() {
        return spinnerCantidad;
    }

    public JLabel getLblTotal() {
        return lblTotal;
    }

    public JLabel getLblSubtotal() {
        return lblSubtotal;
    }

    public JLabel getLblTotalArticulos() {
        return lblTotalArticulos;
    }

    public JLabel getLblEstado() {
        return lblEstado;
    }

    public JProgressBar getBarraProgreso() {
        return barraProgreso;
    }
}