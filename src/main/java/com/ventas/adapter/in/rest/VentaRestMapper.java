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

/**
 * Traduce entre los DTOs REST y el modelo de dominio, para que el núcleo hexagonal no conozca el formato JSON.
 *
 * <p>El contrato OpenAPI identifica productos con un {@code productoId} entero, mientras que el dominio usa
 * {@link Producto#getCodigo()} (texto). La correspondencia es {@code codigo == String.valueOf(productoId)}.
 */
final class VentaRestMapper {

    private VentaRestMapper() {
    }

    static Venta aVenta(NuevaVentaPeticion peticion, List<Producto> catalogo) {
        Map<String, Producto> porCodigo = catalogo.stream()
                .collect(Collectors.toMap(Producto::getCodigo, Function.identity(), (a, b) -> a));

        List<Partida> partidas = new ArrayList<>();
        for (ItemVentaPeticion item : peticion.getPartidas()) {
            Producto producto = Optional.ofNullable(porCodigo.get(codigoDe(item.getProductoId())))
                    .orElseThrow(() -> new ProductoNoEncontradoException(item.getProductoId()));
            partidas.add(new Partida(producto, item.getCantidad()));
        }
        return VentaFactory.crearVenta(partidas);
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
        try {
            return Long.valueOf(codigo);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "El código de producto '" + codigo + "' no es numérico y no puede exponerse como productoId.", e);
        }
    }
}
