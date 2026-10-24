package com.ventas.adapter.out.jdbc;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuracion de conexion para los adaptadores JDBC.
 * <p>
 * Las credenciales se leen de variables de entorno (DB_URL, DB_USER, DB_PASSWORD)
 * para que nunca queden escritas en el codigo ni en el repositorio.
 */
public final class ConfiguracionJdbc {

    public static final String VAR_URL = "DB_URL";
    public static final String VAR_USUARIO = "DB_USER";
    public static final String VAR_PASSWORD = "DB_PASSWORD";

    private final String url;
    private final String usuario;
    private final String password;

    public ConfiguracionJdbc(String url, String usuario, String password) {
        this.url = requerido(url, VAR_URL);
        this.usuario = requerido(usuario, VAR_USUARIO);
        this.password = Objects.requireNonNull(password, VAR_PASSWORD + " es obligatorio");
    }

    /** Lee la configuracion del entorno del proceso. */
    public static Optional<ConfiguracionJdbc> desdeEntorno() {
        return desde(System.getenv());
    }

    /** Lee la configuracion de un mapa; vacio si falta alguna variable. */
    static Optional<ConfiguracionJdbc> desde(Map<String, String> entorno) {
        String url = entorno.get(VAR_URL);
        String usuario = entorno.get(VAR_USUARIO);
        String password = entorno.get(VAR_PASSWORD);
        if (vacio(url) || vacio(usuario) || password == null) {
            return Optional.empty();
        }
        return Optional.of(new ConfiguracionJdbc(url, usuario, password));
    }

    public DataSource crearDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(url);
        dataSource.setUser(usuario);
        dataSource.setPassword(password);
        return dataSource;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        // Nunca se imprime el password.
        return "ConfiguracionJdbc{url='" + url + "', usuario='" + usuario + "', password='***'}";
    }

    private static String requerido(String valor, String nombre) {
        if (vacio(valor)) {
            throw new IllegalArgumentException(nombre + " es obligatorio");
        }
        return valor;
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
