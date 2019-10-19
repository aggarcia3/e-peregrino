package esei.ssi.eperegrino.desempaquetador_cpv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Map.Entry;
import java.util.List;
import java.util.NoSuchElementException;

import javax.crypto.Cipher;

import esei.ssi.eperegrino.common.Actor;
import esei.ssi.eperegrino.common.GestorProveedoresJCA;
import esei.ssi.eperegrino.common.JSONUtils;
import esei.ssi.eperegrino.common.NombresBloques;
import esei.ssi.eperegrino.common.Paquete;
import esei.ssi.eperegrino.common.PaqueteDAO;
import esei.ssi.eperegrino.common.ParametrosCriptograficos;
import esei.ssi.eperegrino.common.Util;

final class DesempaquetadorCpv {
	private static final String SEPARADOR_BLOQUES = "----------";

	/**
	 * Punto de entrada de la aplicación.
	 *
	 * @param args Los argumentos de línea de comandos, en el formato dictaminado
	 *             por {@link LectorArgumentosLineaComandos}.
	 */
	public static void main(final String[] args) {
		final ArgumentosDesempaquetador argumentos = LectorArgumentosLineaComandos.interpretar(args);

		try {
			// Establecer claves comunes e invariantes
			Actor.OFICINA_PEREGRINO.setClavePrivada(Files.readAllBytes(argumentos.getFicheroClavePrivadaOficina().toPath()));
			Actor.PEREGRINO.setClavePublica(Files.readAllBytes(argumentos.getFicheroClavePublicaPeregrino().toPath()));

			desempaquetarPaqueteCPV(new FileInputStream(argumentos.getFicheroPaquete()), argumentos.getAlbergues());
		} catch (final Exception exc) {
			System.err.println("Ha ocurrido un error durante el desempaquetado de la credencial");
			exc.printStackTrace();
		}
	}

