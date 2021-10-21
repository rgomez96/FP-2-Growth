/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fp2.growth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rafa
 */
public class FP2Growth {

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
                System.out.println("Regla: " + subset + "=>" + subset2);
                float CF = obtenerCF(subset, subset2, matriz);
                float mi = 0;
                for (int j = 0; j < listaRL.size(); j++) {
                    if (RL == listaRL.get(j)) {
                        mi = distribucionM.get(j);
                    }
                }
                float CFRL = RL - (mi * CF);
                System.out.println("distri " + mi + "rl " + RL + "CF " + CF);
                System.out.println("CF:" + CFRL);
            }

            listaSubsets.add(subset);

        }
        return listaSubsets;
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

    static float obtenerConfianza(ArrayList<Integer> rule, ArrayList<Integer> head, ArrayList<ArrayList<Float>> matriz) {
        return (obtenerSoporte(rule, matriz) / obtenerSoporte(head, matriz));
    }

    static float obtenerCF(ArrayList<Integer> rule, ArrayList<Integer> head, ArrayList<ArrayList<Float>> matriz) {
        float confianzaRegla = obtenerConfianza(rule, head, matriz);
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
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Empezamos un cronómetro para evaluar el rendimiento del algoritmo.
        long startTime = System.currentTimeMillis();
        long endTime = 0;

        // Comprobamos que los parámetros de entrada se reciben adecuadamente.
        if (args.length < 2) {
            try {
                throw new Exception("El programa debe recibir 2 argumentos: La ruta del dataset y el soporte mínimo, en ese orden.");
            } catch (Exception ex) {
                Logger.getLogger(FP2Growth.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }

        System.out.println("Iniciando programa. Ruta del archivo: " + args[0] + ", soporte mínimo: " + args[1]);

        int minSoporte = Integer.valueOf(args[1]);

        /**
         * Primero leo el dataset en mi fichero.
         */
        ArrayList<ArrayList<Float>> matrizDatos;
        LecturaArchivo lector = new LecturaArchivo(args[0]);
        matrizDatos = lector.leer();
        ArrayList<String> atributos = lector.getAtributos();
        //System.out.println("Atributos: " + atributos);

        /**
         * Creo la matriz de datos difusos.
         */
        MatrizyNiveles reglas = new MatrizyNiveles(matrizDatos);
        reglas.crearMatrizDifusa();

        //Descomentar aquí para ver las matrices.
//        System.out.println("Matriz de datos");
//        reglas.mostrarMatrizDatos();
//        reglas.mostrarMatrizDifusa();
        /**
         * Obtengo los niveles de restricción.
         */
        reglas.obtenerNivelesRestriccion();
        TreeSet<Float> niveles = reglas.getNivelesRestriccion();

        // Iterador para leer el treeset (al mostrar la info)
        Iterator<Float> iterador = niveles.iterator();
        ArrayList<Float> listaRL = new ArrayList<>();

        while (iterador.hasNext()) {
            //System.out.print(iterador.next()
            listaRL.add(iterador.next());
        }

        // Obtén la matriz de valores reales entre 1 y 0.
        ArrayList<ArrayList<Float>> matrizDifusa = reglas.getMatrizDifusa();

        // Crea la masa de distribución que utilizaremos para obtener las reglas de asociación.
        ArrayList<Float> distribucionM = new ArrayList<>();
        for (int i = 0; i < listaRL.size() - 1; i++) {
            distribucionM.add(listaRL.get(i) + listaRL.get(i + 1));
        }
        distribucionM.add(listaRL.get(listaRL.size() - 1));

        /**
         * Lanzo tantos procesos EjecucionParalelo como niveles de restricción
         * haya, cada proceso ejecuta el algoritmo FPGrowth. Utilizo el marco
         * executor y recibo las soluciones mediante la interfaz Future.
         */
        ExecutorService executor = (ExecutorService) Executors.newFixedThreadPool(niveles.size());

        List<EjecucionParalelo> listaEjecuciones = new ArrayList<>();
        for (int i = 0; i < listaRL.size(); i++) {
            EjecucionParalelo ejecucion = new EjecucionParalelo(listaRL.get(i), matrizDifusa, minSoporte, distribucionM, listaRL);
            listaEjecuciones.add(ejecucion);
            niveles.remove(niveles.first());
        }

        List<Future<ArrayList<ArrayList<ArrayList<Integer>>>>> listaResultados = null;
        try {
            listaResultados = executor.invokeAll(listaEjecuciones);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("La ejecución en paralelo ha terminado.");

        ArrayList<ArrayList<ArrayList<Integer>>> soluciones = new ArrayList<>();

        for (int i = 0; i < listaResultados.size(); i++) {
            Future<ArrayList<ArrayList<ArrayList<Integer>>>> future = listaResultados.get(i);
            try {
                ArrayList<ArrayList<ArrayList<Integer>>> numero = future.get();
                System.out.println("Resultados para RL " + listaRL.get(i) + ":  " + numero);
                for (int j = 0; j < numero.size(); j++) {
                    if (soluciones.size() < 1) {
                        soluciones.add(numero.get(j));
                    } else {
                        boolean encontrado = false;
                        for (int z = 0; z < soluciones.size(); z++) {
                            if (numero.get(j).equals(soluciones.get(z))) {
                                encontrado = true;
                            }
                        }
                        if (encontrado == false) {
                            soluciones.add(numero.get(j));
                        }
                    }

                }
                // Si numero de veces presente / num de RL >= 0.5 la añado

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        Integer[] numApariciones = new Integer[soluciones.size()];
        Arrays.fill(numApariciones, 0);

        for (int i = 0; i < listaResultados.size(); i++) {
            Future<ArrayList<ArrayList<ArrayList<Integer>>>> future = listaResultados.get(i);
            try {
                ArrayList<ArrayList<ArrayList<Integer>>> producto = future.get();
                for (int j = 0; j < producto.size(); j++) {
                    for (int z = 0; z < soluciones.size(); z++) {
                        if (soluciones.get(z).equals(producto.get(j))) {
                            numApariciones[z]++;
                            break;
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        System.out.println("\n\nSoluciones únicas confidence factor mayor que 0.7 y con soporte mayor a 0.5:");
        for (int i = 0; i < soluciones.size(); i++) {
            if (numApariciones[i] >= niveles.size() / 2) {
                //System.out.print("\t" + soluciones.get(i));
                //System.out.println(soluciones.get(i));
                for (int j = 0; j < soluciones.get(i).size(); j++) {
                    for (int z = 0; z < soluciones.get(i).get(j).size(); z++) {
                        System.out.print(atributos.get(soluciones.get(i).get(j).get(z)));
                    }
                    if (j != soluciones.get(i).size() - 1) {
                        System.out.print(" -> ");
                    }
                }
                System.out.println("");
            }
        }

//        System.out.println("Soluciones unicas");
//        for(int i = 0 ; i < soluciones.size();i++) {
//            System.out.print(soluciones.get(i)+ "\t");
//        }
//        System.out.println("Num apariciones");
//        for(int i = 0; i < numApariciones.length;i++){
//            System.out.print(numApariciones[i]+"\t");
//        }
        System.out.println("Num niveles de restricción:" + listaRL.size());
        long timeneeded = ((System.currentTimeMillis() - startTime));
        System.out.println("Tiempo tomado para ejecutar el algoritmo (en ms):" + timeneeded);

        //getReglas(set, reglas.matrizDifusa,rl,distribucionM,listaRL);
    }

}
