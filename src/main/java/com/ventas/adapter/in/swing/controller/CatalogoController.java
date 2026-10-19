package com.ventas.adapter.in.swing.controller;

import com.ventas.adapter.in.swing.DTO.CatalogoDTO;
import com.ventas.adapter.in.swing.model.ProductoTableModel;
import com.ventas.application.port.in.ConsultarProductosUseCase;
import com.ventas.model.Producto;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CatalogoController {

    private final ConsultarProductosUseCase consultarProductosUseCase;
    private final ProductoTableModel modeloProductos;

    public CatalogoController(ConsultarProductosUseCase consultarProductosUseCase) {
        if (consultarProductosUseCase == null) {
            throw new IllegalArgumentException("El caso de uso de consulta de productos no puede ser nulo.");
        }
        this.consultarProductosUseCase = consultarProductosUseCase;
        this.modeloProductos = new ProductoTableModel();
    }

    public ProductoTableModel getModeloProductos() {
        return modeloProductos;
    }

    public CatalogoDTO obtenerProductoEnFila(int fila) {
        return modeloProductos.getProductoAt(fila);
    }

    public SwingWorker<List<CatalogoDTO>, Void> cargarProductos(Consumer<Integer> onSuccess,
                                                                Consumer<String> onError) {
        SwingWorker<List<CatalogoDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CatalogoDTO> doInBackground() {
                List<Producto> productos = consultarProductosUseCase.consultarTodos();
                List<CatalogoDTO> catalogoDTOs = new ArrayList<>();
                for (Producto producto : productos) {
                    CatalogoDTO dto = new CatalogoDTO(
                            producto.getCodigo(),
                            producto.getNombre(),
                            producto.getPrecio(),
                            producto.getExistencia()
                    );
                    catalogoDTOs.add(dto);
                }
                return catalogoDTOs;
            }

            @Override
            protected void done() {
                try {
                    List<CatalogoDTO> resultado = get();
                    modeloProductos.setProductos(resultado);
                    if (onSuccess != null) {
                        onSuccess.accept(resultado.size());
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
}

