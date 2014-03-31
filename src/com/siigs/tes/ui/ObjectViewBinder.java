package com.siigs.tes.ui;

import android.view.View;

/**
 * Proporciona un m�todo para modificar el proceso de asignaci�n de un atributo de T a un View
 * en un {@link AdaptadorArrayMultiTextView} y un {@link AdaptadorArrayMultiView}
 * @author Axel
 *
 */
public interface ObjectViewBinder<T> {

	/**
	 * Le comunica al implementador que el adaptador est� a punto de asignar <b>valor</b>
	 * en <b>viewDestino</b> para tomar decisi�n de permitirlo, o alterar el resultado de la asignaci�n.
	 * El implementador puede usar <b>viewDestino.getId()</b> para identificar el destino.
	 * 
	 * @param viewDestino El View en UI al que le ser� asignado <b>valor</b>
	 * @param metodoInvocarDestino El m�todo que adaptador invocar� en <b>viewDestino</b> para asignarle <b>valor</b>.
	 * En el caso de un {@link AdaptadorArrayMultiTextView} siempre ser� <b>setText</b> 
	 * @param origen El objeto en la colecci�n de datos del adaptador de donde se extrae <b>valor</b>
	 * @param atributoOrigen Atributo de <b>origen</b> que se extrae cuyo valor es el denominado por <b>valor</b>
	 * @param valor Valor del atributo <b>atributoOrigen</b> contenido en <b>origen</b> usado para ser asignado a <b>viewDestino</b>
	 * @param posicion La posici�n/fila en la lista/tabla que se visualiza este elemento. 
	 * @return El implementador debe regresar <b>true</b> para avisar al adaptador que NO debe
	 * asignar �l mismo <b>valor</b> a <b>viewDestino</b> pues el implementador lo ha hecho por su cuenta.
	 * Debe regresar <b>false</b> en caso contrario para que el adaptador contin�e con la asignaci�n.
	 */
	public boolean setViewValue(View viewDestino, String metodoInvocarDestino, 
			T origen, String atributoOrigen, Object valor, int posicion);

}
