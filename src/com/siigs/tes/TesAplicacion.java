package com.siigs.tes;

import java.util.Calendar;
import java.util.List;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Type;

import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Permiso;
import com.siigs.tes.datos.tablas.Usuario;
import com.siigs.tes.datos.tablas.UsuarioInvitado;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Clase tipo Application invocada automáticamente al cargar la app.
 * Implementa funciones básicas que son acceseibles desde varios módulos.
 * @author Axel
 *
 */
@ReportsCrashes(formKey = "", 
formUri = "http://sm2015.com.mx:5984/acralyzer",
formUriBasicAuthLogin = "tes", formUriBasicAuthPassword = "tes", 
reportType = Type.JSON,
mode = ReportingInteractionMode.TOAST, resToastText = R.string.acra_mensaje)
public class TesAplicacion extends Application {

	private final static String TAG = "TesAplicacion";
	
	SharedPreferences preferencias;
	//Preferencias...
	private final static String URL_SINCRONIZACION = "url_sincronizacion";
	private final static String REINTENTOS_CONEXION = "reintentos_conexion";
	private final static String TIEMPO_ESPERA_REINTENTO = "tiempo_espera_reintento";
	private final static String TIPO_CENSO = "tipo_censo";
	private final static String UNIDAD_MEDICA = "unidad_medica";
	private final static String FILTRO_DIAS_ANTIGUEDAD_IRAS = "filtro_dias_antiguedad_iras";
	private final static String FILTRO_DIAS_ANTIGUEDAD_EDAS = "filtro_dias_antiguedad_edas";
	private final static String FILTRO_DIAS_ANTIGUEDAD_CONSULTAS = "filtro_dias_antiguedad_consultas";
	private final static String ES_INSTALACION_NUEVA = "es_instalacion_nueva";
	private final static String FECHA_ULTIMA_SINCRONIZACION = "fecha_ultima_sincronizacion";
	private final static String REQUIERE_ACTUALIZAR_VERSION_APK = "requiere_actualizar_version_apk";
	private final static String URL_ACTUALIZACION_APK = "url_actualizacion_apk";
	private final static String ULTIMO_APK_CONOCIDO = "ultimo_apk_conocido"; // para determinar si es upgrade

	private Sesion sesion = null; //La sesión de uso
	
	private ProgressDialog pdProgreso = null; //Contenedor para diálogos de progreso
	
	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
		//Cargamos preferencias para usarlas
		preferencias= PreferenceManager.getDefaultSharedPreferences(this);
//		CargarPreferencias();
//		preferencias.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
//			@Override
//			public void onSharedPreferenceChanged(SharedPreferences sp, String arg1) {
//				preferencias = sp;
//				CargarPreferencias();
//			}
//		});
		
