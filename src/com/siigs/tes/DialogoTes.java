/**
 * Muestra un diálogo modal que intenta escuchar la presencia de una TES
 * y realiza las acciones necesarias según el caso
 */
package com.siigs.tes;

import java.io.IOException;
import java.util.List;

import com.siigs.tes.datos.ManejadorNfc;
import com.siigs.tes.datos.ManejadorNfc.LectorUsb;
import com.siigs.tes.datos.ManejadorNfc.LectorUsb.EventosLector;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.PendientesTarjeta;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * @author Axel
 *
 */
public class DialogoTes extends DialogFragment {

	public static final String TAG=DialogoTes.class.getSimpleName();
	public static final int REQUEST_CODE=123;
	public static final int RESULT_OK=0;
	public static final int RESULT_CANCELAR=-5;
	private static final String PARAM_MODO_OPERACION= "modo_operacion";
	
	/**
	 * Crea un nuevo diálogo modal de esta clase. 
	 * 
	 * Este diálogo avisará su fin en onActivityResult() de <b>llamador</b>
	 * @param llamador Fragmento que se apoderará de la salida del nuevo diálogo
	 * @param modoOperacion El modo de operación que tendrá el nuevo diálogo. LOGIN para leer la TES o GUARDAR
	 */
	public static void IniciarNuevo(Fragment llamador, ModoOperacion modoOperacion){
		IniciarNuevo(llamador, modoOperacion, null);
	}
	/**
	 * Crea un nuevo diálogo modal de esta clase. Si <b>modoOperacion</b> es de GUARDAR, <b>pendiente</b>
	 * se usará para crear un nuevo {@link PendientesTarjeta} en caso de no escribir exitosamente en la TES.
	 * 
	 * Este diálogo avisará su fin en onActivityResult() de <b>llamador</b>
	 * @param llamador Fragmento que se apoderará de la salida del nuevo diálogo
	 * @param modoOperacion El modo de operación que tendrá el nuevo diálogo. LOGIN para leer la TES o GUARDAR 
	 * @param pendiente Si no es nulo y modoOperación es GUARDAR, pendiente será insertado en base de datos si 
	 * no se logra guardar con éxito la información en la TES.
	 */
	public static void IniciarNuevo(Fragment llamador, ModoOperacion modoOperacion, PendientesTarjeta pendiente){
		//Si el usuario no es para guardado en NFC...
		if(modoOperacion==ModoOperacion.GUARDAR)
			if( !((TesAplicacion)llamador.getActivity().getApplication())
					.getSesion().getDatosPacienteActual().fueCargadoDesdeNfc()){
				//... solo guardamos como un pendiente si lo hay
				if(pendiente!=null)
					PendientesTarjeta.AgregarNuevoPendienteLocal(llamador.getActivity(), pendiente);
				//... y no iniciamos ventana pero hacemos aviso como si hubiera guardado normal.
				llamador.onActivityResult(DialogoTes.REQUEST_CODE, DialogoTes.RESULT_OK, null);
				return;
				}
		
		DialogoTes dialogo=new DialogoTes();
		Bundle args = new Bundle();
		args.putSerializable(DialogoTes.PARAM_MODO_OPERACION, modoOperacion);
		dialogo.setArguments(args);
		dialogo.setTargetFragment(llamador, DialogoTes.REQUEST_CODE);
		dialogo.setPendienteTarjeta(pendiente);
		dialogo.show(llamador.getFragmentManager(),
				//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
				DialogoTes.TAG);
		//Este diálogo avisará su fin en onActivityResult() de llamador
	}
	
	//Modos de operación de este diálogo
	public static enum ModoOperacion {LOGIN, GUARDAR}
	private ModoOperacion modoOperacion;

	//NFC Nativo
	private NfcAdapter adaptadorNFC = null;
	private PendingIntent pendingIntentNFC;
	private IntentFilter writeTagFiltersNFC[];
	
	//NFC Usb
	private LectorUsb miLectorUsb = null;
	private String idTarjetaLeida = null;
	private boolean escribirUsbConNdef = true; //Indica que la escritura en lector Usb será en formato Ndef
	
