package esei.ssi.eperegrino.common;

import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Contiene métodos estáticos utilitarios para realizar algunas tareas comunes,
 * sencillas y típicamente tediosas de reimplementar.
 *
 * @author Alejandro González García
 */
public final class Util {
	/**
	 * Pide interactivamente un fichero al usuario. Se asume que los parámetros de
	 * entrada son no nulos.
	 *
	 * @param mensaje     El mensaje a mostrar el usuario.
	 * @param in          La entrada de datos del usuario.
	 * @param out         La salida de texto al usuario.
	 * @param debeExistir Verdadero si el fichero debe de existir, ser legible y un
	 *                    fichero de tipo normal, falso en caso de que deba ser
	 *                    inexistente.
	 * @return El fichero especificado por el usuario.
	 */
	public static File pedirFichero(final String mensaje, final Scanner in, final PrintStream out, final boolean debeExistir) {
		File toret = null;

		do {
			out.print(mensaje + ": ");
			toret = new File(in.nextLine());
			if ((debeExistir && (!toret.canRead() || !toret.isFile())) || (!debeExistir && toret.exists())) {
				out.println("Ese fichero " + (debeExistir ? "no existe" : "ya existe") + ", introduzca otro");
				toret = null;
			}
		} while (toret == null);

		return toret;
	}
}
