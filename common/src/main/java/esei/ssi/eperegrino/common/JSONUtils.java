package esei.ssi.eperegrino.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ribadas
 */
public final class JSONUtils {
    /**
     * Parsea una cadena en el formato JSON simplificado y devuelve los pares
     * clave-valor en un Map<String,String>
     */
    public static Map<String, String> json2map(String json) {
        Map<String, String> resultado = new HashMap<String, String>();
        int inicio = json.indexOf("{");
        int fin = json.indexOf("}");
        if ((inicio != -1) && (fin != -1) && (inicio < fin)) {
            String contenido = json.substring((inicio + 1), fin).trim();
            String[] entradas = contenido.split("\\s*,\\s*"); // Separar por ","
            for (String entrada : entradas) {
                String[] par = entrada.trim().split("\\s*:\\s*", 2); // Separar por ";"
                resultado.put(par[0].replace("\"", ""), par[1].replace("\"", ""));
            }
        }
        return resultado;
    }

    /**
     * Crea una cadena en formato JSON simplificado a partir de los pares
     * clave-valor de un Map<String,String> Si es necesario, omite los caracteres
     * "especiales" presentes en claves y valores
     */
    public static String map2json(Map<String, String> datos) {
        StringBuilder resultado = new StringBuilder();
        resultado.append('{');
        if (datos != null) {
            for (Map.Entry<String, String> entrada : datos.entrySet()) {
                if (resultado.length() > 1) { // Anadir separador ","
                    resultado.append(',');
                }
                resultado.append('\"');
                resultado.append(limpiarCadena(entrada.getKey()));
                resultado.append('\"');
                resultado.append(':');
                resultado.append('\"');
                resultado.append(limpiarCadena(entrada.getValue()));
                resultado.append('\"');
            }
        }
        resultado.append('}');
        return resultado.toString();
    }

    /**
     * TRAMPA: Elimina caracteres del formato JSON simplificado ('{' '}' ',' ':'
     * '"') para facilitar el parseo
     */
    private static String limpiarCadena(String cadena) {
        return cadena.replaceAll("\\{|\\}|:|,|\\\"", "");
    }
}