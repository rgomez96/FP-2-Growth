/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fp2.growth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author rafa
 */
public class FPTree {

    static class Nodo {

        /**
         * Identificador del Item
         */
        int identificador;

        /**
         * Número de transacciones representadas por la porción del camino
         * alcanzando el nodo.
         */
        int contador;

        /**
         * Enlace hacia el padre.
         */
        Nodo padre = null;

        /**
         * Enlace hacia el siguiente nodo en la lista enlazada con el mismo
         * identificador.
         */
        Nodo siguiente = null;

        /**
         * Nodos hijos. Utilizo un hashmap para buscar rápidamente a los hijos
         * de un nodo a la hora de actualizar el árbol.
         */
        ArrayList<Nodo> hijos = null;

        /**
         * Constructor por defecto.
         */
        Nodo() {
            this.identificador = -3000;
            this.hijos = new ArrayList<>();
            this.contador = 1;
        }

        Nodo(Nodo orig) {
            this.identificador = orig.identificador;
            hijos = new ArrayList<>();
            this.contador = orig.contador;
        }

        /**
         * Constructor parametrizado.
         */
        Nodo(int identificador, int apoyos, Nodo padre) {
            this.identificador = identificador;
            this.contador = apoyos;
            this.padre = padre;
            this.hijos = new ArrayList<>();
        }

        /**
         * Añade un nuevo nodo hijo al nodo actual.
         */
        void adjuntar(int idItem) {
            /**
             * Compruebo otra vez si el nodo tiene hijos, por si acaso.
             */
            if (hijos == null) {
                hijos = new ArrayList<>();
            }
            /**
             * Si el id de este nodo es menor que 0 el padre es nulo (para
             * evitar problemas al crear el primer nodo).
             */
            Nodo hijo = new Nodo(idItem, 1, this);
            hijos.add(hijo);
        }

    } // FIN DE LA CLASE NODO

    /**
     * Utilizo una tabla para acceder fácilmente a los elementos del árbol que
     * están enlazados. Cada elemento de la tabla será un HeaderTableItem.
     */
    static class HeaderTableItem implements Comparable<HeaderTableItem> {

        /**
         * Identificador del elemento de la tabla.
         */
        int id;

        /**
         * Contador del soporte de un item.
         */
        int contador;

        /**
         * Siguiente nodo.
         */
        Nodo nodo = null;

        /**
         * Constructor prametrizado.
         *
         * @param id
         */
        public HeaderTableItem(int id) {
            this.id = id;
            contador = 0;
        }

        @Override
        public int compareTo(HeaderTableItem orig) {
            return Integer.compare(orig.contador, contador);
        }

    }

    // CLASE FPTREE
    /**
     * Nodo raíz. No debe almacenar ningún dato. como padre.
     */
    Nodo raiz = new Nodo();

    /**
     * Número de transacciones (filas) realizadas.
     */
    int numTransacciones;

    /**
     * Soporte mínimo requerido por un nodo. Recibir por argumento.
     */
    int soporteMin;

    /**
     * Lista de los soportes de cada item.
     */
    int[] soportes;

    /**
     * Tabla de nodos enlazados.
     */
    HeaderTableItem[] headerTable;

    /**
     * Número de Items
     */
    int numItems;

    /**
     * Número de items con soporte lo suficientemente alto.
     */
    int numItemsFrecuentes;

    /**
     * El tamaño del itemset más frecuente (con sólo items frecuentes)
     * almacenado.
     */
    int maximoTamItemset;

    /**
     * Orden de los items según su soporte.
     */
    int[] orden;

    /**
     * Matriz de 0 y 1 al cual aplicamos el algoritmo.
     */
    ArrayList<ArrayList<Integer>> matrizRL;

    /**
     * Constructor parametrizado del FPTree
     *
     * @param soporteMin
     * @param matrizRL
     */
    public FPTree(int soporteMin, ArrayList<ArrayList<Integer>> matrizRL) {
        this.soporteMin = soporteMin;
        this.numItems = matrizRL.size();
        this.soportes = new int[numItems];
        this.matrizRL = matrizRL;
        crearFPTree();
    }

