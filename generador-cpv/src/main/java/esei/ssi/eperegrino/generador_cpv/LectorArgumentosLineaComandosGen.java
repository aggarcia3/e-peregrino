package esei.ssi.eperegrino.generador_cpv;

import java.io.File;

/**
 * Contiene la lógica necesaria para leer y validar los argumentos de línea de
 * comandos.
 *
 * @author Pablo Lama Valencia
 */
final class LectorArgumentosLineaComandosGen {
	/**
	 * Interpreta los argumentos de línea de comandos especificados, deteniendo la
	 * ejecución de la aplicación si alguno es inválido, o encapsulando dichos
	 * argumentos en un objeto si todo va bien para un procesamiento más fácil.
	 *
	 * @param args Los argumentos de línea de comandos a interpretar, tal cual son
	 *             pasados al método estático main.
	 * @return Los descritos argumentos de operación del desempaquetador de CPV.
	 */
	public static ArgumentosGen interpretar(final String[] args) {
		// Como mínimo, siempre necesitaremos 3 argumentos
		if (args.length < 3) {
			mostrarSintaxisYSalir();
		}

		// Obtener y comprobar que el fichero con la CPV sea aparentemente válido
		final File ficheroPaquete = new File(args[0]);
		if (ficheroPaquete.exists()) {
			System.out.println("El fichero " + ficheroPaquete.getName() + " ya existe, y será reemplazado con la CPV.");
		}

		// Hacer lo mismo para la clave privada del peregrino y la pública de la oficina
		final File ficheroPrivadaPeregrino = new File(args[1]);
		comprobarFicheroPlausible(ficheroPrivadaPeregrino, "la clave privada del peregrino");

		final File ficheroPublicaOficina = new File(args[2]);
		comprobarFicheroPlausible(ficheroPublicaOficina, "la clave pública de la oficina");

		return new ArgumentosGen(ficheroPaquete, ficheroPrivadaPeregrino, ficheroPublicaOficina);
	}

	/**
	 * Muestra un mensaje por la salida estándar que indica la sintaxis que deben de
	 * seguir los argumentos de línea de comandos pasados a esta aplicación, y
	 * finaliza su proceso con el código de salida de error 1.
	 */
	private static void mostrarSintaxisYSalir() {
		System.out.println(
				"Sintaxis: GenerarCredencial (fichero paquete) (fichero clave privada peregrino) (fichero clave pública oficina)");
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
