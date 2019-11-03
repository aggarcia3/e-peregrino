package esei.ssi.eperegrino.desempaquetador_cpv;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

/**
 * Modela los argumentos de operación del desempaquetador de CPV.
 *
 * @author Alejandro González García
 */
final class ArgumentosDesempaquetador {
	private final File ficheroPaquete;
	private final File ficheroClavePrivadaOficina;
	private final File ficheroClavePublicaPeregrino;
	private final List<Entry<String, byte[]>> albergues;

	/**
	 * Crea un objeto de argumentos de operación para el desempaquetador de CPV.
	 *
	 * @param ficheroPaquete               El fichero que contiene la CPV a
	 *                                     desempaquetar.
	 * @param ficheroClavePrivadaOficina   El fichero que contiene la clave privada
	 *                                     de la oficina del peregrino.
	 * @param ficheroClavePublicaPeregrino El fichero que contiene la clave pública
	 *                                     del peregrino propietario de la CPV.
	 * @param albergues                    Una lista de identificadores de albergues
	 *                                     por los que se piensa que fue sellada la
	 *                                     CPV, asociados con su clave pública.
	 * @throws IllegalArgumentException Si algún parámetro es nulo.
	 */
	public ArgumentosDesempaquetador(final File ficheroPaquete, final File ficheroClavePrivadaOficina, final File ficheroClavePublicaPeregrino, final List<Entry<String, byte[]>> albergues) {
		if (ficheroPaquete == null || ficheroClavePrivadaOficina == null || ficheroClavePublicaPeregrino == null || albergues == null) {
			throw new IllegalArgumentException("Los argumentos del desempaquetador de CPV no pueden ser nulos");
		}

		this.ficheroPaquete = ficheroPaquete;
		this.ficheroClavePrivadaOficina = ficheroClavePrivadaOficina;
		this.ficheroClavePublicaPeregrino = ficheroClavePublicaPeregrino;
		this.albergues = Collections.unmodifiableList(albergues);
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
	 * Obtiene el fichero que contiene la clave privada de la oficina del peregrino.
	 *
	 * @return El descrito fichero.
	 */
	public File getFicheroClavePrivadaOficina() {
		return ficheroClavePrivadaOficina;
	}

	/**
	 * Obtiene el fichero que contiene la clave pública del peregrino propietario de
	 * la CPV.
	 *
	 * @return El descrito fichero.
	 */
	public File getFicheroClavePublicaPeregrino() {
		return ficheroClavePublicaPeregrino;
	}

	/**
	 * Obtiene la lista de identificadores de albergues por los que se piensa que
	 * fue sellada la CPV, asociados con su clave pública.
	 *
	 * @return La lista descrita. No se garantiza que sea una lista modificable.
	 */
	public List<Entry<String, byte[]>> getAlbergues() {
		return albergues;
	}
}
