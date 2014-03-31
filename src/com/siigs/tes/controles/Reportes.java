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
import com.siigs.tes.datos.vistas.ReportesVacunas;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.ListaSimple;
import com.siigs.tes.ui.ObjectViewBinder;
import com.siigs.tes.ui.WidgetUtil;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
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
public class Reportes extends Fragment {

	private static final String TAG = Reportes.class.getSimpleName();
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private ListaSimple lsSinSincronizar = null;
	private ListaSimple lsTodos = null;
	
	private Cursor curSinSincronizar = null;
	private Cursor curTodos = null;
		
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public Reportes() {
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
				R.layout.controles_reportes, container, false);		
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo, "Reportes de desempeño", 
				R.string.ayuda_reportes, getFragmentManager());

		
		//VER CONTROL
		lsSinSincronizar = (ListaSimple)rootView.findViewById(R.id.lsSinSincronizar); 
		lsTodos = (ListaSimple)rootView.findViewById(R.id.lsTodos);
		
		curSinSincronizar = FiltrarResultados(lsSinSincronizar, aplicacion.getFechaUltimaSincronizacion(), null);
		
		curTodos = FiltrarResultados(lsTodos, null, null);
		
		return rootView;
	}
	
	private Cursor FiltrarResultados(ListaSimple lista, String fechaInicio, String fechaFin){
		Cursor cur = ReportesVacunas.getReportesVacunas(getActivity(), fechaInicio, fechaFin);
		SimpleCursorAdapter adaptador = new SimpleCursorAdapter(getActivity(), 
				R.layout.fila_reportes_vacunas, cur, 
				new String[]{ReportesVacunas.DESCRIPCION, ReportesVacunas.APLICADAS, 
					ReportesVacunas.LOTES, ReportesVacunas.SIN_LOTE}, 
				new int[]{R.id.txtVacuna, R.id.txtAplicadas, R.id.txtLotes, R.id.txtSinLote}, 0);
		adaptador.setViewBinder(miViewBinder);
		lista.setAdaptador(adaptador);
		return cur;
	}

	SimpleCursorAdapter.ViewBinder miViewBinder = new SimpleCursorAdapter.ViewBinder(){ 
		@Override
		public boolean setViewValue(View view, Cursor cur, int col) {
			if(view.getId()==R.id.txtVacuna){
				//Dentro de un if para que solo se haga una vez
				int fondo = 0;
				if(cur.getPosition() % 2 == 0)
					fondo = R.drawable.selector_fila_tabla;
				else fondo = R.drawable.selector_fila_tabla_alterno;
					((LinearLayout)view.getParent()).setBackgroundResource(fondo);
			}
			
			return false;
		}
	};
	
	@Override
	public void onPause() {
		super.onPause();
		if(curSinSincronizar != null)curSinSincronizar.close();
		if(curTodos != null)curTodos.close();
	}


	

	
	
	
}//fin clase
