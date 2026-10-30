# C02 — Consistencia transaccional del inventario (issue #40)

> Mi parte individual en C02 — Alessandro Osorio (GitHub: `Alessito54`).
> Criterio de R03 que atiendo: **consistencia transaccional (1.5 pts)** —
> *operación completa, error controlado y estado consistente*.

## 1. El problema que encontré

`JdbcVentaRepository.insertar()` abría la transacción, insertaba la venta, insertaba
sus partidas y hacía `commit`. Nunca tocaba `producto.existencia`.

Consecuencias reales, no hipotéticas:

| Síntoma | Evidencia en el código anterior |
|---|---|
| Sobreventa | Una venta de 1000 piezas de `P001` (semilla: `existencia = 40`) commiteaba sin error |
| Inventario congelado | Después de N ventas, `producto.existencia` seguía mostrando el valor de la semilla |
| La regla vivía en la vista | `VentaController.agregarProducto()` comparaba contra el `existencia` del `CatalogoDTO` cargado al abrir la ventana, y solo si `existencia > 0` |
| Carrera entre clientes | Dos ventanas abiertas con el mismo catálogo pasaban ambas la validación y ambas commiteaban |

Dicho en los términos de M11: la transacción era **atómica pero incompleta**. Garantizaba
que venta y partidas entraran juntas, pero la unidad de trabajo del negocio incluye un
tercer efecto —mover el inventario— que quedaba fuera del `commit`.

## 2. Lo que decidí

El descuento se hace **dentro de la misma transacción**, con una sola sentencia por
producto:

```sql
UPDATE producto SET existencia = existencia - ?
 WHERE codigo = ? AND existencia >= ?
RETURNING existencia
```

Tres decisiones que quiero justificar, porque en todas descarté algo antes:

**a) La condición va en el `WHERE`, no en un `SELECT` previo.**
Lo primero que escribí fue leer la existencia, compararla en Java y después actualizar.
Lo tiré: entre la lectura y la escritura hay una ventana en la que dos transacciones leen
`40`, las dos concluyen que caben `30` y las dos actualizan. Al poner la condición en el
`WHERE`, la segunda transacción espera el lock del renglón y, cuando lo obtiene, ya ve el
valor descontado y **no actualiza ninguna fila**. La decisión la toma la base, que es la
única que ve el estado comprometido. El `CHECK (existencia >= 0)` del esquema queda como
segunda barrera, no como barrera única.

**b) Los `UPDATE` van ordenados por código de producto.**
Hice que `unidadesPorProducto()` devuelva un `TreeMap`, así todas las ventas toman los
locks de `producto` en el mismo orden. Dos ventas con los mismos artículos en orden inverso no se
pueden interbloquear: una espera a la otra y termina.

**c) Consolido las partidas repetidas antes de descontar.**
Si la venta trae dos partidas del mismo artículo, sumo la cantidad y descuento una sola
vez. El documento de la venta conserva las dos partidas; el inventario se mueve una.

## 3. Error controlado: negocio vs. infraestructura

Un `UPDATE` que no afectó renglones tiene dos causas posibles, y no son la misma cosa:

| Causa | Excepción | Tipo de error |
|---|---|---|
| El producto no existe en el catálogo | `PersistenciaException` (envuelve una `SQLException`) | Integridad / infraestructura |
| El producto existe pero no alcanza | `ExistenciaInsuficienteException` | **Regla de negocio** |

Puse `ExistenciaInsuficienteException` en `com.ventas.model` porque "no vender más de lo
que hay" es una regla del dominio, no un detalle de PostgreSQL. Lleva `codigo`,
`solicitado` y `disponible`, así que el cliente Swing puede decirle al cajero exactamente
qué faltó en lugar de mostrar un error de SQL. El dominio sigue sin importar `java.sql`.

Las pruebas de integración que ya existían cubrían rollbacks por **violación de FK o de
`CHECK`**, es decir casos que la base rechaza por sí sola. Ésta es la primera de una
operación que la base **aceptaría** y que el negocio debe rechazar: es el error controlado
que pide R03.

En los dos casos la transacción se revierte completa: no queda la venta, no quedan las
partidas, y las existencias que ya se habían descontado en la misma transacción —las de
los productos procesados antes del que falló— vuelven a su valor original. Eso último es lo
que compruebo en `unaVentaQueExcedeLaExistenciaSeRechazaYNoDejaRastro`.

## 4. Cambios

| Archivo | Cambio |
|---|---|
| `src/main/java/com/ventas/model/ExistenciaInsuficienteException.java` | **Nuevo.** Regla de negocio con `codigo`, `solicitado` y `disponible` |
| `src/main/java/com/ventas/adapter/out/jdbc/JdbcVentaRepository.java` | `descontarExistencias()`, `unidadesPorProducto()` y `sinExistencia()` dentro de la transacción existente |
| `src/test/java/com/ventas/adapter/out/jdbc/JdbcVentaRepositoryTest.java` | 5 pruebas unitarias nuevas (sin base de datos) |
| `src/test/java/com/ventas/adapter/out/jdbc/JdbcVentaRepositoryIT.java` | 7 pruebas de integración nuevas + 1 aserción añadida a una existente |

