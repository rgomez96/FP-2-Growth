/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fp2.growth;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Callable;

/**
 *
 * @author rafa
 */
public class EjecucionParalelo implements Callable<ArrayList<ArrayList<ArrayList<Integer>>>> {

    /**
     * Nivel de restricción que gestiona este objeto.
     */
    float nivel;

    /**
     * Soporte mínimo especificado por el usuario para que un itemset sea
     * frecuente.
     */
    int minSupport;

    /**
     * Matriz compuesta de valores reales entre 0 y 1. Lógica difusa.
     */
    ArrayList<ArrayList<Float>> matrizDifusa;

    /**
     * Matriz compuesta únicamente de 0 y 1. Hecha a partir de matrizDifusa y el
     * nivel de restricción.
     */
    ArrayList<ArrayList<Integer>> matrizRL;

    /**
     * Masa de distribución asociada a nuestros RL.
     */
    ArrayList<Float> masa;

    /**
     * Lista de todas las RL
     */
    ArrayList<Float> listaRL;

    static ArrayList<ArrayList<ArrayList<Integer>>> solucionesMostradas;

    /**
     * Constructor parametrizado.
     *
     * @param nivel
     * @param matrizDifusa
     * @param minSoporte
     */
    public EjecucionParalelo(float nivel, ArrayList<ArrayList<Float>> matrizDifusa, int minSoporte, ArrayList<Float> masa, ArrayList<Float> listaRL) {
        this.nivel = nivel;
        this.matrizDifusa = matrizDifusa;
        this.matrizRL = new ArrayList<>();
        this.minSupport = minSoporte;
        this.masa = masa;
        this.listaRL = listaRL;
        solucionesMostradas = new ArrayList<>();
    }

    /**
     * Devuelve los subconjuntos de ítems.
     *
     * @param set
     * @param valor
     * @return
     */
    static ArrayList<ArrayList<Integer>> getReglas(Integer set[], ArrayList<ArrayList<Float>> matriz, float RL, ArrayList<Float> distribucionM, ArrayList<Float> listaRL) {
        int n = set.length;
        ArrayList<Integer> subset;
        ArrayList<Integer> subset2;
        ArrayList<ArrayList<Integer>> listaSubsets = new ArrayList<>();

        // Run a loop for printing all 2^n
        // subsets one by one
        for (int i = 0; i < (1 << n); i++) {
            subset = new ArrayList<>();
            subset2 = new ArrayList<>();
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
                } else {
                    subset2.add(set[j]);
                }

            }

            if (subset.size() != 0 && subset2.size() != 0) {
                //System.out.println("Subset1:" + subset + ", subset2 " + subset2);
                float CF = obtenerCF(subset, subset2, matriz, RL, distribucionM, listaRL);
                float mi = 0;
                for (int j = 0; j < listaRL.size(); j++) {
                    if (RL == listaRL.get(j)) {
                        mi = distribucionM.get(j);
                    }
                }
                float CFRL = RL - (mi * CF);
                //System.out.println("\tRL: " + RL + ". Regla: " + subset + "=>" + subset2 + ". Confidence Factor: " + CFRL);
                if (CFRL > 0.7) {
                    String solucion = ("RL: " + RL + ". Regla: " + subset + "=>" + subset2 + ". Confidence Factor: " + CFRL);
                    boolean mostrada = false;
                    ArrayList<ArrayList<Integer>> soluc = new ArrayList<>();
                    soluc.add(subset);
                    soluc.add(subset2);
                    for (int j = 0; j < solucionesMostradas.size(); j++) {
                        if (solucionesMostradas.get(j).equals(soluc)) {
                            mostrada = true;
                            break;
                        }
                    }
                    if (mostrada == false) {
                        //System.out.println(solucion);
                        solucionesMostradas.add(soluc);
                    }

                }
                //System.out.println("distri " + mi + "rl " + RL + "CF " + CF);
                //System.out.println("CF:" + CFRL);
            }

