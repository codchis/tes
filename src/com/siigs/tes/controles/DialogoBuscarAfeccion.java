/**
 * DialogFragment usado por {@link ControlConsultasNuevo} para
 * ofrecer la capacidad de buscar una afección en modo autocompletar al escribir.
 */

package com.siigs.tes.controles;

import com.siigs.tes.DialogoAyuda;
import com.siigs.tes.R;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.tablas.Consulta;
import com.siigs.tes.ui.AdaptadorAutoComplete;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.Toast;

public class DialogoBuscarAfeccion extends DialogFragment {


	public static final String TAG=DialogoBuscarAfeccion.class.toString();

	public static final int REQUEST_CODE=987;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;
	
	//Valores de salida producidos por este cuadro de dialogo
	public static final String SALIDA_ID_AFECCION = "afeccion";
	
	private int idRegistroSeleccionado = -1;
	private String textoRegistroSeleccionado = "";
	

	public static void CrearNuevo(Fragment llamador){
		DialogoBuscarAfeccion dialogo=new DialogoBuscarAfeccion();
		Bundle args=new Bundle();
		dialogo.setArguments(args);
		dialogo.setTargetFragment(llamador, DialogoBuscarAfeccion.REQUEST_CODE);
		dialogo.show(llamador.getFragmentManager(), DialogoBuscarAfeccion.TAG);
		//Este diálogo avisará su fin en onActivityResult() de llamador
	}
	
	private TesAplicacion aplicacion;
	
	//Constructor requerido
	public DialogoBuscarAfeccion(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Estílo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//Método 2 para hacer la ventana modal 
		setCancelable(false);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Diálogo sin título (pues setCancelable() deja título que no queremos)
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		//Permite que resultados de AutocompleteTextView aparezcan sobre el teclado
		dialogo.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View vista = inflater.inflate(R.layout.dialogo_buscar_afeccion, container,false);
		
		
		final AutoCompleteTextView acBuscar = (AutoCompleteTextView) vista.findViewById(R.id.acAfeccion);
		GenerarAutoCompleteAfeccion(acBuscar);
		acBuscar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				idRegistroSeleccionado = (int)id;
				textoRegistroSeleccionado = acBuscar.getText().toString();
			}
		});
		acBuscar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus)acBuscar.setText(textoRegistroSeleccionado);
			}
		});
		
		//Botón confirmar
		Button btnConfirmar = (Button)vista.findViewById(R.id.btnConfirmar);
		btnConfirmar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(idRegistroSeleccionado<0){
					Toast.makeText(getActivity(), "Debe escribir una afección válida", Toast.LENGTH_LONG).show();
					return;
				}
				Cerrar(RESULT_OK);
			}
		});
		
		//Botón ayuda
		ImageButton btnAyuda=(ImageButton)vista.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DialogoAyuda.CrearNuevo(getFragmentManager(), R.string.ayuda_buscar_afeccion);
			}
		});
		
		//Botón cerrar
		Button btnCancelar=(Button)vista.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cerrar(DialogoBuscarAfeccion.RESULT_CANCELAR);
			}
		});
		
		return vista;
	}

	
	/**
	 * Genera adaptador para el texto sugerido de afecciones del cie10
	 * @param acBuscar View de autocompletado a configurar 
	 */
	private void GenerarAutoCompleteAfeccion(AutoCompleteTextView acBuscar){

		String[] de = new String[]{Consulta.DESCRIPCION};
		int[] hacia = new int[]{android.R.id.text1};
		AdaptadorAutoComplete adaptador = new AdaptadorAutoComplete(
				getActivity(), android.R.layout.simple_dropdown_item_1line,
				null, de, hacia,0);
		//Convertidor de lo legible
		adaptador.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
			@Override
			public CharSequence convertToString(Cursor cur) {
				return cur.getString(cur.getColumnIndex(Consulta.DESCRIPCION));
			}
		});
		
		adaptador.setFilterQueryProvider(new FilterQueryProvider() {
			@Override
			public Cursor runQuery(CharSequence description) {
				return Consulta.buscar(getActivity(), description.toString());
			}
		});
		
		acBuscar.setAdapter(adaptador);
	}

	/**
	 * Cierra este diálogo y notifica a fragmento padre los datos.
	 * @param resultado Código de resultado oficial de la ejecución de este diálogo
	 */
	private void Cerrar(int resultado){
		Intent datos=new Intent();
		if(resultado == RESULT_OK){
			Consulta salida = Consulta.getConsultaConId(getActivity(), idRegistroSeleccionado);
			if(salida == null){ //NO DEBERÍA PASAR
				Cerrar(RESULT_CANCELAR);
				return;
			}
			datos.putExtra(SALIDA_ID_AFECCION, salida.id_cie10);
		}
		getTargetFragment().onActivityResult(getTargetRequestCode(), 
				resultado, datos);
		dismiss();
	}
	
}//fin clase