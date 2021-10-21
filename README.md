# FP²-Growth
FP²-Growth es un algoritmo de minería de datos basado en el algoritmo FP-Growth, modificado para trabajar con lógica difusa y valores reales. Para ello incorpora un número de _alfa-cortes_ en los datos equivalente a sus _niveles de restricción_ y y para cada alfa-corte ejecuta el algoritmo FP-Growth en paralelo. Para cada hilo evalúa las posibles soluciones según su grado de certidumbre y combina todas las soluciones devueltas por los hilos según su soporte.

## Cómo se usa
Este algoritmo está programado para ser invocado en un terminal, ya sea individualmente o como parte de una cadena de comandos para el tratamiento de datos. 

El programa recibe como parámetros la ruta relativa del dataset y el soporte mínimo para que una agrupación generada por el algoritmo FP-Growth sea considerada común. Por ejemplo:

java -jar FP2-Growth.jar datasets/baskball.arff 2