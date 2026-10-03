# Contribución individual — Alessandro Osorio

## 1. Datos de la contribución

- **Integrante:** Alessandro Osorio
- **Usuario de GitHub:** Alessito54
- **Repositorio:** tcsw_ventas_Gabriel (https://github.com/Aleks-Lpz/tcsw_ventas_Gabriel.git)
- **Rama(s):** `feat/issue-1-contribucion-individual`, `feat/issue-4-registrar-venta-hexagonal`, `feat/issue-19-evaluacion-sobreingenieria`
- **Actividad:** Actividad P04 (Flujo colaborativo GitHub), Actividad P05 (Arquitectura Hexagonal), Actividad P06 (Patrones de diseño)

## 2. Issues asignados

### Issue #1 — Crear documentación y bitácora de contribución individual

- **Descripción:** Establecer la bitácora de contribución individual del integrante dentro del flujo colaborativo de la actividad P04 en GitHub.
- **Trabajo realizado:** Creación del archivo de documentación `docs/contribuciones/lopez_aleks.md` definiendo el formato de seguimiento y registro de trazabilidad individual.
- **Estado:** Completado (Integrado en `main` mediante PR #7).
- **Relación con la actividad:** Actividad P04 — Flujo colaborativo GitHub.

### Issue #4 — Implementar el núcleo hexagonal para registrar venta

- **Descripción:** Diseñar e implementar el núcleo con arquitectura hexagonal para el caso de uso de registrar venta, separando la lógica de negocio de los detalles de infraestructura.
- **Trabajo realizado:**
  - Creación del puerto de entrada `RegistrarVentaUseCase`.
  - Implementación del servicio de aplicación `RegistrarVentaService`.
  - Definición del puerto de salida `VentaRepository`.
  - Implementación del adaptador de persistencia en memoria `InMemoryVentaRepository`.
  - Elaboración de pruebas unitarias (`RegistrarVentaServiceTest`, `InMemoryVentaRepositoryTest`) y de arquitectura hexagonal (`HexagonalArchitectureTest`).
- **Estado:** Completado (Integrado en `main` mediante PR #9).
- **Relación con la actividad:** Actividad P05 — Arquitectura Hexagonal.

### Issue #19 — Evaluar la complejidad de las interacciones del dominio para evitar sobreingeniería

- **Descripción:** Evaluar el código y la arquitectura del sistema para determinar si la adición de patrones como Facade o Singleton resulta justificada o constituye sobreingeniería.
- **Trabajo realizado:** Elaboración de la evaluación técnica arquitectónica en `docs/contribuciones/lopez_aleks.md`. Se concluyó rechazar la inclusión de Facade y Singleton en el estado actual, dado que los puertos de entrada (`RegistrarVentaUseCase`, `RegistrarProductoUseCase`) y de salida (`VentaRepository`, `ProductoRepository`) ya proveen contratos limpios e inyección explícita de dependencias por constructor sin acoplamiento global ni capas redundantes.
- **Estado:** Completado (Integrado en `main` mediante PR #20).
- **Relación con la actividad:** Actividad P06 — Patrones de diseño y evaluación de sobreingeniería.

## 3. Justificación técnica

El trabajo desarrollado por Alessandro Osorio abarca tres etapas fundamentales del proyecto:

1. **Gestión Colaborativa y Bitácora (Issue #1):**
   - **Problema:** Necesidad de registrar y dar seguimiento formal a la contribución individual dentro del flujo de trabajo de Git/GitHub.
   - **Solución:** Se diseñó y construyó la estructura inicial en `docs/contribuciones/lopez_aleks.md`, asegurando que los cambios queden aislados en ramas de características (`feat/`) antes de su integración a `main`.

2. **Arquitectura Hexagonal para Registrar Venta (Issue #4):**
   - **Problema:** Aislar las reglas del dominio de ventas frente a dependencias externas o de almacenamiento.
   - **Solución:**
     - **Puerto de entrada:** `com.ventas.application.port.in.RegistrarVentaUseCase` establece la interfaz del caso de uso.
     - **Servicio de aplicación:** `com.ventas.application.service.RegistrarVentaService` implementa `RegistrarVentaUseCase`, recibiendo `VentaRepository` por constructor.
     - **Puerto de salida:** `com.ventas.application.port.out.VentaRepository` desacopla la persistencia del núcleo.
     - **Adaptador:** `com.ventas.adapter.out.memory.InMemoryVentaRepository` proporciona el almacenamiento en memoria con colecciones inmutables (`Collections.unmodifiableList`).
     - **Prueba arquitectónica:** `HexagonalArchitectureTest` verifica mediante reflexión y lectura de archivos que el núcleo no importe paquetes de adaptadores (`com.ventas.adapter.*`).
   - **Patrones y decisiones:** Aplicación estricta de Inversión de Dependencias (DIP) y Separación de Responsabilidades (SOC).

3. **Evaluación de Complejidad y Sobreingeniería (Issue #19):**
   - **Problema:** Determinar si la adición de Facade y Singleton añade valor o introduce complejidad innecesaria.
   - **Solución:**
     - **Facade:** Se descartó porque los casos de uso actuales (`RegistrarVentaService`, `ProductoService`) no coordinan subsistemas múltiples complejos. Una Facade agregaría una capa de delegación redundante.
     - **Singleton:** Se descartó a favor de la inyección de dependencias por constructor. El patrón Singleton agregaría estado global oculto y dificultaría las pruebas unitarias aisladas.

## 4. Commits relacionados

| Hash | Mensaje | Rama | Relación con Issue |
|---|---|---|---|
| `f447cfc29d61d9f300e52226df85a2cfeac977af` | `docs: agregar bitacora de contribucion individual` | `feat/issue-1-contribucion-individual` | Issue #1 |
| `ba8a7e2ee6da3fc84a475c6990ef19e897aee02a` | `feat: implementa núcleo hexagonal para registrar venta` | `feat/issue-4-registrar-venta-hexagonal` | Issue #4 |
| `accb11d21e0643a31f3650bffb0b083e9c078222` | `docs: documenta evaluación de Facade y Singleton` | `feat/issue-19-evaluacion-sobreingenieria` | Issue #19 |

*Nota: Se registran adicionalmente los commits de integración (merge) ejecutados por el integrante:*
- `9c098d15414d13b18818ab4134a515c8a16928bd`: Merge pull request #7 from Aleks-Lpz/feat/issue-1-contribucion-individual
- `bf81d2509aa4639d33d5829293c2b31eda0c6baa`: Merge pull request #21 from Aleks-Lpz/feat/issue1-strategy-descuentos

## 5. Pull Requests relacionadas

| PR | Título | Rama origen | Rama destino | Relación |
|---|---|---|---|---|
| #7 | `Merge pull request #7 from Aleks-Lpz/feat/issue-1-contribucion-individual` | `feat/issue-1-contribucion-individual` | `main` | Integración de bitácora individual (Issue #1) |
| #9 | `Merge pull request #9 from Aleks-Lpz/feat/issue-4-registrar-venta-hexagonal` | `feat/issue-4-registrar-venta-hexagonal` | `main` | Integración de núcleo hexagonal para registrar venta (Issue #4) |
| #20 | `Merge pull request #20 from Aleks-Lpz/feat/issue-19-evaluacion-sobreingenieria` | `feat/issue-19-evaluacion-sobreingenieria` | `main` | Integración de evaluación de sobreingeniería Facade/Singleton (Issue #19) |

## 6. Resultados de las pruebas

Comando:
```bash
mvn clean test
```

Resultado:
```text
BUILD SUCCESS
```

Pruebas:
- **Ejecutadas:** 44
- **Fallos:** 0
- **Errores:** 0
- **Omitidas:** 0

Detalle de clases de prueba ejecutadas:
- `InMemoryProductoRepositoryTest`: 5 pruebas (PASSED)
- `InMemoryVentaRepositoryTest`: 5 pruebas (PASSED)
- `ReciboObservadorTest`: 4 pruebas (PASSED)
- `ProductoServiceTest`: 5 pruebas (PASSED)
- `PublicadorSimpleTest`: 5 pruebas (PASSED)
- `RegistrarVentaServiceTest`: 8 pruebas (PASSED)
- `HexagonalArchitectureTest`: 3 pruebas (PASSED)
- `ProductoTest`: 3 pruebas (PASSED)
- `VentaFactoryTest`: 2 pruebas (PASSED)
- `VentaTest`: 4 pruebas (PASSED)

## 7. Trazabilidad

| Issue | Implementación | Commit | Pull Request | Pruebas |
|---|---|---|---|---|
| #1 | `docs/contribuciones/lopez_aleks.md` | `f447cfc29d61d9f300e52226df85a2cfeac977af` | #7 | PASS |
| #4 | Ports (`RegistrarVentaUseCase`), Service (`RegistrarVentaService`), Port out (`VentaRepository`), Adapter (`InMemoryVentaRepository`), Tests (`RegistrarVentaServiceTest`, `InMemoryVentaRepositoryTest`, `HexagonalArchitectureTest`) | `ba8a7e2ee6da3fc84a475c6990ef19e897aee02a` | #9 | PASS |
| #19 | Documentación de evaluación Facade y Singleton en `docs/contribuciones/lopez_aleks.md` | `accb11d21e0643a31f3650bffb0b083e9c078222` | #20 | PASS |

## 8. Estado final

- **Qué se completó:** Creación y consolidación del documento de trazabilidad de contribución individual para Alessandro Osorio (`Alessito54`), cubriendo los desarrollos e integraciones de las Issues #1, #4 y #19.
- **Qué quedó pendiente:** Ningún pendiente respecto a la documentación de las contribuciones realizadas.
- **Estado de las pruebas:** `BUILD SUCCESS` (44/44 pruebas unitarias y arquitectónicas aprobadas).
- **Estado de la contribución:** Finalizado y verificado.
- **Rama utilizada:** `docs/alessandro-osorio`
