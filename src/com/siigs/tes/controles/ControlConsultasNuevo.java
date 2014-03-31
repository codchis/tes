/**
 * Muestra un diálogo modal para agregar una nueva consulta al paciente
 */
package com.siigs.tes.controles;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.siigs.tes.DialogoTes;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Bitacora;
import com.siigs.tes.datos.tablas.Consulta;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.Tratamiento;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.ObjectViewBinder;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * @author Axel
 *
 */
public class ControlConsultasNuevo extends DialogFragment {

	public static final String TAG= ControlConsultasNuevo.class.getSimpleName();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;	

	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	//Constructor requerido
	public ControlConsultasNuevo(){}
	
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
		View vista=inflater.inflate(R.layout.controles_atencion_control_consultas_nuevo, container,false);			
		
		final Persona p = sesion.getDatosPacienteActual().persona;
		
		WidgetUtil.setBarraTitulo(vista, R.id.barra_titulo_consulta, R.string.agregar_consulta, 
				R.string.ayuda_agregar_consulta, getFragmentManager());
		
		((TextView)vista.findViewById(R.id.txtNombre)).setText(p.getNombreCompleto());
		
		//Widgets
		final Spinner spConsultas = (Spinner)vista.findViewById(R.id.spConsultas);
		final Spinner spTratamiento = (Spinner)vista.findViewById(R.id.spTratamiento);
		final Spinner spTipoTratamiento = (Spinner)vista.findViewById(R.id.spTipoTratamiento);
		final Spinner spPadecimiento = (Spinner)vista.findViewById(R.id.spPadecimiento);
		final CheckBox chkPrimeraVez = (CheckBox)vista.findViewById(R.id.chkPrimeraVez);
		
		//Lista de consultas
		final List<Consulta> consultas = Consulta.getConsultasActivas(getActivity());
		AdaptadorArrayMultiTextView<Consulta> adaptadorConsultas = new AdaptadorArrayMultiTextView<Consulta>(
				getActivity(), android.R.layout.simple_dropdown_item_1line, consultas, 
				new String[]{Consulta.DESCRIPCION}, new int[]{android.R.id.text1});
		spConsultas.setAdapter(adaptadorConsultas);
		
