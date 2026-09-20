

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

# Reporte de Contribución - Aleks López

## Issue #11: Análisis de Calidad y Configuración SonarQube / SonarLint
- Se ejecutó el análisis de calidad de código estático mediante SonarLint sobre la arquitectura del proyecto.
- Se configuró la compatibilidad de compilación para Java 11 utilizando `<maven.compiler.release>11</maven.compiler.release>` en el `pom.xml`.
- Se verificó la ausencia de Code Smells y vulnerabilidades en la barra de estado de VS Code (0 errores).
- Se ejecutaron las pruebas unitarias del módulo con Maven (`mvn clean test`), obteniendo el resultado BUILD SUCCESS.
