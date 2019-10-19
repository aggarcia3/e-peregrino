package esei.ssi.eperegrino.common;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

/**
 * Contiene métodos estáticos utilitarios para realizar algunas tareas comunes,
 * sencillas y típicamente tediosas de reimplementar.
 *
 * @author Alejandro González García
 */
public final class Util {
	private Util() {
		// No permitir instanciar esta clase
	}

	/**
	 * Pide interactivamente un fichero al usuario. Se asume que los parámetros de
	 * entrada son no nulos.
	 *
	 * @param mensaje     El mensaje a mostrar el usuario.
	 * @param in          La entrada de datos del usuario.
	 * @param out         La salida de texto al usuario.
	 * @param debeExistir Verdadero si el fichero debe de existir, ser legible y un
	 *                    fichero de tipo normal, falso en caso de que deba ser
	 *                    inexistente.
	 * @return El fichero especificado por el usuario.
	 */
	public static File pedirFichero(final String mensaje, final Scanner in, final PrintStream out, final boolean debeExistir) {
		File toret = null;

		do {
			out.print(mensaje + ": ");
			toret = new File(in.nextLine());
			if ((debeExistir && (!toret.canRead() || !toret.isFile())) || (!debeExistir && toret.exists())) {
				out.println("Ese fichero " + (debeExistir ? "no existe" : "ya existe") + ", introduzca otro");
				toret = null;
			}
		} while (toret == null);

		return toret;
	}

	/**
	 * Comprueba la validez de una firma digital. La implementación de este método
	 * asume que sus parámetros de entrada son no nulos y, en el caso de los
	 * cifradores, han sido inicializados con el algoritmo correspondiente.
	 *
	 * @param contenidoEsperado    Una descripción textual del contenido firmado
	 *                             digitalmente, que se puede mostrar al usuario.
	 * @param clavePublicaFirmante La clave pública del actor que ha firmado el
	 *                             contenido.
	 * @param cifradorAsimetrico   El cifrador asimétrico con el que se ha creado la
	 *                             firma digital.
	 * @param cargaUtil            El contenido firmado digitalmente.
	 * @param resumenCargaUtil     El resumen (hash) del contenido que el firmante
	 *                             ha firmado.
	 * @throws GeneralSecurityException Si ocurre algún error al realizar alguna
	 *                                  operación criptográfica, o la firma no es
	 *                                  válida.
	 */
	public static void comprobarValidezFirma(
		final String contenidoEsperado,
		final PublicKey clavePublicaFirmante,
		final Cipher cifradorAsimetrico,
		final byte[] cargaUtil,
		final byte[] resumenCargaUtil
	) throws GeneralSecurityException {
		// Calcular el hash de la carga útil
		final byte[] hashCargaUtil = MessageDigest.getInstance(
			ParametrosCriptograficos.ALGORITMO_RESUMEN,
			ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS
		).digest(cargaUtil);

		// Ahora desencriptar el hash del contenido firmado por la entidad
		cifradorAsimetrico.init(Cipher.DECRYPT_MODE, clavePublicaFirmante);
		final byte[] hashEsperadoCargaUtil = cifradorAsimetrico.doFinal(resumenCargaUtil);

		// Los hashes coinciden si y solo si la firma es válida
		if (!Arrays.equals(hashCargaUtil, hashEsperadoCargaUtil)) {
			throw new GeneralSecurityException(contenidoEsperado + " no se corresponde con el firmado. Es posible que la clave pública no sea de la misma entidad, o los datos se hayan modificado de manera no autorizada");
		}
	}

	/**
	 * Desencripta una carga útil encriptada con un cifrador simétrico, donde la
	 * clave a usar para tal cifrador está, a su vez, cifrada con un cifrador
	 * asimétrico, del que conocemos la clave privada. La implementación de este
	 * método asume que sus parámetros de entrada son no nulos y, en el caso de los
	 * cifradores, han sido inicializados con el algoritmo correspondiente.
	 *
	 * @param cifradorAsimetrico       El cifrador asimétrico que se ha empleado
	 *                                 para cifrar la clave del cifrador simétrico.
	 * @param cifradorSimetrico        El cifrador simétrico que se ha empleado para
	 *                                 cifrar la carga útil.
	 * @param clavePrivadaDestinatario La clave privada del destinatario de la carga
	 *                                 útil, con la que se descifrará la clave del
	 *                                 cifrador simétrico.
	 * @param claveSimetricoEncriptada Los bytes que contienen la clave del cifrador
	 *                                 simétrico, encriptada con el cifrador
	 *                                 asimétrico.
	 * @param cargaUtilSimetrico       La carga útil que ha sido cifrada con el
	 *                                 cifrador simétrico.
	 * @return Una cadena de texto que contiene la carga útil descifrada,
	 *         interpretando sus bytes según la codificación UTF-8.
	 * @throws GeneralSecurityException Si ocurre algún error al realizar alguna
	 *                                  operación criptográfica.
	 */
	public static String desencriptarCargaUtil(
		final Cipher cifradorAsimetrico,
		final Cipher cifradorSimetrico,
		final PrivateKey clavePrivadaDestinatario,
		final byte[] claveSimetricoEncriptada,
		final byte[] cargaUtilSimetrico
	) throws GeneralSecurityException {
		SecretKey claveCifradorSimetrico;

		// Obtener la clave del cifrador simétrico desencriptándola con cifrador asimétrico
		// (esto ha garantizado confidencialidad)
		cifradorAsimetrico.init(Cipher.DECRYPT_MODE, clavePrivadaDestinatario);
		claveCifradorSimetrico = SecretKeyFactory.getInstance(
			ParametrosCriptograficos.ALGORITMO_GENERADOR_CLAVES_SIMETRICO,
			ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS
		).generateSecret(
			new SecretKeySpec(
				cifradorAsimetrico.doFinal(claveSimetricoEncriptada),
				ParametrosCriptograficos.ALGORITMO_GENERADOR_CLAVES_SIMETRICO
			)
		);

		// Desencriptar la carga útil encriptada con el cifrador asimétrico, usando
		// la clave obtenida anteriormente
		cifradorSimetrico.init(Cipher.DECRYPT_MODE, claveCifradorSimetrico);
		return new String(
			cifradorSimetrico.doFinal(cargaUtilSimetrico),
			StandardCharsets.UTF_8
		);
	}
}
