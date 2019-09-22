package esei.ssi.eperegrino.generador_cpv;

import esei.ssi.eperegrino.common.Bloque;

import java.io.File;
import java.util.Map;

final class GeneradorCPV {
	// Argumentos de línea de comandos: <nombre paquete> <ficheros con las claves necesarias>  
	public static void main(final String[] args) {
		/* TODO: pedir datos por entrada estándar para crear un Map<String, String>.
		 * Manejar errores que puedan surgir en el proceso, mostrándolos al usuario */
		// try {
		// 	generarPaquete(...);
		// } catch (Exception exc) { ... }
	}

	private static final class FicherosClave {
		private final File ficheroClavePublicaOficina;
		private final File ficheroClavePrivadaPeregrino;

		FicherosClave(final File ficheroClavePublicaOficina, final File ficheroClavePrivadaPeregrino) {
			if (ficheroClavePrivadaPeregrino == null || ficheroClavePublicaOficina == null) {
				throw new IllegalArgumentException();
			}

			this.ficheroClavePrivadaPeregrino = ficheroClavePrivadaPeregrino;
			this.ficheroClavePublicaOficina = ficheroClavePublicaOficina;
		}

		File getFicheroClavePublicaOficina() {
			return ficheroClavePublicaOficina;
		}

		File getFicheroClavePrivadaPeregrino() {
			return ficheroClavePrivadaPeregrino;
		}
	}

	private static void generarPaquete(final Map<String, String> datos, final File paquete, final FicherosClave ficherosClave) throws Exception {
		/* TODO:
		 * convertir mapa a cadena JSON (JSONUtils.map2json), cadena JSON a bytes
		 * en UTF-8 (getBytes(Charset.forName("UTF-8"))).
		 * Para la confidencialidad:
		 * 1. generar clave aleatoria,
		 * 2. encriptar cadena JSON con simétrico,
		 * 3. encriptar clave aleatoria con asimétrico (usando clave pública de la oficina),
		 * 4. meter resultado de 2 en bloque llamado "DATOS PEREGRINO",
		 * 5. meter resultado de 3 en bloque llamado "CLAVE DATOS PEREGRINO".
		 * Para la autenticidad y no repudio (firma digital):
		 * 1. calcular hash del resultado del punto 2 anterior,
		 * 2. encriptar hash con RSA (usando clave privada del peregrino),
		 * 3. meter resultado de 2 en bloque "FIRMA PEREGRINO".
		 */
	}
}