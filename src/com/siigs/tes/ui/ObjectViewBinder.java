package com.siigs.tes.ui;

import android.view.View;

/**
 * Proporciona un método para modificar el proceso de asignación de un atributo de T a un View
 * en un {@link AdaptadorArrayMultiTextView} y un {@link AdaptadorArrayMultiView}
 * @author Axel
 *
 */
public interface ObjectViewBinder<T> {

	/**
	 * Le comunica al implementador que el adaptador está a punto de asignar <b>valor</b>
	 * en <b>viewDestino</b> para tomar decisión de permitirlo, o alterar el resultado de la asignación.
	 * El implementador puede usar <b>viewDestino.getId()</b> para identificar el destino.
	 * 
	 * @param viewDestino El View en UI al que le será asignado <b>valor</b>
	 * @param metodoInvocarDestino El método que adaptador invocará en <b>viewDestino</b> para asignarle <b>valor</b>.
	 * En el caso de un {@link AdaptadorArrayMultiTextView} siempre será <b>setText</b> 
	 * @param origen El objeto en la colección de datos del adaptador de donde se extrae <b>valor</b>
	 * @param atributoOrigen Atributo de <b>origen</b> que se extrae cuyo valor es el denominado por <b>valor</b>
	 * @param valor Valor del atributo <b>atributoOrigen</b> contenido en <b>origen</b> usado para ser asignado a <b>viewDestino</b>
	 * @param posicion La posición/fila en la lista/tabla que se visualiza este elemento. 
	 * @return El implementador debe regresar <b>true</b> para avisar al adaptador que NO debe
	 * asignar él mismo <b>valor</b> a <b>viewDestino</b> pues el implementador lo ha hecho por su cuenta.
	 * Debe regresar <b>false</b> en caso contrario para que el adaptador continúe con la asignación.
	 */
	public boolean setViewValue(View viewDestino, String metodoInvocarDestino, 
			T origen, String atributoOrigen, Object valor, int posicion);

}
