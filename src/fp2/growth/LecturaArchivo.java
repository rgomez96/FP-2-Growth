/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fp2.growth;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author rafa
 */
public class LecturaArchivo {

    /**
     * Nombre del archivo.
     */
    private final String nombreArchivo;

    /**
     * Lista de nombres de los ítems.
     */
    ArrayList<String> atributos;
    
    /**
     * Constructor parametrizado.
     * @param nombreArchivo 
     */
    public LecturaArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
        this.atributos=new ArrayList<>();
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public ArrayList<String> getAtributos() {
        return atributos;
    }
    

    /**
     * Se busca leer el dataset linea a linea. Los datasets utilizan el formato
     * de weka. Se deben ignorar los comentarios (lineas que comienzan con %).
     */
    public ArrayList<ArrayList<Float>> leer() {
        ArrayList<ArrayList<Float>> matrizDatos = new ArrayList<>();
        try {

            File fichero = new File(nombreArchivo);
            Scanner read = new Scanner(fichero);

            /**
             * Leemos el dataset linea a linea. Primero buscamos las líneas que
             * comienzan por @attribute para identificar los nombres de nuestros
             * datos.
             */
            boolean leyendoAtributos = true;
            while (leyendoAtributos) {
                String data = read.nextLine();
                if (!data.isEmpty()) {
                    if (data.contains("@attribute")) {
                        //System.out.println(data);
                        atributos.add(data.split(" ")[1]);
                    } else if (data.contains("@data")) {
                        leyendoAtributos = false;
                    }
                }
            }

            /**
             * A partir de ahora leemos los datos.
             */
            while (read.hasNextLine()) {
                String data = read.nextLine();
                if (!data.isEmpty()) {
                    String primeraLetra = "";
                    primeraLetra = data.substring(0, 1);
                    if (!primeraLetra.equals("%") && !primeraLetra.equals("@")) {
                        //System.out.println(data);
                        //ArrayList<String> fila=data.split(",");
                        ArrayList<Float> fila = new ArrayList();
                        String[] lista = data.split(",");
                        for (String dato : lista) {
                            float datoNum = Float.parseFloat(dato);
                            fila.add(datoNum);
                        }
                        //System.out.println(fila.get(0));
                        matrizDatos.add(fila);
                    }
                }
            }
            read.close();

        } catch (FileNotFoundException e) {
            System.out.println("Hay un error leyendo el fichero :(");
            e.printStackTrace();
        }
        return matrizDatos;
    }
}
