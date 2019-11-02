package esei.ssi.eperegrino.generador_cpv;

import esei.ssi.eperegrino.common.Actor;

import esei.ssi.eperegrino.common.GestorProveedoresJCA;
import esei.ssi.eperegrino.common.JSONUtils;
import esei.ssi.eperegrino.common.Paquete;
import esei.ssi.eperegrino.common.PaqueteDAO;
import esei.ssi.eperegrino.common.ParametrosCriptograficos;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import static esei.ssi.eperegrino.common.NombresBloques.TITULO_BLOQUE_DATOS_PEREGRINO;
import static esei.ssi.eperegrino.common.NombresBloques.TITULO_BLOQUE_CLAVE_DATOS_PEREGRINO;
import static esei.ssi.eperegrino.common.NombresBloques.TITULO_BLOQUE_RESUMEN_DATOS_PEREGRINO_ENCRIPTADOS;

/**
 * Contiene la lógica de negocio de una aplicación que genera una credencial de
 * peregrino virtual, a petición del peregrino que la posee.
 *
 * @author Alejandro González García
 */
public final class GeneradorCpv {
	// Argumentos de línea de comandos: <nombre paquete> <ficheros con las claves necesarias>  
	public static void main(final String[] args) {
		try {
			String nombre;
			String DNI;
			String domicilio;
			String lugar;
			String motivacion;

			// Pares de datos clave-valor en Map.
			final Map<String, String> map = new HashMap<>();
			File ficheroPaqueteCpv;

			Scanner teclado = new Scanner(System.in);

			// Pedimos los datos por teclado
			System.out.print("Introduzca su nombre: ");
			nombre = teclado.nextLine();

			System.out.print("Introduzca su DNI: ");
			DNI = teclado.nextLine();

			System.out.print("Introduzca su domicilio: ");
			domicilio = teclado.nextLine();

			System.out.print("Indique el lugar de creación: ");
			lugar = teclado.nextLine();

			System.out.print("Indique su motivación: ");
			motivacion = teclado.nextLine();
                        
                        final ArgumentosGen argumentos = LectorArgumentosLineaComandosGen.interpretar(args);

			// FIXME: leer de argumentos de línea de comandos en lugar de pedir interactivamente
                        Actor.OFICINA_PEREGRINO.setClavePublica(Files.readAllBytes(argumentos.getFicheroClavePublicaOficina().toPath()));
                        Actor.PEREGRINO.setClavePrivada(Files.readAllBytes(argumentos.getFicheroClavePrivadaPeregrino().toPath()));

			map.put("Nombre", nombre);
			map.put("DNI", DNI);
			map.put("Domicilio", domicilio);
			map.put("Fecha de creación", DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("Europe/Madrid")).format(Instant.now()));
			map.put("Lugar de creación", lugar);
			map.put("Motivación", motivacion);

			generarPaqueteCPV(map, new FileOutputStream(argumentos.getFicheroPaquete()));

			System.out.println("¡Buen viaje!");
		} catch (Exception exc) {
			System.err.println("Ha ocurrido un error durante la creación de la credencial");
			exc.printStackTrace();
		}
	}

	/**
	 * Genera el paquete inicial de la CPV, conteniendo los datos del peregrino, y
	 * lo guarda a disco cumpliendo los requisitos de seguridad de la información
	 * estipulados.
	 *
	 * @param datos              Los pares clave-valor de datos que conformarán un
	 *                           bloque del paquete. Pueden experimentar pérdida de
	 *                           información si las cadenas de texto contienen algún
	 *                           caracter reservado para la codificación JSON. Véase
	 *                           el método
	 *                           {@link esei.ssi.eperegrino.common.JSONUtils.map2json}.
	 * @param flujoSalidaPaquete El flujo a donde guardar el paquete resultante.
	 * @throws IllegalArgumentException Si algún parámetro recibido no es válido.
	 * @throws GeneralSecurityException Si ocurre algún error relacionado con las
	 *                                  operaciones criptográficas que no haya sido
	 *                                  manejado por un tipo de excepción más
	 *                                  específico.
	 * @throws InvalidKeySpecException  Si alguna de las claves asociadas a los
	 *                                  actores no se ha podido interpretar
	 *                                  correctamente como tal.
	 * @throws IOException              Si ocurre algún error de E/S durante el
	 *                                  generado del paquete.
	 * @author Alejandro González García
	 */
	public static void generarPaqueteCPV(final Map<String, String> datos, final OutputStream flujoSalidaPaquete)
			throws GeneralSecurityException, InvalidKeySpecException, IOException
	{
		KeyGenerator generadorClaveCifradorSimetrico;
		SecretKey claveCifrador;
		Cipher cifradorSimetrico, cifradorAsimetrico;
		byte[] datosEncriptados, claveCifradorEncriptada, resumenEncriptadoDatos;
		Paquete paqueteCpv;

		if (datos == null || flujoSalidaPaquete == null) {
			throw new IllegalArgumentException("Un parámetro recibido para generar el paquete de la CPV es nulo, y no debería de serlo");
		}

		// Registrar los proveedores JCA que usaremos
		GestorProveedoresJCA.registrarProveedores();

		// Generar una clave aleatoria para un cifrado simétrico
		generadorClaveCifradorSimetrico = KeyGenerator.getInstance(ParametrosCriptograficos.ALGORITMO_GENERADOR_CLAVES_SIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS);
		generadorClaveCifradorSimetrico.init(ParametrosCriptograficos.LONGITUD_CLAVE_SIMETRICO);
		claveCifrador = generadorClaveCifradorSimetrico.generateKey();

		// Inicializar cifrador simétrico
		cifradorSimetrico = Cipher.getInstance(ParametrosCriptograficos.ALGORITMO_SIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS);
		cifradorSimetrico.init(Cipher.ENCRYPT_MODE, claveCifrador);

		// Inicializar algoritmo de cifrado asimétrico
		cifradorAsimetrico = Cipher.getInstance(ParametrosCriptograficos.ALGORITMO_ASIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS);
		cifradorAsimetrico.init(Cipher.ENCRYPT_MODE, Actor.OFICINA_PEREGRINO.getClavePublica());

		// Generar la representación encriptada con el cifrador simétrico y la clave
		// anterior de los pares de datos en JSON
		datosEncriptados = cifradorSimetrico.doFinal(JSONUtils.map2json(datos).getBytes(StandardCharsets.UTF_8));

		// Generar la representación encriptada con el cifrador asimétrico de la clave usada para el cifrador simétrico
		try {
			claveCifradorEncriptada = cifradorAsimetrico.doFinal(claveCifrador.getEncoded());
		} catch (final ArrayIndexOutOfBoundsException exc) {
			throw new GeneralSecurityException("La clave pública de la oficina del peregrino no tiene longitud suficiente para encriptar los datos requeridos");
		}

		// Encriptar el resumen de los datos encriptados con la clave privada del peregrino,
		// usando el cifrador asimétrico. De esta manera garantizamos que fue el peregrino quien
		// generó este paquete (firma digital)
		cifradorAsimetrico.init(Cipher.ENCRYPT_MODE, Actor.PEREGRINO.getClavePrivada());
		try {
			resumenEncriptadoDatos = cifradorAsimetrico.doFinal(
				MessageDigest.getInstance(
					ParametrosCriptograficos.ALGORITMO_RESUMEN, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS
				).digest(datosEncriptados)
			);
		} catch (final ArrayIndexOutOfBoundsException exc) {
			throw new GeneralSecurityException("La clave privada del peregrino no tiene longitud suficiente para encriptar los datos requeridos");
		}

		// Se crea el paquete inicial con 3 bloques: los datos del peregrino encriptados con un cifrador
		// simétrico, la clave de los datos encriptada con un cifrador asimétrico, y el resumen encriptado
		// con un cifrador asimétrico de los datos encriptados
		paqueteCpv = new Paquete();
		paqueteCpv.anadirBloque(TITULO_BLOQUE_DATOS_PEREGRINO, datosEncriptados);
		paqueteCpv.anadirBloque(TITULO_BLOQUE_CLAVE_DATOS_PEREGRINO, claveCifradorEncriptada);
		paqueteCpv.anadirBloque(TITULO_BLOQUE_RESUMEN_DATOS_PEREGRINO_ENCRIPTADOS, resumenEncriptadoDatos);

		// Finalmente, escribir el paquete al flujo
		PaqueteDAO.escribirPaquete(flujoSalidaPaquete, paqueteCpv);
	}
}
