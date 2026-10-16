package com.ventas.adapter.in.swing;

import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClienteVentasPanel extends JPanel {

    private final VentaController controller;
    private final List<Producto> listaProductosActual = new ArrayList<>();
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    private JTable tablaProductos;
    private DefaultTableModel modeloProductos;
    private JTable tablaPartidas;
    private DefaultTableModel modeloPartidas;

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

    public ClienteVentasPanel(VentaController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("El controlador de ventas no puede ser nulo.");
        }
        this.controller = controller;

        inicializarComponentes();
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

        String[] columnas = {"Código", "Nombre", "Precio", "Existencia"};
        modeloProductos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProductos = new JTable(modeloProductos);
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setName("tablaProductos");
        JScrollPane scroll = new JScrollPane(tablaProductos);
        panel.add(scroll, BorderLayout.CENTER);

        // Barra inferior del catálogo: Cantidad y botón agregar
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelControles.add(new JLabel("Cantidad:"));
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spinnerCantidad.setName("spinnerCantidad");
        panelControles.add(spinnerCantidad);

        btnAgregarProducto = new JButton("Agregar a la Venta");
        btnAgregarProducto.setName("btnAgregarProducto");
        panelControles.add(btnAgregarProducto);

        btnActualizarCatalogo = new JButton("Recargar");
        btnActualizarCatalogo.setName("btnActualizarCatalogo");
        panelControles.add(btnActualizarCatalogo);

        panel.add(panelControles, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelVenta() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Venta en Curso"));

        String[] columnas = {"Código", "Producto", "Precio Unit.", "Cant.", "Subtotal"};
        modeloPartidas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPartidas = new JTable(modeloPartidas);
        tablaPartidas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaPartidas.setName("tablaPartidas");
        JScrollPane scroll = new JScrollPane(tablaPartidas);
        panel.add(scroll, BorderLayout.CENTER);

        // Panel de totales y botones de la venta
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

    private void configurarEventos() {
        btnActualizarCatalogo.addActionListener(e -> cargarCatalogo());

        btnAgregarProducto.addActionListener(e -> {
            int filaSeleccionada = tablaProductos.getSelectedRow();
            if (filaSeleccionada < 0 || filaSeleccionada >= listaProductosActual.size()) {
                mostrarMensajeError("Debe seleccionar un producto del catálogo.");
                return;
            }

            Producto producto = listaProductosActual.get(filaSeleccionada);
            int cantidad = (Integer) spinnerCantidad.getValue();

            try {
                controller.agregarProducto(producto, cantidad);
                actualizarTablaPartidas();
                setEstado("Producto agregado a la venta: " + producto.getNombre(), false);
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
                controller.eliminarPartida(filaSeleccionada);
                actualizarTablaPartidas();
                setEstado("Partida eliminada.", false);
            } catch (Exception ex) {
                mostrarMensajeError(ex.getMessage());
            }
        });

        btnLimpiarVenta.addActionListener(e -> {
            controller.limpiarVenta();
            actualizarTablaPartidas();
            setEstado("Venta reiniciada.", false);
        });

        btnRegistrarVenta.addActionListener(e -> ejecutarRegistroVenta());
    }

    public void cargarCatalogo() {
        setEnProgreso(true, "Consultando catálogo de productos...");
        controller.cargarProductos(
                productos -> {
                    setEnProgreso(false, "Catálogo cargado con éxito (" + productos.size() + " productos).");
                    actualizarTablaProductos(productos);
                },
                error -> {
                    setEnProgreso(false, "Error: " + error);
                    mostrarMensajeError(error);
                }
        );
    }

    private void ejecutarRegistroVenta() {
        if (controller.getPartidas().isEmpty()) {
            mostrarMensajeError("No se puede registrar una venta sin partidas.");
            return;
        }

        setEnProgreso(true, "Registrando venta en el sistema...");
        controller.registrarVenta(
                ventaGuardada -> {
                    setEnProgreso(false, "Venta registrada con éxito.");
                    actualizarTablaPartidas();
                    mostrarMensajeExito("¡Venta registrada con éxito!\nTotal cobrado: "
                            + formatoMoneda.format(ventaGuardada.calcularTotal()));
                    cargarCatalogo();
                },
                error -> {
                    setEnProgreso(false, "Error al registrar: " + error);
                    mostrarMensajeError(error);
                }
        );
    }

    public void actualizarTablaProductos(List<Producto> productos) {
        listaProductosActual.clear();
        modeloProductos.setRowCount(0);
        if (productos != null) {
            listaProductosActual.addAll(productos);
            for (Producto p : productos) {
                modeloProductos.addRow(new Object[]{
                        p.getCodigo(),
                        p.getNombre(),
                        formatoMoneda.format(p.getPrecio()),
                        p.getExistencia()
                });
            }
        }
    }

    public void actualizarTablaPartidas() {
        modeloPartidas.setRowCount(0);
        List<Partida> partidas = controller.getPartidas();
        for (Partida p : partidas) {
            modeloPartidas.addRow(new Object[]{
                    p.getProducto().getCodigo(),
                    p.getProducto().getNombre(),
                    formatoMoneda.format(p.getProducto().getPrecio()),
                    p.getCantidad(),
                    formatoMoneda.format(p.getSubtotal())
            });
        }

        double subtotal = controller.calcularSubtotal();
        int totalArticulos = controller.getCantidadTotalArticulos();

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

    public VentaController getController() {
        return controller;
    }

    public JTable getTablaProductos() {
        return tablaProductos;
    }

    public JTable getTablaPartidas() {
        return tablaPartidas;
    }

    public DefaultTableModel getModeloProductos() {
        return modeloProductos;
    }

    public DefaultTableModel getModeloPartidas() {
        return modeloPartidas;
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
