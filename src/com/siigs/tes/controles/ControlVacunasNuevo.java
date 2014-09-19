/**
 * Muestra un diálogo modal para agregar una nueva vacuna al paciente
 */
package com.siigs.tes.controles;

import java.util.ArrayList;
import java.util.List;

import com.siigs.tes.DialogoTes;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Bitacora;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.EsquemaIncompleto;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.ReglaVacuna;
import com.siigs.tes.datos.tablas.Vacuna;
import com.siigs.tes.datos.tablas.ViaVacuna;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.WidgetUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author Axel
 *
 */
public class ControlVacunasNuevo extends DialogFragment {

	public static final String TAG=ControlVacunasNuevo.class.getSimpleName();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;	
	public static final String PARAM_ID_VACUNA = "id_vacuna_preseleccionada";

	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	//Constructor requerido
	public ControlVacunasNuevo(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Estílo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//Método 2 para hacer la ventana modal 
		setCancelable(false);
		
		//this.setRetainInstance(true);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Diálogo sin título (pues setCancelable() deja título que no queremos)
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		dialogo.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		View vista=inflater.inflate(R.layout.controles_atencion_control_vacunas_nuevo, container,false);			
		
		final Persona p = sesion.getDatosPacienteActual().persona;
		
		WidgetUtil.setBarraTitulo(vista, R.id.barra_titulo_vacuna, R.string.agregar_vacuna, 
				R.string.ayuda_agregar_vacuna, getFragmentManager());
		
		WidgetUtil.setDatosBasicosPaciente(vista, p);
		
		final TextView txtViaVacuna = (TextView)vista.findViewById(R.id.txtViaVacuna);
		final TextView txtDosis = (TextView)vista.findViewById(R.id.txtDosis);
		final TextView txtRegion = (TextView)vista.findViewById(R.id.txtRegion);
		final TextView txtObservacion = (TextView)vista.findViewById(R.id.txtObservacion);
		
		//Lista de vacunas
		final Spinner spVacunas = (Spinner)vista.findViewById(R.id.spVacunas);
		List<Vacuna> vacunasPosibles = Vacuna.getVacunasActivas(getActivity());
		List<Vacuna> vacunasAplicables = new ArrayList<Vacuna>();
		for(Vacuna vac : vacunasPosibles){
			String motivo = ReglaVacuna.motivoNoEsAplicableVacuna(getActivity(), vac._id, sesion.getDatosPacienteActual());
			if(motivo.equals("")) //Si es aplicable
				vacunasAplicables.add(vac);
		}
		AdaptadorArrayMultiTextView<Vacuna> adaptador = new AdaptadorArrayMultiTextView<Vacuna>(
				getActivity(), android.R.layout.simple_dropdown_item_1line, vacunasAplicables, 
				new String[]{Vacuna.DESCRIPCION}, new int[]{android.R.id.text1});
		spVacunas.setAdapter(adaptador);
		
		spVacunas.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
				txtViaVacuna.setText(R.string.desconocido);
				txtDosis.setText(R.string.desconocido);
				txtRegion.setText(R.string.desconocido);
				Vacuna vacuna = (Vacuna)spVacunas.getSelectedItem();
				try {
					ReglaVacuna regla = ReglaVacuna.getReglaDeVacuna(getActivity(), vacuna._id);
					txtViaVacuna.setText(ViaVacuna.getDescripcion(getActivity(), regla.id_via_vacuna));
					txtDosis.setText(regla.dosis==null ? getString(R.string.ninguno) : regla.dosis+"");
					txtRegion.setText(regla.region==null ? getString(R.string.ninguno) : regla.region);
					
					int maxObservacion = 40;
					final String observacion = regla.observacion_region==null ? 
							getString(R.string.ninguno) : regla.observacion_region;
					txtObservacion.setPaintFlags(txtObservacion.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
					if(observacion.length()>maxObservacion)
						txtObservacion.setText(observacion.substring(0, maxObservacion) + "...[+]");
					else txtObservacion.setText(observacion);
					txtObservacion.setOnClickListener(new OnClickListener() {
						@Override public void onClick(View v) {
							AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
							dialogo.setMessage(observacion);
							dialogo.show();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			@Override public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		if(getArguments()!=null && getArguments().containsKey(PARAM_ID_VACUNA)){
			int idBuscar = getArguments().getInt(PARAM_ID_VACUNA);
			for(int i=0;i<vacunasAplicables.size();i++)
				if(vacunasAplicables.get(i)._id==idBuscar){
					spVacunas.setSelection(i);
					break;
				}
		}
		
		//Cancelación manual
		Button btnCancelar=(Button)vista.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cerrar(ControlVacunasNuevo.RESULT_CANCELAR);
			}
		});
		
		final EditText txtLote = (EditText)vista.findViewById(R.id.txtLote);
		final EditText txtTemperatura = (EditText)vista.findViewById(R.id.txtTemperatura);
		
		//Agregar
		Button btnAgregar = (Button) vista.findViewById(R.id.btnAgregar);
		btnAgregar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(spVacunas.getSelectedItem() == null)return;
				
				//Confirmación
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage("¿En verdad desea aplicar esta vacuna?");
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Guardamos cambios en memoria
						ControlVacuna vacuna = new ControlVacuna();
						vacuna.id_persona = p.id;
						vacuna.id_vacuna = ((Vacuna)spVacunas.getSelectedItem())._id;
						vacuna.id_invitado = sesion.getUsuarioInvitado() != null ? 
								sesion.getUsuarioInvitado()._id : null;
						vacuna.id_asu_um = aplicacion.getUnidadMedica();
						vacuna.fecha = DatosUtil.getAhora();
						vacuna.codigo_barras = txtLote.getText().toString().trim().equals("") ? 
								null : txtLote.getText().toString().trim();
						//Temperatura
						Double temperatura = null;
						try{temperatura = Double.parseDouble(txtTemperatura.getText()+"");}catch(Exception e){}
						vacuna.temperatura = temperatura+"";
						//Ubicación
						Location loc = aplicacion.getLocalizacionGPS();
						if(loc != null){
							vacuna.latitud = loc.getLatitude()+"";
							vacuna.longitud = loc.getLongitude()+"";
						}
						sesion.getDatosPacienteActual().vacunas.add(vacuna);
						//En bd
						int ICA = ContenidoControles.ICA_CONTROLVACUNA_INSERTAR;
						try {
							ControlVacuna.AgregarNuevoControlVacuna(getActivity(), vacuna);
							Bitacora.AgregarRegistro(getActivity(), sesion.getUsuario()._id, 
									ICA, "paciente:"+p.id+", vacuna:"+vacuna.id_vacuna);
						} catch (Exception e) {
							ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
									ICA, e.toString());
							e.printStackTrace();
						}
						try{EsquemaIncompleto.BorrarEsquema(getActivity(), vacuna.id_persona, vacuna.id_vacuna);}
						catch(Exception e){}
						//Si no funcionara el guardado generamos un pendiente
						PendientesTarjeta pendiente = new PendientesTarjeta();
						pendiente.id_persona = p.id;
						pendiente.tabla = ControlVacuna.NOMBRE_TABLA;
						pendiente.registro_json = DatosUtil.CrearStringJson(vacuna);
						// Por default pedimos una TES al usuario en un diálogo modal
						DialogoTes.IniciarNuevo(ControlVacunasNuevo.this,
								DialogoTes.ModoOperacion.GUARDAR, pendiente);
						//onActivityResult recibe respuesta del diálogo
					}
				} );
				dialogo.show();
				
			}
		});
	
		return vista;
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
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case DialogoTes.REQUEST_CODE:
			if(resultCode == DialogoTes.RESULT_CANCELAR)Cerrar(ControlVacunasNuevo.RESULT_CANCELAR);
			else if(resultCode == DialogoTes.RESULT_OK)Cerrar(ControlVacunasNuevo.RESULT_OK);
			break;
		}
	}
	
}//fin clase
