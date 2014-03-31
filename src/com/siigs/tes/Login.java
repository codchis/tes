/**
 * Muestra un diálogo modal que intenta escuchar la presencia de una TES
 * y realiza las acciones necesarias según el caso
 */
package com.siigs.tes;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.SincronizacionTask;
import com.siigs.tes.datos.tablas.Usuario;
import com.siigs.tes.datos.tablas.UsuarioInvitado;
import com.siigs.tes.ui.AdaptadorArrayMultiTextView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Implementa una ventana para inicio de sesión de usuarios normales ó invitados
 * @author Axel
 *
 */
public class Login extends DialogFragment {

	public static final String TAG=Login.class.toString();
	public static final int REQUEST_CODE=987;
	public static final int RESULT_USUARIO_NORMAL=0;
	public static final int RESULT_USUARIO_INVITADO=-5;
	//Parametro de retorno al cerrar
	public static final String PARAM_ID_ESCOGIDO = "id_escogido";
	
	private boolean modoInvitado = false; //Indica si escoge invitado o normal
	//Views
	private LinearLayout pnPassword; //El layout que muestra password
	private Button btnAcceder;
	private Button btnInvitado;
	private Button btnCancelar;
	private Button btnSincronizar;
	private Spinner spUsuario;
	private TextView lblUsuario;
	
	private Usuario usuarioElegido; //El usuario escogido en spinner
	private UsuarioInvitado invitadoElegido; //El invitado escogido en spinner
	
	private AdaptadorArrayMultiTextView<Usuario> adaptadorNormal;
	private AdaptadorArrayMultiTextView<UsuarioInvitado> adaptadorInvitados;
	
	private TesAplicacion aplicacion;
	
