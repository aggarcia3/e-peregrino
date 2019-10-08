package esei.ssi.eperegrino.common;

import java.security.Provider;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Contiene diferentes parámetros que indican los algoritmos criptográficos a
 * emplear, así como sus modos de funcionamiento.
 *
 * @author Alejandro González García
 */
public final class ParametrosCriptograficos {
	/**
	 * El algoritmo a emplear en conjunción con las claves de los actores.
	 */
	public static final String ALGORITMO_ASIMETRICO = "RSA/NONE/PKCS1Padding";
	/**
	 * El algoritmo para el que generar claves asimétricas.
	 */
	public static final String ALGORITMO_GENERADOR_CLAVES_ASIMETRICO = "RSA";
	/**
	 * El algoritmo a emplear para codificar datos de manera simétrica, empleando
	 * una única clave.
	 */
	public static final String ALGORITMO_SIMETRICO = "AES/CTR/NoPadding"; // Counter es un poco más confidencial que ECB
	/**
	 * El algoritmo para el que generar claves simétricas.
	 */
	public static final String ALGORITMO_GENERADOR_CLAVES_SIMETRICO = "AES";
	/**
	 * La longitud de la clave del algoritmo de cifrado simétrico empleado.
	 */
	public static final int LONGITUD_CLAVE_SIMETRICO = 256;
	/**
	 * El algoritmo de resumen a emplear para obtener un resumen criptográfico
	 * (hash) de los datos, cuando sea necesario.
	 */
	public static final String ALGORITMO_RESUMEN = "SHA-512";
	/**
	 * El proveedor de los algoritmos a emplear para realizar operaciones
	 * criptográficas. Los usuarios de esta variable pueden asumir que el proveedor
	 * ya ha sido registrado con la JCA.
	 */
	public static final Provider PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS = new BouncyCastleProvider();
}