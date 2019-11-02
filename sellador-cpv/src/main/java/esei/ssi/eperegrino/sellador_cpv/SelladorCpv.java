package esei.ssi.eperegrino.sellador_cpv;

import esei.ssi.eperegrino.common.Actor;
import esei.ssi.eperegrino.common.GestorProveedoresJCA;
import esei.ssi.eperegrino.common.JSONUtils;
import static esei.ssi.eperegrino.common.NombresBloques.*;
import esei.ssi.eperegrino.common.Paquete;
import esei.ssi.eperegrino.common.PaqueteDAO;
import esei.ssi.eperegrino.common.ParametrosCriptograficos;
import static esei.ssi.eperegrino.common.Util.pedirFichero;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

final class SelladorCpv {

	public static void main(final String[] args) {
		try {
			String nombre;
			String lugar;
			String incidencias;

			final ArgumentosSell argumentos = LectorArgumentosLineaComandosSell.interpretar(args);

			// Pares de datos clave-valor en Map.
			final Map<String, String> map = new HashMap<>();
			File ficheroPaqueteCpv;

			Scanner teclado = new Scanner(System.in);

			// Pedimos los datos por teclado
			System.out.print("Introduzca el nombre completo del albergue: ");
			nombre = teclado.nextLine();

			System.out.print("Indique el lugar de creación: ");
			lugar = teclado.nextLine();

			System.out.print("Indique las incidencias (si se han producido): ");
			incidencias = teclado.nextLine();

			Actor.OFICINA_PEREGRINO.setClavePublica(Files.readAllBytes(argumentos.getFicheroClavePublicaOficina().toPath()));
			Actor.ALBERGUE.setClavePrivada(Files.readAllBytes(argumentos.getFicheroClavePrivadaAlbergue().toPath()));

			ficheroPaqueteCpv = pedirFichero("Escriba la ruta del fichero a generar, con su nueva credencial", teclado, System.out, false);

			map.put("Nombre", nombre);
			map.put("Fecha de creación", DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("Europe/Madrid")).format(Instant.now()));
			map.put("Lugar de creación", lugar);
			map.put("Incidencias", incidencias);

			generarPaqueteCPV(map, new FileInputStream(argumentos.getFicheroPaquete()), new FileOutputStream(argumentos.getFicheroPaquete()), argumentos.getFicheroIdentificadorAlbergue());

			System.out.println("Gracias por su colaboración");
		} catch (Exception exc) {
			System.err.println("Ha ocurrido un error durante la creación de la credencial");
			exc.printStackTrace();
		}
	}

	public static void generarPaqueteCPV(final Map<String, String> datos, final InputStream flujoEntradaPaquete, final OutputStream flujoSalidaPaquete, File identificadorAlbergue)
			throws GeneralSecurityException, InvalidKeySpecException, IOException {
		KeyGenerator generadorClaveCifradorSimetrico;
		SecretKey claveCifrador;
		Cipher cifradorSimetrico, cifradorAsimetrico;
		byte[] datosEncriptados, claveCifradorEncriptada, resumenEncriptadoDatos;
		Paquete paqueteCpv;

		if (datos == null || flujoSalidaPaquete == null) {
			throw new IllegalArgumentException("Un parámetro recibido para generar el paquete de la CPV es nulo, y no debería de serlo");
		}

		// Generar una clave aleatoria simétrica
		generadorClaveCifradorSimetrico = KeyGenerator.getInstance(ParametrosCriptograficos.ALGORITMO_GENERADOR_CLAVES_SIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS);
		generadorClaveCifradorSimetrico.init(ParametrosCriptograficos.LONGITUD_CLAVE_SIMETRICO);
		claveCifrador = generadorClaveCifradorSimetrico.generateKey();

		// Cifrador simétrico con clave aleatoria
		cifradorSimetrico = Cipher.getInstance(ParametrosCriptograficos.ALGORITMO_SIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS);
		cifradorSimetrico.init(Cipher.ENCRYPT_MODE, claveCifrador);

		// Cifrado asimétrico de la clave con la pública de la oficina
		cifradorAsimetrico = Cipher.getInstance(ParametrosCriptograficos.ALGORITMO_ASIMETRICO, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS);
		cifradorAsimetrico.init(Cipher.ENCRYPT_MODE, Actor.OFICINA_PEREGRINO.getClavePublica());

		// Generar la representación encriptada con el cifrador simétrico y la clave anterior de los pares de datos en JSON
		datosEncriptados = cifradorSimetrico.doFinal(JSONUtils.map2json(datos).getBytes(StandardCharsets.UTF_8));

		// Generar la representación encriptada con el cifrador asimétrico de la clave usada para el cifrador simétrico
		try {
			claveCifradorEncriptada = cifradorAsimetrico.doFinal(claveCifrador.getEncoded());
		} catch (final ArrayIndexOutOfBoundsException exc) {
			throw new GeneralSecurityException("La clave pública de la oficina del peregrino no tiene longitud suficiente para encriptar los datos requeridos");
		}

		// Encriptar el resumen de los datos encriptados con la clave privada del albergue,
		// De esta manera garantizamos que fue el albergue quien generó este paquete (firma digital)
		cifradorAsimetrico.init(Cipher.ENCRYPT_MODE, Actor.ALBERGUE.getClavePrivada());
		try {
			resumenEncriptadoDatos = cifradorAsimetrico.doFinal(
				MessageDigest.getInstance(
					ParametrosCriptograficos.ALGORITMO_RESUMEN, ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS
				).digest(datosEncriptados)
			);
		} catch (final ArrayIndexOutOfBoundsException exc) {
			throw new GeneralSecurityException("La clave privada del albergue no tiene longitud suficiente para encriptar los datos requeridos");
		}

		// Se crea el paquete inicial con 3 bloques: los datos del albergue encriptados con un cifrador
		// simétrico, la clave de los datos encriptada con un cifrador asimétrico, y el resumen encriptado
		// con un cifrador asimétrico de los datos encriptados
		paqueteCpv = PaqueteDAO.leerPaquete(flujoEntradaPaquete);
		paqueteCpv.anadirBloque(TITULO_BLOQUE_DATOS_SELLO_ALBERGUE_PARCIAL + identificadorAlbergue, datosEncriptados);
		paqueteCpv.anadirBloque(TITULO_BLOQUE_CLAVE_SELLO_ALBERGUE_PARCIAL + identificadorAlbergue, claveCifradorEncriptada);
		paqueteCpv.anadirBloque(TITULO_BLOQUE_RESUMEN_SELLO_ALBERGUE_ENCRIPTADOS_PARCIAL + identificadorAlbergue, resumenEncriptadoDatos);

		// Finalmente, escribir el paquete al flujo
		PaqueteDAO.escribirPaquete(flujoSalidaPaquete, paqueteCpv);
	}
}