No toqué el puerto `VentaRepository`, ni `RegistrarVentaService`, ni el dominio
`Venta`/`Partida`, ni `adapter.out.hibernate` (#32, #34, #38), para no chocar con los
issues de mi compañero.

El cliente Swing no necesitó cambios: `ClienteVentasPanel` ya recarga el catálogo tras una
venta exitosa, así que la tabla ahora muestra la existencia ya descontada.

## 5. Pruebas

### Positivas

| Prueba | Qué demuestra |
|---|---|
| `laVentaDescuentaLaExistenciaDelCatalogoEnLaMismaTransaccion` | 100 → 93 y 100 → 98 al vender 7 y 2 |
| `variasPartidasDelMismoProductoDescuentanUnaSolaVezElTotal` | 4 + 6 del mismo artículo descuentan 10, no 20; las 2 partidas se conservan |
| `despuesDeUnaSobreventaRevertidaLaSiguienteVentaValidaSiDescuenta` | El adaptador sigue usable después de un rollback |

### Límite

| Prueba | Qué demuestra |
|---|---|
| `unaVentaQueAgotaLaExistenciaExactaSeAceptaYDejaElCatalogoEnCero` | Vender la existencia completa (100 de 100) se acepta y deja `0`. El límite es `>=`, no `>` |

### Negativas

| Prueba | Qué demuestra |
|---|---|
| `unaVentaQueExcedeLaExistenciaSeRechazaYNoDejaRastro` | 101 de 100: sin venta, sin partidas y **las dos** existencias intactas |
| `laSobreventaEsUnErrorDeNegocioYNoDeInfraestructura` | La excepción es de dominio, no `PersistenciaException` |
| `unRollbackPorIntegridadTampocoMueveElInventario` | Un fallo posterior al descuento (`CHECK` de `cantidad > 0`) también devuelve la existencia |
| `siUnaPartidaTieneProductoInexistenteSeHaceRollbackDeTodo` | (ampliada) el producto válido de la misma venta no queda descontado |

### Unitarias (sin base de datos)

`elDescuentoDeExistenciaLoDecideLaBaseYNoElAdaptador`,
`variasPartidasDelMismoProductoSeConsolidanEnUnSoloDescuento`,
`losDescuentosSeOrdenanPorCodigoParaEvitarInterbloqueos`,
`laExcepcionDeNegocioInformaSolicitadoYDisponible` y la ampliación de
`lasSentenciasSonParametrizadas`.

## 6. Reproducción

```bash
cp .env.example .env                 # cambiar el password; .env está en .gitignore
set -a; source .env; set +a
docker compose up -d                 # PostgreSQL 16
mvn clean verify                     # unitarias + integración (*IT)
docker compose down -v
```

Las pruebas de integración siembran y truncan sus propios productos sintéticos
(`SIN-001`, `SIN-002`), así que no dependen de la semilla del catálogo. Si `DB_URL`,
`DB_USER` o `DB_PASSWORD` no están definidas, los `*IT` se omiten en lugar de fallar.

Demostración manual del antes/después sobre el catálogo real:

```bash
docker exec -it tcsw-ventas-postgres \
  psql -U ventas_app -d ventas -c "SELECT codigo, existencia FROM producto ORDER BY codigo"
# registrar una venta desde el cliente Swing
docker exec -it tcsw-ventas-postgres \
  psql -U ventas_app -d ventas -c "SELECT codigo, existencia FROM producto ORDER BY codigo"
```

## 7. Estado declarado

| Elemento | Estado |
|---|---|
| Descuento transaccional de existencias | VERIFICADO |
| Rollback con existencias intactas | VERIFICADO |
| Pruebas unitarias y de integración | VERIFICADO (ver §8) |
| Comportamiento bajo concurrencia real (dos cajas simultáneas) | NO_VERIFICADO: la garantía viene del lock de renglón de PostgreSQL y del orden determinista de los `UPDATE`, pero no escribí una prueba concurrente |
| Reporte de SonarQube | PENDIENTE: se genera al integrar en `main` |
| Adaptador Hibernate equivalente | PENDIENTE y fuera de mi alcance (#32, #34, #38) |

## 8. Evidencia de ejecución

Contra PostgreSQL 16 en contenedor, en la rama `fix/issue-40-descuento-existencias`:

```
[INFO] Running com.ventas.adapter.out.jdbc.JdbcVentaRepositoryTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.060 s
[INFO] Running com.ventas.adapter.out.jdbc.JdbcVentaRepositoryIT
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.032 s
[INFO] BUILD SUCCESS
```

Suite completa del proyecto en la misma rama, `mvn clean verify`:

```
[INFO] Tests run: 70, Failures: 0, Errors: 0, Skipped: 0   <- surefire (unitarias)
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0   <- failsafe (*IT)
[INFO] BUILD SUCCESS
```

88 de 88 en verde, sin romper ninguna prueba existente.

Comandos exactos:

```bash
set -a; source .env; set +a
docker compose up -d
mvn clean verify
```

Nota de entorno: `ClienteVentasSwingFrameTest` tarda ~55 s porque abre ventanas
reales. En una ejecución anterior se quedó bloqueada en `invokeAndWait` y hubo que
abortar la corrida; no se reprodujo en las siguientes. Queda registrado por
transparencia: si vuelve a ocurrir es del entorno gráfico, no de este cambio.

## 9. Trazabilidad

| Issue | Implementación | Pruebas | Docs |
|---|---|---|---|
| [#40](https://github.com/Aleks-Lpz/tcsw_ventas_Gabriel/issues/40) | `model/ExistenciaInsuficienteException.java`, `adapter/out/jdbc/JdbcVentaRepository.java` | 12 nuevas (5 unitarias + 7 de integración) | este documento y §9 de [`docs/contribuciones/alessandro_osorio.md`](contribuciones/alessandro_osorio.md) |

Rama: `fix/issue-40-descuento-existencias`. PR: #42.
