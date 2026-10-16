package com.ventas.adapter.in.swing;

import com.ventas.adapter.out.memory.InMemoryProductoRepository;
import com.ventas.adapter.out.memory.InMemoryVentaRepository;
import com.ventas.adapter.out.notification.ReciboObservador;
import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.application.service.ProductoService;
import com.ventas.application.service.PublicadorSimple;
import com.ventas.application.service.RegistrarVentaService;
import com.ventas.model.PoliticaDescuento;
import com.ventas.model.Producto;

import javax.swing.*;

public class ClienteVentasApp {

    public static void main(String[] args) {
        // Inicializar repositorios y servicios
        InMemoryProductoRepository productoRepository = new InMemoryProductoRepository();
        InMemoryVentaRepository ventaRepository = new InMemoryVentaRepository();

        PublicadorSimple publicador = new PublicadorSimple();
        publicador.agregarObservador(new ReciboObservador());

        PoliticaDescuento politicaDescuento = venta -> {
            if (venta.getPartidas().size() > 10) {
                return venta.calcularTotal() * 0.15;
            }
            return 0.0;
        };

        ProductoService productoService = new ProductoService(productoRepository);
        RegistrarVentaService registrarVentaService = new RegistrarVentaService(ventaRepository, politicaDescuento, publicador);

        // Cargar productos iniciales de demostración
        cargarDatosDePrueba(productoService);

        // Iniciar la interfaz en el Event Dispatch Thread (EDT)
        iniciarCliente(productoService, registrarVentaService);
    }

    public static ClienteVentasSwingFrame iniciarCliente(ConsultarProductosUseCase consultarProductosUseCase,
                                                         RegistrarVentaUseCase registrarVentaUseCase) {
        VentaController controller = new VentaController(consultarProductosUseCase, registrarVentaUseCase);

        final ClienteVentasSwingFrame[] frameHolder = new ClienteVentasSwingFrame[1];
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                frameHolder[0] = crearYMostrarVentana(controller);
            } else {
                SwingUtilities.invokeAndWait(() -> frameHolder[0] = crearYMostrarVentana(controller));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al iniciar cliente Swing en el EDT: " + e.getMessage(), e);
        }

        return frameHolder[0];
    }

    private static ClienteVentasSwingFrame crearYMostrarVentana(VentaController controller) {
        configurarLookAndFeel();
        ClienteVentasSwingFrame frame = new ClienteVentasSwingFrame(controller);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.cargarCatalogo();
        return frame;
    }

    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Se mantiene el Look and Feel predeterminado
        }
    }

    private static void cargarDatosDePrueba(ProductoService productoService) {
        productoService.registrar(new Producto("P001", "Laptop Gamer 16GB", 21500.00, 6));
        productoService.registrar(new Producto("P002", "Teclado Mecánico RGB", 1250.00, 15));
        productoService.registrar(new Producto("P003", "Mouse Ergonómico", 480.00, 20));
        productoService.registrar(new Producto("P004", "Monitor 27 Pulgadas IPS", 4600.00, 8));
        productoService.registrar(new Producto("P005", "Audífonos Inalámbricos", 890.00, 12));
    }
}
