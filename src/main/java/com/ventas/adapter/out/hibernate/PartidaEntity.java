package com.ventas.adapter.out.hibernate;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name = "partida")

public class PartidaEntity {

@Id 
@GeneratedValue (strategy = GenerationType.IDENTITY)
private Long id;

@Column (name = "cantidad", nullable = false)
private int cantidad;

@Column (name = "precio_unitario", nullable = false)
private BigDecimal precioUnitario;


@ManyToOne 
@JoinColumn(name = "venta_id")
private VentaEntity ventaEntity;

@ManyToOne 
@JoinColumn(name = "producto_codigo")
private ProductoEntity productoEntity;

public PartidaEntity(Long id, int cantidad, BigDecimal precioUnitario, VentaEntity ventaEntity,
        ProductoEntity productoEntity) {
    this.id = id;
    this.cantidad = cantidad;
    this.precioUnitario = precioUnitario;
    this.ventaEntity = ventaEntity;
    this.productoEntity = productoEntity;
}

public PartidaEntity() {
}

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public int getCantidad() {
    return cantidad;
}

public void setCantidad(int cantidad) {
    this.cantidad = cantidad;
}

public BigDecimal getPrecioUnitario() {
    return precioUnitario;
}

public void setPrecioUnitario(BigDecimal precioUnitario) {
    this.precioUnitario = precioUnitario;
}

public VentaEntity getVentaEntity() {
    return ventaEntity;
}

public void setVentaEntity(VentaEntity ventaEntity) {
    this.ventaEntity = ventaEntity;
}

public ProductoEntity getProductoEntity() {
    return productoEntity;
}

public void setProductoEntity(ProductoEntity productoEntity) {
    this.productoEntity = productoEntity;
}

}



