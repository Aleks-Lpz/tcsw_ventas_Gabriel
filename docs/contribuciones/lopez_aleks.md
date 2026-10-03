# Bitácora de Contribución Individual - Aleks Fernando López González

## 1. Datos Generales
- **Proyecto:** TCSW Ventas
- **Actividad:** C01. Corte de arquitectura (Instrumento R02)
- **Repositorio:** https://github.com/Aleks-Lpz/tcsw_ventas_Gabriel
- **Ruta del archivo:** docs/contribuciones/lopez_aleks.md

## 2. Issues Asignados y Justificación Técnica

- **Issue #1: Crear documentación y bitácora de contribución individual**
  - **Justificación Técnica:** Se estructuró la trazabilidad exigida por la evaluación R02, documentando las evidencias y justificando las decisiones de diseño para el corte de arquitectura.

- **Issue #2: Validación de precio positivo en la clase Producto**
  - **Justificación Técnica:** Se centralizaron las invariantes dentro de la entidad `Producto` modificando su constructor para bloquear precios negativos. Esto previene que la aplicación instancie objetos inválidos desde la capa de dominio central.

- **Issue #11: Análisis de calidad y Configuración SonarQube**
  - **Justificación Técnica:** Se implementó SonarLint/SonarQube en la arquitectura hexagonal para evaluar los casos de uso, adaptadores y puertos. Se configuró `<maven.compiler.release>11</maven.compiler.release>` en Maven, mitigando vulnerabilidades (code smells) y asegurando la calidad del código.

- **Issue #17: Centralización de Invariantes Creacionales (Patrón Factory)**
  - **Justificación Técnica:** Se implementó la clase `VentaFactory` en el núcleo de dominio para cumplir con la integración de patrones. Esta fábrica centraliza la creación de la entidad `Venta` y garantiza la integridad de negocio: no permite costos totales de cero ni listas vacías, lanzando `IllegalArgumentException` antes de cualquier transacción con el repositorio.

## 3. Matriz de Trazabilidad (PRs y Commits)

| Issue | Rama Local | Pull Request | Hash del Commit | Estado |
| :--- | :--- | :--- | :--- | :--- |
| **Issue #1** | `feat/issue-1-contribucion-individual` | PR #7: docs: agregar bitacora de contribucion individual | `f447cfc` | Merged |
| **Issue #2** | `feature/issue-2-validar-precio` | PR #5: feat: validacion de precio positivo | `33a8466` | Merged |
| **Issue #11** | `feature/issue-11-sonarqube` | PR #11: style: analisis de calidad con SonarLint | `377ddc1` | Merged |
| **Issue #17** | `feature/issue-17-factory` | PR #17: feat: implementa VentaFactory | `2305f9e` | Merged |

## 4. Resultados de Pruebas

Evidencia de ejecución de la suite de pruebas sin dependencias de persistencia externa ni interfaz de usuario.
**Comando ejecutado:** `mvn clean test`

**Casos evaluados:**
- **Caso de éxito (Comportamiento esperado):** Instanciación correcta desde `VentaFactory` con datos válidos.
- **Caso de error/límite:** Pruebas unitarias desarrolladas para casos de error en `ProductoTest.java` (precio negativo) y en la fábrica con `VentaFactoryTest` (lista vacía).

**Salida de terminal:**
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- in com.ventas.model.VentaFactoryTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- in com.ventas.model.ProductoTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in com.ventas.model.VentaTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in com.ventas.application.service.RegistrarVentaServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in com.ventas.adapter.out.memory.InMemoryVentaRepositoryTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- in com.ventas.architecture.HexagonalArchitectureTest
[INFO] BUILD SUCCESS
