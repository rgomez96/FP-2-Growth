/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fp2.growth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import fp2.growth.FPTree.Nodo;

/**
 *
 * @author rafa
 */
public class FPGrowth {

    /**
     * Soporte mínimo para que un item sea considerado frecuente.
     */
    private final int minSoporte;

    /**
     * FP-Tree
     */
    private final FPTree arbol;

    /**
     * Buffer para recoger los resultados.
     */
    ArrayList<ArrayList<Integer>> solucion = new ArrayList<>();

    /**
     * Constructor parametrizado.
     *
     * @param minSoporte
     * @param arbol
     */
    public FPGrowth(int minSoporte, FPTree arbol) {
        this.minSoporte = minSoporte;
        this.arbol = arbol;
    }

    ArrayList<ArrayList<Integer>> metodo() {
        //System.out.println("FPGrowth: orden: ");
        for (int i = 0; i < arbol.numItemsFrecuentes; i++) {
            minea(arbol.orden[i]);
            // Llamamos a minea varias veces así que el array de soluciones puede parecer muy grande pero está bien.
        }
        return solucion;
    }

    /**
     * Dado un valor, localiza todos los nodos cuyo id es dicho valor y lee
     * hacia arriba hasta llegar a la raíz. Colecta todos los caminos de nodos
     * que se han recorrido y obtiene la frecuencia de cada id para los nodos en
     * el camino. Devuelve los subconjuntos de nodos cuya frecuencia sea mayor
     * que la preestablecida por el usuario.
     *
     * @param valor
     */
    void minea(int valor) {
        ArrayList<ArrayList<Nodo>> matrizNodos = new ArrayList<>();

        // Obtener todos los caminos hacia los nodos con valor 'valor' (forma de lista)
        for (int i = 0; i < arbol.headerTable.length; i++) {
            if (arbol.headerTable[i].id == valor) {
                // Obtener camino hacia la raíz.
                Nodo auxiliar = arbol.headerTable[i].nodo;
                while (auxiliar != null) {
                    ArrayList<Nodo> listaNodos = new ArrayList<>();
                    Nodo copia = auxiliar;
                    while (copia.padre != null) {
                        listaNodos.add(copia);
                        copia = copia.padre;
                    }
                    matrizNodos.add(listaNodos);
                    auxiliar = auxiliar.siguiente;
                }
            }
        }

        // Leer las listas y contar cuantas veces aparece cada nodo distinto.
        Integer[] frecuencias = new Integer[arbol.numItems];
        Arrays.fill(frecuencias, 0);
        for (int i = 0; i < matrizNodos.size(); i++) {
            for (int j = 0; j < matrizNodos.get(i).size(); j++) {
                //System.out.println("Nodo con id: " + matrizNodos.get(i).get(j).identificador + " , contador: " + matrizNodos.get(i).get(j).contador);
                frecuencias[matrizNodos.get(i).get(j).identificador]++;
            }
        }

        int numFrecuentes = 0;

        // Comprueba para los nodos en los caminos cuántos son frecuentes.
        for (int i = 0; i < frecuencias.length; i++) {
            //System.out.println("Frecuencia de " + i + ": " + frecuencias[i]);
            if (frecuencias[i] >= minSoporte) {
                numFrecuentes++;
            }
        }
        
        // Ahora comprueba cuáles son frecuentes.
        Integer[] frecuentes = new Integer[numFrecuentes];
        int j = 0;
        for (int i = 0; i < frecuencias.length; i++) {
            if (frecuencias[i] >= minSoporte) {
                frecuentes[j] = i;
                j++;
            }
        }

        // Consigue los subconjuntos de items requeridos para los ids frecuentes que aparecen en los caminos
        // y si el cubconjunto es frecuente añadelo a la lista de soluciones.
        ArrayList<ArrayList<Integer>> subsets = getSubsets(frecuentes, valor);

        for (int i = 0; i < subsets.size(); i++) {
            int menorFrecuencia = Integer.MAX_VALUE;
            for (int z = 0; z < subsets.get(i).size(); z++) {
                if (subsets.get(i).get(z) < menorFrecuencia) {
                    menorFrecuencia = frecuencias[subsets.get(i).get(z)];
                }
            }
            if (menorFrecuencia >= minSoporte) {

                boolean encontrado = false;
                for (int k = 0; k < solucion.size(); k++) {
                    if (subsets.get(i).equals(solucion.get(k))) {
                        encontrado = true;
                    }
                }
                if (encontrado == false) {
                    solucion.add(subsets.get(i));
                }
            }

        }
    }

    /**
     * Devuelve los subconjuntos de ítems que contienen el valor y su tamaño es
     * mayor que 1.
     *
     * @param set
     * @param valor
     * @return
     */
    static ArrayList<ArrayList<Integer>> getSubsets(Integer set[], int valor) {
        int n = set.length;
        ArrayList<Integer> subset;
        ArrayList<ArrayList<Integer>> listaSubsets = new ArrayList<>();
        // Run a loop for printing all 2^n
        // subsets one by one
        for (int i = 0; i < (1 << n); i++) {
            subset = new ArrayList<>();
            // Print current subset
            for (int j = 0; j < n; j++) // (1<<j) is a number with jth bit 1
            {

                // so when we 'and' them with the
                // subset number we get which numbers
                // are present in the subset and which
                // are not
                if ((i & (1 << j)) > 0) {
                    //System.out.print(set[j] + " ");
                    subset.add(set[j]);
                }
            }
            if (subset.size() > 1) {
                if (subset.contains(valor)) {
                    listaSubsets.add(subset);
                }
            }

        }
        return listaSubsets;
    }

}
