/**
 * 
 */
package com.siigs.tes.controles;


import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.Ira;
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
public class ControlIras extends Fragment {

	private static final String TAG = ControlIras.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private ListaSimple lsIras = null;
		
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlIras() {
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
				R.layout.controles_atencion_control_iras, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verIras = (LinearLayout)rootView.findViewById(R.id.accion_ver_iras);
		if(sesion.tienePermiso(ContenidoControles.ICA_IRA_VER))
			verIras.setVisibility(View.VISIBLE); else verIras.setVisibility(View.GONE);
		
		LinearLayout agregarIra = (LinearLayout)rootView.findViewById(R.id.accion_agregar_ira);
		if(sesion.tienePermiso(ContenidoControles.ICA_IRA_INSERTAR))
			agregarIra.setVisibility(View.VISIBLE); else agregarIra.setVisibility(View.GONE);
		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_iras, "Ver Control de IRAs", 
				R.string.ayuda_ver_iras, getFragmentManager());

		
		//VER CONTROL
		lsIras = (ListaSimple)rootView.findViewById(R.id.lsIras); 
		GenerarIras();
		
		
		//AGREGAR CONTROL
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_ira, R.string.agregar_ira, 
				R.string.ayuda_boton_agregar_ira, getFragmentManager());
		
		Button btnAgregarIra = (Button)rootView.findViewById(R.id.btnAgregarIra);
		btnAgregarIra.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Diálogo de nueva ira
				ControlIrasNuevo dialogo=new ControlIrasNuevo();
				Bundle args = new Bundle();
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlIras.this, ControlIrasNuevo.REQUEST_CODE);
				dialogo.show(ControlIras.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlIrasNuevo.TAG);
				//Este diálogo avisará su fin en onActivityResult() de llamador
			}
		});
		
		return rootView;
	}
	
	private void GenerarIras(){
		AdaptadorArrayMultiTextView<ControlIra> adaptador = new AdaptadorArrayMultiTextView<ControlIra>(
				getActivity(), R.layout.fila_comun_para_ira_eda_accion_consulta,
				sesion.getDatosPacienteActual().iras, 
				new String[]{ControlIra.FECHA, ControlIra.ID_ASU_UM, ControlIra.ID_IRA, ControlIra.ID_TRATAMIENTO},
				new int[]{R.id.txtFecha, R.id.txtUM, R.id.txtDetalle, R.id.txtTratamiento});
		adaptador.setViewBinder(binderFila);
		lsIras.setAdaptador(adaptador);
	}

	private ObjectViewBinder<ControlIra> binderFila = new ObjectViewBinder<ControlIra>(){
		@Override
		public boolean setViewValue(View viewDestino, String metodoInvocarDestino, ControlIra origen,
				String atributoOrigen, Object valor, int posicion) {
			TextView destino = (TextView)viewDestino;
			if(atributoOrigen.equals(ControlIra.FECHA)){
				destino.setText(DatosUtil.fechaHoraCorta(valor.toString()));
				//Se pone color aquí pero pudo ser en cualquier columna
				int fondo = 0;
				if(posicion % 2 == 0)
					fondo = R.drawable.selector_fila_tabla;
				else fondo = R.drawable.selector_fila_tabla_alterno;
					((LinearLayout)viewDestino.getParent()).setBackgroundResource(fondo);
				return true;
			}else if(atributoOrigen.equals(ControlIra.ID_ASU_UM)){
				destino.setText(ArbolSegmentacion.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				return true;
			}else if(atributoOrigen.equals(ControlIra.ID_IRA)){
				destino.setText(Ira.getDescripcion(getActivity(), 
						Integer.parseInt(valor.toString())));
				TextView clave = (TextView)destino.getRootView().findViewById(R.id.txtClave);
				if(clave!=null)
					clave.setText(Ira.getClave(getActivity(), origen.id_ira));
				return true;
			}else if(atributoOrigen.equals(ControlIra.ID_TRATAMIENTO)){
				destino.setText(Tratamiento.getDescripcion(getActivity(), origen.id_tratamiento));
				return true;
			}
			return false;
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlIrasNuevo.REQUEST_CODE:
			//if(resultCode==ControlIrasNuevo.RESULT_OK){
				GenerarIras();
			//}
			break;
		}
	}

	
	
	
}//fin clase
