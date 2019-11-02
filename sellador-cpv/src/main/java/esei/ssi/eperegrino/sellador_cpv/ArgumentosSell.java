package esei.ssi.eperegrino.sellador_cpv;

import java.io.File;

/**
 * Modela los argumentos de operación del sellador de CPV.
 *
 * @author Pablo Lama Valencia
 */
final class ArgumentosSell {
	private final File ficheroPaquete;
	private final File ficheroIdentificadorAlbergue;
	private final File ficheroClavePrivadaAlbergue;
	private final File ficheroClavePublicaOficina;

	/**
	 * Crea un objeto de argumentos de operación para el desempaquetador de CPV.
	 *
	 * @param ficheroPaquete               El fichero que contiene la CPV a
	 *                                     desempaquetar.
	 * @param identificadorAlbergue        El fichero que contiene el identificador
	 *                                     del albergue.
	 * @param ficheroClavePrivadaAlbergue  El fichero que contiene la clave privada
	 *                                     del albergue.
	 * @param ficheroClavePublicaOficina   El fichero que contiene la clave pública
	 *                                     de la oficina.
	 * @throws IllegalArgumentException Si algún parámetro es nulo.
	 */
	public ArgumentosSell(final File ficheroPaquete, final File identificadorAlbergue, final File ficheroClavePrivadaAlbergue, final File ficheroClavePublicaOficina) {
		if (ficheroPaquete == null || identificadorAlbergue == null || ficheroClavePrivadaAlbergue == null || ficheroClavePublicaOficina == null) {
			throw new IllegalArgumentException("Los argumentos no pueden ser nulos");
		}

		this.ficheroPaquete = ficheroPaquete;
		this.ficheroIdentificadorAlbergue = identificadorAlbergue;
		this.ficheroClavePrivadaAlbergue = ficheroClavePrivadaAlbergue;
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
	 * Obtiene el fichero que contiene el identificador del albergue.
	 *
	 * @return El descrito fichero.
	 */
	public File getFicheroIdentificadorAlbergue() {
		return ficheroIdentificadorAlbergue;
	}

	/**
	 * Obtiene el fichero que contiene la clave privada del albergue.
	 *
	 * @return El descrito fichero.
	 */
	public File getFicheroClavePrivadaAlbergue() {
		return ficheroClavePrivadaAlbergue;
	}

	/**
	 * Obtiene el fichero que contiene la clave pública de la oficina
	 *
	 * @return El descrito fichero.
	 */
	public File getFicheroClavePublicaOficina() {
		return ficheroClavePublicaOficina;
	}
}
