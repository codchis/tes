package com.siigs.tes.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * Esta clase existe para enlistar items contenidos en un adaptador.
 * Su existencia es debido a que ListView da problemas para enlistar items al estar
 * dentro de un ScrollView.
 * Esta clase es útil para visualizar pequeñas listas de datos fijos que se crearán bien
 * incluso dentro del ScrollView.
 * @author Axel
 *
 */
public class ListaSimple extends LinearLayout {

	private BaseAdapter adaptador;
	
	public ListaSimple(Context context) {
		super(context);
	}
	public ListaSimple(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public ListaSimple(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setAdaptador(BaseAdapter adaptador){
		this.adaptador = adaptador;
		this.removeAllViewsInLayout();
		for(int i=0;i<this.adaptador.getCount();i++)
			this.addView(this.adaptador.getView(i, null, (ViewGroup) getParent()) );
	}

	
}
