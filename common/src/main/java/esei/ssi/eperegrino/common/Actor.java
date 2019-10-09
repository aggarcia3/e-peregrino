package esei.ssi.eperegrino.common;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Modela un actor implicado en los casos de uso de e-peregrino.
 *
 * @author Alejandro González García
 */
public enum Actor {
	/**
	 * El actor Peregrino.
	 */
	PEREGRINO,
	/**
	 * El actor Albergue.
	 */
	ALBERGUE,
	/**
	 * El actor Oficina del Peregrino.
	 */
	OFICINA_PEREGRINO;

	/**
	 * La clave privada del actor.
	 */
	private EncodedKeySpec clavePrivada = null;
	/**
	 * La clave pública del actor.
	 */
	private EncodedKeySpec clavePublica = null;

	/**
	 * Establece la clave privada del actor, a partir de su especificación de
	 * codificación correspondiente.
	 *
	 * @param clavePrivada El objeto EncodedKeySpec que encapsula la representación
	 *                     codificada de la clave privada.
	 * @throws IllegalArgumentException Si la clave privada a establecer es nula.
	 */
	public final void setClavePrivada(final EncodedKeySpec clavePrivada) {
		if (clavePrivada == null) {
			throw new IllegalArgumentException("La clave privada a establecer no puede ser nula");
		}

		this.clavePrivada = clavePrivada;
	}

	/**
	 * Establece la clave privada del actor a partir de los bytes que componen su
	 * clave privada, representados en la codificación PKCS8. Normalmente, esta es
	 * la codificación usada por defecto para guardar claves privadas en ficheros.
	 *
	 * @param clavePrivada Los bytes que conforman la representación PKCS8 de la
	 *                     clave privada.
	 * @throws IllegalArgumentException Si la clave privada a establecer es nula.
	 */
	public final void setClavePrivada(final byte[] clavePrivada) {
		if (clavePrivada == null) {
			throw new IllegalArgumentException("La clave privada a establecer no puede ser nula");
		}

		setClavePrivada(new PKCS8EncodedKeySpec(clavePrivada));
	}

	/**
	 * Establece la clave pública del actor, a partir de su especificación de
	 * codificación correspondiente.
	 *
	 * @param clavePrivada El objeto EncodedKeySpec que encapsula la representación
	 *                     codificada de la clave pública.
	 * @throws IllegalArgumentException Si la clave pública a establecer es nula.
	 */
	public final void setClavePublica(final EncodedKeySpec clavePublica) {
		if (clavePublica == null) {
			throw new IllegalArgumentException("La clave pública a establecer no puede ser nula");
		}

		this.clavePublica = clavePublica;
	}

	/**
	 * Establece la clave pública del actor a partir de los bytes que componen su
	 * clave pública, representados en la codificación X509 (ASN.1). Normalmente,
	 * esta es la codificación usada por defecto para guardar claves públicas en
	 * ficheros.
	 *
	 * @param clavePrivada Los bytes que conforman la representación X509 (ASN.1) de
	 *                     la clave pública.
	 * @throws IllegalArgumentException Si la clave pública a establecer es nula.
	 */
	public final void setClavePublica(final byte[] clavePublica) {
		if (clavePublica == null) {
			throw new IllegalArgumentException("La clave pública a establecer no puede ser nula");
		}
		
		setClavePublica(new X509EncodedKeySpec(clavePublica));
	}

	/**
	 * Obtiene el objeto PrivateKey que encapsula de manera opaca la clave privada
	 * del actor. Es necesario haberle asociado previamente una representación de la
	 * clave privada.
	 *
	 * @return La devandicha clave privada.
	 * @throws IllegalStateException   Si no se ha asociado una clave privada al
	 *                                 actor todavía.
	 * @throws InvalidKeySpecException Si no se ha podido interpretar la
	 *                                 codificación de la clave privada como una
	 *                                 clave privada.
	 */
	public final PrivateKey getClavePrivada() throws InvalidKeySpecException {
		if (clavePrivada == null) {
			throw new IllegalStateException("No se puede obtener la clave privada del actor " + toString() + " sin haberle asociado una antes");
		}

		try {
			return KeyFactory.getInstance(ParametrosCriptograficos.ALGORITMO_GENERADOR_CLAVES_ASIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS).generatePrivate(clavePrivada);
		} catch (final NoSuchAlgorithmException exc) {
			throw new AssertionError("Se han violado invariantes en la implementación del programa");
		}
	}

	/**
	 * Obtiene el objeto PublicKey que encapsula de manera opaca la clave pública
	 * del actor. Es necesario haberle asociado previamente una representación de la
	 * clave pública.
	 *
	 * @return La devandicha clave pública.
	 * @throws IllegalStateException   Si no se ha asociado una clave pública al
	 *                                 actor todavía.
	 * @throws InvalidKeySpecException Si no se ha podido interpretar la
	 *                                 codificación de la clave pública como una
	 *                                 clave privada.
	 */
	public final PublicKey getClavePublica() throws InvalidKeySpecException {
		if (clavePublica == null) {
			throw new IllegalStateException("No se puede obtener la clave pública del actor " + toString() + " sin haberle asociado una antes");
		}

		try {
			return KeyFactory.getInstance(ParametrosCriptograficos.ALGORITMO_GENERADOR_CLAVES_ASIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS).generatePublic(clavePublica);
		} catch (final NoSuchAlgorithmException exc) {
			throw new AssertionError("Se han violado invariantes en la implementación del programa");
		}
	}
}
