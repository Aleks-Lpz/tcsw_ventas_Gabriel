package com.ventas.adapter.in.rest;

import com.ventas.model.Producto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductoRestMapperTest {
    @Test
    void transfiereTodosLosCamposDelRequestAlDominio() {
        ProductoRequest request = new ProductoRequest();
        request.setCodigo("P001");
        request.setNombre("Café molido");
        request.setPrecio(125.75);
        request.setExistencia(0);

        Producto producto = ProductoRestMapper.toDomain(request);

        assertAll(
                () -> assertEquals("P001", producto.getCodigo()),
                () -> assertEquals("Café molido", producto.getNombre()),
                () -> assertEquals(125.75, producto.getPrecio()),
                () -> assertEquals(0, producto.getExistencia()));
        request.setNombre("Otro");
        assertEquals("Café molido", producto.getNombre());
    }

    @Test
    void transfiereTodosLosCamposDelDominioAlResponse() {
        Producto producto = new Producto("P002", "Teclado", 350.50, 12);

        ProductoResponse response = ProductoRestMapper.toResponse(producto);

        assertAll(
                () -> assertEquals("P002", response.getCodigo()),
                () -> assertEquals("Teclado", response.getNombre()),
                () -> assertEquals(350.50, response.getPrecio()),
                () -> assertEquals(12, response.getExistencia()));
        producto.setExistencia(3);
        assertEquals(12, response.getExistencia());
    }
}
