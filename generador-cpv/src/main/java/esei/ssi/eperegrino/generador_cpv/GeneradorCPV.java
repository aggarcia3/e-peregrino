package esei.ssi.eperegrino.generador_cpv;

import esei.ssi.eperegrino.common.Actor;

import esei.ssi.eperegrino.common.GestorProveedoresJCA;
import esei.ssi.eperegrino.common.JSONUtils;
import esei.ssi.eperegrino.common.Paquete;
import esei.ssi.eperegrino.common.PaqueteDAO;
import esei.ssi.eperegrino.common.ParametrosCriptograficos;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
yimport java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Contiene la lógica de negocio de una aplicación que
 * genera una credencial de peregrino virtual, a petición
 * del peregrino que la posee.
 *
 * @author Alejandro González García
 */
final class GeneradorCPV {
	public static final String TITULO_BLOQUE_DATOS_PEREGRINO = "DATOS PEREGRINO";
	public static final String TITULO_BLOQUE_CLAVE_DATOS = "CLAVE DATOS PEREGRINO";
	public static final String TITULO_BLOQUE_RESUMEN_DATOS_ENCRIPTADO = "DATOS ENCRIPTADOS";

	// Argumentos de línea de comandos: <nombre paquete> <ficheros con las claves necesarias>  
	public static void main(final String[] args) {
		/* TODO: pedir datos por entrada estándar para crear un Map<String, String>.
                * Manejar errores que puedan surgir en el proceso, mostrándolos al usuario.
                * Asociar a cada actor los bytes que representan cada una de sus claves necesarias. */
		 
                String nombre;
		String DNI;
		String domicilio;
		String fecha;
		String lugar;
		String motivacion;
                
                
                //Clave pública generada aleatoriamente.
                byte[] clavePublicaOficinaPeregrino = null;
                //Clave privada generada aleatoriamente.
                byte[] clavePrivadaPeregrino = null;
                //Pares de datos clave-valor en Map.
                final Map<String, String> map = new HashMap<>();
                //Salida que se pasará al método generarPaqueceCPV()
                final OutputStream os = new FileOutputStream();

		Scanner teclado = new Scanner(System.in);
                
                //Pedimos los datos por teclado
		System.out.println("Introduzca su nombre");
		nombre = teclado.nextLine();

		System.out.println("Introduzca su DNI");
		DNI = teclado.nextLine();

		System.out.println("Introduzca su domicilio");
		domicilio = teclado.nextLine();

		System.out.println("Indique la fecha de creación");
		fecha = teclado.nextLine();
		
		System.out.println("Indique el lugar de creación");
		lugar = teclado.nextLine();

		System.out.println("Indique su motivacion");
		motivacion = teclado.nextLine();
                 
                //Comprobamos que no salte ninguna excepción al introducir los pares clave-valor en el mapa
                try {
                
                    map.put("Nombre", nombre);
                    map.put("DNI", DNI);
                    map.put("Domicilio", domicilio);
                    map.put("Fecha de creacion", fecha);
                    map.put("Lugar de creacion", lugar);
                    map.put("Motivacion", motivacion);
		
                } catch (IllegalArgumentException exc){
                    System.err.println(exc.getMessage());
                }
                
                // Código de ejemplo que hice para leer eficientemente el fichero de entrada
		// con la clave:

//		final InputStream datosClaveInputStream = new FileInputStream("clave");
//		final byte[] bufer = new byte[4 * 1024]; // 4 KiB es normalmente el tamaño de un clúster de disco o página de memoria
//		byte[] bytesClave;
//
//		// Leemos el fichero completo en bloques (normalmente es más eficiente que leerlo byte a byte)
//		int bytesLeidosIter;
//		while ((bytesLeidosIter = datosClavePublicaOficina.read(bufer, 0, bufer.length)) > 0) {
//			// Se añade una copia del búfer porque puede no haberse llenado.
//			// La clase Arrays usada a continuación está en el paquete org.bouncycastle.util
//			bytesClave = Arrays.concatenate(bytesClave, Arrays.copyOf(bufer, bytesLeidosIter));
//		}

                final InputStream datosPublica = new FileInputStream("clave.publica");
		final byte[] buferPublica = new byte[4 * 1024]; // 4 KiB es normalmente el tamaño de un clúster de disco o página de memoria
		byte[] arrayBytesPublica;
                
		int bytesLeidosPublica;
		while ((bytesLeidosPublica = datosClavePublicaOficina.read(buferPublica, 0, buferPublica.length)) > 0) {
			bytesLeidosPublica = Arrays.concatenate(arrayBytesPublica, Arrays.copyOf(buferPublica, bytesLeidosPublica));
		}
                
                final InputStream datosPrivada = new FileInputStream("clave.privada");
		final byte[] buferPrivada = new byte[4 * 1024]; // 4 KiB es normalmente el tamaño de un clúster de disco o página de memoria
		byte[] arrayBytesPrivada;
                
		int bytesLeidosPrivada;
		while ((bytesLeidosPrivada = datosClavePublicaOficina.read(buferPrivada, 0, buferPrivada.length)) > 0) {
			bytesLeidosPrivada = Arrays.concatenate(arrayBytesPrivada, Arrays.copyOf(buferPrivada, bytesLeidosPrivada));
		}
                
                //Manera más lejible de realizar la lectura del fichero.
                //byte[] todo = Files.readAllBytes("pepe.publica");
                
                
                //Se le asignan las claves generadas a la Oficina del Peregrino y al Peregrino
                Actor.OFICINA_PEREGRINO.setClavePublica(arrayBytesPublica);
		Actor.PEREGRINO.setClavePrivada(bytesLeidosPrivada);
                
                generarPaqueteCPV(map, os);

		// try {
		// 	generarPaquete(...);
		// } catch (Exception exc) { ... }

		// Código de ejemplo que hice para leer eficientemente el fichero de entrada
		// con la clave:

//		final InputStream datosClaveInputStream = new FileInputStream("clave");
//		final byte[] bufer = new byte[4 * 1024]; // 4 KiB es normalmente el tamaño de un clúster de disco o página de memoria
//		byte[] bytesClave;
//
//		// Leemos el fichero completo en bloques (normalmente es más eficiente que leerlo byte a byte)
//		int bytesLeidosIter;
//		while ((bytesLeidosIter = datosClavePublicaOficina.read(bufer, 0, bufer.length)) > 0) {
//			// Se añade una copia del búfer porque puede no haberse llenado.
//			// La clase Arrays usada a continuación está en el paquete org.bouncycastle.util
//			bytesClave = Arrays.concatenate(bytesClave, Arrays.copyOf(bufer, bytesLeidosIter));
//		}
//
//		// bytesClave contiene a partir de esta línea los bytes leídos del InputStream
//		// hasta que señalizó el fin de fichero. Pueden asociarse esos bytes con el actor:
//		Actor.PEREGRINO.setClavePublica(bytesClave);
	}

