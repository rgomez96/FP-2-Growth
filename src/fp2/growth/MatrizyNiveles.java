package fp2.growth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;

/**
 *
 * @author Rafa
 */
public class MatrizyNiveles{

    /**
     * Matriz que utilizaremos para almacenar los datos.
     */
    ArrayList<ArrayList<Float>> matrizDatos;

    /**
     * Matriz que utilizaremos para almacenar los datos (en tanto por 1).
     */
    ArrayList<ArrayList<Float>> matrizDifusa;

    TreeSet nivelesRestriccion;

    public TreeSet getNivelesRestriccion() {
        return nivelesRestriccion;
    }

    public ArrayList<ArrayList<Float>> getMatrizDifusa() {
        return matrizDifusa;
    }

    public void setMatrizDifusa(ArrayList<ArrayList<Float>> matrizDifusa) {
        this.matrizDifusa = matrizDifusa;
    }
    
    

    public MatrizyNiveles(ArrayList<ArrayList<Float>> matrizDatos) {
        this.matrizDatos = matrizDatos;
        this.matrizDifusa = new ArrayList<>();
        this.nivelesRestriccion = new TreeSet<Float>(Collections.reverseOrder());
    }

    void crearMatrizDifusa() {
        for (int i = 0; i < matrizDatos.get(0).size(); i++) {
            float mayor = Float.MIN_VALUE;
            float datoActual = 0;
            ArrayList<Float> fila = new ArrayList<>();

            /**
             * Encontramos el mayor valor en cada columna.
             */
            for (int j = 0; j < matrizDatos.size(); j++) {
                if (matrizDatos.get(j).get(i) > mayor) {
                    mayor = matrizDatos.get(j).get(i);
                }
            }

            /**
             * Obtenemos los valores de todos los datos de la columna en tanto
             * por 1 con respecto al mayor valor y los insertamos en un
             * ArrayList de float. Además redondeamos todos los valores a 2
             * decimales para simplificar el cálculo.
             */
            for (int j = 0; j < matrizDatos.size(); j++) {
                datoActual = matrizDatos.get(j).get(i) / mayor;
                datoActual = Math.round(datoActual * 100f) / 100f;
                fila.add(datoActual);
            }
            //System.out.println("El mayor en la columna " + i + " es " + mayor);
            //System.out.println("Fin de la linea");

            /**
             * NOTA: Dado que estoy trabajando con la matriz de datos difusos en
             * columnas esta matriz está traspuesta con respecto a la matriz
             * matrizDatos!!!!
             */
            matrizDifusa.add(fila);
        }
    }

    /**
     * Añade todos los elementos de la matriz a un TreeSet para obtener todos
     * los elementos únicos en orden descendente. Además, elimina el elemento
     * 0.0 (si existe).
     */
    void obtenerNivelesRestriccion() {
        for (int i = 0; i < matrizDifusa.get(0).size(); i++) {
            for (int j = 0; j < matrizDifusa.size(); j++) {
                nivelesRestriccion.add(matrizDifusa.get(j).get(i));
            }
        }

        // Si pongo directamente nivelesRestriccion.last().equals(0) o .equals(0.0) no funciona.
        float cero = 0;
        if (nivelesRestriccion.last().equals(cero)) {
            nivelesRestriccion.remove(nivelesRestriccion.last());
        }

//        System.out.println("Número de niveles de restricción: " + nivelesRestriccion.size());
//        System.out.println("Niveles de restricción: " + nivelesRestriccion);
    }

    /**
     * Muestra por pantalla los datos leídos del dataset. Para depurar.
     */
    void mostrarMatrizDatos() {
        System.out.println("Matriz de datos leídos del dataset.");
        System.out.println(matrizDatos);
        for (int i = 0; i < matrizDatos.size(); i++) {
            for (int j = 0; j < matrizDatos.get(i).size(); j++) {
                System.out.print(matrizDatos.get(i).get(j) + "\t");
            }
            System.out.println("");
        }
    }

    /**
     * Muestra por pantalla los datos almacenados en la matriz de datos difusos.
     * Para depurar. Ya que trabajo con ella por columnas en lugar de por filas
     * esta matriz está traspuesta comparada con la matriz de datos.
     */
    void mostrarMatrizDifusa() {
        System.out.println("Matriz de datos difusos calculados a través de la matriz inicial.");
        for (int i = 0; i < matrizDifusa.get(0).size(); i++) {
            for (int j = 0; j < matrizDifusa.size(); j++) {
                System.out.print((matrizDifusa.get(j).get(i)) + "\t");
            }
            System.out.println("");
        }
    }

}