	//Para generación de pendientes
	private PendientesTarjeta pendiente = null; //Pendiente que mandaría a escribir
	private List<PendientesTarjeta> pendientesResueltos = null; //En login esta lista podría recibir valores
	private View txtPasarTesDeNuevo=null;
	
	private TesAplicacion aplicacion;
	
	/**
	 * Usado para comunicarse con actividad contenedora la cual DEBE implementar
	 * esta interface
	 * @author Axel
	 *
	 */
	public interface Callbacks {
		/**
		 * Usado para avisar que esta instancia de {@link DialogoTes} existe y que
		 * requiere ser avisada cuando actividad contenedora detecte un tag NFC
		 * @param llamador {@link DialogoTes} que solicita recibir tags NFC
		 */
		public void onIniciarDialogoTes(DialogoTes llamador);
		/**
		 * Usado apra avisar a actividad contenedora que ya no es necesario
		 * dar avisos de tags NFC
		 * @param llamador {@link DialogoTes} que hace la llamada
		 */
		public void onDetenerDialogoTes(DialogoTes llamador);
	}
	
	private Callbacks miCallback = null;
	
	//Constructor requerido
	public DialogoTes(){}
	
	public void setPendienteTarjeta(PendientesTarjeta pendiente){this.pendiente = pendiente;}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		miCallback = (Callbacks)activity;
		miCallback.onIniciarDialogoTes(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if(miCallback!=null)
			miCallback.onDetenerDialogoTes(this);
		miCallback = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Estílo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//Método 2 para hacer la ventana modal 
		setCancelable(false);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		
		//INICIA MODO ESCUCHA NFC NATIVO REDIRIGIENDO A ACTIVIDAD PADRE EN CASO DE ENCONTRAR ALGO
		try{
			adaptadorNFC = NfcAdapter.getDefaultAdapter(getActivity());
		}catch(Exception e){
			String msg = "No es posible iniciar la antena NFC. Asegúrese de que esta tableta cuenta con NFC" +
					" y que se encuentre activo en las opciones de su tableta";
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
		}
		pendingIntentNFC = PendingIntent.getActivity(getActivity(), 0, 
				new Intent(getActivity(), getActivity().getClass())
					.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter tagDetectada = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		tagDetectada.addCategory(Intent.CATEGORY_DEFAULT);
		writeTagFiltersNFC = new IntentFilter[] { tagDetectada };
		
		
		//INICIA LECTOR USB
		EventosLector eventosUsb = new EventosLector() {
			@Override
			public void onGetIdTarjeta(LectorUsb lector, String idTarjeta) {
				if(idTarjeta != null){
					idTarjetaLeida = idTarjeta;
					//miLectorUsb.msg("se recibió id tarjeta:'"+idTarjeta+ "'");
					
					if(modoOperacion == ModoOperacion.LOGIN){
						try {
							miLectorUsb.LeerTarjeta(idTarjeta);
						} catch (IOException e) {
							String msg = "No puede leer la tarjeta USB: "+e.getMessage() + ":\n"+e.toString();
							miLectorUsb.msg(msg, false);
							ErrorSis.AgregarError(getActivity(), aplicacion.getSesion().getUsuario()._id, 
								ErrorSis.ERROR_DESCONOCIDO, "Usb-LlamarLeerTarjeta:"+msg);
						}
					}else{ //Escritura común de tarjeta
						try {
							miLectorUsb.EscribirTarjeta(idTarjeta, aplicacion.getSesion().getDatosPacienteActual(), escribirUsbConNdef);
							//Resultado llegará a onEscritirTarjeta()
						} catch (IOException e) {
							String msg = "No puede escribir la tarjeta USB: "+e.getMessage() + ":\n"+e.toString();
							miLectorUsb.msg(msg, false);
							ErrorSis.AgregarError(getActivity(), aplicacion.getSesion().getUsuario()._id, 
								ErrorSis.ERROR_DESCONOCIDO, "Usb-LlamarEscribirTarjeta:"+msg);
							return;
						}
					}
				}else{
					miLectorUsb.msg("No se detectó la tarjeta. Puede acercarla al lector e intentar de nuevo", false);
				}
			}
			@Override
			public void onLeerTarjeta(LectorUsb lector, List<PendientesTarjeta> pendientes) {
				//miLectorUsb.msg("Terminado onLeerTarjeta");
				pendientesResueltos = pendientes;
				if(pendientesResueltos.size() > 0){
					try {
						//miLectorUsb.msg("Hay pendientes y los va a escribir");
						miLectorUsb.EscribirTarjeta(idTarjetaLeida, aplicacion.getSesion().getDatosPacienteActual(), escribirUsbConNdef);
						//resultado llegará a onEscribirTarjeta()
					} catch (IOException e) {
						String msg = e.getMessage() + ":\n"+e.toString();
						miLectorUsb.msg("Al escribir pendientes sucedio:"+msg, false);
						ErrorSis.AgregarError(getActivity(), aplicacion.getSesion().getUsuario()._id, 
							ErrorSis.ERROR_DESCONOCIDO, "Usb-EscribirPendientes:"+msg);
						return;
					}
				}else{
					//El paciente ya está leído en sesión y cerramos esto (en UI thread)
					//miLectorUsb.msg("No hubo pendientes y salimos normal");
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Cerrar(RESULT_OK);							
						}
					});
				}
			}
			@Override
			public void onEscribirTarjeta(LectorUsb lector) {
				//miLectorUsb.msg("se ha terminado de escribir");
				//Se ha terminado de escribir, ya sea en guardado común o por pendientes
				if(pendientesResueltos != null)
					for(PendientesTarjeta pendiente : pendientesResueltos)
						PendientesTarjeta.MarcarPendienteResuelto(getActivity(), pendiente);
				//Ahora solo cerramos (en UI thread)
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Cerrar(RESULT_OK);							
					}
				});
			}
		};
		try{
			miLectorUsb = new LectorUsb(getActivity(), eventosUsb);
		}catch(Exception e){}
		
		this.modoOperacion = (ModoOperacion) getArguments().getSerializable(PARAM_MODO_OPERACION);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		//Diálogo sin título (pues setCancelable() deja título que no queremos)
		Dialog dialogo=super.onCreateDialog(savedInstanceState);
		dialogo.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialogo;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		View vista;
		
		//TODO si no cambian mucho los layout, fusionarlos en uno solo pues ahora son casi iguales
		if(modoOperacion == ModoOperacion.LOGIN){
			vista=inflater.inflate(R.layout.dialogo_tes_login, container,false);
			txtPasarTesDeNuevo = vista.findViewById(R.id.txtPasarTesDeNuevo);
		}else{
			vista=inflater.inflate(R.layout.dialogo_tes_guardar, container,false);			
		}
		
		//Cancelación manual
		Button btnCancelar=(Button)vista.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Si es modo guardar Y nos piden crear un pendiente al no escribir en TES...
				if(modoOperacion == ModoOperacion.GUARDAR && pendiente !=null)
					PendientesTarjeta.AgregarNuevoPendienteLocal(getActivity(), pendiente);
				Cerrar(DialogoTes.RESULT_CANCELAR);
			}
		});
		
		//Botón ayuda
		ImageButton btnAyuda=(ImageButton)vista.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int layout = modoOperacion == ModoOperacion.LOGIN
						? R.string.ayuda_tes_login : R.string.ayuda_tes_guardar;
				DialogoAyuda.CrearNuevo(getFragmentManager(), layout);
			}
		});
		
		//Botón Lector USB
		Button btnLectorUsb=(Button)vista.findViewById(R.id.btnLectorUsb);
		btnLectorUsb.setVisibility(miLectorUsb == null ? View.GONE : View.VISIBLE);
		btnLectorUsb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					miLectorUsb.IdentificarTarjeta();
					//miLectorUsb.msg("Fin botonazo Usar LECTOR usb");
					//Su resultado va a onGetIdTarjeta()
				} catch (IOException e) {
					String msg = e.getMessage() + ":\n"+e.toString();
					Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
					ErrorSis.AgregarError(getActivity(), aplicacion.getSesion().getUsuario()._id, 
							ErrorSis.ERROR_DESCONOCIDO, "Usb-LlamarIdentificarTarjeta:"+msg);
				}
			}
		});
		
		return vista;
	}
	
	/**
	 * Llamado cuando el contenedor de este fragmento ha detectado un nuevo tag NFC
	 * @param tag El tag recibido por el dispositivo NFC
	 */
	public void onTagNfcDetectado(Tag tag){
		try {
			if(this.modoOperacion == ModoOperacion.LOGIN){
				if(pendientesResueltos== null){
					//Es una lectura normal
					pendientesResueltos = ManejadorNfc.LeerDatosNFC(tag, getActivity());
					if(pendientesResueltos.size()>0){
						//Hay pendientes resueltos pero deben guardarse ahora mismo en tarjeta o nada se modifica en BD
						txtPasarTesDeNuevo.setVisibility(View.VISIBLE);
						return;
					}
				}else{
					//Se pasó la tarjeta nuevamente después de tener pendientes a resolver
					GuardarDatosEnTes(tag);
					for(PendientesTarjeta pendiente : pendientesResueltos)
						PendientesTarjeta.MarcarPendienteResuelto(getActivity(), pendiente);
				}
				Cerrar(DialogoTes.RESULT_OK);
			}else if(this.modoOperacion == ModoOperacion.GUARDAR){
				GuardarDatosEnTes(tag);
				Cerrar(DialogoTes.RESULT_OK);
			}
		} catch (Exception e) {
			String msg = e.getMessage() + ":\n"+e.toString();
			Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
			ErrorSis.AgregarError(getActivity(), aplicacion.getSesion().getUsuario()._id, 
					ErrorSis.ERROR_DESCONOCIDO, "NFC:"+msg);
		}
	}
	
	/**
	 * Llama al guardado de los datos del paciente en tarjeta NFC
	 * @param tag
	 * @throws Exception
	 */
	private void GuardarDatosEnTes(Tag tag) throws Exception{
		Sesion.DatosPaciente datosPaciente = aplicacion.getSesion().getDatosPacienteActual();
		//Esta validación se quitó pues desafortunadamente la tarjeta puede ser desconfigurada
		//facilmente al intentar escribirla, por lo que una tarjeta que se usaba en paciente
		//podría desconfigurarse en una mala escritura, lo que después la dejaría inutilizable
		//en este sistema. Quitamos esta comparación para permitir intentar reescribir
		/*if(!ManejadorNfc.nfcTagPerteneceApersona(datosPaciente.persona.id, tag)){
			Toast.makeText(getActivity(), "La TES presentada no pertenece al paciente "
					+ datosPaciente.persona.nombre + datosPaciente.persona.apellido_paterno
					+ datosPaciente.persona.apellido_materno, Toast.LENGTH_LONG).show();
			return;
		}*/
		ManejadorNfc.EscribirDatosNFC(tag, datosPaciente);
		Toast.makeText(getActivity(), getString(R.string.informacion_guardada_en_tes), Toast.LENGTH_SHORT).show();
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
	public void onPause(){
		super.onPause();
		ModoEscrituraNfcInactivo();
	}

	@Override
	public void onResume(){
		super.onResume();
		ModoEscrituraNfcActivo();
	}

	private void ModoEscrituraNfcActivo(){
		if(adaptadorNFC != null)
			adaptadorNFC.enableForegroundDispatch(getActivity(), pendingIntentNFC, writeTagFiltersNFC, null);
		
		if(miLectorUsb != null)
			miLectorUsb.onResume();;
	}

	private void ModoEscrituraNfcInactivo(){
		if(adaptadorNFC != null)
			adaptadorNFC.disableForegroundDispatch(getActivity());
		
		if(miLectorUsb != null)
			miLectorUsb.onPause();
	}
}//fin clase
