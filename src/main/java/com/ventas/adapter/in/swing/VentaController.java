package com.ventas.adapter.in.swing;

import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import com.ventas.model.VentaFactory;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class VentaController {

    private final ConsultarProductosUseCase consultarProductosUseCase;
    private final RegistrarVentaUseCase registrarVentaUseCase;
    private final List<Partida> partidas = new ArrayList<>();

    public VentaController(ConsultarProductosUseCase consultarProductosUseCase,
                           RegistrarVentaUseCase registrarVentaUseCase) {
        if (consultarProductosUseCase == null) {
            throw new IllegalArgumentException("El caso de uso de consulta de productos no puede ser nulo.");
        }
        if (registrarVentaUseCase == null) {
            throw new IllegalArgumentException("El caso de uso de registro de venta no puede ser nulo.");
        }
        this.consultarProductosUseCase = consultarProductosUseCase;
        this.registrarVentaUseCase = registrarVentaUseCase;
    }

    public synchronized void agregarProducto(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        int cantidadEnCarrito = partidas.stream()
                .filter(p -> Objects.equals(p.getProducto().getCodigo(), producto.getCodigo()))
                .mapToInt(Partida::getCantidad)
                .sum();

        if (cantidadEnCarrito + cantidad > producto.getExistencia()) {
            throw new IllegalArgumentException("Existencia insuficiente para '" + producto.getNombre()
                    + "'. Disponible: " + producto.getExistencia()
                    + ", en carrito: " + cantidadEnCarrito);
        }

        Partida partidaExistente = partidas.stream()
                .filter(p -> Objects.equals(p.getProducto().getCodigo(), producto.getCodigo()))
                .findFirst()
                .orElse(null);

        if (partidaExistente != null) {
            partidaExistente.setCantidad(partidaExistente.getCantidad() + cantidad);
        } else {
            partidas.add(new Partida(producto, cantidad));
        }
    }

    public synchronized void eliminarPartida(int index) {
        if (index < 0 || index >= partidas.size()) {
            throw new IndexOutOfBoundsException("Índice de partida inválido: " + index);
        }
        partidas.remove(index);
    }

    public synchronized void limpiarVenta() {
        partidas.clear();
    }

    public synchronized List<Partida> getPartidas() {
        return Collections.unmodifiableList(new ArrayList<>(partidas));
    }

    public synchronized double calcularSubtotal() {
        return partidas.stream()
                .mapToDouble(Partida::getSubtotal)
                .sum();
    }

    public synchronized int getCantidadTotalArticulos() {
        return partidas.stream()
                .mapToInt(Partida::getCantidad)
                .sum();
    }

    public SwingWorker<List<Producto>, Void> cargarProductos(Consumer<List<Producto>> onSuccess,
                                                             Consumer<String> onError) {
        SwingWorker<List<Producto>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Producto> doInBackground() {
                return consultarProductosUseCase.consultarTodos();
            }

            @Override
            protected void done() {
                try {
                    List<Producto> productos = get();
                    if (onSuccess != null) {
                        onSuccess.accept(productos);
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String mensaje = cause.getMessage() != null ? cause.getMessage() : "Error al consultar productos.";
                    if (onError != null) {
                        onError.accept(mensaje);
                    }
                }
            }
        };
        worker.execute();
        return worker;
    }

    public SwingWorker<Venta, Void> registrarVenta(Consumer<Venta> onSuccess,
                                                   Consumer<String> onError) {
        List<Partida> copiaPartidas;
        synchronized (this) {
            if (partidas.isEmpty()) {
                String mensaje = "No se puede registrar una venta sin partidas.";
                if (onError != null) {
                    onError.accept(mensaje);
                }
                return null;
            }
            copiaPartidas = new ArrayList<>(partidas);
        }

        SwingWorker<Venta, Void> worker = new SwingWorker<>() {
            @Override
            protected Venta doInBackground() {
                Venta venta = VentaFactory.crearVenta(copiaPartidas);
                return registrarVentaUseCase.registrar(venta);
            }

            @Override
            protected void done() {
                try {
                    Venta ventaGuardada = get();
                    limpiarVenta();
                    if (onSuccess != null) {
                        onSuccess.accept(ventaGuardada);
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String mensaje = cause.getMessage() != null ? cause.getMessage() : "Error al registrar la venta.";
                    if (onError != null) {
                        onError.accept(mensaje);
                    }
                }
            }
        };
        worker.execute();
        return worker;
    }
}
