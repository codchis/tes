package com.siigs.tes.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

/**
 * Clase creada únicamente para evitar pequeño comportamiento indeseado en UI de un
 * {@link AutoCompleteTextView} dentro de un {@link DialogFragment} cuando despliega lista de resultados
 * y los mismos aparecen detrás del teclado AL actualizar de un resultado previamente seleccionado.
 * El comportamiento no se manifiesta si en el AutoCompleteTextView borramos todo lo buscado a mano y
 * comenzamos de nuevo, pero para evitar la molestia al usuario cuando va borrando por partes sin haber
 * borrado el texto completamente, es que usamos este adaptador que hará que se esconda el teclado.
 * @author Axel
 *
 */
public class AdaptadorAutoComplete extends SimpleCursorAdapter {

	private Context contexto;
	
	public AdaptadorAutoComplete(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		this.contexto = context;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		//return super.getView(arg0, arg1, arg2);
		final View salida = super.getView(pos, convertView, parent);
		salida.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					InputMethodManager imm = 
							(InputMethodManager)contexto.getSystemService(Context.INPUT_METHOD_SERVICE);
					if(imm!=null)
						imm.hideSoftInputFromWindow(salida.getWindowToken(), 0);
				}
				return false;
			}
		});
		
		return salida;
	}
	
	

}
