# P08 · Persistencia de ventas con JDBC y PostgreSQL 16

Parte del equipo a cargo de **Alessandro Osorio (Alessito54)**: adaptador `JdbcVentaRepository`, que guarda una venta y sus partidas en **una sola transacción**.

## 1. Qué agregué

| Archivo | Propósito |
|---|---|
| `docker-compose.yml` | PostgreSQL 16 en Docker; carga `db/schema.sql` al crear el contenedor |
| `.env.example` | Plantilla de variables; el `.env` real está en `.gitignore` |
| `db/schema.sql` | Tablas `producto`, `venta`, `partida` con PK, FK y `CHECK` |
| `adapter/out/jdbc/JdbcVentaRepository.java` | Implementa el puerto `VentaRepository` con `PreparedStatement` y transacción |
| `adapter/out/jdbc/ConfiguracionJdbc.java` | Lee `DB_URL`, `DB_USER`, `DB_PASSWORD` del entorno y crea el `DataSource` |
| `adapter/out/jdbc/PersistenciaException.java` | Envuelve `SQLException` para no filtrar `java.sql` al núcleo |
| `JdbcVentaRepositoryTest` | 6 pruebas unitarias (sin base de datos) |
| `JdbcVentaRepositoryIT` | 7 pruebas de integración contra PostgreSQL real |

No modifiqué ningún puerto (`VentaRepository` sigue igual) ni el dominio.

## 2. Decisiones técnicas (M10)

- **Mapeo relacional.** `Venta` 1 → N `Partida`; cada `Partida` referencia un `producto` por `codigo` (FK). En `partida` guardo `precio_unitario` porque el precio del producto puede cambiar después y la venta debe conservar el precio con el que se vendió.
- **Dinero como `NUMERIC(12,2)`.** El dominio usa `double`; en el adaptador lo convierto a `BigDecimal` con 2 decimales para no guardar errores de punto flotante.
- **Transacción.** Desactivo `autoCommit`, inserto la venta (`RETURNING id`), inserto las partidas en *batch* y hago `commit`. Ante cualquier error hago `rollback`, restauro `autoCommit` y lanzo `PersistenciaException`. Así nunca queda una venta sin partidas.
- **PreparedStatement.** Todos los valores viajan como parámetros `?`. Hay una prueba que usa un código de producto con `DROP TABLE` y demuestra que se trata como dato.
- **Integridad en dos niveles.** El adaptador rechaza `null` y ventas sin partidas *antes* de abrir conexión. La base rechaza lo que el modelo no alcanza a impedir (por ejemplo, `Partida.setCantidad(0)` después de construirla) con `CHECK (cantidad > 0)`.
- **Credenciales.** No hay contraseñas en el repositorio; Docker Compose falla si `POSTGRES_PASSWORD` no está definido.

## 3. Reproducción

Requisitos: Java 11+, Maven 3.9+, Docker con Docker Compose.

```bash
git clone https://github.com/Aleks-Lpz/tcsw_ventas_Gabriel
cd tcsw_ventas_Gabriel
cp .env.example .env            # cambia el password
docker compose up -d            # PostgreSQL 16 + schema.sql
docker compose ps               # esperar estado "healthy"

set -a; source .env; set +a     # exporta DB_URL, DB_USER, DB_PASSWORD
mvn clean verify                # unitarias (surefire) + integración *IT (failsafe)

docker compose down -v          # limpiar recursos locales
```

Sin las variables `DB_*`, `mvn verify` no falla: las pruebas `*IT` se omiten por una suposición (`assumeTrue`) y las unitarias se ejecutan normal.

## 4. Pruebas

| Prueba | Tipo | Qué demuestra |
|---|---|---|
| `guardaLaVentaConSusPartidasYSePuedeRecuperar` | positiva | Persiste y recupera total y partidas |
| `guardarDevuelveLaMismaVentaComoIndicaElPuerto` | positiva | Respeta el contrato del puerto |
| `guardaElDescuentoYElTotalNeto` | positiva | Guarda descuento y total neto |
| `siUnaPartidaTieneProductoInexistenteSeHaceRollbackDeTodo` | negativa | Atomicidad: FK falla → 0 ventas, 0 partidas |
| `laBaseRechazaUnaCantidadInvalidaYRevierteLaTransaccion` | negativa | `CHECK` + rollback |
| `unCodigoMaliciosoSeTrataComoDatoYNoComoSql` | negativa | Protección contra inyección SQL |
| `despuesDeUnRollbackElRepositorioSigueFuncionando` | límite | La conexión queda sana tras un rollback |
| `unaVentaNullSeRechazaSinAbrirConexion` | negativa | Validación previa, sin tocar la BD |
| `unaVentaSinPartidasSeRechazaSinAbrirConexion` | negativa | Estado inválido rechazado |
| `noSePuedeCrearSinDataSource` | negativa | Dependencia obligatoria |
| `lasSentenciasSonParametrizadas` | estructura | SQL con `?` |
| `laConfiguracionEsVaciaSiFaltaAlgunaVariable` | límite | Sin variables no se intenta conectar |
| `laConfiguracionNuncaMuestraElPassword` | seguridad | `toString()` oculta el password |

