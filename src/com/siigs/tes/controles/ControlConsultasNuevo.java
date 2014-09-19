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
import com.siigs.tes.datos.tablas.CategoriaCie10;
import com.siigs.tes.datos.tablas.Consulta;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.Tratamiento;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.ListaSimple;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;;

/**
 * @author Axel
 *
 */
public class ControlConsultasNuevo extends DialogFragment {

	public static final String TAG= ControlConsultasNuevo.class.getSimpleName();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;	

	private static final int SPINNER_SIN_SELECCION = -1;
	
	private TesAplicacion aplicacion;
	private Sesion sesion;

	private List<CategoriaCie10> categorias=null; //Las categorías fijas de la primera lista
	private Spinner spCategoria = null;
	private Spinner spConsulta = null;	
	
	private List<Tratamiento> TratamientosSeleccionados=null;
	private ListaSimple lsTratamientos = null;
	
	//Bandera para hacer posible selección en cascada automática de Categoría->Afección
	//Debido a que las funciones OnItemSelected no son llamadas inmediatamente al ejecutar Spinner.setSelection()
	//se manda a llamar manualmente la función GenerarAfecciones() del OnItemSelected de spCategoria
	//pero debido a que posteriormente spCategoria mandará a llamar su OnItemSelected por su cuenta, esta bandera
	//es usada para distinguir cuándo se debe y no ejecutar el contenido de GenerarAfecciones()
	private boolean puedeGenerarAfecciones = true;
	
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
		spCategoria = (Spinner)vista.findViewById(R.id.spCategoria);
		spConsulta = (Spinner)vista.findViewById(R.id.spConsulta);
		final Spinner spTipoTratamiento = (Spinner)vista.findViewById(R.id.spTipoTratamiento);
		final Spinner spTratamiento = (Spinner)vista.findViewById(R.id.spTratamiento);
		final Spinner spPadecimientoPrevio = (Spinner)vista.findViewById(R.id.spPadecimiento);
		final CheckBox chkPrimeraVez = (CheckBox)vista.findViewById(R.id.chkPrimeraVez);
		final TextView txtBuscarAfeccion = (TextView)vista.findViewById(R.id.txtBuscarAfeccion);
		lsTratamientos = (ListaSimple)vista.findViewById(R.id.lsTratamientos);
		
		if(TratamientosSeleccionados == null)
			TratamientosSeleccionados = new ArrayList<Tratamiento>();
		
		//Lista de categorías
		categorias = CategoriaCie10.getActivas(getActivity());
		AdaptadorArrayMultiTextView<CategoriaCie10> adaptadorCategorias =
				new AdaptadorArrayMultiTextView<CategoriaCie10>(
				getActivity(), android.R.layout.simple_dropdown_item_1line, categorias,
				new String[]{CategoriaCie10.DESCRIPCION}, new int[]{android.R.id.text1});
		spCategoria.setAdapter(adaptadorCategorias);
		
