package com.ventas.adapter.in.swing.controller;

import com.ventas.adapter.in.swing.DTO.CatalogoDTO;
import com.ventas.adapter.in.swing.DTO.PartidaDTO;
import com.ventas.adapter.in.swing.model.PartidaTableModel;
import com.ventas.application.port.in.RegistrarVentaUseCase;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import com.ventas.model.VentaFactory;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;
    private final PartidaTableModel modeloPartidas;
    private final List<Partida> partidas = new ArrayList<>();

    public VentaController(RegistrarVentaUseCase registrarVentaUseCase) {
        if (registrarVentaUseCase == null) {
            throw new IllegalArgumentException("El caso de uso de registro de venta no puede ser nulo.");
        }
        this.registrarVentaUseCase = registrarVentaUseCase;
        this.modeloPartidas = new PartidaTableModel();
    }

    public PartidaTableModel getModeloPartidas() {
        return modeloPartidas;
    }

    public synchronized void agregarProducto(CatalogoDTO dto, int cantidad) {
        if (dto == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto válido.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        int cantidadEnCarrito = partidas.stream()
                .filter(p -> Objects.equals(p.getProducto().getCodigo(), dto.getCodigo()))
                .mapToInt(Partida::getCantidad)
                .sum();

        if (dto.getExistencia() > 0 && cantidadEnCarrito + cantidad > dto.getExistencia()) {
            throw new IllegalArgumentException("Existencia insuficiente para '" + dto.getNombre()
                    + "'. Disponible: " + dto.getExistencia()
                    + ", en carrito: " + cantidadEnCarrito);
        }

        Partida partidaExistente = partidas.stream()
                .filter(p -> Objects.equals(p.getProducto().getCodigo(), dto.getCodigo()))
                .findFirst()
                .orElse(null);

        if (partidaExistente != null) {
            partidaExistente.setCantidad(partidaExistente.getCantidad() + cantidad);
        } else {
            Producto productoDominio = new Producto(
                    dto.getCodigo(),
                    dto.getNombre(),
                    dto.getPrecio(),
                    dto.getExistencia()
            );
            partidas.add(new Partida(productoDominio, cantidad));
        }
        sincronizarModeloTabla();
    }

    public synchronized void eliminarPartida(int index) {
        if (index < 0 || index >= partidas.size()) {
            throw new IndexOutOfBoundsException("Índice de partida inválido: " + index);
        }
        partidas.remove(index);
        sincronizarModeloTabla();
    }

    public synchronized void limpiarVenta() {
        partidas.clear();
        modeloPartidas.limpiar();
    }

    public synchronized boolean tienePartidas() {
        return !partidas.isEmpty();
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

    private void sincronizarModeloTabla() {
        List<PartidaDTO> proyeccion = partidas.stream()
                .map(p -> new PartidaDTO(
                        p.getProducto().getCodigo(),
                        p.getProducto().getNombre(),
                        p.getProducto().getPrecio(),
                        p.getCantidad(),
                        p.getSubtotal()
                ))
                .collect(Collectors.toList());
        modeloPartidas.setPartidas(proyeccion);
    }

    public SwingWorker<Double, Void> registrarVenta(Consumer<Double> onSuccess,
                                                    Consumer<String> onError) {
        List<Partida> copiaPartidas;
        synchronized (this) {
            if (partidas.isEmpty()) {
                if (onError != null) {
                    onError.accept("No se puede registrar una venta sin partidas.");
                }
                return null;
            }
            copiaPartidas = new ArrayList<>(partidas);
        }

        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() {
                Venta venta = VentaFactory.crearVenta(copiaPartidas);
                Venta ventaGuardada = registrarVentaUseCase.registrar(venta);
                return ventaGuardada.calcularTotal();
            }

            @Override
            protected void done() {
                try {
                    Double totalCobrado = get();
                    limpiarVenta();
                    if (onSuccess != null) {
                        onSuccess.accept(totalCobrado);
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
