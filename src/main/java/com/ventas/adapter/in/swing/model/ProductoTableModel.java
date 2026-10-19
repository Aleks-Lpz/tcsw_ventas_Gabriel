package com.ventas.adapter.in.swing.model;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.table.AbstractTableModel;

import com.ventas.adapter.in.swing.DTO.CatalogoDTO;

public class ProductoTableModel extends AbstractTableModel {
 private final List<CatalogoDTO> productos = new ArrayList<>();
    private final String[] columnas = {"Código", "Nombre", "Precio", "Existencia"};
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public ProductoTableModel() {
    }

    public ProductoTableModel(List<CatalogoDTO> productosIniciales) {
        setProductos(productosIniciales);
    }

    public void setProductos(List<CatalogoDTO> nuevosProductos) {
        productos.clear();
        if (nuevosProductos != null) {
            productos.addAll(nuevosProductos);
        }
        fireTableDataChanged();
    }

    public CatalogoDTO getProductoAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= productos.size()) {
            return null;
        }
        return productos.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return productos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 3) {
            return Integer.class;
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CatalogoDTO producto = productos.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return producto.getCodigo();
            case 1:
                return producto.getNombre();
            case 2:
                return formatoMoneda.format(producto.getPrecio());
            case 3:
                return producto.getExistencia();
            default:
                return null;
        }
    }

}
