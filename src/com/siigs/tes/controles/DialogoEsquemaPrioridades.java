/**
 * DialogFragment usado por {@link CensoCensoNominal} en modalidad <b>Esquemas Incompletos</b> para
 * ofrecer la capacidad de ver estadísticas de prioridades de esquemas incompletos.
 */

package com.siigs.tes.controles;

import com.siigs.tes.DialogoAyuda;
import com.siigs.tes.R;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.vistas.EsquemasIncompletos;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

public class DialogoEsquemaPrioridades extends DialogFragment {


	public static final String TAG=DialogoEsquemaPrioridades.class.toString();
	//Nombre del argumento/parámetro leído en getArguments()
	//que indica la persona a visitar
	
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;

	
	private TesAplicacion aplicacion;
	
	private Cursor datos = null;

	private TextView bcgP=null;
	private TextView hb1P=null;
	private TextView hb2P=null;
	private TextView hb3P=null;
	private TextView pa1P=null;
	private TextView pa2P=null;
	private TextView pa3P=null;
	private TextView pa4P=null;
	private TextView dptrP=null;
	private TextView srp1P=null;
	private TextView srp2P=null;
	private TextView rv1P=null;
	private TextView rv2P=null;
	private TextView rv3P=null;
	private TextView nc1P=null;
	private TextView nc2P=null;
	private TextView nc3P=null;
	private TextView in1P=null;
	private TextView in2P=null;
	private TextView inrP=null;
	private TextView[] controles =null;
	
	//Constructor requerido
	public DialogoEsquemaPrioridades(){}

	public Cursor getDatos(){return datos;}
	public void setDatos(Cursor cur){
		datos = cur;
		datos.moveToFirst();
		LlenarDatos();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Estílo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//Método 2 para hacer la ventana modal 
		//setCancelable(false);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Diálogo sin título (pues setCancelable() deja título que no queremos)
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		View vista = inflater.inflate(R.layout.dialogo_esquema_prioridades, container,false);
		
		bcgP = (TextView) vista.findViewById(R.id.bcg_p);
		
		hb1P = (TextView) vista.findViewById(R.id.hb1_p);
		hb2P = (TextView) vista.findViewById(R.id.hb2_p);
		hb3P = (TextView) vista.findViewById(R.id.hb3_p);
		
		pa1P = (TextView) vista.findViewById(R.id.pa1_p);
		pa2P = (TextView) vista.findViewById(R.id.pa2_p);
		pa3P = (TextView) vista.findViewById(R.id.pa3_p);
		pa4P = (TextView) vista.findViewById(R.id.pa4_p);
		
		dptrP = (TextView) vista.findViewById(R.id.dptr_p);
		
		srp1P = (TextView) vista.findViewById(R.id.srp1_p);
		srp2P = (TextView) vista.findViewById(R.id.srp2_p);
		
		rv1P = (TextView) vista.findViewById(R.id.rv1_p);
		rv2P = (TextView) vista.findViewById(R.id.rv2_p);
		rv3P = (TextView) vista.findViewById(R.id.rv3_p);
		
		nc1P = (TextView) vista.findViewById(R.id.nc1_p);
		nc2P = (TextView) vista.findViewById(R.id.nc2_p);
		nc3P = (TextView) vista.findViewById(R.id.nc3_p);
		
		in1P = (TextView) vista.findViewById(R.id.in1_p);
		in2P = (TextView) vista.findViewById(R.id.in2_p);
		inrP = (TextView) vista.findViewById(R.id.inr_p);
		
		controles = new TextView[]{bcgP, hb1P, hb2P, hb3P, pa1P, pa2P, pa3P, pa4P, dptrP,
				srp1P, srp2P, rv1P, rv2P, rv3P, nc1P, nc2P, nc3P, in1P, in2P,inrP};
		
		if(savedInstanceState!=null){
			for(TextView control : controles)
				if(control!=null)
				control.setText(savedInstanceState.getCharSequence(control.getId()+""));
		}
		
		LlenarDatos();
		
		//Botón ayuda
		ImageButton btnAyuda=(ImageButton)vista.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DialogoAyuda.CrearNuevo(getFragmentManager(), R.string.ayuda_prioridades);
			}
		});
		
		return vista;
	}
	
	
	private void LlenarDatos(){
		if(datos!= null && !datos.isClosed() && bcgP!=null){
			bcgP.setText(getNumero(EsquemasIncompletos.BCG_P)+"");
			
			hb1P.setText(getNumero(EsquemasIncompletos.HEPATITIS_1_P));
			hb2P.setText(getNumero(EsquemasIncompletos.HEPATITIS_2_P));
			hb3P.setText(getNumero(EsquemasIncompletos.HEPATITIS_3_P));
			
			pa1P.setText(getNumero(EsquemasIncompletos.PENTAVALENTE_1_P));
			pa2P.setText(getNumero(EsquemasIncompletos.PENTAVALENTE_2_P));
			pa3P.setText(getNumero(EsquemasIncompletos.PENTAVALENTE_3_P));
			pa4P.setText(getNumero(EsquemasIncompletos.PENTAVALENTE_4_P));
			
			dptrP.setText(getNumero(EsquemasIncompletos.DPT_R_P));
			
			srp1P.setText(getNumero(EsquemasIncompletos.SRP_1_P));
			srp2P.setText(getNumero(EsquemasIncompletos.SRP_2_P));
			
			rv1P.setText(getNumero(EsquemasIncompletos.ROTAVIRUS_1_P));
			rv2P.setText(getNumero(EsquemasIncompletos.ROTAVIRUS_2_P));
			rv3P.setText(getNumero(EsquemasIncompletos.ROTAVIRUS_3_P));
			
			nc1P.setText(getNumero(EsquemasIncompletos.NEUMOCOCO_1_P));
			nc2P.setText(getNumero(EsquemasIncompletos.NEUMOCOCO_2_P));
			nc3P.setText(getNumero(EsquemasIncompletos.NEUMOCOCO_3_P));
			
			in1P.setText(getNumero(EsquemasIncompletos.INFLUENZA_1_P));
			in2P.setText(getNumero(EsquemasIncompletos.INFLUENZA_2_P));
			inrP.setText(getNumero(EsquemasIncompletos.INFLUENZA_R_P));
		}
	}
	
	private String getNumero(String columna){
		int col = datos.getColumnIndex(columna);
		if(datos.isNull(col))
			return "0";
		return datos.getInt(col)+"";
	}

	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		for(TextView control : controles)
			if(control!=null)
				outState.putCharSequence(control.getId()+"", control.getText());
	}

	/**
	 * Cierra este diálogo y notifica a fragmento padre los datos.
	 * @param resultado Código de resultado oficial de la ejecución de este diálogo
	 */
	private void Cerrar(int resultado){
		Intent datos=null;//new Intent();
		//resultado.putExtra("dato", "valor");
		getTargetFragment().onActivityResult(getTargetRequestCode(), 
				resultado, datos);
		dismiss();
	}
	
}//fin clase