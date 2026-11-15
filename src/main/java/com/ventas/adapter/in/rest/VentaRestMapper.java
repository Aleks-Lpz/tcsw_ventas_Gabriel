package com.ventas.adapter.in.rest;

import com.ventas.adapter.in.rest.dto.ItemVentaPeticion;
import com.ventas.adapter.in.rest.dto.NuevaVentaPeticion;
import com.ventas.adapter.in.rest.dto.PartidaRespuesta;
import com.ventas.adapter.in.rest.dto.VentaRespuesta;
import com.ventas.model.Partida;
import com.ventas.model.Producto;
import com.ventas.model.Venta;
import com.ventas.model.VentaFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

final class VentaRestMapper {

    private VentaRestMapper() {
    }

    static Venta aVenta(NuevaVentaPeticion peticion, List<Producto> catalogo) {
        Map<String, Producto> porCodigo = catalogo.stream()
                .collect(Collectors.toMap(Producto::getCodigo, Function.identity(), (a, b) -> a));

        List<Partida> partidas = new ArrayList<>();
        for (ItemVentaPeticion item : peticion.getPartidas()) {
            Producto producto = buscarProducto(porCodigo, item.getProductoId())
                    .orElseThrow(() -> new ProductoNoEncontradoException(item.getProductoId()));
            partidas.add(new Partida(producto, item.getCantidad()));
        }
        return VentaFactory.crearVenta(partidas);
    }

    private static Optional<Producto> buscarProducto(Map<String, Producto> porCodigo, Long productoId) {
        if (productoId == null) {
            return Optional.empty();
        }
        // 1. Coincidencia directa ("101" -> 101)
        Producto exacto = porCodigo.get(String.valueOf(productoId));
        if (exacto != null) {
            return Optional.of(exacto);
        }
        // 2. Coincidencia con formato de semilla ("P001" -> 1)
        String codigoFormateado = String.format("P%03d", productoId);
        Producto porPrefijo = porCodigo.get(codigoFormateado);
        if (porPrefijo != null) {
            return Optional.of(porPrefijo);
        }
        // 3. Búsqueda por equivalencia numérica de productoIdDe(codigo)
        return porCodigo.values().stream()
                .filter(p -> {
                    try {
                        return productoId.equals(productoIdDe(p.getCodigo()));
                    } catch (IllegalStateException ex) {
                        return false;
                    }
                })
                .findFirst();
    }

    static VentaRespuesta aRespuesta(Venta venta) {
        List<PartidaRespuesta> partidas = new ArrayList<>();
        for (Partida partida : venta.getPartidas()) {
            Producto producto = partida.getProducto();
            partidas.add(new PartidaRespuesta(
                    productoIdDe(producto.getCodigo()),
                    producto.getNombre(),
                    partida.getCantidad(),
                    producto.getPrecio(),
                    partida.getSubtotal()));
        }
        return new VentaRespuesta(
                venta.getId(),
                venta.getFecha().toOffsetDateTime(),
                venta.getDescuento(),
                venta.calcularTotal(),
                partidas);
    }

    static String codigoDe(Long productoId) {
        return String.valueOf(productoId);
    }

    static Long productoIdDe(String codigo) {
        if (codigo == null) {
            throw new IllegalStateException("El código de producto no puede ser nulo.");
        }
        try {
            return Long.valueOf(codigo);
        } catch (NumberFormatException e) {
            String soloDigitos = codigo.replaceAll("\\D+", "");
            if (!soloDigitos.isEmpty()) {
                return Long.valueOf(soloDigitos);
            }
            throw new IllegalStateException(
                    "El código de producto '" + codigo + "' no contiene dígitos para exponerse como productoId.", e);
        }
    }
}