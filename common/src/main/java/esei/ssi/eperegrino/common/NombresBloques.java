package esei.ssi.eperegrino.common;

/**
 * Contiene los nombres de bloques generados por este sistema, que otras partes
 * del sistema esperarán encontrar en los paquetes.
 *
 * @author Alejandro González García
 */
public final class NombresBloques {
	public static final String TITULO_BLOQUE_DATOS_PEREGRINO = "DATOS PEREGRINO";
	public static final String TITULO_BLOQUE_CLAVE_DATOS_PEREGRINO = "CLAVE DATOS PEREGRINO";
	public static final String TITULO_BLOQUE_RESUMEN_DATOS_PEREGRINO_ENCRIPTADOS = "RESUMEN DATOS PEREGRINO ENCRIPTADOS";

	public static final String TITULO_BLOQUE_DATOS_SELLO_ALBERGUE = "DATOS SELLO ALBERGUE {ID}";
	public static final String TITULO_BLOQUE_CLAVE_SELLO_ALBERGUE = "CLAVE DATOS SELLO ALBERGUE {ID}";
	public static final String TITULO_BLOQUE_RESUMEN_SELLO_ALBERGUE_ENCRIPTADO = "RESUMEN DATOS SELLO ALBERGUE {ID}";

	private NombresBloques() {
		// No permitir instanciar esta clase
	}
}
