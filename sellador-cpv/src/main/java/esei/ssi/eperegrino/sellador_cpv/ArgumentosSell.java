package esei.ssi.eperegrino.sellador_cpv;

import java.io.File;

/**
 * Modela los argumentos de operación del sellador de CPV.
 *
 * @author Pablo Lama Valencia
 */
final class ArgumentosSell {
	private final File ficheroPaquete;
	private final String identificadorAlbergue;
	private final File ficheroClavePrivadaAlbergue;
	private final File ficheroClavePublicaOficina;

	/**
	 * Crea un objeto de argumentos de operación para el sellador de CPV.
	 *
	 * @param ficheroPaquete              El fichero que contiene la CPV a
	 *                                    sellar.
	 * @param identificadorAlbergue       El identificador del albergue.
	 * @param ficheroClavePrivadaAlbergue El fichero que contiene la clave privada
	 *                                    del albergue.
	 * @param ficheroClavePublicaOficina  El fichero que contiene la clave pública
	 *                                    de la oficina.
	 * @throws IllegalArgumentException Si algún parámetro es nulo.
	 */
	public ArgumentosSell(final File ficheroPaquete, final String identificadorAlbergue, final File ficheroClavePrivadaAlbergue, final File ficheroClavePublicaOficina) {
		if (ficheroPaquete == null || identificadorAlbergue == null || ficheroClavePrivadaAlbergue == null || ficheroClavePublicaOficina == null) {
			throw new IllegalArgumentException("Los argumentos no pueden ser nulos");
		}

		this.ficheroPaquete = ficheroPaquete;
		this.identificadorAlbergue = identificadorAlbergue;
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
	 * Obtiene el identificador del albergue.
	 *
	 * @return El descrito identificador.
	 */
	public String getIdentificadorAlbergue() {
		return identificadorAlbergue;
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
