package com.ventas.adapter.out.hibernate;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity 
@ Table (name = "venta")

public class VentaEntity {

@Id
@GeneratedValue (strategy = GenerationType.IDENTITY)
private Long id;

@Column (name = "fecha", nullable = false)
private ZonedDateTime fecha;

@Column (name = "descuento", nullable = false)
private BigDecimal descuento;

@Column (name = "total", nullable = false)
private BigDecimal total;

@OneToMany (mappedBy = "ventaEntity", cascade = CascadeType.ALL, orphanRemoval = true)
private List<PartidaEntity> partidas;

public VentaEntity(Long id, ZonedDateTime fecha, BigDecimal descuento, BigDecimal total, 
        List<PartidaEntity> partidas) {
            this.id=id;
            this.fecha=fecha;
            this.descuento=descuento;
            this.total=total;
            this.partidas=partidas;
    }

public VentaEntity() {

}

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}

public ZonedDateTime getFecha() {
    return fecha;
}

public void setFecha(ZonedDateTime fecha) {
    this.fecha = fecha;
}

public BigDecimal getDescuento() {
    return descuento;
}

public void setDescuento(BigDecimal descuento) {
    this.descuento = descuento;
}

public BigDecimal getTotal() {
    return total;
}

public void setTotal(BigDecimal total) {
    this.total = total;
}

public List<PartidaEntity> getPartidas() {
    return partidas;
}

public void setPartidas(List<PartidaEntity> partidas) {
    this.partidas = partidas;
}


}
