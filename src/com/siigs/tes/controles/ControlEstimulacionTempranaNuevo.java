/**
 * Muestra un diálogo modal para agregar una nueva accion nutricional al paciente
 */
package com.siigs.tes.controles;

import com.siigs.tes.DialogoTes;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Bitacora;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.ui.WidgetUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * @author Axel
 *
 */
public class ControlEstimulacionTempranaNuevo extends DialogFragment {

	public static final String TAG= ControlEstimulacionTempranaNuevo.class.getSimpleName();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;	

	private static final String SI = "Si";
	private static final String NO = "No";
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	//Constructor requerido
	public ControlEstimulacionTempranaNuevo(){}
	
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
		View vista=inflater.inflate(R.layout.controles_atencion_control_estimulacion_temprana_nuevo, container,false);			
		
		final Persona p = sesion.getDatosPacienteActual().persona;
		
		WidgetUtil.setBarraTitulo(vista, R.id.barra_titulo_estimulacion, R.string.agregar_estimulacion, 
				R.string.ayuda_agregar_estimulacion_temprana, getFragmentManager());
		
		((TextView)vista.findViewById(R.id.txtNombre)).setText(p.getNombreCompleto());
		
		final CheckBox chkCapacitado = (CheckBox)vista.findViewById(R.id.chkCapacitado);
		chkCapacitado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				chkCapacitado.setText(isChecked ? SI : NO);
			}
		});
		chkCapacitado.setText(NO);
		
		
		//Cancelación manual
		Button btnCancelar=(Button)vista.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cerrar(ControlEstimulacionTempranaNuevo.RESULT_CANCELAR);
			}
		});
		
		//Agregar
		Button btnAgregar = (Button) vista.findViewById(R.id.btnAgregar);
		btnAgregar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Confirmación
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage("¿En verdad desea aplicar este control?");
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Guardamos cambios en memoria
						com.siigs.tes.datos.tablas.EstimulacionTemprana estimulacion = 
								new com.siigs.tes.datos.tablas.EstimulacionTemprana();
						estimulacion.id_persona = p.id;
						estimulacion.tutor_capacitado = chkCapacitado.isChecked() ? 1 : 0;
						/*accion.id_invitado = sesion.getUsuarioInvitado() != null ? 
								sesion.getUsuarioInvitado()._id : null;*/
						estimulacion.id_asu_um = aplicacion.getUnidadMedica();
						estimulacion.fecha = DatosUtil.getAhora();
						sesion.getDatosPacienteActual().estimulacionesTempranas.add(estimulacion);
						//En bd
						int ICA = ContenidoControles.ICA_ESTIMULACIONTEMPRANA_INSERTAR;
						try {
							com.siigs.tes.datos.tablas.EstimulacionTemprana
								.AgregarNuevaEstimulacion(getActivity(), estimulacion);
							Bitacora.AgregarRegistro(getActivity(), sesion.getUsuario()._id, 
									ICA, "paciente:"+p.id+", tutor_capacitado:"+estimulacion.tutor_capacitado);
						} catch (Exception e) {
							ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
									ICA, e.toString());
							e.printStackTrace();
						}
						//Si no funcionara el guardado generamos un pendiente
						PendientesTarjeta pendiente = new PendientesTarjeta();
						pendiente.id_persona = p.id;
						pendiente.tabla = com.siigs.tes.datos.tablas.EstimulacionTemprana.NOMBRE_TABLA;
						pendiente.registro_json = DatosUtil.CrearStringJson(estimulacion);
						// Por default pedimos una TES al usuario en un diálogo modal
						DialogoTes.IniciarNuevo(ControlEstimulacionTempranaNuevo.this,
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
			if(resultCode == DialogoTes.RESULT_CANCELAR)Cerrar(ControlEstimulacionTempranaNuevo.RESULT_CANCELAR);
			else if(resultCode == DialogoTes.RESULT_OK)Cerrar(ControlEstimulacionTempranaNuevo.RESULT_OK);
			break;
		}
	}
	
}//fin clase
