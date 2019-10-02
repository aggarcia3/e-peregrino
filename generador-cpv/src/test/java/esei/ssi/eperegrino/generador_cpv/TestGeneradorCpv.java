package esei.ssi.eperegrino.generador_cpv;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import esei.ssi.eperegrino.common.Actor;
import esei.ssi.eperegrino.common.GestorProveedoresJCA;
import esei.ssi.eperegrino.common.ParametrosCriptograficos;

/**
 * La batería de tests de JUnit a ejecutar sobre la clase GeneradorCpv.
 * 
 * @author Alejandro González García
 */
public class TestGeneradorCpv {
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
	private static final OutputStream os = new ByteArrayOutputStream();

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

	/**
	 * Comprueba que la generación de un paquete de CPV con datos válidos no lance
	 * excepciones.
	 */
	@Test
	public void testGenerarPaqueteCpv() throws Exception {
		Actor.OFICINA_PEREGRINO.setClavePublica(clavePublicaOficinaPeregrino);
		Actor.PEREGRINO.setClavePrivada(clavePrivadaPeregrino);

		long inicio = System.currentTimeMillis();

		GeneradorCPV.generarPaqueteCPV(datos, os);

		System.out.println("> Generación del paquete de CPV finalizada en " + (System.currentTimeMillis() - inicio) + " ms");
	}

	/**
	 * Comprueba que la generación de un paquete de CPV con claves de actores
	 * inválidas no tenga éxito.
	 */
	@Test
	public void testGenerarPaqueteCpvClavesInvalidas() {
		Actor.OFICINA_PEREGRINO.setClavePublica(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });
		Actor.PEREGRINO.setClavePrivada(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 });

		try {
			GeneradorCPV.generarPaqueteCPV(datos, os);
		} catch (final InvalidKeySpecException exc) {
			// Es la excepción que debería de lanzar
		} catch (final Exception exc) {
			fail("No se ha lanzado la excepción esperada al asociar claves inválidas a los actores");
		}
	}

	/**
	 * Comprueba que la generación de un paquete de CPV con datos inválidos lance
	 * las excepciones definidas en el contrato.
	 */
	@Test
	public void testGenerarPaqueteCpvParametrosInvalidos() {
		try {
			GeneradorCPV.generarPaqueteCPV(null, null);
		} catch (final IllegalArgumentException exc) {
			// Es la excepción que debería de lanzar
		} catch (final Exception exc) {
			fail("No se ha lanzado la excepción esperada al recibir parámetros de entrada inválidos");
		}
	}
}