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
import com.siigs.tes.datos.tablas.Consulta;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.Tratamiento;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.ListaSimple;
import com.siigs.tes.ui.ObjectViewBinder;
import com.siigs.tes.ui.WidgetUtil;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author Axel
 *
 */
public class ControlConsultas extends Fragment {

	private static final String TAG = ControlConsultas.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private ListaSimple lsConsultas = null;
		
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlConsultas() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.setRetainInstance(true);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_atencion_control_consultas, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verConsultas = (LinearLayout)rootView.findViewById(R.id.accion_ver_consultas);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLCONSULTA_VER))
			verConsultas.setVisibility(View.VISIBLE); else verConsultas.setVisibility(View.GONE);
		
		LinearLayout agregarConsulta = (LinearLayout)rootView.findViewById(R.id.accion_agregar_consulta);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLCONSULTA_INSERTAR))
			agregarConsulta.setVisibility(View.VISIBLE); else agregarConsulta.setVisibility(View.GONE);
		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_consultas, "Ver Control de Consultas", 
				R.string.ayuda_ver_consultas, getFragmentManager());

		
		//VER CONTROL
		lsConsultas = (ListaSimple)rootView.findViewById(R.id.lsConsultas); 
		GenerarConsultas();
		
		
		//AGREGAR CONTROL
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_consulta, R.string.agregar_consulta, 
				R.string.ayuda_boton_agregar_consulta, getFragmentManager());
		
		Button btnAgregarConsulta = (Button)rootView.findViewById(R.id.btnAgregarConsulta);
		btnAgregarConsulta.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Diálogo de nueva consulta
				ControlConsultasNuevo dialogo=new ControlConsultasNuevo();
				Bundle args = new Bundle();
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlConsultas.this, ControlConsultasNuevo.REQUEST_CODE);
				dialogo.show(ControlConsultas.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlConsultasNuevo.TAG);
				//Este diálogo avisará su fin en onActivityResult() de llamador
			}
		});
		
		return rootView;
	}
	
	private void GenerarConsultas(){
		AdaptadorArrayMultiTextView<ControlConsulta> adaptador = new AdaptadorArrayMultiTextView<ControlConsulta>(
				getActivity(), R.layout.fila_comun_para_ira_eda_accion_consulta,
				sesion.getDatosPacienteActual().consultas, 
				new String[]{ControlConsulta.FECHA, ControlConsulta.ID_ASU_UM, ControlConsulta.CLAVE_CIE10, 
					ControlConsulta.ID_TRATAMIENTO},
				new int[]{R.id.txtFecha, R.id.txtUM, R.id.txtDetalle, 
					R.id.txtTratamiento});
		adaptador.setViewBinder(binderFila);
		lsConsultas.setAdaptador(adaptador);
	}

	private ObjectViewBinder<ControlConsulta> binderFila = new ObjectViewBinder<ControlConsulta>(){
		@Override
		public boolean setViewValue(View viewDestino, String metodoInvocarDestino, ControlConsulta origen,
				String atributoOrigen, Object valor, int posicion) {
			TextView destino = (TextView)viewDestino;
			if(atributoOrigen.equals(ControlConsulta.FECHA)){
				destino.setText(DatosUtil.fechaHoraCorta(valor.toString()));
				//Se pone color aquí pero pudo ser en cualquier columna
				int fondo = 0;
				if(posicion % 2 == 0)
					fondo = R.drawable.selector_fila_tabla;
				else fondo = R.drawable.selector_fila_tabla_alterno;
					((LinearLayout)viewDestino.getParent()).setBackgroundResource(fondo);
				return true;
			}else if(atributoOrigen.equals(ControlConsulta.ID_ASU_UM)){
				destino.setText(ArbolSegmentacion.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				return true;
			}else if(atributoOrigen.equals(ControlConsulta.CLAVE_CIE10)){
				destino.setText(Consulta.getDescripcion(getActivity(), valor.toString()) );
				TextView clave = (TextView)destino.getRootView().findViewById(R.id.txtClave);
				if(clave!=null)
					clave.setText(/*Consulta.getClave(getActivity(), origen.clave_cie10)*/origen.clave_cie10);
				return true;
			}else if(atributoOrigen.equals(ControlConsulta.ID_TRATAMIENTO)){
				List<Tratamiento> tratamientos = origen.getTratamientos(getActivity());
				String texto = "";
				for(Tratamiento tratamiento : tratamientos)
					texto += (texto.equals("") ? "" : ", ") + tratamiento.descripcion;
				destino.setText(texto);
				return true;
			}
			return false;
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlConsultasNuevo.REQUEST_CODE:
			//if(resultCode==ControlConsultasNuevo.RESULT_OK){
				GenerarConsultas();
			//}
			break;
		}
	}

	
	
}//fin clase
