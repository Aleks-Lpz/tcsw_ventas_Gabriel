

# Bitácora de contribución individual

## Datos de la contribución

- **Integrante:** Alessandro
- **Actividad:** P04. Flujo colaborativo GitHub
- **Issue:** #1 - Crear documentación y bitácora de contribución individual
- **Rama:** `feat/issue-1-contribucion-individual`



## Objetivo

Crear la documentación correspondiente a la contribución individual dentro del flujo colaborativo del proyecto, manteniendo la trazabilidad de la actividad mediante GitHub.

## Estado inicial

Antes de realizar cualquier cambio se verificó el estado del repositorio mediante:

```bash
git status
```

**Resultado:**

```text
On branch main
Your branch is up to date with 'origin/main'.

nothing to commit, working tree clean
```

Esto confirmó que el repositorio se encontraba limpio y actualizado antes de iniciar la contribución.

## Creación de la rama

Para trabajar de forma aislada sin modificar directamente la rama principal se creó la rama:

```bash
git checkout -b feat/issue-1-contribucion-individual
```

La contribución se desarrolló dentro de esta rama.

## Contribución realizada

Se creó el archivo de documentación:

```text
docs/contribuciones/lopez_aleks.md
```

El archivo contiene la bitácora de la contribución individual y permite relacionar el trabajo realizado con la Issue #1 y la rama utilizada.

## Pruebas y comprobación

Como comprobación inicial del proyecto se ejecutó:

```bash
mvn test
```

**Resultado:**

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

Las pruebas ejecutadas correspondieron a:

* `ProductoTest`
* `VentaTest`

### Resultados

* **Pruebas ejecutadas:** 6
* **Fallos:** 0
* **Errores:** 0
* **Pruebas omitidas:** 0
* **Resultado de compilación:** `BUILD SUCCESS`

## Caso verificado

Se verificó que el proyecto pudiera compilarse y ejecutar sus pruebas unitarias correctamente antes de realizar la contribución documental.

**Resultado:** VERIFICADO

## Trazabilidad

La contribución queda relacionada con los siguientes elementos del flujo colaborativo:

* **Issue:** #1
* **Rama:** `feat/issue-1-contribucion-individual`
* **Commit:** pendiente de realizar
* **Pull Request:** pendiente de realizar
* **Revisión:** pendiente de realizar
* **Merge:** pendiente de realizar

## Resultado final

La documentación individual queda preparada para formar parte del flujo colaborativo de la actividad P04, manteniendo la trazabilidad del trabajo realizado y de las comprobaciones efectuadas.

**Estado:** VERIFICADO

## P06 - Issue #19: Evaluación de la complejidad de las interacciones del dominio

### Actividad e issue

- **Actividad:** P06
- **Issue:** #19 - Evaluar la complejidad de las interacciones del dominio para evitar sobreingeniería

### Objetivo

Evaluar, con base en la arquitectura y el código actuales, si la introducción de los patrones estructurales o creacionales Facade y Singleton aporta un beneficio concreto. La finalidad es evitar agregar participantes arquitectónicos sin una necesidad real y conservar la transparencia de las dependencias.

### Estado y arquitectura evaluada

La solución actual expone contratos mediante puertos de entrada y conecta cada caso de uso con su puerto de salida correspondiente:

```text
RegistrarVentaUseCase
	↓
RegistrarVentaService
	↓
VentaRepository

RegistrarProductoUseCase
	↓
ProductoService
	↓
ProductoRepository
```

La evidencia del código es la siguiente:

- `RegistrarVentaService` implementa `RegistrarVentaUseCase`, recibe `VentaRepository` por constructor y, después de validar la venta, delega en `guardar`.
- `ProductoService` implementa `RegistrarProductoUseCase`, recibe `ProductoRepository` por constructor y, después de validar el producto, delega en `guardar`.
- Los repositorios son interfaces de salida; los servicios no dependen de adaptadores concretos.
- Las pruebas de ambos servicios usan implementaciones falsas de los repositorios, lo que confirma que la dependencia es explícita y sustituible.

### Evaluación de Facade

La alternativa conceptual evaluada sería:

```text
Cliente
	↓
VentaFacade
	↓
RegistrarVentaService
	↓
VentaRepository
```

No se identificó un conjunto de subsistemas ni una secuencia de operaciones coordinadas cuya complejidad requiera ocultarse detrás de una Facade. Para registrar una venta, el contrato de entrada ya está definido por `RegistrarVentaUseCase` y el servicio realiza una validación y una delegación a `VentaRepository`. En consecuencia, una `VentaFacade` no resolvería un problema adicional demostrado por el código actual; repetiría o reenviaría una operación que ya tiene un punto de entrada claro.

Introducir esa capa tendría, en este contexto, costos potenciales derivados de agregar una clase y otra abstracción, aumentar la navegación del código, añadir una dependencia entre el cliente y la Facade, y crear un posible solapamiento entre las responsabilidades de la Facade y `RegistrarVentaService`. Estos costos no representan fallos actuales, pero sí mantenimiento adicional sin un beneficio proporcional mientras la interacción siga siendo directa.

