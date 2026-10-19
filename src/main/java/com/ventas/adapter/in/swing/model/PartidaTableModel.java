package com.ventas.adapter.in.swing.model;

import com.ventas.adapter.in.swing.DTO.PartidaDTO;
import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PartidaTableModel extends AbstractTableModel {

    private final List<PartidaDTO> partidas = new ArrayList<>();
    private final String[] columnas = {"Código", "Producto", "Precio Unit.", "Cant.", "Subtotal"};
    private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

    public void setPartidas(List<PartidaDTO> nuevasPartidas) {
        partidas.clear();
        if (nuevasPartidas != null) {
            partidas.addAll(nuevasPartidas);
        }
        fireTableDataChanged();
    }

    public void eliminarFila(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < partidas.size()) {
            partidas.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    public void limpiar() {
        int totalFilas = partidas.size();
        if (totalFilas > 0) {
            partidas.clear();
            fireTableRowsDeleted(0, totalFilas - 1);
        }
    }

    @Override
    public int getRowCount() {
        return partidas.size();
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
        PartidaDTO partida = partidas.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return partida.getCodigo();
            case 1:
                return partida.getNombre();
            case 2:
                return formatoMoneda.format(partida.getPrecioUnitario());
            case 3:
                return partida.getCantidad();
            case 4:
                return formatoMoneda.format(partida.getSubtotal());
            default:
                return null;
        }
    }
}