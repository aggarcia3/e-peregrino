package esei.ssi.eperegrino.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

/**
 * @author ribadas
 */
public final class PaqueteDAO {
	// Modificaciones menores para trabajar con flujos en lugar de ficheros.
	// Ello es más flexible y facilita tests unitarios

	public final static String MARCA_CABECERA = "-----";
	public final static String INICIO_PAQUETE = MARCA_CABECERA + "BEGIN PACKAGE" + MARCA_CABECERA;
	public final static String FIN_PAQUETE = MARCA_CABECERA + "END PACKAGE" + MARCA_CABECERA;
	public final static String INICIO_BLOQUE = MARCA_CABECERA + "BEGIN BLOCK";
	public final static String FIN_BLOQUE = MARCA_CABECERA + "END BLOCK";
	public final static String INICIO_BLOQUE_FORMATO = INICIO_BLOQUE + " %s" + MARCA_CABECERA;
	public final static String FIN_BLOQUE_FORMATO = FIN_BLOQUE + " %s" + MARCA_CABECERA;
	public final static int ANCHO_LINEA = 65;

	public static Paquete leerPaquete(InputStream entrada) throws IOException {
		Paquete result = null;

		BufferedReader in = new BufferedReader(new InputStreamReader(entrada));
		String linea = in.readLine();

		result = new Paquete();

		// Modificado por Alejandro para evitar lanzar NullPointerException
		// si el paquete no contiene INICIO_PAQUETE
		while (linea != null && !linea.equals(INICIO_PAQUETE)) {
			linea = in.readLine();
		}

		if (INICIO_PAQUETE.equals(linea)) {
			Bloque bloque = leerBloque(in);
			while (bloque != null) {
				result.anadirBloque(bloque.getNombre(), bloque.getContenido());
				bloque = leerBloque(in);
			}
		}

		return result;
	}

	public static void escribirPaquete(OutputStream out, Paquete paquete) throws IOException {
		final PrintStream outPs = new PrintStream(out);

		outPs.println(INICIO_PAQUETE);

		for (String nombreBloque : paquete.getNombresBloque()) {
			escribirBloque(outPs, nombreBloque, paquete.getContenidoBloque(nombreBloque));
		}

		outPs.println(FIN_PAQUETE);

		if (outPs.checkError()) {
			throw new IOException("Ha ocurrido un error de E/S durante la escritura de un paquete");
		}
	}

	private static void escribirBloque(PrintStream out, String nombreBloque, byte[] contenido) {
		if ((nombreBloque != null) && (contenido != null)) {
			out.printf(INICIO_BLOQUE_FORMATO + "\n", nombreBloque);

			byte[] contenidoBASE64 = Base64.getEncoder().encode(contenido);

			int lineas = contenidoBASE64.length / ANCHO_LINEA;
			int resto = contenidoBASE64.length % ANCHO_LINEA;
			for (int i = 0; i < lineas; i++) {
				out.println(new String(contenidoBASE64, i * ANCHO_LINEA, ANCHO_LINEA));
			}
			out.println(new String(contenidoBASE64, lineas * ANCHO_LINEA, resto));

			out.printf(FIN_BLOQUE_FORMATO + "\n", nombreBloque);
		}
	}

	private static Bloque leerBloque(BufferedReader in) throws IOException {
		String linea = in.readLine();

		while ((!linea.startsWith(INICIO_BLOQUE) && (!linea.equals(FIN_PAQUETE)))) {
			linea = in.readLine();
		}

		if (linea.equals(FIN_PAQUETE)) {
			return null; // No hay más bloques
		} else {
			Bloque result = new Bloque();
			result.setNombre(extraerNombreBloque(linea));
			result.setContenido(extraerContenidoBloque(in));
			return result;
		}
	}

	private static String extraerNombreBloque(String texto) {
		int inicioNombreBloque = INICIO_BLOQUE.length() + 1;
		int finNombreBloque = texto.lastIndexOf(MARCA_CABECERA);
		return texto.substring(inicioNombreBloque, finNombreBloque);
	}

	private static byte[] extraerContenidoBloque(BufferedReader in) throws IOException {
		List<String> partesBloque = new ArrayList<String>();
		int tamanoBloque = 0;

		String linea = in.readLine(); // Avanzar una linea
		while (!linea.startsWith(FIN_BLOQUE)) {
			partesBloque.add(linea);
			tamanoBloque += linea.length();
			linea = in.readLine();
		}

		byte[] result = new byte[tamanoBloque];
		int posicion = 0;
		for (String parte : partesBloque) {
			byte[] contenidoParte = parte.getBytes();
			for (byte b : contenidoParte) {
				result[posicion] = b;
				posicion++;
			}
		}
		return Base64.getDecoder().decode(result);
	}
}