	/**
	 * Genera el paquete inicial de la CPV, conteniendo los datos del peregrino, y lo guarda a disco
	 * cumpliendo los requisitos de seguridad de la información estipulados.
	 * @param datos Los pares clave-valor de datos que conformarán un bloque del paquete. Pueden experimentar
	 * pérdida de información si las cadenas de texto contienen algún caracter reservado para la codificación
	 * JSON. Véase el método {@link esei.ssi.eperegrino.common.JSONUtils.map2json}.
	 * @param flujoSalidaPaquete El flujo a donde guardar el paquete resultante.
	 * @throws IllegalArgumentException Si algún parámetro recibido no es válido.
	 * @throws GeneralSecurityException Si ocurre algún error relacionado con las operaciones criptográficas
	 * que no haya sido manejado por un tipo de excepción más específico.
	 * @throws InvalidKeySpecException Si alguna de las claves asociadas a los actores no se ha podido interpretar
	 * correctamente como tal.
	 * @throws IOException Si ocurre algún error de E/S durante el generado del paquete.
	 * @author Alejandro González García
	 */
	static void generarPaqueteCPV(final Map<String, String> datos, final OutputStream flujoSalidaPaquete)
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
		paqueteCpv.anadirBloque(TITULO_BLOQUE_CLAVE_DATOS, claveCifradorEncriptada);
		paqueteCpv.anadirBloque(TITULO_BLOQUE_RESUMEN_DATOS_ENCRIPTADO, resumenEncriptadoDatos);

		// Finalmente, escribir el paquete al flujo
		PaqueteDAO.escribirPaquete(flujoSalidaPaquete, paqueteCpv);
	}
}