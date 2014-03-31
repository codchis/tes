/**
 * 
 */
package com.siigs.tes.controles;

import java.util.List;

import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.EsquemaIncompleto;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.ReglaVacuna;
import com.siigs.tes.datos.tablas.Vacuna;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.ListaSimple;
import com.siigs.tes.ui.ObjectViewBinder;
import com.siigs.tes.ui.WidgetUtil;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Axel
 *
 */
public class ControlVacunas extends Fragment {

	private static final String TAG = ControlVacunas.class.getSimpleName();
	
	private static final String PREFIJO_VACUNA = "id_vacuna_"; //Para localizar celdas en tabla
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private View tablaEsquema; //Contiene las celdas a marcar
	
	private ListaSimple lsOtrasVacunas;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlVacunas() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setRetainInstance(true);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_atencion_control_vacunas, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verEsquema = (LinearLayout)rootView.findViewById(R.id.accion_ver_esquema);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLVACUNA_VER))
			verEsquema.setVisibility(View.VISIBLE); else verEsquema.setVisibility(View.GONE);
		
		LinearLayout agregarVacuna = (LinearLayout)rootView.findViewById(R.id.accion_agregar_vacuna);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLVACUNA_INSERTAR))
			agregarVacuna.setVisibility(View.VISIBLE); else agregarVacuna.setVisibility(View.GONE);
		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_esquema, "Ver Esquema de Vacunación", 
				R.string.ayuda_ver_vacunas, getFragmentManager());

		//Marcado de vacunas en tablaEsquema
		tablaEsquema = rootView.findViewById(R.id.tablaEsquema);
		lsOtrasVacunas = (ListaSimple)rootView.findViewById(R.id.lsOtrasVacunas);
		MarcarVacunas();
		
		//AGREGAR VACUNA
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_vacuna, R.string.agregar_vacuna, 
				R.string.ayuda_boton_agregar_vacuna, getFragmentManager());
		
		Button btnAgregarVacuna = (Button)rootView.findViewById(R.id.btnAgregarVacuna);
		btnAgregarVacuna.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AplicarVacuna(null);
			}
		});

		
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLVACUNA_INSERTAR)){
			//Asignación de click a cada celda de vacuna
			List<Vacuna> vacunas = Vacuna.getVacunasActivas(getActivity());
			for(final Vacuna vacuna : vacunas){
				View celda = tablaEsquema.findViewWithTag(PREFIJO_VACUNA+vacuna._id);
				if(celda != null){
					final String motivo = ReglaVacuna.motivoNoEsAplicableVacuna(getActivity(), 
							vacuna._id, sesion.getDatosPacienteActual());
					celda.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							if(!motivo.equals("")){
								Toast.makeText(getActivity(), motivo, Toast.LENGTH_LONG).show();
								return;
							}
							TextView opcion =(TextView)view;
							//Si no está marcada ya
							if(!opcion.getText().toString().equals(getString(R.string.MARCA_VACUNA)))
								AplicarVacuna(vacuna._id);
						}
					});
				}
			}
		}
		
		return rootView;
	}
	
	
	private void AplicarVacuna(Integer idVacuna){
		//Diálogo de nueva vacuna
		ControlVacunasNuevo dialogo=new ControlVacunasNuevo();
		Bundle args = new Bundle();
		if(idVacuna != null)args.putInt(ControlVacunasNuevo.PARAM_ID_VACUNA, idVacuna);
		dialogo.setArguments(args);
		dialogo.setTargetFragment(ControlVacunas.this, ControlVacunasNuevo.REQUEST_CODE);
		dialogo.show(ControlVacunas.this.getFragmentManager(),
				//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
				ControlVacunasNuevo.TAG);
		//Este diálogo avisará su fin en onActivityResult() de llamador
	}
	
	
	/**
	 * Marca en el esquema las vacunas aplicadas a la persona Y las que están incompletas
	 */
	private void MarcarVacunas(){
		for(ControlVacuna vacuna : sesion.getDatosPacienteActual().vacunas){
			TextView celda = (TextView)tablaEsquema.findViewWithTag(PREFIJO_VACUNA+vacuna.id_vacuna);
			if(celda == null){
				//Vacuna inexistente en vista
				ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
						ContenidoControles.ICA_CONTROLVACUNA_VER, 
						"No hay celda en esquema para id_vacuna "+vacuna.id_vacuna);
				continue;
			}
			//La vacuna debe ser marcada
			celda.setBackgroundResource(R.drawable.selector_celda_vacuna); //Estético, solo sirve si se acaba de aplicar vacuna incompleta
			celda.setText(getText(R.string.MARCA_VACUNA));
		}
	
		//Checamos vacunas que califican como incompletas
		List<EsquemaIncompleto> incompletos = 
				EsquemaIncompleto.getEsquemasIncompletoPaciente(getActivity(), 
						sesion.getDatosPacienteActual().persona.id);
		for(EsquemaIncompleto incompleto : incompletos){
			TextView celda = (TextView)tablaEsquema.findViewWithTag(PREFIJO_VACUNA+incompleto.id_vacuna);
			if(celda == null){
				ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
						ContenidoControles.ICA_CONTROLVACUNA_VER, 
						"No hay celda en esquema para poner incompleto id_vacuna "+incompleto.id_vacuna);
				continue;
			}
			//Si bd no está actualizada, esto puede pasar
			if(celda.getText().toString().equals(getText(R.string.MARCA_VACUNA)))
				continue;
			//La vacuna debe ser coloreada como incompleta
			if(incompleto.prioridad == EsquemaIncompleto.PRIORIDAD_0)
				celda.setBackgroundResource(R.drawable.selector_celda_vacuna_prioridad_0);
			else if(incompleto.prioridad == EsquemaIncompleto.PRIORIDAD_1)
				celda.setBackgroundResource(R.drawable.selector_celda_vacuna_prioridad_1);
			else
				ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
						ContenidoControles.ICA_CONTROLVACUNA_VER, 
						"La vacuna incompleta:"+incompleto.id_vacuna+" de la persona:"+incompleto.id_persona
						+" tiene una prioridad desconocida:"+incompleto.prioridad);
		}
		
		GenerarDetalleVacunas();
	}
	
	/**
	 * Genera la tabla con detalle de todas las vacunas sin importar su presencia en el esquema
	 */
	private void GenerarDetalleVacunas(){		
		AdaptadorArrayMultiTextView<ControlVacuna> adaptador = new AdaptadorArrayMultiTextView<ControlVacuna>(
				getActivity(), R.layout.fila_comun_para_ira_eda_accion_consulta,
				sesion.getDatosPacienteActual().vacunas, 
				new String[]{ControlVacuna.FECHA, ControlVacuna.ID_ASU_UM, ControlVacuna.ID_VACUNA},
				new int[]{R.id.txtFecha, R.id.txtUM, R.id.txtDetalle});
		adaptador.setViewBinder(binderFila);
		lsOtrasVacunas.setAdaptador(adaptador);
	}

	private ObjectViewBinder<ControlVacuna> binderFila = new ObjectViewBinder<ControlVacuna>(){
		@Override
		public boolean setViewValue(View viewDestino, String metodoInvocarDestino, ControlVacuna origen,
				String atributoOrigen, Object valor, int posicion) {
			TextView destino = (TextView)viewDestino;
			if(atributoOrigen.equals(ControlVacuna.FECHA)){
				destino.setText(DatosUtil.fechaHoraCorta(valor.toString()));
				//Se pone color aquí pero pudo ser en cualquier columna
				int fondo = 0;
				if(posicion % 2 == 0)
					fondo = R.drawable.selector_fila_tabla;
				else fondo = R.drawable.selector_fila_tabla_alterno;
					((LinearLayout)viewDestino.getParent()).setBackgroundResource(fondo);
				return true;
			}else if(atributoOrigen.equals(ControlVacuna.ID_ASU_UM)){
				destino.setText(ArbolSegmentacion.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				return true;
			}else if(atributoOrigen.equals(ControlVacuna.ID_VACUNA)){
				destino.setText(Vacuna.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				destino.setTypeface(destino.getTypeface(), Typeface.BOLD);
				View escondible = destino.getRootView().findViewById(R.id.txtClave);
				if(escondible!=null)
					escondible.setVisibility(View.GONE); //No se ocupa esta columna aquí
				escondible = destino.getRootView().findViewById(R.id.txtTratamiento);
				if(escondible!=null)
					escondible.setVisibility(View.GONE); //... tampoco esta
				return true;
			}
			return false;
		}
	};


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlVacunasNuevo.REQUEST_CODE:
			//if(resultCode==ControlVacunasNuevo.RESULT_OK){
				MarcarVacunas();
			//}
			break;
		}
	}

	
	
	
}//fin clase
