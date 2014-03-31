package com.siigs.tes.controles;

import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.SincronizacionTask;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.ControlAccionNutricional;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ControlNutricional;
import com.siigs.tes.datos.tablas.ControlEda;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.ui.WidgetUtil;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Sincronizacion extends Fragment {
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private TextView txtVersion;
	private TextView txtUltimaSincronizacion;
	private TextView txtControlesSincronizar;
	private TextView txtLocalidadAsignada;
	private TextView txtActualizacionesPacientes;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public Sincronizacion(){}

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
				R.layout.controles_sincronizacion, container, false);

		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver, R.string.sincronizacion, 
				R.string.ayuda_sincronizacion, getFragmentManager());
		
		txtVersion = (TextView)rootView.findViewById(R.id.txtVersionDatos);
		txtUltimaSincronizacion = (TextView)rootView.findViewById(R.id.txtUltimaSincronizacion);
		txtControlesSincronizar = (TextView)rootView.findViewById(R.id.txtControlesSincronizar);
		txtLocalidadAsignada = (TextView)rootView.findViewById(R.id.txtLocalidad);
		txtActualizacionesPacientes = (TextView)rootView.findViewById(R.id.txtActualizacionesPacientes);

		WifiManager wifi=(WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
		String macaddress = wifi.getConnectionInfo().getMacAddress();
		if(macaddress == null)macaddress="";
		macaddress = macaddress.replace(":", "");
		((TextView)rootView.findViewById(R.id.txtMacAddress)).setText(macaddress);
		
		//Botón sincronizar
		Button btnSincronizar=(Button)rootView.findViewById(R.id.btnSincronizar);
		btnSincronizar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!((TesAplicacion)getActivity().getApplication()).hayInternet() ){
					String msg="No existe conexión a la red en este momento";
					Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
					return;
				}
				
				//Confirmación
				String mensaje ="¿En verdad desea sincronizar?";
				if(((TesAplicacion)getActivity().getApplication()).getEsInstalacionNueva())
					mensaje="Está a punto de sincronizar por primera vez. " +
							"Esta acción puede tardar más de 30 minutos y requiere de " +
							"conexión a Internet y suficiente batería\n\n¿Desea continuar?";
				AlertDialog dialogo=new AlertDialog.Builder(getActivity()).create();
				dialogo.setMessage(mensaje);
				dialogo.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {}
				});
				dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Un dialogo vacío que la sincronización puede usar para mostrar resultados
						AlertDialog dlgResultado=new AlertDialog.Builder(getActivity()).create();
						dlgResultado.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface arg0) {
								ActualizarInformacion();
							}
						});

						SincronizacionTask sinc = new SincronizacionTask(getActivity(), dlgResultado);
						sinc.execute("");
					}
				} );
				dialogo.show();
			}
		});
		
		ActualizarInformacion();
		
		return rootView;
	}
	
	/**
	 * Actualiza widgets con la información actual
	 */
	private void ActualizarInformacion(){
		txtVersion.setText(aplicacion.getVersionApk());
		String fecha = aplicacion.getFechaUltimaSincronizacion();
		txtUltimaSincronizacion.setText(DatosUtil.fechaHoraCorta(fecha));
		
		txtLocalidadAsignada.setText(
				ArbolSegmentacion.getDescripcion(getActivity(), aplicacion.getUnidadMedica()));
		
		txtActualizacionesPacientes.setText(""+Persona.getTotalActualizadosDespues(getActivity(), 
				fecha));

		int totalControles = 0;
		if(ControlAccionNutricional.getTotalCreadosDespues(getActivity(), fecha)>0)totalControles++;
		if(ControlConsulta.getTotalCreadosDespues(getActivity(), fecha)>0)totalControles++;
		if(ControlEda.getTotalCreadosDespues(getActivity(), fecha)>0)totalControles++;
		if(ControlIra.getTotalCreadosDespues(getActivity(), fecha)>0)totalControles++;
		if(ControlNutricional.getTotalCreadosDespues(getActivity(), fecha)>0)totalControles++;
		if(ControlVacuna.getTotalCreadosDespues(getActivity(), fecha)>0)totalControles++;
		txtControlesSincronizar.setText(totalControles+"");
	}
		
	
}//fin clase
