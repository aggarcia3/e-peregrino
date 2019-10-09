package esei.ssi.eperegrino.generador_claves;

import java.io.*;

import java.security.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

// Necesario para usar el provider Bouncy Castle (BC)
//    Para compilar incluir el fichero JAR en el classpath

/**
 * @author ribadas
 */
public class GenerarClaves {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			mensajeAyuda();
			System.exit(1);
		}

		
		// Anadir provider  (el provider por defecto no soporta RSA)
		Security.addProvider(new BouncyCastleProvider()); // Cargar el provider BC

		/*** Crear claves RSA 4096 bits  */
		KeyPairGenerator generadorRSA = KeyPairGenerator.getInstance("RSA", "BC"); // Hace uso del provider BC
		generadorRSA.initialize(4096);
		KeyPair clavesRSA = generadorRSA.generateKeyPair();
		PrivateKey clavePrivada = clavesRSA.getPrivate();
		PublicKey clavePublica = clavesRSA.getPublic();

		/*** 1 Volcar clave privada  a fichero */
		// 1.1 Recuperar de la clave su codificación en formato PKS8 (necesario para escribirla a disco)
		byte[] encodedPKCS8 = clavePrivada.getEncoded();

		// 1.2 Escribirla a fichero binario
		FileOutputStream out = new FileOutputStream(args[0] + ".privada");
		out.write(encodedPKCS8);
		out.close();

		/*** 3 Volcar clave publica  a fichero */
		// 3.1  Recuperar de la clave su codificación en formato X509 (necesario para escribirla a disco)
		byte[] encodedX509 = clavePublica.getEncoded();

		// 3.2 Escribirla a fichero binario
		out = new FileOutputStream(args[0] + ".publica");
		out.write(encodedX509);
		out.close();
		
		System.out.println("Generadas claves RSA pública y privada de 4096 bits en ficheros "+args[0] + ".publica"+ " y "+args[0] + ".privada");

	}

	public static void mensajeAyuda() {
		System.out.println("Generador de pares de clave RSA de 4096 bits");
		System.out.println("\tSintaxis:   java GenerarClaves prefijo");
		System.out.println();
	}
}
