package com.ventas.adapter.out.jdbc;
import com.ventas.application.port.out.ProductoRepository;

import com.ventas.model.Producto;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class JdbcProductoRepository implements ProductoRepository {
    static final String INSERT_PRODUCTO = 
        "INSERT INTO producto (codigo, nombre, precio, existencia) VALUES (?, ?, ?, ?) RETURNING codigo";

    static final String SELECT_PRODUCTOS = 
        "SELECT * FROM producto";

    private final DataSource dataSource;

    public JdbcProductoRepository(DataSource dataSource) {
        this.dataSource=Objects.requireNonNull(dataSource, "El dataSource no puede ser nulo");
    }

    @Override
    public Producto guardar(Producto producto) {
    insertar(producto);
    return producto;
    }

    @Override
    public List<Producto> obtenerTodos() {
        List<Producto> productos = new ArrayList<Producto>();

        try(Connection conexion = dataSource.getConnection();
             PreparedStatement ps = conexion.prepareStatement(SELECT_PRODUCTOS);
            ResultSet rs = ps.executeQuery()) {

                while(rs.next()) {
                    String codigo = rs.getString("codigo");
                    String nombre = rs.getString("nombre");
                    double precio = rs.getBigDecimal("precio").doubleValue();
                    int existencia = rs.getInt("existencia");
                    Producto producto = new Producto(codigo, nombre, precio, existencia);
                    productos.add(producto);
                }
        } catch(SQLException e) {
            throw new PersistenciaException(
                "Error al consultar los productos", e);
        }
                return productos;
    }

    public String insertar(Producto producto) {
        validar(producto);

        try(Connection conexion = dataSource.getConnection()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
        try {
            String codigo = insertarProducto(conexion, producto);
            conexion.commit();
            return codigo;
        } catch(SQLException | RuntimeException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(autoCommitOriginal);
        }
        } catch(SQLException e) {
            throw new PersistenciaException(
        "No se pudo guardar el producto, la inserción se revirtio. Causa: " + causaRaiz(e), e);
        }
    }

    private void validar(Producto producto) {
        if(producto==null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }

        if(producto.getExistencia() < 0 || producto.getPrecio()<=0) {
            throw new IllegalArgumentException("Las existencias y el precio no pueden ser negativos");
        }
    }

    private String insertarProducto(Connection conexion, Producto producto) throws SQLException {
        BigDecimal precio = dinero(producto.getPrecio());

        try (PreparedStatement ps = conexion.prepareStatement(INSERT_PRODUCTO)){
            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setBigDecimal(3, precio);
            ps.setInt(4, producto.getExistencia());
            try(ResultSet rs = ps.executeQuery()) {
                if(!rs.next()) {
                    throw new SQLException("La base de datos no devolvió el codigo del producto");
                }
            return rs.getString(1);
            }
        }
    }

    private static BigDecimal dinero(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

        private static String causaRaiz(SQLException e) {
        SQLException siguiente = e.getNextException();
        return siguiente != null ? siguiente.getMessage() : e.getMessage();
    }
}