		if(getEsInstalacionNueva()){
			//Rutinas en caso de ser nueva...
		}else if(!getVersionUltimoApkConocido().equals(getVersionApk())){
			//Ha habido un cambio de APK
			setRequiereActualizarApk(false); //para dejar de pedir actualizar
		}
		setVersionUltimoApkConocido(getVersionApk());
	}
	
	/**
	 * Indica si hay conectividad en Wifi, aunque no garantiza que haya internet.
	 */
	public boolean hayInternet() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	//FUNCIONES DE MANEJO DE PREFERENCIAS
	
	/**
	 * Preferencia disponible también para el usuario administrador
	 */
	public String getUrlSincronizacion(){
		return preferencias.getString(URL_SINCRONIZACION, "http://www.sm2015.com.mx/tes/servicios/synchronization");
	}
	public void setUrlSincronizacion(String url){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putString(URL_SINCRONIZACION, url);
		editor.apply();
		Log.d(TAG, "Cambiado url sincronización a:"+url);
	}
	
	public int getReintentosConexion(){
		return preferencias.getInt(REINTENTOS_CONEXION, 3);
	}
	public void setReintentosConexion(int cantidad){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(REINTENTOS_CONEXION, cantidad);
		editor.apply();
		Log.d(TAG, "Cambiado reintentos conexión a:"+cantidad);
	}
	
	public int getTiempoEsperaReintento(){
		return preferencias.getInt(TIEMPO_ESPERA_REINTENTO, 2000);
	}
	public void setTiempoEsperaReintento(int milisegundos){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(TIEMPO_ESPERA_REINTENTO, milisegundos);
		editor.apply();
		Log.d(TAG, "Cambiado tiempo espera reintento a:"+milisegundos);
	}
	
	public int getTipoCenso(){
		return preferencias.getInt(TIPO_CENSO, -1);
	}
	public void setTipoCenso(int tipo){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(TIPO_CENSO, tipo);
		editor.apply();
		Log.d(TAG, "Cambiado tipo censo a:"+tipo);
	}
	
	public int getUnidadMedica(){
		return preferencias.getInt(UNIDAD_MEDICA, -1);
	}
	public void setUnidadMedica(int um){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(UNIDAD_MEDICA, um);
		editor.apply();
		Log.d(TAG, "Cambiada unidad médica a:"+um);
	}
	
	public int getFiltroDiasAntiguedadIras(){
		return preferencias.getInt(FILTRO_DIAS_ANTIGUEDAD_IRAS, 10);
	}
	public void setFiltroDiasAntiguedadIras(int dias){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(FILTRO_DIAS_ANTIGUEDAD_IRAS, dias);
		editor.apply();
		Log.d(TAG, "Cambiado filtro días de iras a:"+dias);
	}
	
	public int getFiltroDiasAntiguedadEdas(){
		return preferencias.getInt(FILTRO_DIAS_ANTIGUEDAD_EDAS, 10);
	}
	public void setFiltroDiasAntiguedadEdas(int dias){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(FILTRO_DIAS_ANTIGUEDAD_EDAS, dias);
		editor.apply();
		Log.d(TAG, "Cambiado filtro días de edas a:"+dias);
	}
	
	public int getFiltroDiasAntiguedadConsultas(){
		return preferencias.getInt(FILTRO_DIAS_ANTIGUEDAD_CONSULTAS, 10);
	}
	public void setFiltroDiasAntiguedadConsultas(int dias){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(FILTRO_DIAS_ANTIGUEDAD_CONSULTAS, dias);
		editor.apply();
		Log.d(TAG, "Cambiado filtro días de consultas a:"+dias);
	}
	
	public boolean getEsInstalacionNueva(){
		return preferencias.getBoolean(ES_INSTALACION_NUEVA, true);
	}
	public void setEsInstalacionNueva(boolean valor){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putBoolean(ES_INSTALACION_NUEVA, valor);
		editor.apply();
		Log.d(TAG, "Cambiado instalación nueva a:"+valor);
	}
	
	public boolean getRequiereActualizarApk(){
		return preferencias.getBoolean(REQUIERE_ACTUALIZAR_VERSION_APK, false);
	}
	public void setRequiereActualizarApk(boolean valor){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putBoolean(REQUIERE_ACTUALIZAR_VERSION_APK, valor);
		editor.apply();
		Log.i(TAG, "Cambiado requiere actualizar APK a:"+valor);
	}
	
	public String getUrlActualizacionApk(){
		return preferencias.getString(URL_ACTUALIZACION_APK, "http://www.sm2015.com.mx/tes/servicios/prueba");
	}
	public void setUrlActualizacionApk(String url){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putString(URL_ACTUALIZACION_APK, url);
		editor.apply();
		Log.d(TAG, "Cambiado url actualización APK a:"+url);
	}
	
	/**
	 * Regresa versión del APK que se supone está corriendo ahora.
	 * Si hubiera actualización del APK, este valor ayuda a definir si el APK ha cambiado y por lo tanto
	 * realizar las acciones necesarias.
	 * Esta función puede usarse para reemplazar el método de definición getEsInstalacionNueva()
	 * @return
	 */
	public String getVersionUltimoApkConocido(){
		return preferencias.getString(ULTIMO_APK_CONOCIDO, "0");
	}
	public void setVersionUltimoApkConocido(String versionApk){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putString(ULTIMO_APK_CONOCIDO, versionApk);
		editor.apply();
		Log.d(TAG, "Cambiado versión del último APK conocido a:"+versionApk);
	}
	
	/**
	 * Valida si se requiere actualizar el apk y en tal caso muestra el mensaje apropiado
	 * usando Builder dialogo.
	 * Inicia 2 Intent, el primero para mandar un mensaje a la actividad principal pidiéndole
	 * cerrar la sesión de usuario. Dicha actividad valida este mensaje usando getIntent()
	 * 
	 * El segundo Intent inicia el navegador para ir al URL de actualización.
	 * @param dialogo Ventana usada para notificar al usuario
	 */
	public void ValidarRequiereActualizarApk(AlertDialog dialogo){
		if(getRequiereActualizarApk()){
			dialogo.setMessage("Esta aplicación requiere actualizarse. " +
					"Puede presionar 'Actualizar' para ser enviado a la página de actualización");
			dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.actualizar), new DialogInterface.OnClickListener() {
			//dialogo.setPositiveButton(R.string.actualizar, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = getBaseContext().getPackageManager()
				             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra(PrincipalActivity.FORZAR_CIERRE_SESION_USUARIO, true);
					startActivity(i);
					
					// Envía a la página de actualización
					i = new Intent(Intent.ACTION_VIEW);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.setData(Uri.parse(getUrlActualizacionApk()));
					startActivity(i);
				}
			});
		}
		
		dialogo.show();
	}
	
	public String getVersionApk(){
		String version="";
		try {
			version += getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e1) {
			version="imposible determinar version";
		}
		return version;
	}
	
	public String getFechaUltimaSincronizacion(){
		String fecha = preferencias.getString(FECHA_ULTIMA_SINCRONIZACION, "2000-01-01 00:00:00");
		return fecha;
/*		try {
			return DateFormat.getDateTimeInstance().parse(fecha);
		} catch (ParseException e) {
			Log.e(TAG, "No se pudo parsear fecha:"+fecha);
			Calendar cal = Calendar.getInstance();
			cal.clear();
			cal.set(2000, Calendar.JANUARY, 1);
			return cal.getTime();
		}*/
	}
	public void setFechaUltimaSincronizacion(){
		//setFechaUltimaSincronizacion(new Date(System.currentTimeMillis()));
		setFechaUltimaSincronizacion(Calendar.getInstance());
	}
	public void setFechaUltimaSincronizacion(Calendar cal){
		SharedPreferences.Editor editor = preferencias.edit();
		//editor.putString(FECHA_ULTIMA_SINCRONIZACION, valor.toString());
		String salida= DatosUtil.getAhora();
		editor.putString(FECHA_ULTIMA_SINCRONIZACION, salida);
		editor.apply();
		Log.d(TAG, "Cambiada última sincronización a:"+salida);
	}
	
	/**
	 * Genera una nueva sesión para usuario normal
	 * @param idUsuario Usuario que inicia sesión
	 */
	public void IniciarSesion(int idUsuario){
		Usuario usuario = Usuario.getUsuarioConId(this, idUsuario);
		Cursor cur = Permiso.getPermisosGrupo(this, usuario.id_grupo);
		List<Permiso> permisos = DatosUtil.ObjetosDesdeCursor(cur, Permiso.class);
		cur.close();
		this.sesion = new Sesion(usuario, null, permisos); //Asigna nueva sesión
		
		try{
			//Bitacora.AgregarRegistro(this, usuario._id, Bitacora.LOGIN, parametros)
		}catch(Exception e){e.printStackTrace();}
		//Actualiza las listas usadas para crear los submenús izquierdos y menú superior
		ContenidoControles.RecargarControles(permisos);
	}
	/**
	 * Genera una nueva nueva sesión para usuario invitado 
	 * @param idInvitado Invitado que inicia sesión
	 */
	public void IniciarSesionInvitado(int idInvitado){
		UsuarioInvitado invitado = UsuarioInvitado.getUsuarioInvitadoConId(this, idInvitado);
		Usuario usuario = Usuario.getUsuarioConId(this, invitado.id_usuario_creador);
		Cursor cur = Permiso.getPermisosGrupo(this, UsuarioInvitado.ID_GRUPO);
		List<Permiso> permisos = DatosUtil.ObjetosDesdeCursor(cur, Permiso.class);
		cur.close();
		this.sesion = new Sesion(usuario, invitado, permisos);
		
		//Actualiza las listas usadas para crear los submenús izquierdos y menú superior
		ContenidoControles.RecargarControles(permisos);
	}
	/**
	 * Cierra la sesión del usuario actual
	 */
	public void CerrarSesion(){
		//Registrar status relevantes
		this.sesion = null;
	}
	/**
	 * Indica si existe una sesión de usuario
	 */
	public boolean haySesion(){return this.sesion != null;}
	/**
	 * Devuelve la sesión de usuario actual
	 */
	public Sesion getSesion(){return this.sesion;}
	
	
	public void onPausa(Activity llamador){
		if(pdProgreso !=null)
			pdProgreso.dismiss();
	}
	public void onResumir(Activity llamador){
		if(pdProgreso!=null){ // && sinctask running
			destruirDialogoProgreso();
			crearDialogoProgreso(llamador);
		}
	}
	
	public void crearDialogoProgreso(Activity llamador){
		String mensaje= "El dispositivo se está sincronizando en "+getUrlSincronizacion()
				+"\nPor favor espere.";
		if(getEsInstalacionNueva())
			mensaje="("+getUrlSincronizacion()+")"+"\nEsta acción puede tardar varios minutos. Por favor espere.";
		boolean indeterminado=true, cancelable=false;
		this.pdProgreso = ProgressDialog.show(llamador, "Sincronizando", 
				mensaje, indeterminado, cancelable);
	}
	public void destruirDialogoProgreso(){this.pdProgreso = null;}
	public ProgressDialog getDialogoProgreso(){return pdProgreso;}

	
}