La misma conclusión aplica al flujo de productos: `RegistrarProductoUseCase` y `ProductoService` ya aíslan la operación mediante el puerto de entrada y `ProductoRepository`.

### Evaluación de Singleton

No existe evidencia de una necesidad de una instancia global. Tanto `RegistrarVentaService` como `ProductoService` reciben su repositorio mediante el constructor y conservan esa dependencia en un atributo privado final. Esta inyección expresa de forma visible qué necesita cada servicio y permite proporcionar implementaciones distintas, incluidas las usadas por las pruebas.

La diferencia arquitectónica es explícita:

- La dependencia mediante constructor pertenece a cada instancia y queda visible en el contrato de creación del servicio.
- Una dependencia mediante Singleton se obtiene desde un estado global, ocultando el origen del repositorio y vinculando el servicio a una instancia compartida.

En este proyecto, introducir Singleton podría añadir estado global, acoplamiento y menor transparencia de las dependencias. También dificultaría potencialmente las pruebas al compartir estado entre casos y reduciría la flexibilidad para sustituir implementaciones de `VentaRepository` o `ProductoRepository`. Ninguno de esos costos resuelve una necesidad presente, porque la construcción explícita ya permite controlar el ciclo de vida y la implementación utilizada.

### Alternativa directa

La alternativa adoptada permanece en la solución existente:

- Cada puerto de entrada expone una operación concreta del caso de uso.
- Cada servicio implementa ese puerto y recibe por constructor el puerto de salida que necesita.
- Cada repositorio puede tener una implementación concreta sin que el servicio conozca el adaptador.

Esta estructura es suficiente para la complejidad observada. No se agregan Facade, Singleton, clases, interfaces ni dependencias nuevas sólo para demostrar un patrón.

### Costos técnicos considerados

La evaluación consideró los siguientes costos de introducir una capa o una instancia global sin una causa funcional demostrada:

- clases y abstracciones adicionales;
- dependencias y rutas de navegación adicionales;
- posible duplicación o solapamiento de responsabilidades;
- estado global y acoplamiento en el caso de Singleton;
- menor transparencia de las dependencias;
- dificultad potencial para aislar pruebas;
- menor flexibilidad para sustituir implementaciones;
- mantenimiento adicional para conservar una estructura que no simplifica las interacciones actuales.

Estos son costos derivados de las alternativas evaluadas; no se reportan como defectos existentes en la implementación actual.

### Decisión arquitectónica

**NO se introduce Facade ni Singleton en el estado actual del proyecto.**

La decisión se basa en que los puertos de entrada ya proporcionan contratos limpios, los servicios tienen responsabilidades pequeñas y las dependencias de persistencia ya están aisladas mediante puertos de salida. Facade y Singleton no son patrones incorrectos en general, pero no existe en este repositorio una necesidad concreta que justifique su costo y la pérdida de transparencia que podrían introducir.

La decisión podría reevaluarse si en el futuro apareciera una interacción demostrablemente compuesta por varios subsistemas que necesitara un único punto de coordinación, o una necesidad explícita y justificada de compartir el ciclo de vida de una instancia. Esas condiciones no forman parte del estado actual evaluado y no se implementan como parte de esta issue.

### Verificación

Antes de la modificación documental se ejecutó:

```bash
mvn clean test
```

Resultado de línea base: 29 pruebas ejecutadas, 0 fallos, 0 errores, 0 omitidas y `BUILD SUCCESS`.

Después de la modificación se ejecutó nuevamente el mismo comando. Resultado posterior: 29 pruebas ejecutadas, 0 fallos, 0 errores, 0 omitidas y `BUILD SUCCESS`. Las pruebas y el código permanecieron sin cambios; la documentación es la única modificación de la issue.

### Trazabilidad

- **Actividad:** P06
- **Issue:** #19
- **Archivo modificado:** `docs/contribuciones/lopez_aleks.md`
- **Commit:** pendiente de realizar
- **Pull Request:** pendiente de realizar
- **Revisión:** pendiente de realizar
- **Merge:** pendiente de realizar

**Estado:** VERIFICADO

# Reporte de Contribución - Aleks López

## Issue #11: Análisis de Calidad y Configuración SonarQube / SonarLint
- Se ejecutó el análisis de calidad de código estático mediante SonarLint sobre la arquitectura del proyecto.
- Se configuró la compatibilidad de compilación para Java 11 utilizando `<maven.compiler.release>11</maven.compiler.release>` en el `pom.xml`.
- Se verificó la ausencia de Code Smells y vulnerabilidades en la barra de estado de VS Code (0 errores).
- Se ejecutaron las pruebas unitarias del módulo con Maven (`mvn clean test`), obteniendo el resultado BUILD SUCCESS.
