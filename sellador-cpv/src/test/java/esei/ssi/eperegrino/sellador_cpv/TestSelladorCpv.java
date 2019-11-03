package esei.ssi.eperegrino.sellador_cpv;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.After;
import org.junit.Test;

import esei.ssi.eperegrino.common.Actor;
import esei.ssi.eperegrino.common.GestorProveedoresJCA;
import esei.ssi.eperegrino.common.NombresBloques;
import esei.ssi.eperegrino.common.PaqueteDAO;
import esei.ssi.eperegrino.common.ParametrosCriptograficos;
import esei.ssi.eperegrino.generador_cpv.GeneradorCpv;

/**
 * La batería de tests de JUnit a ejecutar sobre la clase SelladorCpv.
 *
 * @author Alejandro González García
 */
public final class TestSelladorCpv {
	/**
	 * La clave pública de la oficina del peregrino, generada aleatoriamente.
	 */
	private static final byte[] clavePublicaOficinaPeregrino;
	/**
	 * La clave pública del peregrino, generada aleatoriamente.
	 */
	private static final byte[] clavePrivadaPeregrino;
	/**
	 * La clave privada del albergue, generada aleatoriamente.
	 */
	private static final byte[] clavePrivadaAlbergue;
	/**
	 * Unos pares de datos clave-valor a probar su conversión.
	 */
	private static final Map<String, String> datos = new HashMap<>();
	/**
	 * Unos pares de datos clave-valor a probar su conversión.
	 */
	private static final Map<String, String> datosSello = new HashMap<>();
	/**
	 * Un flujo de salida para recoger los resultados de las pruebas.
	 */
	private static final ByteArrayOutputStream bos = new ByteArrayOutputStream();

	static {
		// Inicializar pares clave-valor
		datos.put("Nombre", "Alejandro");
		datos.put("Motivación", "Aprobar");
		datosSello.put("Nombre", "Albergue de prueba");
		datosSello.put("Incidencias", "Ninguna");

		GestorProveedoresJCA.registrarProveedores();

		KeyPairGenerator generadorClaves;
		try {
			generadorClaves = KeyPairGenerator.getInstance(ParametrosCriptograficos.ALGORITMO_GENERADOR_CLAVES_ASIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS);
		} catch (final NoSuchAlgorithmException exc) {
			throw new AssertionError(exc);
		}
		generadorClaves.initialize(4096); // Clave grande para evitar problemas de longitud insuficiente en tests

		// Generar claves aleatorias para probar los casos de uso
		final KeyPair parClavesPeregrino = generadorClaves.generateKeyPair();
		final KeyPair parClavesOficinaPeregrino = generadorClaves.generateKeyPair();
		final KeyPair parClavesAlbergue = generadorClaves.generateKeyPair();

		clavePrivadaPeregrino = parClavesPeregrino.getPrivate().getEncoded();
		clavePublicaOficinaPeregrino = parClavesOficinaPeregrino.getPublic().getEncoded();
		clavePrivadaAlbergue = parClavesAlbergue.getPrivate().getEncoded();
	}

	@After
	public void restaurarFlujos() {
	    bos.reset();
	}

	/**
	 * Comprueba que la generación y sellado de un paquete de CPV con datos válidos
	 * no lance excepciones.
	 */
	@Test
	public void testGenerarYSellarPaqueteCpv() throws Exception {
		final String idAlbergue = "Albergue de prueba";

		Actor.OFICINA_PEREGRINO.setClavePublica(clavePublicaOficinaPeregrino);
		Actor.PEREGRINO.setClavePrivada(clavePrivadaPeregrino);

		GeneradorCpv.generarPaqueteCPV(datos, bos);

		final byte[] salidaPaquete = bos.toByteArray();
		bos.reset();

		Actor.ALBERGUE.setClavePrivada(clavePrivadaAlbergue);

		SelladorCpv.sellarCpv(datosSello, new ByteArrayInputStream(salidaPaquete), bos, idAlbergue);

		// Un paquete aparentemente bien formado, con seis bloques
		assertThat(
			bos.toString(StandardCharsets.UTF_8.displayName()),
			allOf(
				stringContainsInOrder(
					PaqueteDAO.INICIO_PAQUETE,
					PaqueteDAO.INICIO_BLOQUE,
					PaqueteDAO.FIN_BLOQUE,
					PaqueteDAO.INICIO_BLOQUE,
					PaqueteDAO.FIN_BLOQUE,
					PaqueteDAO.INICIO_BLOQUE,
					PaqueteDAO.FIN_BLOQUE,
					PaqueteDAO.INICIO_BLOQUE,
					PaqueteDAO.FIN_BLOQUE,
					PaqueteDAO.INICIO_BLOQUE,
					PaqueteDAO.FIN_BLOQUE,
					PaqueteDAO.INICIO_BLOQUE,
					PaqueteDAO.FIN_BLOQUE,
					PaqueteDAO.FIN_PAQUETE
				),
				containsString(
					NombresBloques.TITULO_BLOQUE_DATOS_SELLO_ALBERGUE.replace("{ID}", idAlbergue)
					.trim().replaceAll(" ", "_").toUpperCase()
				),
				containsString(
					NombresBloques.TITULO_BLOQUE_CLAVE_SELLO_ALBERGUE.replace("{ID}", idAlbergue)
					.trim().replaceAll(" ", "_").toUpperCase()
				),
				containsString(
					NombresBloques.TITULO_BLOQUE_RESUMEN_SELLO_ALBERGUE_ENCRIPTADO.replace("{ID}", idAlbergue)
					.trim().replaceAll(" ", "_").toUpperCase()
				)
			)
		);
	}

	/**
	 * Comprueba que el sellado de un paquete de CPV con claves de actores
	 * inválidas no tenga éxito.
	 */
	@Test(expected = InvalidKeySpecException.class)
	public void testSellarPaqueteCpvClavesInvalidas() throws InvalidKeySpecException, GeneralSecurityException, IOException {
		final byte[] claveInvalidaAleatoria = new byte[8];

		Actor.OFICINA_PEREGRINO.setClavePublica(clavePublicaOficinaPeregrino);
		Actor.PEREGRINO.setClavePrivada(clavePrivadaPeregrino);

		GeneradorCpv.generarPaqueteCPV(datos, bos);

		final byte[] salidaPaquete = bos.toByteArray();
		bos.reset();

		new Random().nextBytes(claveInvalidaAleatoria);
		Actor.ALBERGUE.setClavePrivada(claveInvalidaAleatoria);

		SelladorCpv.sellarCpv(datosSello, new ByteArrayInputStream(salidaPaquete), bos, "Albergue de prueba");
	}

	/**
	 * Comprueba que el sellado de un paquete de CPV con datos inválidos lance
	 * las excepciones definidas en el contrato.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGenerarPaqueteCpvParametrosInvalidos() throws InvalidKeySpecException, GeneralSecurityException, IOException {
		SelladorCpv.sellarCpv(null, null, null, null);
	}
}
