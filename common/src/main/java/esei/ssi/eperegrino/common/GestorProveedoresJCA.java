package esei.ssi.eperegrino.common;

import static java.security.Security.addProvider;
import static java.security.Security.getProvider;

import java.security.Provider;

/**
 * Proporciona funcionalidades comunes de gestión de proveedores
 * de JCA.
 * @author Alejandro González García
 */
public final class GestorProveedoresJCA {
	private GestorProveedoresJCA() {
		// No permitir instanciar esta clase
	}

	/**
	 * Registra en la JCA los proveedores que empleará el sistema.
	 * Este método debe de ejecutarse una sola vez poco después de
	 * iniciarse la aplicación.
	 */
	public static void registrarProveedores() {
		addProviderIfNotRegistered(ParametrosCriptograficos.PROVEEDOR_ALGORITMOS_CRIPTOGRAFICOS);
	}

	/**
	 * Añade un proveedor de JCA solo si no ha sido ya añadido.
	 * @param p El proveedor a añadir. Se asume que no es nulo.
	 */
	private static void addProviderIfNotRegistered(final Provider p) {
		if (getProvider(p.getName()) != null) {
			addProvider(p);
		}
	}
}
