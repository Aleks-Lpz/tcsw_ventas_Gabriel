package com.ventas.adapter.in.swing.view;

import com.ventas.adapter.in.swing.controller.CatalogoController;
import com.ventas.adapter.in.swing.controller.VentaController;
import com.ventas.adapter.out.hibernate.HibernateProductoRepository;
import com.ventas.adapter.out.hibernate.HibernateVentaRepository;
import com.ventas.adapter.out.notification.ReciboObservador;
import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.application.port.out.ProductoRepository;
import com.ventas.application.port.out.VentaRepository;
import com.ventas.application.service.ProductoService;
import com.ventas.application.service.PublicadorSimple;
import com.ventas.application.service.RegistrarVentaService;
import com.ventas.model.PoliticaDescuento;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import javax.swing.*;

public class ClienteVentasApp {

    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("VentasPU");
       
        // Inicializar repositorios y servicios
        ProductoRepository productoRepository = new HibernateProductoRepository(entityManagerFactory);
        VentaRepository ventaRepository = new HibernateVentaRepository(entityManagerFactory);

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
        // Iniciar la interfaz en el Event Dispatch Thread (EDT)
        iniciarCliente(productoService, registrarVentaService);
    }

    public static ClienteVentasSwingFrame iniciarCliente(ConsultarProductosUseCase consultarProductosUseCase,
   RegistrarVentaUseCase registrarVentaUseCase) {
    CatalogoController catalogoController = new CatalogoController(consultarProductosUseCase);
    VentaController ventaController = new VentaController(registrarVentaUseCase);
    final ClienteVentasSwingFrame[] frameHolder = new ClienteVentasSwingFrame[1];

    try {
        if (SwingUtilities.isEventDispatchThread()) {
            frameHolder[0] = crearYMostrarVentana(catalogoController, ventaController);
        } else {
            SwingUtilities.invokeAndWait(() -> frameHolder[0] = crearYMostrarVentana(catalogoController, ventaController));
        }
    } catch (Exception e) {
        throw new RuntimeException("Error al iniciar cliente Swing en el EDT: " + e.getMessage(), e);
    }
    return frameHolder[0];
}

private static ClienteVentasSwingFrame crearYMostrarVentana(CatalogoController catalogoController,
                                                            VentaController ventaController) {
    configurarLookAndFeel();
    ClienteVentasSwingFrame frame = new ClienteVentasSwingFrame(catalogoController, ventaController);
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

}
