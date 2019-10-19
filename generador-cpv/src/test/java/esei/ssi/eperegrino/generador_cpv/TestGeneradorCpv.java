package esei.ssi.eperegrino.generador_cpv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
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
import esei.ssi.eperegrino.common.PaqueteDAO;
import esei.ssi.eperegrino.common.ParametrosCriptograficos;

import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;
import static org.junit.Assert.assertThat;

/**
 * La batería de tests de JUnit a ejecutar sobre la clase GeneradorCpv.
 * 
 * @author Alejandro González García
 */
public final class TestGeneradorCpv {
	/**
	 * Una clave pública generada aleatoriamente.
	 */
	private static final byte[] clavePublicaOficinaPeregrino;
	/**
	 * Una clave privada generada aleatoriamente.
	 */
	private static final byte[] clavePrivadaPeregrino;
	/**
	 * Unos pares de datos clave-valor a probar su conversión.
	 */
	private static final Map<String, String> datos = new HashMap<>();
	/**
	 * Un flujo de salida para recoger los resultados de las pruebas.
	 */
	private static final ByteArrayOutputStream bos = new ByteArrayOutputStream();

	static {
		// Inicializar pares clave-valor
		datos.put("Nombre", "Alejandro");

		GestorProveedoresJCA.registrarProveedores();

		KeyPairGenerator generadorClaves;
		try {
			generadorClaves = KeyPairGenerator.getInstance(ParametrosCriptograficos.ALGORITMO_GENERADOR_CLAVES_ASIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS);
		} catch (final NoSuchAlgorithmException exc) {
			throw new AssertionError(exc);
		}
		generadorClaves.initialize(4096); // Clave grande para evitar problemas de longitud insuficiente en tests

		// Generar claves aleatorias para probar los casos de uso
		clavePublicaOficinaPeregrino = generadorClaves.generateKeyPair().getPublic().getEncoded();
		clavePrivadaPeregrino = generadorClaves.generateKeyPair().getPrivate().getEncoded();
	}

	@After
	public void restaurarFlujos() {
		bos.reset();
	}

	/**
	 * Comprueba que la generación de un paquete de CPV con datos válidos no lance
	 * excepciones.
	 */
	@Test
	public void testGenerarPaqueteCpv() throws Exception {
		Actor.OFICINA_PEREGRINO.setClavePublica(clavePublicaOficinaPeregrino);
		Actor.PEREGRINO.setClavePrivada(clavePrivadaPeregrino);

		GeneradorCpv.generarPaqueteCPV(datos, bos);

		// Un paquete aparentemente bien formado, con tres bloques
		assertThat(
			bos.toString(StandardCharsets.UTF_8.displayName()),
			stringContainsInOrder(
				PaqueteDAO.INICIO_PAQUETE,
				PaqueteDAO.INICIO_BLOQUE,
				PaqueteDAO.FIN_BLOQUE,
				PaqueteDAO.INICIO_BLOQUE,
				PaqueteDAO.FIN_BLOQUE,
				PaqueteDAO.INICIO_BLOQUE,
				PaqueteDAO.FIN_BLOQUE,
				PaqueteDAO.FIN_PAQUETE
			)
		);
	}

	/**
	 * Comprueba que la generación de un paquete de CPV con claves de actores
	 * inválidas no tenga éxito.
	 */
	@Test(expected = InvalidKeySpecException.class)
	public void testGenerarPaqueteCpvClavesInvalidas() throws InvalidKeySpecException, GeneralSecurityException, IOException {
		final byte[] claveInvalidaAleatoria = new byte[8];
		final Random rng = new Random();

		rng.nextBytes(claveInvalidaAleatoria);
		Actor.OFICINA_PEREGRINO.setClavePublica(claveInvalidaAleatoria);
		rng.nextBytes(claveInvalidaAleatoria);
		Actor.PEREGRINO.setClavePrivada(claveInvalidaAleatoria);

		GeneradorCpv.generarPaqueteCPV(datos, bos);
	}

	/**
	 * Comprueba que la generación de un paquete de CPV con datos inválidos lance
	 * las excepciones definidas en el contrato.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGenerarPaqueteCpvParametrosInvalidos() throws InvalidKeySpecException, GeneralSecurityException, IOException {
		GeneradorCpv.generarPaqueteCPV(null, null);
	}
}