	/**
	 * Desempaqueta un paquete que representa una credencial de peregrino virtual
	 * (CPV), mostrando por la salida estándar los contenidos del paquete. Durante
	 * la lectura del paquete se comprueba la validez de la información contenida en
	 * él, incluyendo si fue modificada durante su tránsito o si puede no pertenecer
	 * al actor que se espera que haya generado la información.
	 *
	 * @param flujoEntradaPaquete El flujo de entrada con los datos de la CPV.
	 * @param albergues           Los albergues cuyos sellos se comprobará si
	 *                            existen en la CPV, y de ser así se validarán y
	 *                            mostrarán.
	 * @throws IOException              Si ocurre algún error de E/S durante la
	 *                                  lectura de algún dato.
	 * @throws GeneralSecurityException Si ocurre algún error durante alguna
	 *                                  operación criptográfica, o alguna firma
	 *                                  digital o carga útil contenida en la CPV no
	 *                                  es válida.
	 * @throws NoSuchElementException   Si falta algún bloque necesario en el
	 *                                  paquete.
	 * @throws IllegalArgumentException Si algún parámetro es nulo.
	 */
	static void desempaquetarPaqueteCPV(final InputStream flujoEntradaPaquete, final List<Entry<String, File>> albergues) throws IOException, GeneralSecurityException {
		Paquete cpv;
		Cipher cifradorSimetrico, cifradorAsimetrico;
		String datosPeregrino;

		if (flujoEntradaPaquete == null || albergues == null) {
			throw new IllegalArgumentException("Un parámetro recibido para desempaquetar el paquete de la CPV es nulo, y no debería de serlo");
		}

		cpv = PaqueteDAO.leerPaquete(flujoEntradaPaquete);

		// Obtener los datos del peregrino y de su firma
		final byte[] datosPeregrinoEncriptados = cpv.getContenidoBloque(NombresBloques.TITULO_BLOQUE_DATOS_PEREGRINO);
		if (datosPeregrinoEncriptados == null) {
			throw new NoSuchElementException("CPV mal formada: no contiene un bloque con los datos del peregrino");
		}

		final byte[] claveSimetricoDatosPeregrinoEncriptada = cpv.getContenidoBloque(NombresBloques.TITULO_BLOQUE_CLAVE_DATOS_PEREGRINO);
		if (claveSimetricoDatosPeregrinoEncriptada == null) {
			throw new NoSuchElementException("CPV mal formada: no contiene un bloque con la clave del cifrador simétrico usado para encriptar los datos del peregrino");
		}

		final byte[] resumenDatosPeregrinoEncriptados = cpv.getContenidoBloque(NombresBloques.TITULO_BLOQUE_RESUMEN_DATOS_PEREGRINO_ENCRIPTADOS);
		if (resumenDatosPeregrinoEncriptados == null) {
			throw new NoSuchElementException("CPV mal formada: no contiene un bloque con el resumen de los datos del peregrino encriptados");
		}

		// Registrar los proveedores JCA que usaremos
		GestorProveedoresJCA.registrarProveedores();

		// Inicializar el cifrador asimétrico
		cifradorAsimetrico = Cipher.getInstance(
			ParametrosCriptograficos.ALGORITMO_ASIMETRICO,
			ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS
		);

		// Comprobar que hemos los datos recibidos coinciden con los firmados
		Util.comprobarValidezFirma(
			"El bloque de datos del peregrino",
			Actor.PEREGRINO.getClavePublica(),
			cifradorAsimetrico,
			datosPeregrinoEncriptados,
			resumenDatosPeregrinoEncriptados
		);

		// Inicializar el cifrador simétrico
		cifradorSimetrico = Cipher.getInstance(
			ParametrosCriptograficos.ALGORITMO_SIMETRICO,
			ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS
		);

		// Desencriptar los datos del peregrino, y mostrarlos por la salida
		// estándar
		datosPeregrino = Util.desencriptarCargaUtil(
			cifradorAsimetrico,
			cifradorSimetrico,
			Actor.OFICINA_PEREGRINO.getClavePrivada(),
			claveSimetricoDatosPeregrinoEncriptada,
			datosPeregrinoEncriptados
		);

		// Los datos del peregrino deben de ser una cadena de texto interpretable como JSON.
		// Si no lo son, la clave del cifrador simétrico que usamos no es la correcta
		if (JSONUtils.json2map(datosPeregrino).isEmpty()) {
			throw new GeneralSecurityException("CPV mal formada: los datos del peregrino no siguen el formato JSON. Esto puede indicar que esta CPV ha sido destinada a otra oficina del peregrino, o que una clave de encriptación ha sido modificada durante su envío");
		}

		System.out.println("-- Credencial de Peregrino Virtual --");
		System.out.println("Datos del peregrino:");
		System.out.println(datosPeregrino);
		System.out.println(SEPARADOR_BLOQUES);

		// Ahora repetir similares estrategias para cada albergue
		for (final Entry<String, File> datosAlbergue : albergues) {
			final String id = datosAlbergue.getKey();
			final File ficheroClavePublicaAlbergue = datosAlbergue.getValue();
			String sello;

			// Obtener los datos del albergue y su firma
			final byte[] datosSelloAlbergueEncriptados = cpv.getContenidoBloque(
				NombresBloques.TITULO_BLOQUE_DATOS_SELLO_ALBERGUE.replace("{ID}", id)
			);
			if (datosSelloAlbergueEncriptados == null) {
				// Avisar en lugar de lanzar una excepción, para permitir pasar como parámetro
				// todos los albergues posibles, y mostrar solamente aquellos contenidos en la
				// CPV
				System.out.println("No se han encontrado datos del sello para el albergue con identificador \"" + id + "\". Ignorando albergue");
				continue;
			}

			final byte[] claveSimetricoDatosSelloEncriptada = cpv.getContenidoBloque(
				NombresBloques.TITULO_BLOQUE_CLAVE_SELLO_ALBERGUE.replace("{ID}", id)
			);
			if (claveSimetricoDatosSelloEncriptada == null) {
				throw new NoSuchElementException("CPV mal formada: no contiene un bloque con la clave del cifrador simétrico usado para encriptar el sello del albergue \"" + id + "\"");
			}

			final byte[] resumenDatosSelloEncriptados = cpv.getContenidoBloque(
				NombresBloques.TITULO_BLOQUE_RESUMEN_SELLO_ALBERGUE_ENCRIPTADOS.replace("{ID}", id)
			);
			if (resumenDatosSelloEncriptados == null) {
				throw new NoSuchElementException("CPV mal formada: no contiene un bloque con el resumen de los datos del sello del albergue \"" + id + "\" encriptados");
			}

			// Establecer la clave pública del albergue actual
			Actor.ALBERGUE.setClavePublica(Files.readAllBytes(ficheroClavePublicaAlbergue.toPath()));

			// Comprobar que el sello coincide con el firmado
			Util.comprobarValidezFirma(
				"El sello del albergue \"" + id + "\"",
				Actor.ALBERGUE.getClavePublica(),
				cifradorAsimetrico,
				datosSelloAlbergueEncriptados,
				resumenDatosSelloEncriptados
			);

			// Obtener y mostrar el sello
			sello = Util.desencriptarCargaUtil(
				cifradorAsimetrico,
				cifradorSimetrico,
				Actor.OFICINA_PEREGRINO.getClavePrivada(),
				claveSimetricoDatosSelloEncriptada,
				datosSelloAlbergueEncriptados
			);

			// El sello debe de ser una cadena interpretable como JSON.
			// Si no lo es, la clave del cifrador simétrico que usamos no es la correcta
			if (JSONUtils.json2map(sello).isEmpty()) {
				throw new GeneralSecurityException("CPV mal formada: el sello del albergue \"" + id + "\" no sigue el formato JSON. Esto puede indicar que esta CPV ha sido destinada a otra oficina del peregrino, o que una clave de encriptación ha sido modificada durante su envío");
			}

			System.out.println("Sello del albergue \"" + id + "\":");
			System.out.println(sello);
			System.out.println(SEPARADOR_BLOQUES);
		}
	}
}
