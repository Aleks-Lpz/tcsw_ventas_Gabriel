# Bitácora de contribución individual

## Datos de contribución de Eventos de dominio y mantenimiento del proyecto

- **Integrante:** Evelin Aleida Patlani Tlehuactle
- **Issues asignados:** Actualizar el README.md y verificar el reporte de pruebas unitarias | Extensión del Dominio o Refactorización | Desacoplar los efectos secundarios tras la confirmación de la venta
- **Ramas:** feature/issue-3-actualizar-readme | feat/segundo-caso-uso | feat/evento-venta-confirmada
**Código y commits:**
-Se actualizo el README.md y se agregaron archivos de /target al gitignore | a46dbda8b4df05d08f6664c302539988637b92ad

-Se creo el segundo caso de prueba, y se modifico el pom.xml | 1904a40238af4f704b8bdba8f1f13573869e9e68

-Se creao una interfaz y tambien se crearon nuevos archivo java | f01c6137e1587295a5ed72a151b16a5dcebe8383

## Contribuciones

**Actualización del README.md y configuración del proyecto**
Se corrigió la guía de reproducción del README.md para que los comandos de clonación, compilación y pruebas funcionen correctamente con `mvn clean test`. Se corrigió la URL del repositorio, el nombre del directorio de clonación, se agregaron los pasos de compilación y resultado esperado, y se eliminó texto residual de un conflicto de merge. Además, se agregó la carpeta `/target` al `.gitignore` para evitar que los archivos generados por Maven se incluyan en el repositorio.

**Segundo caso de uso: RegistrarProducto**
Se implementó el caso de uso RegistrarProducto siguiendo estrictamente la arquitectura hexagonal. Se creó el puerto de entrada (RegistrarProductoUseCase), el puerto de salida (ProductoRepository), el servicio de aplicación (ProductoService) y el adaptador en memoria (InMemoryProductoRepository). Se corrigió el `pom.xml` reemplazando `maven.compiler.release` por `maven.compiler.source` y `maven.compiler.target` para compatibilidad con la versión del plugin del compilador. Se agregaron pruebas unitarias del servicio, del adaptador y del test de arquitectura hexagonal.

**Evento de dominio VentaConfirmada con patrón Observer**
Se implementó un modelo de publicación-suscripción para desacoplar los efectos secundarios tras la confirmación de una venta. Se creó la interface marcadora EventoDeDominio, el evento concreto VentaConfirmada, los puertos PublicadorDeEventos y ObservadorDeEventos, el PublicadorSimple como implementación del publicador, y el ReciboObservador como adaptador simulado que genera recibos. Se modificó RegistrarVentaService para publicar el evento tras guardar la venta, sin acoplar el flujo principal a servicios periféricos.

## Resultados
La implementación del patrón Observer requirió modificar el constructor de RegistrarVentaService para inyectar el PublicadorDeEventos, lo que impactó las pruebas existentes del servicio de ventas al agregar el nuevo parámetro. Se actualizaron todas las pruebas afectadas y se agregaron pruebas específicas para verificar la publicación del evento y la reacción del observador. Todas las pruebas pasan exitosamente con `mvn clean test`.