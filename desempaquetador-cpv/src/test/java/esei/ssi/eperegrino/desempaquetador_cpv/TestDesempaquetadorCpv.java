package esei.ssi.eperegrino.desempaquetador_cpv;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esei.ssi.eperegrino.common.Actor;
import esei.ssi.eperegrino.common.GestorProveedoresJCA;
import esei.ssi.eperegrino.common.JSONUtils;
import esei.ssi.eperegrino.common.ParametrosCriptograficos;
import esei.ssi.eperegrino.generador_cpv.GeneradorCpv;

/**
 * La batería de tests de JUnit a ejecutar sobre la clase DesempaquetadorCpv.
 *
 * @author Alejandro González García
 */
public final class TestDesempaquetadorCpv {
	/**
	 * La clave pública de la oficina del peregrino, generada aleatoriamente.
	 */
	private static final byte[] clavePublicaOficinaPeregrino;
	/**
	 * La clave privada de la oficina del peregrino, generada aleatoriamente.
	 */
	private static final byte[] clavePrivadaOficinaPeregrino;
	/**
	 * La clave pública del peregrino, generada aleatoriamente.
	 */
	private static final byte[] clavePublicaPeregrino;
	/**
	 * La clave privada del peregrino, generada aleatoriamente.
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
	/**
	 * Un flujo de salida para recoger lo mostrado por la salida estándar.
	 */
	private static final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
	/**
	 * Una referencia a la salida estándar vinculada inicialmente al programa.
	 */
	private static final PrintStream stdoutOriginal = System.out;

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
		final KeyPair parClavesPeregrino = generadorClaves.generateKeyPair();
		final KeyPair parClavesOficinaPeregrino = generadorClaves.generateKeyPair();

		clavePublicaPeregrino = parClavesPeregrino.getPublic().getEncoded();
		clavePrivadaPeregrino = parClavesPeregrino.getPrivate().getEncoded();
		clavePublicaOficinaPeregrino = parClavesOficinaPeregrino.getPublic().getEncoded();
		clavePrivadaOficinaPeregrino = parClavesOficinaPeregrino.getPrivate().getEncoded();
	}

	@Before
	public void configurarSalidaEstandar() {
		System.setOut(new PrintStream(stdout));
	}

	@After
	public void restaurarFlujos() {
	    System.setOut(stdoutOriginal);
	    stdout.reset();
	    bos.reset();
	}

	@Test
	public void testEmpaquetarYDesempaquetarPaqueteCpv() throws Exception {
		Actor.OFICINA_PEREGRINO.setClavePublica(clavePublicaOficinaPeregrino);
		Actor.PEREGRINO.setClavePrivada(clavePrivadaPeregrino);

		GeneradorCpv.generarPaqueteCPV(datos, bos);

		Actor.OFICINA_PEREGRINO.setClavePrivada(clavePrivadaOficinaPeregrino);
		Actor.PEREGRINO.setClavePublica(clavePublicaPeregrino);

		DesempaquetadorCpv.desempaquetarPaqueteCPV(
			new ByteArrayInputStream(bos.toByteArray()),
			new ArrayList<>()
		);

		assertThat(
			stdout.toString(StandardCharsets.UTF_8.displayName()),
			containsString(JSONUtils.map2json(datos))
		);
	}

	/**
	 * Comprueba que el desempaquetado de un paquete de CPV con claves de actores
	 * inválidas no tenga éxito.
	 */
	@Test(expected = GeneralSecurityException.class)
	public void testEmpaquetarYDesempaquetarPaqueteCpvClavesInvalidas() throws GeneralSecurityException, IOException {
		final byte[] claveInvalidaAleatoria = new byte[8];
		final Random rng = new Random();

		Actor.OFICINA_PEREGRINO.setClavePublica(clavePublicaOficinaPeregrino);
		Actor.PEREGRINO.setClavePrivada(clavePrivadaPeregrino);

		GeneradorCpv.generarPaqueteCPV(datos, bos);

		rng.nextBytes(claveInvalidaAleatoria);
		Actor.OFICINA_PEREGRINO.setClavePrivada(claveInvalidaAleatoria);
		rng.nextBytes(claveInvalidaAleatoria);
		Actor.PEREGRINO.setClavePublica(claveInvalidaAleatoria);

		DesempaquetadorCpv.desempaquetarPaqueteCPV(new ByteArrayInputStream(bos.toByteArray()), new ArrayList<>());
	}

	/**
	 * Comprueba que el desempaquetado de un paquete de CPV con datos inválidos
	 * lance las excepciones definidas en el contrato.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testDesempaquetarPaqueteCpvParametrosInvalidos() throws GeneralSecurityException, IOException {
		DesempaquetadorCpv.desempaquetarPaqueteCPV(null, null);
	}

	// TODO: añadir más tests cuando esté listo el código responsable del sellado
}