            listaSubsets.add(subset);

        }
        return listaSubsets;
    }

    static float obtenerSoporteRL(ArrayList<Integer> indices, ArrayList<ArrayList<Float>> matriz, float RL, ArrayList<Float> distribucionM, ArrayList<Float> listaRL) {
        float soporteRL = 0;
        float soporte = obtenerSoporte(indices, matriz);

        float mi = 0;
        for (int j = 0; j < listaRL.size(); j++) {
            if (RL == listaRL.get(j)) {
                mi = distribucionM.get(j);
            }
        }
        soporteRL = RL - (mi * soporte);
        return soporteRL;
    }

    /**
     * Dado un conjunto de ítems calcula su soporte bajo la matriz difusa
     * (valores reales entre 0 y 1).
     *
     * @param indices
     * @return
     */
    static float obtenerSoporte(ArrayList<Integer> indices, ArrayList<ArrayList<Float>> matriz) {

        Float[] soportes = new Float[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            float soporteActual = 0;
            for (int j = 0; j < matriz.get(i).size(); j++) {
                soporteActual += matriz.get(i).get(j);
            }
            soporteActual = soporteActual / matriz.get(0).size();
            soportes[i] = soporteActual;
        }
        float menorsoporte = Float.MAX_VALUE;
        for (int i = 0; i < soportes.length; i++) {
            if (soportes[i] < menorsoporte) {
                menorsoporte = soportes[i];
            }
        }
        return menorsoporte;
    }

    static float obtenerConfianzaRL(ArrayList<Integer> rule, ArrayList<Integer> head, ArrayList<ArrayList<Float>> matriz, float RL, ArrayList<Float> distribucionM, ArrayList<Float> listaRL) {
        float confianzaRL = 0;
        float confianza = obtenerConfianza(rule, head, matriz, RL, distribucionM, listaRL);
        float mi = 0;
        for (int j = 0; j < listaRL.size(); j++) {
            if (RL == listaRL.get(j)) {
                mi = distribucionM.get(j);
            }
        }
        confianzaRL = RL - (mi * confianza);
        return confianzaRL;
    }

    static float obtenerConfianza(ArrayList<Integer> rule, ArrayList<Integer> head, ArrayList<ArrayList<Float>> matriz, float RL, ArrayList<Float> distribucionM, ArrayList<Float> listaRL) {
        ArrayList<Integer> all = new ArrayList<>();
        all.addAll(rule);
        all.addAll(head);
        return (obtenerSoporteRL(all, matriz, RL, distribucionM, listaRL) / obtenerSoporteRL(rule, matriz, RL, distribucionM, listaRL));
    }

    static float obtenerCF(ArrayList<Integer> rule, ArrayList<Integer> head, ArrayList<ArrayList<Float>> matriz, float RL, ArrayList<Float> distribucionM, ArrayList<Float> listaRL) {
        float confianzaRegla = obtenerConfianzaRL(rule, head, matriz, RL, distribucionM, listaRL);
        float soporteHead = obtenerSoporte(head, matriz);
        float CF = 0;

        if (confianzaRegla > soporteHead) {
            CF = (confianzaRegla - soporteHead) / soporteHead;
        } else {
            CF = (confianzaRegla - soporteHead) / (1 - soporteHead);
        }
        return CF;
    }

    /**
     * Muestra la matriz de 0 y 1 por pantalla.
     */
    void mostrarMatrizRL() {
        for (int i = 0; i < matrizRL.get(0).size(); i++) {
            for (int j = 0; j < matrizRL.size(); j++) {
                System.out.print((matrizRL.get(j).get(i)) + "\t");
            }
            System.out.println("");
        }
    }

    /**
     * Método que se ejecuta concurrentemente. Crea la matriz de 0 y 1. Crea el
     * FPTree y llama a FPGrowth.
     *
     * @return
     * @throws Exception
     */
    @Override
    public ArrayList<ArrayList<ArrayList<Integer>>> call() throws Exception {
        //System.out.printf("Iniciando hilo con nivel de restriccion" + nivel + "\n");

        // Obtener una matriz de datos "crisp" (1 y 0) a través de la tabla de datos difusos.
        ArrayList<Integer> fila = new ArrayList<>();
        for (int i = 0; i < matrizDifusa.size(); i++) {
            for (int j = 0; j < matrizDifusa.get(i).size(); j++) {
                //System.out.print((matrizDifusa.get(j).get(i)) + "\t");
                if (nivel <= matrizDifusa.get(i).get(j)) {
                    fila.add(1);
                } else {
                    fila.add(0);
                }
            }

            matrizRL.add(clonarArrayInteger(fila));
            fila.clear();

        }

        // Lista de subconjuntos frecuentes devueltos por fpgrowth
        ArrayList<ArrayList<Integer>> solucion;

        // Llamar al método FP-Growth y recibir la solución para este RL
        FPTree fptree = new FPTree(minSupport, matrizRL);
        FPGrowth fpgrowth = new FPGrowth(minSupport, fptree);
        solucion = fpgrowth.metodo();

        Integer[] set = new Integer[2];
        set[0] = 1;
        set[1] = 2;
        for (int i = 0; i < solucion.size(); i++) {
            Integer[] nuevoSet = new Integer[solucion.get(i).size()];
            for (int j = 0; j < solucion.get(i).size(); j++) {
                nuevoSet[j] = solucion.get(i).get(j);
            }
            getReglas(nuevoSet, matrizDifusa, nivel, masa, listaRL);
        }
        return solucionesMostradas;
    }

    /**
     * Método auxiliar para hacer una copia dura de un arraylist de enteros.
     *
     * @param orig
     * @return
     */
    ArrayList<Integer> clonarArrayInteger(ArrayList<Integer> orig) {
        ArrayList<Integer> nuevo = new ArrayList<>();
        for (int i = 0; i < orig.size(); i++) {
            nuevo.add(orig.get(i));
        }
        return nuevo;
    }
}
