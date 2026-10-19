package com.ventas.adapter.in.swing.DTO;

import java.util.HashMap;
import java.util.Map;

public class CarritoDTO {
Map<String, Integer> productoYCantidad = new HashMap<>();

public void añadirProductos(String codigo, int cantidad) {
productoYCantidad.put(codigo, cantidad);
}

public Map getProductos() {
    return productoYCantidad;
}

}
