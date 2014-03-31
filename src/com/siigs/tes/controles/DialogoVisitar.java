/**
 * DialogFragment usado por {@link CensoCensoNominal} en modalidad <b>Esquemas Incompletos</b> para
 * ofrecer la capacidad de declarar una visita a una persona.
 */

package com.siigs.tes.controles;

import com.siigs.tes.DialogoAyuda;
import com.siigs.tes.R;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Bitacora;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.EsquemaIncompleto;
import com.siigs.tes.datos.tablas.EstadoVisita;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.Visita;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.WidgetUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

public class DialogoVisitar extends DialogFragment {


	public static final String TAG=DialogoVisitar.class.toString();
	//Nombre del argumento/parámetro leído en getArguments()
	//que indica la persona a visitar
	public static final String ARG_ID_PERSONA="id_persona";
	
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;


	public static void CrearNuevo(Fragment llamador, int idPersona){
		DialogoVisitar dialogo=new DialogoVisitar();
		Bundle args=new Bundle();
		args.putInt(DialogoVisitar.ARG_ID_PERSONA, idPersona);
		dialogo.setArguments(args);
		dialogo.setTargetFragment(llamador, DialogoVisitar.REQUEST_CODE);
		dialogo.show(llamador.getFragmentManager(), DialogoVisitar.TAG);
		//Este diálogo avisará su fin en onActivityResult() de llamador
	}
	
	private TesAplicacion aplicacion;
	
	//Constructor requerido
	public DialogoVisitar(){}
	
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
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		View vista = inflater.inflate(R.layout.dialogo_visitar, container,false);
		
		final Persona p;
		int idPersona = getArguments().getInt(ARG_ID_PERSONA);
		try {
			p = Persona.getPersona(getActivity(), idPersona);
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getActivity(), "No se pudo leer la persona con _id:"+idPersona, Toast.LENGTH_SHORT).show();
			Cerrar(DialogoVisitar.RESULT_CANCELAR);
			return vista;
		}
		
		WidgetUtil.setDatosBasicosPaciente(vista, p);
		
		//Opciones
		AdaptadorArrayMultiTextView<EstadoVisita> adaptador = new AdaptadorArrayMultiTextView<EstadoVisita>(
				getActivity(), android.R.layout.simple_dropdown_item_1line,
				EstadoVisita.getEstadosVisitasActivas(getActivity()), 
				new String[]{EstadoVisita.DESCRIPCION}, new int[]{android.R.id.text1});
		final Spinner spEstadosVisitas = (Spinner)vista.findViewById(R.id.spEstadoVisita);
		spEstadosVisitas.setAdapter(adaptador);
		
		//Botón registrar
		Button btnRegistrar = (Button)vista.findViewById(R.id.btnRegistrarVisita);
		btnRegistrar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(spEstadosVisitas.getSelectedItem() == null) return;
				
				//Confirmación
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage("¿En verdad desea registrar la visita?");
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Guardamos cambios en memoria
						Visita visita = new Visita();
						visita.id_persona = p.id;
						visita.fecha = DatosUtil.getAhora();
						visita.id_asu_um = aplicacion.getUnidadMedica();
						visita.id_estado_visita = ((EstadoVisita)spEstadosVisitas.getSelectedItem()).id;
						
						//En bd
						int ICA = ContenidoControles.ICA_AGREGAR_VISITA;
						try {
							Visita.AgregarNuevaVisita(getActivity(), visita);
							EsquemaIncompleto.BorrarDePersona(getActivity(), p.id);
							Bitacora.AgregarRegistro(getActivity(), aplicacion.getSesion().getUsuario()._id, 
									ICA, "paciente:"+p.id+", estado_visita:"+visita.id_estado_visita);
							Cerrar(RESULT_OK);
						} catch (Exception e) {
							ErrorSis.AgregarError(getActivity(), aplicacion.getSesion().getUsuario()._id, 
									ICA, e.toString());
							e.printStackTrace();
							Cerrar(RESULT_CANCELAR);
						}
					}
				} );
				dialogo.show();
			}
		});
		
		//Botón ayuda
		ImageButton btnAyuda=(ImageButton)vista.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DialogoAyuda.CrearNuevo(getFragmentManager(), R.string.ayuda_visitar);
			}
		});
		
		//Botón cerrar
		Button btnCancelar=(Button)vista.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cerrar(DialogoVisitar.RESULT_CANCELAR);
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
	
}//fin clase