### Salida obtenida (sanitizada)

PostgreSQL 16.15, Java 21 compilando con `--release 11`, JUnit 5.10:

```text
JdbcVentaRepositoryTest
  laConfiguracionEsVaciaSiFaltaAlgunaVariable()        ✔
  lasSentenciasSonParametrizadas()                     ✔
  laConfiguracionNuncaMuestraElPassword()              ✔
  unaVentaNullSeRechazaSinAbrirConexion()              ✔
  noSePuedeCrearSinDataSource()                        ✔
  unaVentaSinPartidasSeRechazaSinAbrirConexion()       ✔
JdbcVentaRepositoryIT
  siUnaPartidaTieneProductoInexistenteSeHaceRollbackDeTodo()  ✔
  guardaElDescuentoYElTotalNeto()                             ✔
  laBaseRechazaUnaCantidadInvalidaYRevierteLaTransaccion()    ✔
  guardarDevuelveLaMismaVentaComoIndicaElPuerto()             ✔
  unCodigoMaliciosoSeTrataComoDatoYNoComoSql()                ✔
  despuesDeUnRollbackElRepositorioSigueFuncionando()          ✔
  guardaLaVentaConSusPartidasYSePuedeRecuperar()              ✔

[ 73 tests found ]  [ 73 tests successful ]  [ 0 tests failed ]
```

```text
ventas=> \dt
 Schema |   Name   | Type  |   Owner
--------+----------+-------+------------
 public | partida  | table | ventas_app
 public | producto | table | ventas_app
 public | venta    | table | ventas_app

ventas=> \d partida   (restricciones)
Check constraints:
    "partida_cantidad_check" CHECK (cantidad > 0)
    "partida_precio_unitario_check" CHECK (precio_unitario > 0::numeric)
Foreign-key constraints:
    "partida_producto_codigo_fkey" FOREIGN KEY (producto_codigo) REFERENCES producto(codigo)
    "partida_venta_id_fkey" FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE CASCADE
```

## 5. Estado de verificación

| Elemento | Estado | Evidencia |
|---|---|---|
| `schema.sql` en PostgreSQL 16 | VERIFICADO | Aplicado sobre PostgreSQL 16.15; salida de `\dt` y `\d partida` |
| `JdbcVentaRepository` (guardar, recuperar, rollback) | VERIFICADO | 7 pruebas IT en verde |
| Pruebas unitarias del adaptador | VERIFICADO | 6 pruebas en verde |
| Pruebas existentes del proyecto | VERIFICADO | 73/73 en verde, sin regresiones |
| Compatibilidad Java 11 | VERIFICADO | Compilado con `javac --release 11` |
| `mvn clean verify` | NO_VERIFICADO | Mi entorno no tenía acceso a Maven Central; ejecuté las mismas pruebas con JUnit Console |
| `docker compose up` | NO_VERIFICADO | Mi entorno no tenía Docker; probé con PostgreSQL 16 nativo y el mismo `schema.sql` |
| `JdbcProductoRepository` | PENDIENTE | Fuera de mi parte; queda para otro integrante |
| Reporte Sonar | PENDIENTE | Se genera al integrar en `main` |

## 6. Índice contra R01

| Criterio R01 | Evidencia |
|---|---|
| Funcionamiento (0.8) | Sección 3 (reproducción) y salida de la sección 4 |
| Solución técnica (0.5) | Sección 2 y `adapter/out/jdbc/` |
| Pruebas (0.3) | `JdbcVentaRepositoryTest`, `JdbcVentaRepositoryIT` |
| Calidad y Sonar (0.2) | Recursos con `try-with-resources`, sin secretos; Sonar PENDIENTE |
| Git y evidencia (0.2) | Issue, rama `feat/p08-jdbc-venta-transaccional`, commits y PR; `docs/contribuciones/alessandro_osorio.md` |