    /**
     * Lee el dataset para rellenar la header table, ordena la header table y
     * invoca a rellenarArbol.
     */
    private void crearFPTree() {
        //System.out.println(matrizRL);
        obtenerItemsFrecuentes();

        headerTable = new HeaderTableItem[numItemsFrecuentes];
        for (int i = 0, j = 0; i < numItems; i++) {
            if (soportes[i] >= soporteMin) {
                //System.out.println("Creando header con id " + i + " y soporte" + soportes[i]);
                HeaderTableItem item = new HeaderTableItem(i);
                item.contador = soportes[i];
                //System.out.println(item.contador);
                headerTable[j++] = item;

            }
        }

        Arrays.sort(headerTable);

//        System.out.println("\nheaderTable:");
//        for (int i = 0; i < headerTable.length; i++) {
//            System.out.print("\t" + headerTable[i].contador);
//        }
        orden = new int[numItemsFrecuentes];
        for (int i = 0; i < numItemsFrecuentes; i++) {
            orden[i] = headerTable[i].id;
        }

//        System.out.println("\nOrden:");
//        for (int i = 0; i < orden.length; i++) {
//            System.out.print("\t" + orden[i]);
//        }
        rellenarArbol();
    }

    /**
     * Rellena el árbol FP-Tree. A los nodos pertinentes les añade tanto sus
     * hijos como el siguiente nodo en la lista enlazada.
     */
    void rellenarArbol() {
        Nodo nodoactual = raiz;
        for (int i = 0; i < matrizRL.get(0).size(); i++) {
            for (int j = 0; j < matrizRL.size(); j++) {
                //System.out.print((matrizDifusa.get(j).get(i)) + "\t");
                if (matrizRL.get(j).get(i) == 1) {
                    if (nodoactual.hijos == null) {
                        nodoactual.hijos = new ArrayList<>();
                    }
                    boolean encontrado = false;
                    for (int z = 0; z < nodoactual.hijos.size(); z++) {
                        if (nodoactual.hijos.get(z).identificador == j) {
                            encontrado = true;
                            nodoactual.hijos.get(z).contador++;
                            nodoactual = nodoactual.hijos.get(z);
                            break;
                        }
                    }
                    if (encontrado == false) {
                        nodoactual.adjuntar(j);
                        nodoactual = nodoactual.hijos.get(nodoactual.hijos.size() - 1);

                        for (HeaderTableItem item : headerTable) {
                            if (item.id == nodoactual.identificador) {
                                if (item.nodo == null) {
                                    item.nodo = nodoactual;
                                } else {
                                    Nodo nodo = item.nodo;
                                    while (nodo.siguiente != null) {
                                        nodo = nodo.siguiente;
                                    }
                                    nodo.siguiente = nodoactual;
                                }

                            }
                        }
                    }
                }
            }
            nodoactual = raiz;
            //System.out.println("");

        }
        //mostrarArbol();
    }

    /**
     * Obtiene cuáles son los ítems frecuentes para construir la header table.
     */
    void obtenerItemsFrecuentes() {
        for (int i = 0; i < matrizRL.size(); i++) {
            /**
             * Como todos los elementos de cada columna son 1 o 0 puedo sumar
             * los valores de cada columna para obtener la frecuencia de cada
             * item.
             */
            int frecuenciaItem = 0;
            for (int j = 0; j < matrizRL.get(i).size(); j++) {
                frecuenciaItem += matrizRL.get(i).get(j);
                //System.out.println("FrecuenciaItem: "+frecuenciaItem);
            }
            soportes[i] = frecuenciaItem;
            //System.out.println("");
            // Fin de una transacción.
        }
        //System.out.println("Soportes:");
        for (int i = 0; i < soportes.length; i++) {
            //System.out.print("\t" + soportes[i]);
            if (soportes[i] >= soporteMin) {
                numItemsFrecuentes++;
            }
        }
    }

}
