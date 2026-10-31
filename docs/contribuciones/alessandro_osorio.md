# Contribución individual P08 — Alessandro Osorio

> Bitácora de P08. Mis contribuciones de P04, P05 y P06 están en `docs/alessandro_osorio.md`.

## 1. Datos

- **Integrante:** Alessandro Osorio
- **Usuario de GitHub:** Alessito54
- **Actividad:** P08. Persistencia (M10)
- **Issue:** sin issue previo; el alcance se acordó en el PR
- **Rama:** `feat/p08-jdbc-venta-transaccional`
- **Pull request:** [#29](https://github.com/Aleks-Lpz/tcsw_ventas_Gabriel/pull/29) (merge `967da6f`)

## 2. Mi parte

En el equipo me encargué de **persistir las ventas**. Elegí esta parte porque en P05 yo implementé el puerto `VentaRepository` y su adaptador en memoria; en P08 lo continué con un adaptador real sobre PostgreSQL 16 sin tocar el puerto ni el dominio.

Lo que hice:

1. Levanté PostgreSQL 16 con `docker-compose.yml` y escribí `db/schema.sql` con las tablas `producto`, `venta` y `partida`, con llaves foráneas y `CHECK` que replican las reglas del dominio.
2. Implementé `JdbcVentaRepository`, que guarda la venta y todas sus partidas en una sola transacción usando `PreparedStatement`. Si algo falla, hago `rollback` y no queda nada a medias.
3. Separé la configuración en `ConfiguracionJdbc`, que lee las credenciales de variables de entorno, para no subir ninguna contraseña al repositorio.
4. Escribí 6 pruebas unitarias y 7 de integración, con casos positivos, negativos y de límite.
5. Documenté decisiones, reproducción y evidencia en `docs/P08-Persistencia-Venta.md`.

Dejé `JdbcProductoRepository` para otro integrante; mi esquema ya incluye la tabla `producto` que ese adaptador necesita.

## 3. Decisiones que tomé

- **Una transacción por venta.** Una venta sin sus partidas es un estado inválido. Por eso desactivo `autoCommit` y confirmo solo cuando todo se insertó.
- **Guardar `precio_unitario` en la partida.** Si el precio del producto cambia mañana, la venta de hoy debe conservar su precio original.
- **`NUMERIC(12,2)` y `BigDecimal`.** No quise guardar dinero con errores de `double`.
- **Validar antes de conectar.** Rechazo `null` y ventas sin partidas sin abrir conexión; la base de datos queda como segunda barrera.
- **No cambiar el puerto.** `VentaRepository` sigue igual, así que `RegistrarVentaService` puede usar el adaptador en memoria o el JDBC sin cambios.

## 4. Commits

| Hash | Mensaje |
|---|---|
Hashes tal como quedaron en `main` tras el merge del PR #29:

| Hash | Mensaje |
|---|---|
| `8a7aa6f` | `feat(db): agregar PostgreSQL 16 en Docker Compose y schema.sql` |
| `a55be97` | `feat(jdbc): implementar JdbcVentaRepository transaccional con PreparedStatement` |
| `4e2e6db` | `test(jdbc): agregar pruebas unitarias y de integracion del adaptador de ventas` |
| `095e792` | `docs: documentar persistencia de ventas P08 y bitacora individual` |

## 5. Pruebas

Ejecuté todas las pruebas del proyecto contra PostgreSQL 16.15: **73 de 73 en verde**, incluidas mis 13 pruebas nuevas, sin romper las existentes.

## 6. Estado

| Elemento | Estado |
|---|---|
| Esquema, adaptador y pruebas | VERIFICADO |
| `mvn clean verify` y `docker compose up` | **VERIFICADO en C02** con Docker y PostgreSQL 16 en contenedor: 77/77 pruebas. Ver [`docs/C02-Verificacion.md`](../C02-Verificacion.md). En P08 estaba NO_VERIFICADO por falta de Docker en mi entorno |
| Reporte Sonar | PENDIENTE al integrar en `main` |

## 7. Trazabilidad

| Issue | Implementación | Commits | PR | Pruebas |
|---|---|---|---|---|
fix/issue-40-descuento-existencias
| #ISSUE | `db/schema.sql`, `docker-compose.yml`, `adapter/out/jdbc/*` | `75e1d0a`, `81cb0f4`, `b59af62` | #PR | 13/13 PASS |

## 9. C02 — Consistencia transaccional del inventario (issue #40)

> Esta sección va después de la de reproducibilidad (#35); las dos son mi parte de C02.

Terminada la parte de reproducibilidad me puse a seguir el recorrido completo
guardar-consultar con la base reconstruida, y encontré que la venta se guardaba bien pero
el catálogo no se movía nunca. `JdbcVentaRepository` insertaba `venta` y `partida` dentro
de la transacción y commiteaba sin tocar `producto.existencia`. Vendí 1000 piezas de un
producto que tenía 40 en catálogo y la operación commiteó sin un solo error.

Lo peor no era la sobreventa, era dónde vivía la regla: la única validación estaba en
`VentaController.agregarProducto()`, comparando contra la existencia que traía el DTO
cargado al abrir la ventana, y sólo cuando era mayor a cero. Con dos ventanas abiertas las
dos pasaban la validación y las dos commiteaban.

Visto con lo de M11: la transacción era atómica pero incompleta. Garantizaba que la venta y
sus partidas entraran juntas, pero la unidad de trabajo del negocio incluye un tercer
efecto —mover el inventario— que se estaba quedando fuera del commit.

### Lo que hice

Metí el descuento en la misma transacción, con un `UPDATE` por producto cuya condición
viaja en el `WHERE`:

```sql
UPDATE producto SET existencia = existencia - ?
 WHERE codigo = ? AND existencia >= ?
RETURNING existencia
```

Agregué `ExistenciaInsuficienteException` en `com.ventas.model`, con `codigo`,
`solicitado` y `disponible`, y escribí 12 pruebas: 5 unitarias y 7 de integración.

### Decisiones que tomé y por qué

- **La condición va en el `WHERE`, no en un `SELECT` previo.** Lo primero que escribí fue
  leer la existencia, compararla en Java y después actualizar. Lo tiré: entre la lectura y
  la escritura hay una ventana en la que dos transacciones leen 40, las dos concluyen que
  caben 30 y las dos actualizan. Poniendo la condición en el `WHERE`, la segunda espera el
  lock del renglón y, cuando lo obtiene, ya ve el valor descontado y no actualiza ninguna
  fila. La decisión la toma la base, que es la única que ve el estado comprometido. El
  `CHECK (existencia >= 0)` del esquema queda como segunda barrera, no como única.
- **Los `UPDATE` ordenados por código de producto.** Así todas las ventas toman los locks
  en el mismo orden y dos ventas con los mismos artículos en orden inverso no se pueden
  interbloquear.
- **Distinguí el error de negocio del de integridad.** Un `UPDATE` sin filas afectadas
  tiene dos causas: el producto no existe (integridad, `PersistenciaException`) o existe y
  no alcanza (regla de negocio, `ExistenciaInsuficienteException`). Puse la segunda en
  `com.ventas.model` porque "no vender más de lo que hay" es del dominio, no de PostgreSQL;
  el dominio sigue sin importar `java.sql`.
- **No toqué el puerto ni el dominio existente,** ni `adapter.out.hibernate`, para no
  chocar con los issues #32, #34 y #38 de mi compañero.

### Pruebas

Las de integración que ya existían cubrían rollbacks por violación de FK o de `CHECK`, es
decir casos que la base rechaza sola. Ésta es la primera de una operación que la base
**aceptaría** y que el negocio debe rechazar, que es el error controlado que pide R03.

| Tipo | Prueba | Resultado |
|---|---|---|
| Positiva | `laVentaDescuentaLaExistenciaDelCatalogoEnLaMismaTransaccion` | 100 → 93 y 100 → 98 |
| Límite | `unaVentaQueAgotaLaExistenciaExactaSeAceptaYDejaElCatalogoEnCero` | vender 100 de 100 deja 0 |
| Negativa | `unaVentaQueExcedeLaExistenciaSeRechazaYNoDejaRastro` | sin venta, sin partidas, existencias intactas |
| Negativa | `unRollbackPorIntegridadTampocoMueveElInventario` | la existencia vuelve a su valor |

`mvn clean verify` contra PostgreSQL 16 en contenedor: **88 de 88 en verde** (70 unitarias y
18 de integración), sin romper nada de lo que ya estaba.

### Revisión que hice

Revisé los tres PRs de Hibernate de mi compañero (#33, #37 y #39) leyendo el mapeo contra
`db/schema.sql` y contra lo que ya hace el adaptador JDBC. Reporté dos cosas que considero
bloqueantes: `toModelPartida` reconstruye la partida con el precio actual del producto e
ignora la columna `precio_unitario`, así que al leer una venta vieja el total se recalcula
con el precio de hoy y deja de coincidir con el `total` guardado; y el constructor nuevo
`Venta(List<Partida>, double)` asigna sus campos sin validar, de modo que `new Venta(null,
-500)` es un objeto válido y se abre un segundo camino de construcción que `VentaFactory`
(#17) existía para evitar. Además señalé la fecha tomada del reloj de la JVM en lugar del
motor, el redondeo del dinero a dos decimales, las FK sin `nullable = false`, la lista de
partidas sin inicializar, la falta de `equals`/`hashCode` y la ausencia de pruebas de los
mapeadores, que son el sitio más fácil de probar porque son estáticos y puros.

### Estado

| Elemento | Estado |
|---|---|
| Descuento transaccional y rollback con existencias intactas | VERIFICADO |
| `mvn clean verify` completo | VERIFICADO: 88/88 con Docker y PostgreSQL 16 |
| Concurrencia real con dos cajas simultáneas | NO_VERIFICADO: la garantía viene del lock de renglón y del orden determinista de los `UPDATE`, pero no escribí una prueba concurrente |
| Reporte de Sonar | PENDIENTE al integrar en `main` |
| Equivalente en el adaptador Hibernate | PENDIENTE y fuera de mi alcance (#32, #34, #38) |

### Trazabilidad

| Issue | Implementación | Commits | PR | Pruebas | Docs |
|---|---|---|---|---|---|
| #40 | `model/ExistenciaInsuficienteException.java`, `adapter/out/jdbc/JdbcVentaRepository.java` | ver PR | #42 | 12 nuevas, 88/88 en total | [`docs/C02-Inventario-Transaccional.md`](../C02-Inventario-Transaccional.md) |
| (revisión) | revisión de los PRs de Hibernate de mi compañero | — | #39 | — | §9 de este documento |
| (sin issue) | `db/schema.sql`, `docker-compose.yml`, `adapter/out/jdbc/*` | `8a7aa6f`, `a55be97`, `4e2e6db`, `095e792` | #29 | 13/13 PASS |
| #35 | `docker-compose.yml` (semilla), `db/02-data.sql` (idempotente), `scripts/verify-module.sh`, `docs/C02-Verificacion.md`, limpieza de `*.class` y `.javamicro` | ver PR | #36 | 77/77 PASS |

## 8. C02 — Verificación reproducible (issue #35)

En C02 tomé la parte de reproducibilidad, que es la que sostiene el saber heurístico de R03.

Encontré que `db/02-data.sql` nunca se ejecutaba —`docker-compose.yml` solo montaba
`schema.sql`—, así que al reconstruir la base desde cero el catálogo quedaba vacío y el
recorrido guardar-consultar no se podía demostrar. Además, las pruebas de integración hacen
`TRUNCATE` de `producto`, de modo que la semilla también desaparecía después de `mvn verify`
y no se podía reaplicar porque usaba `INSERT` sin `ON CONFLICT`.

Lo resuelto: monté la semilla como `02-data.sql`, la hice idempotente y escribí
`scripts/verify-module.sh M11`, que reconstruye el esquema, corre las 77 pruebas, reaplica la
semilla en el orden correcto, confirma una venta, comprueba que un error controlado revierte la
transacción completa y demuestra que los datos sobreviven a `docker compose restart`. También
eliminé `Producto.class` y `Partida.javamicro` del árbol de fuentes.

Decisión que vale la pena explicar: el script **falla a propósito** si la base acepta una
partida con un `producto_codigo` inexistente. Preferí una comprobación que pueda fallar a una
que siempre pase; una verificación que no puede fallar no es evidencia.

Estado: VERIFICADO de punta a punta en mi entorno (log en `.verify/verify-M11.log`).
PENDIENTE y fuera de mi alcance: el adaptador Hibernate (#32, #34) y el tag del corte.
