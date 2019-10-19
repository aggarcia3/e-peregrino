package esei.ssi.eperegrino.desempaquetador_cpv;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

/**
 * Contiene la lógica necesaria para leer y validar los argumentos de línea de
 * comandos.
 *
 * @author Alejandro González García
 */
final class LectorArgumentosLineaComandos {
	/**
	 * Interpreta los argumentos de línea de comandos especificados, deteniendo la
	 * ejecución de la aplicación si alguno es inválido, o encapsulando dichos
	 * argumentos en un objeto si todo va bien para un procesamiento más fácil.
	 *
	 * @param args Los argumentos de línea de comandos a interpretar, tal cual son
	 *             pasados al método estático main.
	 * @return Los descritos argumentos de operación del desempaquetador de CPV.
	 */
	public static ArgumentosDesempaquetador interpretar(final String[] args) {
		// Como mínimo, siempre necesitaremos 4 argumentos
		if (args.length < 4) {
			mostrarSintaxisYSalir();
		}

		// Empezar leyendo el número de albergues, ya que eso
		// influye en el número de argumentos necesarios
		short nAlbergues = 0; // Valor no usado
		try {
			nAlbergues = Short.parseShort(args[1]);
			if (nAlbergues < 0) {
				throw new NumberFormatException();
			}
		} catch (final NumberFormatException exc) {
			System.err.println("El número de albergues especificado no es un entero positivo");
			mostrarSintaxisYSalir();
		}

		// Ahora que sabemos el número de albergues, podemos hacer
		// una comparación exacta con el número de argumentos esperado
		if (args.length != 4 + nAlbergues * 2) {
			mostrarSintaxisYSalir();
		}

		// Obtener y comprobar que el fichero con la CPV sea aparentemente válido
		final File ficheroPaquete = new File(args[0]);
		comprobarFicheroPlausible(ficheroPaquete, "la credencial virtual del peregrino");

		// Hacer lo mismo para la clave privada de la oficina y la pública del peregrino
		final File ficheroPrivadaOficina = new File(args[args.length - 2]);
		comprobarFicheroPlausible(ficheroPrivadaOficina, "la clave privada de la oficina del peregrino");

		final File ficheroPublicaPeregrino = new File(args[args.length - 1]);
		comprobarFicheroPlausible(ficheroPublicaPeregrino, "la clave pública del peregrino");

		// Para cada albergue, leer su identificador y clave pública,
		// y añadir esa información a una lista
		final List<Entry<String, File>> listaAlbergues = new ArrayList<>(nAlbergues);
		for (int i = 0; i < nAlbergues; ++i) {
			 final String identificadorAlbergue = args[2 + i];
			 final File ficheroPublicaAlbergue = new File(args[2 + i + 1]);

			 // Abortar el proceso si algún fichero de clave pública no se puede leer
			 comprobarFicheroPlausible(ficheroPublicaAlbergue, "la clave pública del albergue \"" + identificadorAlbergue + "\"");

			 listaAlbergues.add(new SimpleImmutableEntry<>(identificadorAlbergue, ficheroPublicaAlbergue));
		}

		return new ArgumentosDesempaquetador(ficheroPaquete, ficheroPrivadaOficina, ficheroPublicaPeregrino, listaAlbergues);
	}

	/**
	 * Muestra un mensaje por la salida estándar que indica la sintaxis que deben de
	 * seguir los argumentos de línea de comandos pasados a esta aplicación, y
	 * finaliza su proceso con el código de salida de error 1.
	 */
	private static void mostrarSintaxisYSalir() {
		System.out.println("Sintaxis: DesempaquetarCredencial (fichero paquete) (núm. albergues) [identificador albergue 1] [clave pública albergue 1] ... [identificador albergue N] [clave pública albergue N] (fichero clave privada oficina) (fichero clave pública peregrino)");
		System.exit(1);
	}

	/**
	 * Si el fichero pasado como parámetro no es plausible (legible en el momento
	 * presente), finaliza el proceso de la aplicación con código de salida de error
	 * 2, mostrando antes un mensaje por el flujo de salida de error con más
	 * información acerca del fichero que se ha comprobado.
	 *
	 * @param fichero   El fichero a comprobar si es plausible.
	 * @param contenido Una descripción textual del contenido esperado del fichero,
	 *                  que se mostrará al usuario.
	 */
	private static void comprobarFicheroPlausible(final File fichero, final String contenido) {
		 if (!fichero.canRead() || !fichero.isFile()) {
			 System.err.println("No se puede leer el fichero " + fichero.getAbsolutePath() + " con " + contenido + ", o no es un fichero");
			 System.exit(2);
		 }
	}
}
