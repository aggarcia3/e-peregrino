package esei.ssi.eperegrino.generador_cpv;

import java.io.File;

/**
 * Modela los argumentos de operación del desempaquetador de CPV.
 *
 * @author Pablo Lama Valencia
 */
final class ArgumentosGen {
	private final File ficheroPaquete;
	private final File ficheroClavePrivadaPeregrino;
	private final File ficheroClavePublicaOficina;

	/**
	 * Crea un objeto de argumentos de operación para el desempaquetador de CPV.
	 *
	 * @param ficheroPaquete               El fichero que contiene la CPV a
	 *                                     desempaquetar.
	 * @param ficheroClavePrivadaPeregrino El fichero que contiene la clave privada
	 *                                     del peregrino.
	 * @param ficheroClavePublicaOficina   El fichero que contiene la clave pública
	 *                                     de la oficina.
	 * @throws IllegalArgumentException Si algún parámetro es nulo.
	 */
	public ArgumentosGen(final File ficheroPaquete, final File ficheroClavePrivadaPeregrino, final File ficheroClavePublicaOficina) {
		if (ficheroPaquete == null || ficheroClavePrivadaPeregrino == null || ficheroClavePublicaOficina == null) {
			throw new IllegalArgumentException("Los argumentos no pueden ser nulos");
		}

		this.ficheroPaquete = ficheroPaquete;
		this.ficheroClavePrivadaPeregrino = ficheroClavePrivadaPeregrino;
		this.ficheroClavePublicaOficina = ficheroClavePublicaOficina;
	}

	/**
	 * Obtiene el fichero que contiene la CPV a desempaquetar.
	 *
	 * @return El descrito fichero.
	 */
	public File getFicheroPaquete() {
		return ficheroPaquete;
	}

	/**
	 * Obtiene el fichero que contiene la clave privada del peregrino.
	 *
	 * @return El descrito fichero.
	 */
	public File getFicheroClavePrivadaPeregrino() {
		return ficheroClavePrivadaPeregrino;
	}

	/**
	 * Obtiene el fichero que contiene la clave pública de la oficina del peregrino.
	 *
	 * @return El descrito fichero.
	 */
	public File getFicheroClavePublicaOficina() {
		return ficheroClavePublicaOficina;
	}
}
