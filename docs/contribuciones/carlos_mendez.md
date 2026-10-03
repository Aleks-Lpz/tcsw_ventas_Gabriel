# Bitácora de contribución individual

## Datos de contribución de Arquitectura hexagonal y patrones para el dominio

- **Integrante** Carlos Jesús Méndez Coria
- **Issues asignados:** Diseño arquitectónico y documentación , Desacoplar el cálculo de variaciones comerciales del flujo principal de ventas
- **Ramas:** docs/diagrama-arquitectura-p05 | feat/issue1-strategy-descuentos | docs/actualizar-diagrama-uml
**Código y commits:** 
-docs: agregar diagrama UML y justificación arquitectonica P05 | a2ca95ce72dc3b2f374e85acecc3cd8ed16261c2

-feat: implementar patrón strategy para calculo de descuentos (Resuelve #1) | c0fd2f872b72839cdb07a3e3bd58f66724fdd811

-chore: resolver conflictos entre strategy y observer en el servicio de ventas | 429cdb2f9a10ba5846760c7617b5d16d3f1ee125

## Contribuciones

**Diagrama UML V1.0**
Se realizo el diagrama de arquitectura en docs/contribuciones/img/ por medio de la herramienta draw.io, se tomaron en cuenta las capas de la arquitectura y el contenido de esta para no únicamente plasmar la interacción entre clases, sino también la división de paquetes. 
**Descuentos y patrón Strategy**
EN la segunda iteración del proyecto "PO6" Se valido la implementación de patrones para el dominio dentro del sistema de ventas. En mi caso, se creo una nueva regla de negocio que condicionara al patrón a realizar descuentos, esto para plasmar una necesidad real de aplicar patrones de diseño y no únicamente implementar un patrón sin justificación, lo que puede conllevar sobre-ingenieria. 
Se aplicó el patrón strategy utilizando un modelo contractual que separará a la acción del algorítmo, la clase que implementa el modelo contractual es "DescuentoPorCantidad" que plasma la regla de negocio que se creo, si una venta contiene más de 10 partidas, se realiza un descuento del 15%. Asímismo, se mantuvo limpia la arquitectura hexagonal con la aplicación de este patrón de diseño al inyectar la dependencia "PoliticaDescuento" dentro del servicio de ventas, esto para que el servicio no dependa de la implementación concreta del patrón.
**Diagrama UML V2.0**
Se agrego la representación de todos los patrones de diseño unificados en la rama main dentro del diagrama, lo que conllevo a la creación de un nuevo paquete de notificación en la capa de aplicación además de la creación y relación de diversas clases en los adaptadores y la capa de dominio. La mayoría de relaciones generadas en base a las nuevas clases convergen en las clases de servicio, ya que estas son los puentes centralizados por las cuales pasan los datos de dominio.

## Resultados
La implementación del patrón strategy conllevo realizar algunas modificaciones en los paquetes de prueba, en especial en la clase de prueba del servicio de ventas, ya que al inyectar una nueva dependencia en la clase de RegistrarVentaService y recibirla como parámetro dentro de nuestro constructor, afectan especialmente a nuestras validaciones creacionales del servicio dentro de las clases de prueba. Además de esto se creo una clase de prueba individual dentro del paquete de model donde se plasman las pruebas creacionales del patrón strategy
