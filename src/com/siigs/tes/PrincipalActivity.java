package com.siigs.tes;

import java.util.List;

import com.siigs.tes.Sesion.DatosPaciente;
import com.siigs.tes.controles.CensoCensoNominal;
import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.controles.ContenidoControles.ItemControl;
import com.siigs.tes.datos.BaseDatos;
import com.siigs.tes.datos.tablas.ErrorSis;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * An activity representing a list of Secciones. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ControlActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PrincipalFragment} and the item details (if present) is a
 * {@link ControlFragment}.
 * <p>
 * This activity also implements the required
 * {@link PrincipalFragment.Callbacks} interface to listen for item
 * selections.
 */
public class PrincipalActivity extends FragmentActivity implements
		PrincipalFragment.Callbacks, DialogoTes.Callbacks {

	public final static String TAG = PrincipalActivity.class.getSimpleName();
	public final static String FORZAR_CIERRE_SESION_USUARIO = "flag_forzar_cierre_sesion_usuario";
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean enDosPaneles;
	
	private PrincipalFragment lfMenuIzquierdo;
	private MenuSuperior miMenuSuperior;
	private TesAplicacion aplicacion;
	private DialogoTes miDialogoTes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_principal_twopane);//.activity_principal_onepane);
		
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		this.aplicacion = (TesAplicacion)getApplication();
		

		if (findViewById(R.id.seccion_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			enDosPaneles = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			lfMenuIzquierdo=(PrincipalFragment) getSupportFragmentManager()
					.findFragmentById(R.id.seccion_list);
			lfMenuIzquierdo.setActivateOnItemClick(true);
			
			//Escuchamos seleccion de menú superior
			MenuSuperior.OnAccionMenuListener listener=new MenuSuperior.OnAccionMenuListener() {
				@Override
				public void onSeleccionarMenu(List<ItemControl> lista) {
					if(!lfMenuIzquierdo.Navegar(lista))
						return;
					
					if(lista.size()<=1){
						//Esconder menú izquierdo
						//lfMenuIzquierdo.

						getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(R.anim.esconder_principal_fragment, R.anim.esconder_principal_fragment)
						.hide(lfMenuIzquierdo).commit();
					}else{
						//Mostrar menú izquierdo
						getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(android.R.anim.slide_in_left, R.anim.esconder_principal_fragment)
						.show(lfMenuIzquierdo).commit();
					}
				}//fin onSeleccionarMenu
				@Override
				public void onAtenderPacienteSinTes(int _id){
					DatosPaciente historial = DatosPaciente.cargarDesdeBaseDatos(aplicacion, _id);
					if(historial == null){
						//Esto nunca debería pasar
						Toast.makeText(aplicacion, "No fue posible cargar datos del paciente con _id:"+_id,Toast.LENGTH_LONG).show();
						return;
					}
					aplicacion.getSesion().setDatosPacienteNuevo(historial);
					this.onSeleccionarMenu(ContenidoControles.CONTROLES_ATENCION);
				}
				@Override 
				public void onIniciarSesionUsuario(int idUsuario, boolean esInvitado){
					if(esInvitado){
						aplicacion.IniciarSesionInvitado(idUsuario);
						miMenuSuperior.setTitulo(aplicacion.getSesion().getUsuarioInvitado().nombre);
					}else {
						aplicacion.IniciarSesion(idUsuario);
						miMenuSuperior.setTitulo(aplicacion.getSesion().getUsuario().nombre);
					}
				}
				@Override
				public void onClickCerrarSesionUsuario(){
					CerrarSesionUsuario();
				}
			};
			miMenuSuperior=(MenuSuperior)getSupportFragmentManager()
				.findFragmentById(R.id.menu_superior);
			miMenuSuperior.setOnAccionMenuListener(listener);

			//Si NO es cambio de orientación y NO hay sesión
			if(savedInstanceState==null && aplicacion.getSesion()==null) {
				PonerPortada();
				miMenuSuperior.PedirLoginUsuario();
			}
		}//fin si es doble panel

		
		//Si se le pide cerrar el usuario ahora... (solo pasaría desde TesAplicacion.ValidarRequiereActualizarApk() )
		if(getIntent()!=null && getIntent().hasExtra(FORZAR_CIERRE_SESION_USUARIO))
				if(getIntent().getBooleanExtra(FORZAR_CIERRE_SESION_USUARIO, false))
					CerrarSesionUsuario();
	}//fin onCreate
	

	/**
	 * Manda a cerrar la sesión actual en curso
	 */
	private void CerrarSesionUsuario(){
		//if(aplicacion.getSesion()==null)return;
		
		Log.i(TAG, "Cierra sesión");
		aplicacion.CerrarSesion();
		PonerPortada();
		miMenuSuperior.PedirLoginUsuario();
	}
	
	private void PonerPortada(){
		getSupportFragmentManager().beginTransaction()
		.setCustomAnimations(R.anim.esconder_principal_fragment, R.anim.esconder_principal_fragment)
		.hide(lfMenuIzquierdo).commit();

		getSupportFragmentManager().beginTransaction()
		.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
				//TODO reemplazar este control vacío con posible versión grande de MenuSuperior
				R.id.seccion_detail_container, new ControlFragment()).commit();
	}

	/**
	 * Recibe avisos diversos. Nos enfocamos en los de NFC
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		if(intent == null) return;
		
		if(intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)){
			if(miDialogoTes!=null){
				Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
				miDialogoTes.onTagNfcDetectado(nfcTag);
			}
		}
	}
	
	/**
	 * Callback de {@link DialogoTes.Callbacks} indicando
	 * que el dialogo está listo para recibir avisos de tags NFC
	 * @param llamador
	 */
	@Override
	public void onIniciarDialogoTes(DialogoTes llamador){
		miDialogoTes = llamador;
	}
	
	/**
	 * Callback de {@link DialogoTes.Callbacks} indicando
	 * que el dialogo ya no requiere recibir avisos de tags NFC
	 */
	@Override
	public void onDetenerDialogoTes(DialogoTes llamador){
		miDialogoTes = null;
	}


	/**
	 * Callback method from {@link PrincipalFragment.Callbacks} indicating
	 * that the item with the given ID_PERSONA was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (enDosPaneles) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ContenidoControles.ARG_ICA, id);
			Fragment fragment=null;
			try {
				fragment = (Fragment)ContenidoControles.CONTROLES_TODOS_MAP
						.get(id).clase.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			fragment.setArguments(arguments);
			
			//Censo es "linkeado" a {@link MenuSuperior} de forma que Censo invocará onActivityResult()
			//con su request_code y esto lo escuchará {@link MenuSuperior} para mostrar un paciente en BD.
			if(fragment instanceof CensoCensoNominal){
				fragment.setTargetFragment(this.miMenuSuperior, CensoCensoNominal.REQUEST_CODE);
			}
			
			getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.replace(R.id.seccion_detail_container, fragment)//.addToBackStack(null)
				.commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID_PERSONA.
			Intent detailIntent = new Intent(this, ControlActivity.class);
			detailIntent.putExtra("clase", ContenidoControles.CONTROLES_TODOS_MAP.get(id).clase.getName());
			startActivity(detailIntent);
		}
	}


	@Override
	/**
	 * Captura el presionado del botón back haciendo que su comportamiento sea
	 * igual al botón HOME, para mandar la aplicación al fondo, en vez de destruir
	 * esta actividad, lo cual irónicamente NO destruye {@link TesAplicacion} ni las
	 * clases estáticas y sus valores generados como sucede con {@link ContenidoControles}
	 */
	public void onBackPressed() {
		this.moveTaskToBack(true);
	}


	@Override
	protected void onPause() {
		super.onPause();
		aplicacion.onPausa(this);
	}


	@Override
	protected void onResume() {
		super.onResume();
		aplicacion.onResumir(this);
	}
	
	
	
}//fin clase
