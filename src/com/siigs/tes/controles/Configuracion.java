package com.siigs.tes.controles;

import com.siigs.tes.DialogoAyuda;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.ui.WidgetUtil;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Configuracion extends Fragment {
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public Configuracion(){}

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
				R.layout.controles_configuracion, container, false);

		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver, R.string.configuracion, 
				R.string.ayuda_configuracion, getFragmentManager());
		
		final TextView txtUrl = (TextView)rootView.findViewById(R.id.txtUrl);
		txtUrl.setText(aplicacion.getUrlSincronizacion());
		
		/*final TextView txtIras = (TextView)rootView.findViewById(R.id.txtFiltroIras);
		txtIras.setText(aplicacion.getFiltroDiasAntiguedadIras()+"");
		
		final TextView txtEdas = (TextView)rootView.findViewById(R.id.txtFiltroEdas);
		txtEdas.setText(aplicacion.getFiltroDiasAntiguedadEdas()+"");*/
		
		final TextView txtConsultas = (TextView)rootView.findViewById(R.id.txtFiltroConsultas);
		txtConsultas.setText(aplicacion.getFiltroDiasAntiguedadConsultas()+"");
		
		//Botón actualizar
		Button btnActualizar=(Button)rootView.findViewById(R.id.btnActualizar);
		btnActualizar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int diasIras, diasEdas, diasConsultas;
				try{
					/*diasIras = Integer.parseInt(txtIras.getText().toString());
					diasEdas = Integer.parseInt(txtEdas.getText().toString());*/
					diasConsultas = Integer.parseInt(txtConsultas.getText().toString());
				}catch(Exception e){
					Toast.makeText(getActivity(), "Por favor introduzca número correcto de días visibles", Toast.LENGTH_LONG).show();
					return;
				}
				aplicacion.setUrlSincronizacion(txtUrl.getText().toString());
				/*aplicacion.setFiltroDiasAntiguedadIras(diasIras);
				aplicacion.setFiltroDiasAntiguedadEdas(diasEdas);*/
				aplicacion.setFiltroDiasAntiguedadConsultas(diasConsultas);
				Toast.makeText(getActivity(), "Datos guardados", Toast.LENGTH_SHORT).show();			
			}
		});
		
		
		return rootView;
	}
		
}//fin clase