		//Lista de tipos de tratamiento
		final String[] tipos = Tratamiento.getTipos(getActivity());
		ArrayAdapter<String> adaptadorTipos = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_dropdown_item_1line,android.R.id.text1, tipos);
		spTipoTratamiento.setAdapter(adaptadorTipos);
		
		spTipoTratamiento.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
				GenerarTratamientos(spTratamiento, tipos[position]);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		GenerarTratamientos(spTratamiento, tipos[0]);
		
		//Lista de padecimientos previos
		final List<ControlConsulta> previos = FiltrarPadecimientosPrevios();
		AdaptadorArrayMultiTextView<ControlConsulta> adaptadorPadecimiento = 
				new AdaptadorArrayMultiTextView<ControlConsulta>(getActivity(), android.R.layout.simple_spinner_item,
						previos, new String[]{ControlConsulta.ID_CONSULTA}, new int[]{android.R.id.text1});
		adaptadorPadecimiento.setViewBinder(new ObjectViewBinder<ControlConsulta>() {
			@Override
			public boolean setViewValue(View viewDestino, String metodoInvocarDestino,
					ControlConsulta origen, String atributoOrigen, Object valor, int posicion) {
				//viewDestino.setBackgroundResource(R.drawable.borde_blanco);
				TextView destino = (TextView)viewDestino;
				String titulo = Consulta.getDescripcion(getActivity(), origen.id_consulta)
					+" ("+DatosUtil.fechaHoraCorta(origen.fecha)+")";
				destino.setSingleLine(false);
				destino.setText(titulo);
				return true;
			}
		});
		spPadecimiento.setAdapter(adaptadorPadecimiento);
		spPadecimiento.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
				for(int i=0;i<consultas.size();i++)
					if(consultas.get(i)._id == ((ControlConsulta)spPadecimiento.getSelectedItem()).id_consulta)
						spConsultas.setSelection(i);
			}
			@Override public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		//Checkbox para ver o no ver padecimientos previos
		chkPrimeraVez.setEnabled(previos.size()!=0);
		chkPrimeraVez.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				spPadecimiento.setVisibility(isChecked ? View.GONE : View.VISIBLE);
				buttonView.setText(isChecked ? R.string.primera_vez : R.string.secuencial );
				if(!isChecked) //Seleccionamos según padecimiento
					for(int i=0;i<consultas.size();i++)
						if(consultas.get(i)._id == ((ControlConsulta)spPadecimiento.getSelectedItem()).id_consulta)
							spConsultas.setSelection(i);
			}
		});
		
		//Cancelación manual
		Button btnCancelar=(Button)vista.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Cerrar(ControlConsultasNuevo.RESULT_CANCELAR);
			}
		});
		
		//Agregar
		Button btnAgregar = (Button) vista.findViewById(R.id.btnAgregar);
		btnAgregar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(spConsultas.getSelectedItem() == null)return;
				if(spTratamiento.getSelectedItem() == null)return;
				
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
						ControlConsulta consulta = new ControlConsulta();
						consulta.id_persona = p.id;
						consulta.id_consulta = ((Consulta)spConsultas.getSelectedItem())._id;
						/*consulta.id_invitado = sesion.getUsuarioInvitado() != null ? 
								sesion.getUsuarioInvitado()._id : null;*/
						consulta.id_asu_um = aplicacion.getUnidadMedica();
						consulta.fecha = DatosUtil.getAhora();
						consulta.id_tratamiento = ((Tratamiento)spTratamiento.getSelectedItem()).id;
						if(chkPrimeraVez.isChecked())
							consulta.grupo_fecha_secuencial = consulta.fecha;
						else 
							consulta.grupo_fecha_secuencial = ((ControlConsulta)spPadecimiento.getSelectedItem()).grupo_fecha_secuencial;
						
						sesion.getDatosPacienteActual().consultas.add(consulta);
						//En bd
						int ICA = ContenidoControles.ICA_CONTROLCONSULTA_INSERTAR;
						try {
							ControlConsulta.AgregarNuevoControlConsulta(getActivity(), consulta);
							Bitacora.AgregarRegistro(getActivity(), sesion.getUsuario()._id, 
									ICA, "paciente:"+p.id+", consulta:"+consulta.id_consulta);
						} catch (Exception e) {
							ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
									ICA, e.toString());
							e.printStackTrace();
						}
						//Si no funcionara el guardado generamos un pendiente
						PendientesTarjeta pendiente = new PendientesTarjeta();
						pendiente.id_persona = p.id;
						pendiente.tabla = ControlConsulta.NOMBRE_TABLA;
						pendiente.registro_json = DatosUtil.CrearStringJson(consulta);
						// Por default pedimos una TES al usuario en un diálogo modal
						DialogoTes.IniciarNuevo(ControlConsultasNuevo.this,
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
	 * Genera tratamientos a visualizar según el tipo especificado
	 * @param sp Spinner que será alimentado con resultados
	 * @param tipo El tipo de tratamiento a filtrar
	 */
	private void GenerarTratamientos(Spinner sp, String tipo){
		AdaptadorArrayMultiTextView<Tratamiento> adaptador = 
				new AdaptadorArrayMultiTextView<Tratamiento>(getActivity(), 
						android.R.layout.simple_dropdown_item_1line,
						Tratamiento.getTratamientosConTipo(getActivity(), tipo),
						new String[]{Tratamiento.DESCRIPCION}, new int[]{android.R.id.text1});
		sp.setAdapter(adaptador);
	}
	

	/**
	 * Filtra el histórico de grupos de padecimientos del paciente devolviendo
	 * el último miembro de cada grupo que cumpla con el límite de días.
	 * Ej. si tenemos 20 padecimientos los cuales se dividen en 6 grupos, se devolverán
	 * los controles más recientes de cada uno de esos 6 grupos, siempre que la
	 * fecha del control final a devolver de cada grupo esté en el rango de días permitido.
	 */
	private List<ControlConsulta> FiltrarPadecimientosPrevios(){
		int dias = aplicacion.getFiltroDiasAntiguedadConsultas();
		DateTime limite = DateTime.now().minusDays(dias);
		
		List<ControlConsulta> filtradosFecha = new ArrayList<ControlConsulta>();
		//Filtramos primero por fecha (están ordenados de más viejo a más nuevo)
		for(ControlConsulta control : sesion.getDatosPacienteActual().consultas)
			if( DatosUtil.parsearFechaHora(control.fecha).isAfter(limite))
				filtradosFecha.add(control);
		
		List<ControlConsulta> ultimosDeCadaGrupo = new ArrayList<ControlConsulta>();
		List<String> gruposEncontrados = new ArrayList<String>(); //para determinar si un grupo ya se usó
		//Ahora filtramos sacando el último elemento de cada grupo existente
		for(int i=filtradosFecha.size()-1; i>=0;i--) //están ordenados de más viejo a más nuevo
			if(!gruposEncontrados.contains(filtradosFecha.get(i).grupo_fecha_secuencial)){
				gruposEncontrados.add(filtradosFecha.get(i).grupo_fecha_secuencial);
				ultimosDeCadaGrupo.add(filtradosFecha.get(i));
			}

		//Si hubieran subsecuentes iguales en distintos tiempos, dejamos el más reciente
		List<ControlConsulta> sinRepetir = new ArrayList<ControlConsulta>();
		List<Integer> idsEncontrados = new ArrayList<Integer>();
		//Recorremos aquí normal pues los datos ya están ordenados de más nuevo a más viejo
		for(ControlConsulta consulta : ultimosDeCadaGrupo)
			if(!idsEncontrados.contains(consulta.id_consulta)){
				idsEncontrados.add(consulta.id_consulta);
				sinRepetir.add(consulta);
			}
		return sinRepetir;
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
			if(resultCode == DialogoTes.RESULT_CANCELAR)Cerrar(ControlConsultasNuevo.RESULT_CANCELAR);
			else if(resultCode == DialogoTes.RESULT_OK)Cerrar(ControlConsultasNuevo.RESULT_OK);
			break;
		}
	}
	
}//fin clase
