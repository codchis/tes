/**
 * 
 */
package com.siigs.tes.controles;


import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.Persona;
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
public class ControlEstimulacionTemprana extends Fragment {

	private static final String TAG = ControlEstimulacionTemprana.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private ListaSimple lsEstimulaciones = null;
		
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlEstimulacionTemprana() {
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
				R.layout.controles_atencion_control_estimulacion_temprana, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verEstimulaciones = (LinearLayout)rootView.findViewById(R.id.accion_ver_estimulaciones);
		if(sesion.tienePermiso(ContenidoControles.ICA_ESTIMULACIONTEMPRANA_VER))
			verEstimulaciones.setVisibility(View.VISIBLE); else verEstimulaciones.setVisibility(View.GONE);
		
		LinearLayout agregarEstimulacion = (LinearLayout)rootView.findViewById(R.id.accion_agregar_estimulacion);
		if(sesion.tienePermiso(ContenidoControles.ICA_ESTIMULACIONTEMPRANA_INSERTAR))
			agregarEstimulacion.setVisibility(View.VISIBLE); else agregarEstimulacion.setVisibility(View.GONE);
		
				
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_estimulaciones, "Ver Estimulaciones Tempranas", 
				R.string.ayuda_ver_estimulaciones_tempranas, getFragmentManager());
		
		
		//VER CONTROL
		lsEstimulaciones = (ListaSimple)rootView.findViewById(R.id.lsEstimulaciones); 
		GenerarEstimulaciones();
		
		
		//AGREGAR CONTROL
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_estimulacion, R.string.agregar_estimulacion, 
				R.string.ayuda_boton_agregar_estimulacion_temprana, getFragmentManager());
		
		Button btnAgregarEstimulacion = (Button)rootView.findViewById(R.id.btnAgregarEstimulacion);
		btnAgregarEstimulacion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Diálogo de nueva acción
				ControlEstimulacionTempranaNuevo dialogo=new ControlEstimulacionTempranaNuevo();
				Bundle args = new Bundle();
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlEstimulacionTemprana.this, ControlEstimulacionTempranaNuevo.REQUEST_CODE);
				dialogo.show(ControlEstimulacionTemprana.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlEstimulacionTempranaNuevo.TAG);
				//Este diálogo avisará su fin en onActivityResult() de llamador
			}
		});
		
		
		return rootView;
	}
	
	
	private void GenerarEstimulaciones(){
		AdaptadorArrayMultiTextView<com.siigs.tes.datos.tablas.EstimulacionTemprana> adaptador = 
				new AdaptadorArrayMultiTextView<com.siigs.tes.datos.tablas.EstimulacionTemprana>(
						
				getActivity(), R.layout.fila_comun_para_ira_eda_accion_consulta,
				sesion.getDatosPacienteActual().estimulacionesTempranas, 
				new String[]{com.siigs.tes.datos.tablas.EstimulacionTemprana.FECHA, 
						com.siigs.tes.datos.tablas.EstimulacionTemprana.ID_ASU_UM, 
						com.siigs.tes.datos.tablas.EstimulacionTemprana.TUTOR_CAPACITADO},
				new int[]{R.id.txtFecha, R.id.txtUM, R.id.txtDetalle});
		adaptador.setViewBinder(binderFila);
		lsEstimulaciones.setAdaptador(adaptador);
	}

	private ObjectViewBinder<com.siigs.tes.datos.tablas.EstimulacionTemprana> binderFila = 
			new ObjectViewBinder<com.siigs.tes.datos.tablas.EstimulacionTemprana>(){
		@Override
		public boolean setViewValue(View viewDestino, String metodoInvocarDestino, 
				com.siigs.tes.datos.tablas.EstimulacionTemprana origen,
				String atributoOrigen, Object valor, int posicion) {
			
			TextView destino = (TextView)viewDestino;
			if(atributoOrigen.equals(com.siigs.tes.datos.tablas.EstimulacionTemprana.FECHA)){
				destino.setText(DatosUtil.fechaHoraCorta(valor.toString()));
				//Se pone color aquí pero pudo ser en cualquier columna
				int fondo = 0;
				if(posicion % 2 == 0)
					fondo = R.drawable.selector_fila_tabla;
				else fondo = R.drawable.selector_fila_tabla_alterno;
					((LinearLayout)viewDestino.getParent()).setBackgroundResource(fondo);
				return true;
			}else if(atributoOrigen.equals(com.siigs.tes.datos.tablas.EstimulacionTemprana.ID_ASU_UM)){
				destino.setText(ArbolSegmentacion.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				return true;
			}else if(atributoOrigen.equals(com.siigs.tes.datos.tablas.EstimulacionTemprana.TUTOR_CAPACITADO)){
				destino.setText(origen.tutor_capacitado == 1? "Si" : "No");
				View v = destino.getRootView().findViewById(R.id.txtClave);
				if(v!=null)v.setVisibility(View.GONE); //No se maneja clave en acciones nutricionales
				v = destino.getRootView().findViewById(R.id.txtTratamiento);
				if(v!=null)v.setVisibility(View.GONE); //... tampoco tratamiento
				return true;
			}
			return false;
		}
	};
		
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlEstimulacionTempranaNuevo.REQUEST_CODE:
			//if(resultCode==ControlAccionNutricionalNuevo.RESULT_OK){
				GenerarEstimulaciones();
			//}
			break;
		}
	}

	
	
	
}//fin clase