	//Constructor requerido
	public Login(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Estílo no_frame para que la ventana sea tipo modal
		//setStyle(STYLE_NO_FRAME, R.style.AppBaseTheme);
		//Método 2 para hacer la ventana modal 
		
		//setRetainInstance(true);
		
		setCancelable(false);
		aplicacion = (TesAplicacion)getActivity().getApplication();
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
		final View view=inflater.inflate(R.layout.login, container,false);
		
		ActualizarUsuarios();
		
		spUsuario = (Spinner)view.findViewById(R.id.spUsuario);
		//spUsuario.setAdapter(adaptadorNormal); //Se asigna en llamada a setModoInvitado()
		//Manejo de click
		spUsuario.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view,
					int position, long id) {
				if(!modoInvitado)
					usuarioElegido = (Usuario) adapterView.getItemAtPosition(position);
				else invitadoElegido = (UsuarioInvitado)adapterView.getItemAtPosition(position);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		//Acceder normal
		btnAcceder=(Button)view.findViewById(R.id.btnAcceder);
		btnAcceder.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!modoInvitado){
					if(usuarioElegido == null){
						Toast.makeText(getActivity(), "No ha elegido un usuario", Toast.LENGTH_SHORT).show();
						return;
					}
					
					//Validamos usuario normal
					String clave = ((TextView)view.findViewById(R.id.txtPassword)).getText().toString();
					if(usuarioElegido.clave.equals(getMd5Hash(clave))){
						Cerrar(Login.RESULT_USUARIO_NORMAL);
					}else{
						Toast.makeText(getActivity(), "Clave no reconocida", Toast.LENGTH_SHORT).show();
					}
					//Log.d(TAG, "comparando clave usuario "+ usuarioElegido.clave + " con "+ clave+" iguales?"+(usuarioElegido.clave.equals(clave)));
				}else{
					//Avisamos que fue invitado
					if(invitadoElegido == null){
						Toast.makeText(getActivity(), "No ha elegido un invitado", Toast.LENGTH_SHORT).show();
						return;
					}
					Cerrar(Login.RESULT_USUARIO_INVITADO);
				}
			}
		});
		
		//Acceder invitado
		btnInvitado=(Button)view.findViewById(R.id.btnInvitado);
		btnInvitado.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setModoInvitado(true);
			}
		});
		
		//Cancelar modo invitado
		btnCancelar=(Button)view.findViewById(R.id.btnCancelar);
		btnCancelar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setModoInvitado(false);
			}
		});
		
		//Sincronizar
		btnSincronizar = (Button)view.findViewById(R.id.btnSincronizar);
		btnSincronizar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Sincronizar();
			}
		});
				
		//Botón ayuda
		ImageButton btnAyuda=(ImageButton)view.findViewById(R.id.btnAyuda);
		btnAyuda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogoAyuda.CrearNuevo(getFragmentManager(), R.string.ayuda_login);
			}
		});
		
		pnPassword = (LinearLayout)view.findViewById(R.id.pnPassword);
		lblUsuario = (TextView)view.findViewById(R.id.lblUsuario);
		
		this.setModoInvitado(modoInvitado);
		
		return view;
	}
	
	/**
	 * Asigna estado en widgets de acuerdo a la modalidad esInvitado
	 * @param esInvitado Define si se visualiza login de invitado o de usuario normal
	 */
	private void setModoInvitado(boolean esInvitado){
		this.modoInvitado = esInvitado;
		pnPassword.setVisibility(modoInvitado? View.GONE:View.VISIBLE);
		btnInvitado.setVisibility(pnPassword.getVisibility());
		btnCancelar.setVisibility(modoInvitado? View.VISIBLE:View.GONE);
		lblUsuario.setText(modoInvitado? R.string.invitado : R.string.usuario);
		
		if(aplicacion.getEsInstalacionNueva() || aplicacion.getRequiereActualizarApk()){
			btnSincronizar.setVisibility(View.VISIBLE);
			btnAcceder.setVisibility(View.GONE);
			
			btnInvitado.setVisibility(View.GONE);
			btnCancelar.setVisibility(View.GONE);
		}else{
			btnSincronizar.setVisibility(View.GONE);
			btnAcceder.setVisibility(View.VISIBLE);
		}
		
		
		spUsuario.setAdapter(modoInvitado? adaptadorInvitados : adaptadorNormal);
	}
	
	/**
	 * Actualiza las listas de usuarios (solo útil en caso que haya primera sincronización para refrescar usuarios
	 */
	private void ActualizarUsuarios(){
		//Lista de usuarios activos
		Cursor cur = Usuario.getUsuariosActivos(getActivity());
		List<Usuario> usuarios = DatosUtil.ObjetosDesdeCursor(cur, Usuario.class);
		cur.close();
		adaptadorNormal = new AdaptadorArrayMultiTextView<Usuario>(
				getActivity(), android.R.layout.simple_dropdown_item_1line, usuarios, 
				new String[]{Usuario.NOMBRE_USUARIO}, new int[]{android.R.id.text1});
		//adaptadorNormal.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		cur = UsuarioInvitado.getUsuariosInvitadosActivos(getActivity());
		List<UsuarioInvitado> invitados = DatosUtil.ObjetosDesdeCursor(cur, UsuarioInvitado.class);
		cur.close();
		adaptadorInvitados = new AdaptadorArrayMultiTextView<UsuarioInvitado>(
				getActivity(), android.R.layout.simple_dropdown_item_1line, invitados,
				new String[]{UsuarioInvitado.NOMBRE}, new int[]{android.R.id.text1});
		//adaptadorInvitados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}
	
	/**
	 * Cierra este diálogo y notifica a fragmento padre los datos.
	 * @param código de resultado oficial de la ejecución de este diálogo
	 */
	private void Cerrar(int resultado){
		Intent datos= new Intent();
		datos.putExtra(Login.PARAM_ID_ESCOGIDO, modoInvitado? invitadoElegido._id : usuarioElegido._id);
		
		getTargetFragment().onActivityResult(getTargetRequestCode(), 
				resultado, datos);
		dismiss();
	}
	
	
	/**
	 * Sincroniza el sistema igual que en pantalla Sincronización. Esto solo debería ejecutarse cuando
	 * la instalación de la app es nueva Ó cuando requiere actualizar su apk.
	 */
	private void Sincronizar(){
		AlertDialog dialogoAviso=new AlertDialog.Builder(getActivity()).create();
		
		if(aplicacion.getRequiereActualizarApk()){
			aplicacion.ValidarRequiereActualizarApk(dialogoAviso);
			return; //Salimos pues el mensaje a enviar se hizo arriba.
		}
		
		//Confirmación de sincronización
		String mensaje ="Esta instalación nueva necesita sincronizarse antes de continuar. " +
				"Este proceso puede tardar varios minutos. Verifique su conexión a Internet y que tenga batería suficiente." +
				"\n\nPuede cambiar la URL de sincronización si lo desea.";
		
		final EditText txtUrl = new EditText(getActivity());
		txtUrl.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		txtUrl.setSingleLine();
		txtUrl.setText(aplicacion.getUrlSincronizacion());
		
		
		dialogoAviso.setMessage(mensaje);
		dialogoAviso.setView(txtUrl);
		//dialogoAviso.setNegativeButton(android.R.string.cancel, null);
		dialogoAviso.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Un dialogo vacío que la sincronización puede usar para mostrar resultados
				//AlertDialog.Builder dlgResultado=new AlertDialog.Builder(getActivity());
				AlertDialog dlgResultado=new AlertDialog.Builder(getActivity()).create();
				dlgResultado.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface arg0) {
						ActualizarUsuarios();
						setModoInvitado(modoInvitado);
					}
				});
				//dlgResultado.create();

				aplicacion.setUrlSincronizacion(txtUrl.getText().toString());
				
				SincronizacionTask sinc = new SincronizacionTask(getActivity(), dlgResultado);
				sinc.execute("");
			}
		} );
		dialogoAviso.show();
	}

	
	public static String getMd5Hash(String input) {
	    try {
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        byte[] messageDigest = md.digest(input.getBytes());
	        BigInteger number = new BigInteger(1, messageDigest);
	        String md5 = number.toString(16);

	        while (md5.length() < 32)
	            md5 = "0" + md5;

	        return md5;
	    } catch (NoSuchAlgorithmException e) {
	        Log.e("MD5", e.getLocalizedMessage());
	        return "";
	    }
	}
}//fin clase
