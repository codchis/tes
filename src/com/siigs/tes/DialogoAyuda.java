/**
 * Esta es una clase genérica para desplegar un cuadro de diálogo simple
 * que carga dinámicamente el layout indicado a través de de argumentos
 * de tipo Bundle.
 * Por convención, los layouts usados tienen la nomenclatura: ayuda_XXX.xml
 */

package com.siigs.tes;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class DialogoAyuda extends DialogFragment {


	public static final String TAG=DialogoAyuda.class.toString();
	//Nombre del argumento/parámetro leído en getArguments()
	//que indica el layout a visualizar
	public static final String ARG_TEXT="textId";

	/**
	 * Crea un nuevo diálogo de ayuda con el fragment manager y layout
	 * especificados.
	 * @param fm
	 * @param textResId
	 */
	public static void CrearNuevo(FragmentManager fm, int textResId){
		DialogoAyuda dialogo=new DialogoAyuda();
		Bundle args=new Bundle();
		args.putInt(DialogoAyuda.ARG_TEXT, textResId);
		dialogo.setArguments(args);
		dialogo.show(fm, DialogoAyuda.TAG);
	}
	
	//Constructor requerido
	public DialogoAyuda(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Diálogo sin título
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//Visualizamos el layout indicado en los argumentos
		View view=inflater.inflate(R.layout.ayuda_template, 
				container,false);
		
		TextView contenido = (TextView)view.findViewById(R.id.txtContenedorAyuda);
		contenido.setText(getArguments().getInt(ARG_TEXT));
		
		//Antes con método ViewStub...
		//ViewStub contenido = (ViewStub)view.findViewById(R.id.vsContenedorAyuda);
		//contenido.setLayoutResource(getArguments().getInt(ARG_TEXT));
		//contenido.inflate();
		
		//Botón cerrar
		Button btnCerrar=(Button)view.findViewById(R.id.btnCerrar);
		btnCerrar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		return view;
	}


}//fin clase