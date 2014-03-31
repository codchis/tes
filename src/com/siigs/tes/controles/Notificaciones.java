package com.siigs.tes.controles;

import java.util.List;

import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Notificacion;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;
import com.siigs.tes.ui.ObjectViewBinder;
import com.siigs.tes.ui.WidgetUtil;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class Notificaciones extends Fragment {
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public Notificaciones(){}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_notificaciones, container, false);
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver, R.string.notificaciones, 
				R.string.ayuda_notificaciones, getFragmentManager());
		
		List<Notificacion> datos = Notificacion.getNotificacioones(getActivity());
		AdaptadorArrayMultiTextView<Notificacion> adaptador = new AdaptadorArrayMultiTextView<Notificacion>(
				getActivity(), R.layout.fila_notificaciones, datos,
				new String[]{Notificacion.TITULO, Notificacion.CONTENIDO, Notificacion.FECHA_INICIO}, 
				new int[]{R.id.txtTitulo, R.id.txtContenido, R.id.txtFechas});
		adaptador.setViewBinder(binder);
		
		ListView lsContenido = (ListView)rootView.findViewById(R.id.lvContenido);
		lsContenido.setAdapter(adaptador);
		lsContenido.setEmptyView(rootView.findViewById(R.id.txtSinResultados));

		return rootView;
	}
	
	ObjectViewBinder<Notificacion> binder = new ObjectViewBinder<Notificacion>(){
		@Override
		public boolean setViewValue(View viewDestino,
				String metodoInvocarDestino, Notificacion origen,
				String atributoOrigen, Object valor, int posicion) {

			if(viewDestino.getId()==R.id.txtFechas){
				((TextView)viewDestino).setText("De: "+DatosUtil.fechaHoraCorta(origen.fecha_inicio)
						+" a " + DatosUtil.fechaHoraCorta(origen.fecha_fin));
				return true;
			}
			return false;
		}
	};
		
	
}//fin clase
