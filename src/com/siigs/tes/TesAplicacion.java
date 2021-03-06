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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Clase tipo Application invocada autom�ticamente al cargar la app.
 * Implementa funciones b�sicas que son acceseibles desde varios m�dulos.
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
	private final static String NIVEL_ATENCION = "nivel_atencion";
	private final static String UNIDAD_MEDICA = "unidad_medica";
	private final static String FILTRO_DIAS_ANTIGUEDAD_IRAS = "filtro_dias_antiguedad_iras";
	private final static String FILTRO_DIAS_ANTIGUEDAD_EDAS = "filtro_dias_antiguedad_edas";
	private final static String FILTRO_DIAS_ANTIGUEDAD_CONSULTAS = "filtro_dias_antiguedad_consultas";
	private final static String ES_INSTALACION_NUEVA = "es_instalacion_nueva";
	private final static String FECHA_ULTIMA_SINCRONIZACION = "fecha_ultima_sincronizacion";
	private final static String REQUIERE_ACTUALIZAR_VERSION_APK = "requiere_actualizar_version_apk";
	private final static String URL_ACTUALIZACION_APK = "url_actualizacion_apk";
	private final static String ULTIMO_APK_CONOCIDO = "ultimo_apk_conocido"; // para determinar si es upgrade

	private Sesion sesion = null; //La sesi�n de uso
	
	private ProgressDialog pdProgreso = null; //Contenedor para di�logos de progreso
	
	//Manejo de GPS
	private LocationManager miLocationManager = null;
	private Location ubicacionGPS = null;
	private LocationListener listenerGPS = null;
	private final static long MILISEGUNDOS_ACTUALIZAR_GPS = 300000;
	private final static float METROS_ACTUALIZAR_GPS = 10;
	
	
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
		
		//Servicio de localizaci�n GPS
		miLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		listenerGPS = new LocationListener() {
			@Override public void onStatusChanged(String provider, int status, Bundle extras) {}
			@Override public void onProviderEnabled(String provider) {}
			@Override public void onProviderDisabled(String provider) {}
			@Override public void onLocationChanged(Location location) {
				ubicacionGPS = location;
				Log.d(TAG, "Se ha actualizado la ubicaci�n GPS: "+ location);
			}
		};
		IniciarLocalizacionGPS();
	}
	
	
	/**
	 * Inicia la localizaci�n de ubicaci�n GPS. La ubicaci�n se puede obtener llamando a {@link getLocalizacionGPS}
	 * Debido a que este servicio gasta la bater�a es importante usarlo solo cuando sea necesario y 
	 * llamar la funci�n {@link DetenerLocalizacionGPS} cuando ya no sea necesario obtener la ubicaci�n GPS.
	 * @return <i>true</i> si el servicio de localizaci�n fue iniciado correctamente. <i>false</i> caso contrario
	 */
	public boolean IniciarLocalizacionGPS(){
		try{
			miLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
					MILISEGUNDOS_ACTUALIZAR_GPS, METROS_ACTUALIZAR_GPS, listenerGPS);
			Log.d(TAG, "Se ha iniciado la localizaci�n GPS");
			return true;
		}catch(Exception e){
			Log.d(TAG, "No fue posible iniciar la localizaci�n GPS:"+e);
			return false;
		}
	}
	
	public void DetenerLocalizacionGPS(){
		miLocationManager.removeUpdates(listenerGPS);
		Log.d(TAG, "Se ha detenido la localizaci�n GPS");
	}
	
	/**
	 * Regresa la ubicaci�n GPS m�s reciente desde la �ltima vez que se llam� a {@link IniciarLocalizacionGPS}
	 * Debido a que {@link IniciarLocalizacionGPS} puede tomar tiempo para conseguir una ubicaci�n, es posible que 
	 * la ubicaci�n no sea exacta � que el valor regresado sea <i>null</i>. Por esa raz�n es recomendable llamar 
	 * a {@link IniciarLocalizacionGPS} con suficiente tiempo antes de llamar esta funci�n.
	 * Tambi�n es importante notar que si el valor regresado por {@link IniciarLocalizacionGPS} es <i>false</i> entonces
	 * el valor regresado en esta funci�n puede ser <i>null</i> � una ubicaci�n no exacta.
	 * @return Ubicaci�n GPS m�s reciente o <i>null</i> si no se ha podido conseguir.
	 */
	public Location getLocalizacionGPS(){return ubicacionGPS;}
	
	
	/**
	 * Indica si hay conectividad en Wifi, aunque no garantiza que haya internet.
	 * @return true cuando hay conectividad y false caso contrario
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
	 * Preferencia disponible tambi�n para el usuario administrador
	 * @return Url de la sincronizaci�n
	 */
	public String getUrlSincronizacion(){
		return preferencias.getString(URL_SINCRONIZACION, "http://www.sm2015.com.mx/tes/servicios/synchronization");
	}
	public void setUrlSincronizacion(String url){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putString(URL_SINCRONIZACION, url);
		editor.apply();
		Log.d(TAG, "Cambiado url sincronizaci�n a:"+url);
	}
	
	public int getReintentosConexion(){
		return preferencias.getInt(REINTENTOS_CONEXION, 3);
	}
	public void setReintentosConexion(int cantidad){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(REINTENTOS_CONEXION, cantidad);
		editor.apply();
		Log.d(TAG, "Cambiado reintentos conexi�n a:"+cantidad);
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
	
	public int getNivelAtencion(){
		return preferencias.getInt(NIVEL_ATENCION, 3);
	}
	public void setNivelAtencion(int nivel){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(NIVEL_ATENCION, nivel);
		editor.apply();
		Log.d(TAG, "Cambiado nivel atenci�n:"+nivel);
	}
	
	public int getUnidadMedica(){
		return preferencias.getInt(UNIDAD_MEDICA, -1);
	}
	public void setUnidadMedica(int um){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(UNIDAD_MEDICA, um);
		editor.apply();
		Log.d(TAG, "Cambiada unidad m�dica a:"+um);
	}
	
	public int getFiltroDiasAntiguedadIras(){
		return preferencias.getInt(FILTRO_DIAS_ANTIGUEDAD_IRAS, 10);
	}
	public void setFiltroDiasAntiguedadIras(int dias){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(FILTRO_DIAS_ANTIGUEDAD_IRAS, dias);
		editor.apply();
		Log.d(TAG, "Cambiado filtro d�as de iras a:"+dias);
	}
	
	public int getFiltroDiasAntiguedadEdas(){
		return preferencias.getInt(FILTRO_DIAS_ANTIGUEDAD_EDAS, 10);
	}
	public void setFiltroDiasAntiguedadEdas(int dias){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(FILTRO_DIAS_ANTIGUEDAD_EDAS, dias);
		editor.apply();
		Log.d(TAG, "Cambiado filtro d�as de edas a:"+dias);
	}
	
	public int getFiltroDiasAntiguedadConsultas(){
		return preferencias.getInt(FILTRO_DIAS_ANTIGUEDAD_CONSULTAS, 10);
	}
	public void setFiltroDiasAntiguedadConsultas(int dias){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putInt(FILTRO_DIAS_ANTIGUEDAD_CONSULTAS, dias);
		editor.apply();
		Log.d(TAG, "Cambiado filtro d�as de consultas a:"+dias);
	}
	
	public boolean getEsInstalacionNueva(){
		return preferencias.getBoolean(ES_INSTALACION_NUEVA, true);
	}
	public void setEsInstalacionNueva(boolean valor){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putBoolean(ES_INSTALACION_NUEVA, valor);
		editor.apply();
		Log.d(TAG, "Cambiado instalaci�n nueva a:"+valor);
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
		Log.d(TAG, "Cambiado url actualizaci�n APK a:"+url);
	}
	
	/**
	 * Regresa versi�n del APK que se supone est� corriendo ahora.
	 * Si hubiera actualizaci�n del APK, este valor ayuda a definir si el APK ha cambiado y por lo tanto
	 * realizar las acciones necesarias.
	 * Esta funci�n puede usarse para reemplazar el m�todo de definici�n getEsInstalacionNueva()
	 * @return Versi�n del APK actual
	 */
	public String getVersionUltimoApkConocido(){
		return preferencias.getString(ULTIMO_APK_CONOCIDO, "0");
	}
	public void setVersionUltimoApkConocido(String versionApk){
		SharedPreferences.Editor editor = preferencias.edit();
		editor.putString(ULTIMO_APK_CONOCIDO, versionApk);
		editor.apply();
		Log.d(TAG, "Cambiado versi�n del �ltimo APK conocido a:"+versionApk);
	}
	
	/**
	 * Valida si se requiere actualizar el apk y en tal caso muestra el mensaje apropiado
	 * usando Builder dialogo.
	 * Inicia 2 Intent, el primero para mandar un mensaje a la actividad principal pidi�ndole
	 * cerrar la sesi�n de usuario. Dicha actividad valida este mensaje usando getIntent()
	 * 
	 * El segundo Intent inicia el navegador para ir al URL de actualizaci�n.
	 * @param dialogo Ventana usada para notificar al usuario
	 */
	public void ValidarRequiereActualizarApk(AlertDialog dialogo){
		if(getRequiereActualizarApk()){
			dialogo.setMessage("Esta aplicaci�n requiere actualizarse. " +
					"Puede presionar 'Actualizar' para ser enviado a la p�gina de actualizaci�n");
			dialogo.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.actualizar), new DialogInterface.OnClickListener() {
			//dialogo.setPositiveButton(R.string.actualizar, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = getBaseContext().getPackageManager()
				             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra(PrincipalActivity.FORZAR_CIERRE_SESION_USUARIO, true);
					startActivity(i);
					
					// Env�a a la p�gina de actualizaci�n
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
		Log.d(TAG, "Cambiada �ltima sincronizaci�n a:"+salida);
	}
	
	/**
	 * Genera una nueva sesi�n para usuario normal
	 * @param idUsuario Usuario que inicia sesi�n
	 */
	public void IniciarSesion(int idUsuario){
		Usuario usuario = Usuario.getUsuarioConId(this, idUsuario);
		Cursor cur = Permiso.getPermisosGrupo(this, usuario.id_grupo);
		List<Permiso> permisos = DatosUtil.ObjetosDesdeCursor(cur, Permiso.class);
		cur.close();
		this.sesion = new Sesion(usuario, null, permisos); //Asigna nueva sesi�n
		
		try{
			//Bitacora.AgregarRegistro(this, usuario._id, Bitacora.LOGIN, parametros)
		}catch(Exception e){e.printStackTrace();}
		//Actualiza las listas usadas para crear los submen�s izquierdos y men� superior
		ContenidoControles.RecargarControles(permisos);
	}
	/**
	 * Genera una nueva nueva sesi�n para usuario invitado 
	 * @param idInvitado Invitado que inicia sesi�n
	 */
	public void IniciarSesionInvitado(int idInvitado){
		UsuarioInvitado invitado = UsuarioInvitado.getUsuarioInvitadoConId(this, idInvitado);
		Usuario usuario = Usuario.getUsuarioConId(this, invitado.id_usuario_creador);
		Cursor cur = Permiso.getPermisosGrupo(this, UsuarioInvitado.ID_GRUPO);
		List<Permiso> permisos = DatosUtil.ObjetosDesdeCursor(cur, Permiso.class);
		cur.close();
		this.sesion = new Sesion(usuario, invitado, permisos);
		
		//Actualiza las listas usadas para crear los submen�s izquierdos y men� superior
		ContenidoControles.RecargarControles(permisos);
	}
	/**
	 * Cierra la sesi�n del usuario actual
	 */
	public void CerrarSesion(){
		//Registrar status relevantes
		this.sesion = null;
	}
	/**
	 * Indica si existe una sesi�n de usuario
	 * @return true cuando hay sesi�n activa y false caso contrario
	 */
	public boolean haySesion(){return this.sesion != null;}
	/**
	 * Devuelve la sesi�n de usuario actual
	 * @return Objeto {@link Sesion} actual o null
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
		String mensaje= "El dispositivo se est� sincronizando en "+getUrlSincronizacion()
				+"\nPor favor espere.";
		if(getEsInstalacionNueva())
			mensaje="("+getUrlSincronizacion()+")"+"\nEsta acci�n puede tardar varios minutos. Por favor espere.";
		boolean indeterminado=true, cancelable=false;
		this.pdProgreso = ProgressDialog.show(llamador, "Sincronizando", 
				mensaje, indeterminado, cancelable);
	}
	public void destruirDialogoProgreso(){this.pdProgreso = null;}
	public ProgressDialog getDialogoProgreso(){return pdProgreso;}

	
}