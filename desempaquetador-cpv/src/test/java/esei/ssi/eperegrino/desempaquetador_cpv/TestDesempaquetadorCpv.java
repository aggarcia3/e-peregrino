package esei.ssi.eperegrino.desempaquetador_cpv;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import esei.ssi.eperegrino.common.Actor;
import esei.ssi.eperegrino.common.GestorProveedoresJCA;
import esei.ssi.eperegrino.common.JSONUtils;
import esei.ssi.eperegrino.common.ParametrosCriptograficos;
import esei.ssi.eperegrino.generador_cpv.GeneradorCpv;
import esei.ssi.eperegrino.sellador_cpv.SelladorCpv;

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
	 * La clave pública del albergue, generada aleatoriamente.
	 */
	private static final byte[] clavePublicaAlbergue;
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
		datos.put("Motivo", "Aprobar");
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

		clavePublicaPeregrino = parClavesPeregrino.getPublic().getEncoded();
		clavePrivadaPeregrino = parClavesPeregrino.getPrivate().getEncoded();
		clavePublicaOficinaPeregrino = parClavesOficinaPeregrino.getPublic().getEncoded();
		clavePrivadaOficinaPeregrino = parClavesOficinaPeregrino.getPrivate().getEncoded();
		clavePublicaAlbergue = parClavesAlbergue.getPublic().getEncoded();
		clavePrivadaAlbergue = parClavesAlbergue.getPrivate().getEncoded();
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

	/**
	 * Comprueba que el empaquetado y desempaquetado de una CPV válida y recién
	 * generada funcione.
	 */
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
	 * Comprueba que el empaquetado, sellado y desempaquetado de una CPV válida y
	 * recién generada funcione.
	 */
	@Test
	public void testEmpaquetarSellarYDesempaquetarPaqueteCpv() throws Exception {
		Actor.OFICINA_PEREGRINO.setClavePublica(clavePublicaOficinaPeregrino);
		Actor.PEREGRINO.setClavePrivada(clavePrivadaPeregrino);

		GeneradorCpv.generarPaqueteCPV(datos, bos);

		Actor.ALBERGUE.setClavePrivada(clavePrivadaAlbergue);

		final byte[] salidaPaquete = bos.toByteArray();
		bos.reset();

		SelladorCpv.sellarCpv(datosSello, new ByteArrayInputStream(salidaPaquete), bos, "Albergue de prueba");

		Actor.OFICINA_PEREGRINO.setClavePrivada(clavePrivadaOficinaPeregrino);
		Actor.PEREGRINO.setClavePublica(clavePublicaPeregrino);

		final List<Entry<String, byte[]>> albergues = new ArrayList<>();
		albergues.add(new AbstractMap.SimpleImmutableEntry<>("Albergue de prueba", clavePublicaAlbergue));

		DesempaquetadorCpv.desempaquetarPaqueteCPV(
			new ByteArrayInputStream(bos.toByteArray()),
			albergues
		);

		assertThat(
			stdout.toString(StandardCharsets.UTF_8.displayName()),
			allOf(
				containsString(JSONUtils.map2json(datos)),
				containsString(JSONUtils.map2json(datosSello))
			)
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
}