		spCategoria.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> av, View view, int position, long id){
				GenerarAfecciones(spConsulta, categorias.get(position));
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0){}
		});
		
		if(categorias.size()>0)
			GenerarAfecciones(spConsulta, categorias.get(0));
		
		//Opción de búsqueda de afecciones
		txtBuscarAfeccion.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				// Diálogo de búsqueda de afecciones
				DialogoBuscarAfeccion.CrearNuevo(ControlConsultasNuevo.this);
				//Este diálogo avisará su fin en onActivityResult() de llamador
			}
		});
		
		
		//Lista de tipos de tratamiento
		final String[] tiposTratamiento = Tratamiento.getTipos(getActivity(), aplicacion.getNivelAtencion());
		ArrayAdapter<String> adaptadorTipos = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_dropdown_item_1line,android.R.id.text1, tiposTratamiento);
		spTipoTratamiento.setAdapter(adaptadorTipos);
		
		spTipoTratamiento.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
				GenerarTratamientos(spTratamiento, tiposTratamiento[position]);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		if(tiposTratamiento.length>0)
			GenerarTratamientos(spTratamiento, tiposTratamiento[0]);
		
		//Selección de tratamiento
		spTratamiento.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
				Tratamiento nuevo = (Tratamiento)av.getItemAtPosition(position);
				if(nuevo.id == SPINNER_SIN_SELECCION)return;
				for(Tratamiento listado : TratamientosSeleccionados)
					if(listado.id == nuevo.id)
						return;
				TratamientosSeleccionados.add(nuevo);
				GenerarTratamientosSeleccionados();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
			
		});
		GenerarTratamientosSeleccionados();
		
		//Lista de padecimientos previos
		final List<ControlConsulta> previos = FiltrarPadecimientosPrevios();
		AdaptadorArrayMultiTextView<ControlConsulta> adaptadorPadecimiento =
				new AdaptadorArrayMultiTextView<ControlConsulta>(getActivity(), android.R.layout.simple_spinner_dropdown_item,
						previos, new String[]{ControlConsulta.CLAVE_CIE10}, new int[]{android.R.id.text1});
		adaptadorPadecimiento.setViewBinder(new ObjectViewBinder<ControlConsulta>() {
			@Override
			public boolean setViewValue(View viewDestino, String metodoInvocarDestino,
					ControlConsulta origen, String atributoOrigen, Object valor, int posicion) {
				//viewDestino.setBackgroundResource(R.drawable.borde_blanco);
				TextView destino = (TextView)viewDestino;
				String titulo = Consulta.getDescripcion(getActivity(), origen.clave_cie10)
					+"\n("+DatosUtil.fechaHoraCorta(origen.fecha)+")";
				destino.setSingleLine(false);
				destino.setText(titulo);
				return true;
			}
		});
		spPadecimientoPrevio.setAdapter(adaptadorPadecimiento);
		spPadecimientoPrevio.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
				//Seleccionamos el cie10 previo en cascada Categoría->Afección(cie10/consulta)
				String idCie10 = ((ControlConsulta)spPadecimientoPrevio.getSelectedItem()).clave_cie10;
				SeleccionarAfeccionEnCascada(idCie10);
			}
			@Override public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		//Checkbox para ver o no ver padecimientos previos
		chkPrimeraVez.setEnabled(previos.size()!=0);
		chkPrimeraVez.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				spPadecimientoPrevio.setVisibility(isChecked ? View.GONE : View.VISIBLE);
				buttonView.setText(isChecked ? R.string.primera_vez : R.string.secuencial );
				if(!isChecked){ //Seleccionamos según padecimiento
					//Seleccionamos el cie10 previo en cascada Categoría->Afección(cie10/consulta)
					String idCie10 = ((ControlConsulta)spPadecimientoPrevio.getSelectedItem()).clave_cie10;
					SeleccionarAfeccionEnCascada(idCie10);
				}
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
		
		final LinearLayout llAfeccion = (LinearLayout) vista.findViewById(R.id.llAfeccion);
		final LinearLayout llTratamientos = (LinearLayout) vista.findViewById(R.id.llTratamientos);
		final TextView txtAfeccion = (TextView)vista.findViewById(R.id.txtAfeccion);
		llTratamientos.setVisibility(View.GONE);
		
		//Siguiente
		Button btnSiguiente=(Button)vista.findViewById(R.id.btnSiguiente);
		btnSiguiente.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(spConsulta.getSelectedItem() == null)return;
				txtAfeccion.setText(((Consulta)spConsulta.getSelectedItem()).descripcion);
				llAfeccion.setVisibility(View.GONE);
				llTratamientos.setVisibility(View.VISIBLE);
			}
		});
		
		//Atras
		Button btnAtras=(Button)vista.findViewById(R.id.btnAtras);
		btnAtras.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				llAfeccion.setVisibility(View.VISIBLE);
				llTratamientos.setVisibility(View.GONE);
			}
		});
		
		//Agregar
		Button btnAgregar = (Button) vista.findViewById(R.id.btnAgregar);
		btnAgregar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(spConsulta.getSelectedItem() == null)return;
				if(TratamientosSeleccionados.size()<=0){
					Toast.makeText(getActivity(), "Debe agregar medicamentos", Toast.LENGTH_SHORT).show();
					return;
				}
				
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
						consulta.clave_cie10 = ((Consulta)spConsulta.getSelectedItem()).id_cie10;
						/*consulta.id_invitado = sesion.getUsuarioInvitado() != null ? 
								sesion.getUsuarioInvitado()._id : null;*/
						consulta.id_asu_um = aplicacion.getUnidadMedica();
						consulta.fecha = DatosUtil.getAhora();
						//Generamos lista de tratamientos
						String lista = "";
						for(Tratamiento tratamiento : TratamientosSeleccionados)
							lista += (lista.equals("") ? "" : ",") + tratamiento.id;
						consulta.id_tratamiento = lista;

						if(chkPrimeraVez.isChecked())
							consulta.grupo_fecha_secuencial = consulta.fecha;
						else 
							consulta.grupo_fecha_secuencial = ((ControlConsulta)spPadecimientoPrevio.getSelectedItem()).grupo_fecha_secuencial;
						
						sesion.getDatosPacienteActual().consultas.add(consulta);
						//En bd
						int ICA = ContenidoControles.ICA_CONTROLCONSULTA_INSERTAR;
						try {
							ControlConsulta.AgregarNuevoControlConsulta(getActivity(), consulta);
							Bitacora.AgregarRegistro(getActivity(), sesion.getUsuario()._id, 
									ICA, "paciente:"+p.id+", consulta:"+consulta.clave_cie10);
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
	 * Genera una lista de tratamientos aplicados en este control de consultas
	 */
	private void GenerarTratamientosSeleccionados(){
		AdaptadorArrayMultiTextView<Tratamiento> adaptador = 
				new AdaptadorArrayMultiTextView<Tratamiento>(getActivity(), 
						R.layout.fila_alergias, TratamientosSeleccionados,
						new String[]{Tratamiento.ID, Tratamiento.DESCRIPCION}, 
						new int[]{R.id.imgIcono, android.R.id.text1});
		adaptador.setViewBinder(binderTratamientoSeleccionado);
		lsTratamientos.setAdaptador(adaptador);
	}

	/**
	 * Genera tratamientos a visualizar según el tipo especificado
	 * @param sp Spinner que será alimentado con resultados
	 * @param tipo El tipo de tratamiento a filtrar
	 */
	private void GenerarTratamientos(Spinner sp, String tipo){
		List<Tratamiento> tratamientos = 
				Tratamiento.getTratamientosConTipo(getActivity(), tipo, aplicacion.getNivelAtencion());
		Tratamiento sinSeleccion = new Tratamiento();
		sinSeleccion.descripcion = "-- Seleccione --";
		sinSeleccion.id = SPINNER_SIN_SELECCION;
		tratamientos.add(0, sinSeleccion);
		AdaptadorArrayMultiTextView<Tratamiento> adaptador = 
				new AdaptadorArrayMultiTextView<Tratamiento>(getActivity(), 
						android.R.layout.simple_dropdown_item_1line,
						tratamientos,
						new String[]{Tratamiento.DESCRIPCION}, new int[]{android.R.id.text1});
		sp.setAdapter(adaptador);
	}
	
	/**
	 * Genera afecciones (cie10) a visualizar según la categoría especificada
	 * @param sp Spinner que será alimentado con resultados
	 * @param categoria La categoria para filtrar
	 */
	private void GenerarAfecciones(Spinner sp, CategoriaCie10 categoria){
		if(!puedeGenerarAfecciones){puedeGenerarAfecciones=true;return;}
		AdaptadorArrayMultiTextView<Consulta> adaptador = 
				new AdaptadorArrayMultiTextView<Consulta>(getActivity(), 
						android.R.layout.simple_dropdown_item_1line,
						Consulta.getConsultasConCategoria(getActivity(), categoria.id),
						new String[]{Consulta.DESCRIPCION}, new int[]{android.R.id.text1});
		sp.setAdapter(adaptador);
	}
	
	/**
	 * Selecciona el cie10 en cascada: Categoría -> Afección(cie10/consulta)
	 * @param idCie10 Afección a seleccionar en spConsulta
	 */
	private void SeleccionarAfeccionEnCascada(String idCie10){
		String idCategoria = Consulta.getCategoria(getActivity(), idCie10);
		for(int i=0; i<categorias.size(); i++)
			if(categorias.get(i).id.equals(idCategoria) ){
				spCategoria.setSelection(i); //setSelection(i, true) solo funcionaba una vez
				CategoriaCie10 dummy = new CategoriaCie10();dummy.id=idCategoria;
				GenerarAfecciones(spConsulta, dummy);
				puedeGenerarAfecciones = false; //Como spCategoría llamará a GenerarAfecciones(), indicamos que la llamada no debe ejecutar
				SpinnerAdapter adConsulta = spConsulta.getAdapter();
				for(int k=0; k<adConsulta.getCount(); k++)
					if( ((Consulta)adConsulta.getItem(k)).id_cie10.equals(idCie10) )
						spConsulta.setSelection(k);
			}
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
		List<String> idsEncontrados = new ArrayList<String>();
		//Recorremos aquí normal pues los datos ya están ordenados de más nuevo a más viejo
		for(ControlConsulta consulta : ultimosDeCadaGrupo)
			if(!idsEncontrados.contains(consulta.clave_cie10)){
				idsEncontrados.add(consulta.clave_cie10);
				sinRepetir.add(consulta);
			}
		return sinRepetir;
	}
	
	private ObjectViewBinder<Tratamiento> binderTratamientoSeleccionado = new ObjectViewBinder<Tratamiento>(){
		@Override
		public boolean setViewValue(View viewDestino, String metodoInvocarDestino, Tratamiento origen,
				String atributoOrigen, Object valor, final int posicion) {

			if(atributoOrigen.equals(Tratamiento.DESCRIPCION)){
				//Se pone color aquí pero pudo ser en cualquier columna
				int fondo = 0;
				if(posicion % 2 == 0)
					fondo = R.drawable.selector_fila_tabla;
				else fondo = R.drawable.selector_fila_tabla_alterno;
					((LinearLayout)viewDestino.getParent()).setBackgroundResource(fondo);
				return false; //Para que adaptador asigne el texto
			}else if(atributoOrigen.equals(Tratamiento.ID)){
				ImageView boton = (ImageView)viewDestino;
				boton.setBackgroundResource(R.drawable.borrar);
				boton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						TratamientosSeleccionados.remove(posicion);
						lsTratamientos.invalidate(); //Útil cuando se borra todo
						GenerarTratamientosSeleccionados();
					}
				});
				return true;
			}
			return false;
		}
	};
	
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
			
		case DialogoBuscarAfeccion.REQUEST_CODE:
			if(resultCode == DialogoBuscarAfeccion.RESULT_OK){
				String idAfeccion = data.getStringExtra(DialogoBuscarAfeccion.SALIDA_ID_AFECCION);
				SeleccionarAfeccionEnCascada(idAfeccion);
			}
		}
	}
	
}//fin clase
