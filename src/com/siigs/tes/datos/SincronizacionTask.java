package com.siigs.tes.datos;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;








import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.tablas.*;
import com.siigs.tes.datos.tablas.graficas.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * @author Axel
 * 
 * Los 3 tipos de datos son <Parametros, Progreso, Resultado>
 * 
 */
public class SincronizacionTask extends AsyncTask<String, String, String> {

	private final static String TAG= SincronizacionTask.class.getSimpleName();
	
	//Constantes de acciones realizables con servidor
	private final static String ACCION_INICIAR_SESION="1";
	private final static String ACCION_PRIMEROS_CATALOGOS="2";
	private final static String ACCION_RESULTADO="3"; //Para informar resultado de una operaci�n (ok/error/etc)
	private final static String ACCION_PRIMEROS_DATOS = "4";
	private final static String ACCION_ENVIAR_SERVIDOR = "5";
	private final static String ACCION_RECIBIR_ACTUALIZACIONES = "6";
	//Constantes de par�metros enviados dentro de acciones de sincronizaci�n
	private final static String PARAMETRO_ID_TAB="id_tab";
	private final static String PARAMETRO_VERSION_APK = "version_apk";
	private final static String PARAMETRO_SESION ="id_sesion";
	private final static String PARAMETRO_ACCION ="id_accion";
	private final static String PARAMETRO_RESULTADO_MSG = "msg";
	private final static String PARAMETRO_DATOS = "datos";
	//Constantes de resultados enviables al servidor en ACCION_RESULTADO
	private final static String RESULTADO_OK = "ok";
	private final static String RESULTADO_JSON_EXCEPTION = "JSONException";
	private final static String RESULTADO_EXCEPTION = "Exception";
	private final static String RESULTADO_NULL_POINTER_EXCEPTION = "NullPointerException";
	private final static String RESULTADO_SQLITE_EXCEPTION = "SQLiteException";
	//Constantes de respuestas que puede devolver servidor en ACCION_INICIAR_SESION o ACCION_RESULTADO
	private final static String RESPUESTA_SESION = "id_sesion";
	private final static String RESPUESTA_INESPERADO = "id_resultado";
		//Constantes de los valores que RESPUESTA_INESPERADO contiene en json
		private final static String RESPUESTA_INESPERADO_DESACTUALIZADO = "Desactualizado";
	private final static String RESPUESTA_URL = "url"; //cuando RESPUESTA_INESPERADO vale 
											//RESPUESTA_INESPERADO_DESACTUALIZADO, esta constante es regresada tambi�n
		
	//Estados de comunicaci�n HTTP
	private final static int HTTP_STATUS_OK = 200;
	private final static int HTTP_STATUS_NOT_FOUND = 404;
	
	private final static String ARCHIVO_JSON = "descarga.json";
	
	
	//private ProgressDialog pdProgreso;
	private AlertDialog dlgResultado; //guarda dialogo que visualizar� salida
	private Context contexto;
	private Activity invocador;
	private TesAplicacion aplicacion;
	private WakeLock miWakeLock=null;
	
	private HttpHelper webHelper;
		
	//banderas de status
	boolean pedirPrimerosDatosDeNuevo = false; //Indicar� que se debe pedir de nuevo datos iniciales al servidor
	
	
	public SincronizacionTask(Activity invocador, AlertDialog resultado){
		super();
		this.invocador=invocador;
		this.dlgResultado=resultado;
		this.aplicacion = (TesAplicacion)invocador.getApplication();
		this.contexto=invocador.getApplicationContext();
		this.webHelper = new HttpHelper();

		//Lock para seguir trabajando en fondo
		try{
			PowerManager pm = (PowerManager) aplicacion.getSystemService(Context.POWER_SERVICE);
		    miWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		    miWakeLock.acquire();
		}catch(Exception e){
			Log.d(TAG,"No se pudo establecer WakeLock");
		}
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 * Se ejecuta en Thread de UI, as� que mostramos di�logo de progreso antes de comenzar tarea as�ncrona.
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//aplicacion.setUrlSincronizacion("http://192.168.3.14/tes/servicios/prueba");
		aplicacion.crearDialogoProgreso(invocador);
	}


	/**
	 * Ejecutado en hilo as�ncrono. Recibe uno o m�s par�metros en forma de arreglo ej: parametros[0]
	 */
	@Override
	protected String doInBackground(String... parametros) {
		/*try {
			Thread.sleep(5000);
			this.publishProgress("ACTUALIZACI�N");
		} catch (InterruptedException e) {
			Log.e(TAG, "interrumpido trabajo "+ e);
		}
		try {
			Thread.sleep(5000);
			this.publishProgress("otra cosa");
		} catch (InterruptedException e) {
			Log.e(TAG, "interrumpido trabajo "+ e);
		}
		return "terminado";*/
		try{
			Log.d(TAG, "Sincronizaci�n en fondo "+ Thread.currentThread().getName());
			SincronizacionTotal();
			return "Sincronizaci�n exitosa";
		}catch(Exception ex){
			Log.d(TAG, "Error:"+ex.toString());
			if(ex.getCause()!=null && (ex.getCause() instanceof NoHttpResponseException 
					||  ex.getCause() instanceof SocketTimeoutException) )
				return "No se pudo comunicar con el servidor. Verifique su conexi�n a Internet e intente de nuevo.";
			return "Hubo un problema al sincronizar" +
					"\n\nDetalles: "+ex.toString();
		}
	}

	/**
	 * Se ejecuta en Thread de UI. Quitamos el di�logo de progreso.
	 */
	@Override
	protected void onPostExecute(String resultado) {
		super.onPostExecute(resultado);

		try{miWakeLock.release();}catch(Exception e){}
		
		Log.d(TAG, "Terminado proceso en fondo "+ Thread.currentThread().getName());
		if(aplicacion.getDialogoProgreso()!=null){
			aplicacion.getDialogoProgreso().dismiss();
			aplicacion.destruirDialogoProgreso();
		}
		
		

		this.dlgResultado.setMessage(resultado);
		
		if(aplicacion.getRequiereActualizarApk()){
			aplicacion.ValidarRequiereActualizarApk(dlgResultado);
		}else{
			this.dlgResultado.show();
		}
	}

	/**
	 * Se ejecuta en Thread de UI. 
	 * Es llamado desde este thread asincrono con publishProgress()
	 * Actualiza el contenido del mensaje de espera.
	 * @param values Arreglo contenedor del mensaje a publicar en localidad 0
	 */
	@Override
	protected void onProgressUpdate(String... values) {
		super.onProgressUpdate(values);
		
		if(aplicacion.getDialogoProgreso()!=null){
			aplicacion.getDialogoProgreso().setMessage(values[0]);
		}
		Log.d(TAG, values[0]);
	}
	

	/**
	 * Implementa la sincronizaci�n de datos
	 * @throws Exception 
	 */
	protected synchronized void SincronizacionTotal() throws Exception{
		boolean esNueva= this.aplicacion.getEsInstalacionNueva();

		String idSesion = AccionIniciarSesion();
		Log.d(TAG,"Se ha obtenido llave de sesi�n:"+idSesion);
		publishProgress("Sesi�n iniciada...");
		
		if(esNueva){
			AccionPrimerosCatalogos(idSesion);
			AccionPrimerosDatos(idSesion);
			//Si por accidente externo a tableta no hubieran usuarios, evitamos bloquear sincronizaci�n futura
			if(Usuario.getTotalUsuariosActivos(contexto)>0)
				this.aplicacion.setEsInstalacionNueva(false);
		}else{
			String ultimaSinc = this.aplicacion.getFechaUltimaSincronizacion();
			AccionEnviarCambiosServidor(idSesion, ultimaSinc);
			if(!this.aplicacion.getRequiereActualizarApk()){
				BorrarDatosAntesSinc();
				AccionRecibirActualizaciones(idSesion);
			}
		}
		this.aplicacion.setFechaUltimaSincronizacion();
 	}

	/**
	 * Informa de resultados al servidor.
	 * @param idSesion Sesi�n que se lleva a cabo
	 * @param idResultado Identificador del tipo de resultado (�xito o error) que informamos
	 * @param descripcion Describe a detalle la causa de idResultado en caso de tratarse de un error
	 * @return String con resultado del request o null en caso contrario.
	 */
	private String EnviarResultado(String idSesion, String idResultado, String descripcion){
		JSONObject msgSalida=new JSONObject();
		try {
			msgSalida.put("id_resultado", idResultado);
			msgSalida.put("descripcion", descripcion == null?"":descripcion);
		} catch (JSONException e) {
			Log.e(TAG, "Esto nunca en la vida deber�a suceder. No se pudo encapsular resultado en JSON."+e.toString());
		}
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
		parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_RESULTADO));
        parametros.add(new BasicNameValuePair(PARAMETRO_RESULTADO_MSG, msgSalida.toString() ));
		
        Log.d(TAG,"Enviando resultado de acci�n con id_resultado:"+ idResultado+ " y descripci�n:"+descripcion);
		try {
			return webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
		} catch (Exception e) {
			Log.d(TAG,"No se pudo enviar resultado. "+e);
		}
		return null;
	}
	
	/**
	 * Esta acci�n recibe como respuesta una cadena JSON con el id de la sesi�n de sincronizaci�n
	 * @return id de la sesi�n
	 * @throws Exception 
	 */
	private String AccionIniciarSesion() throws Exception {
		WifiManager wifi=(WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);
		String macaddress = wifi.getConnectionInfo().getMacAddress();
		if(macaddress == null)macaddress="";
		macaddress = macaddress.replace(":", "");
		if(macaddress.equals(""))macaddress= "123456789"; //PARA DEBUGEO, EN DISPOSITIVO REAL NO DEBER�A PASAR
		//macaddress="08606E3CCAC5";//macaddress="08606E418F7d"; //TODO COMENTAR L�NEA
		Log.d(TAG, "mac es:"+macaddress);
		
		String version= aplicacion.getVersionApk();
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_INICIAR_SESION));
        parametros.add(new BasicNameValuePair(PARAMETRO_ID_TAB, macaddress));
        parametros.add(new BasicNameValuePair(PARAMETRO_VERSION_APK, version+"" ));
		
        Log.d(TAG, "Request Inicio de sesi�n");
        publishProgress("Conectando con servidor...");
		String json = webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
		
		JSONObject jo;
		try {
			jo = new JSONObject(json);
		} catch (JSONException e) {
			String msgError="No se interpret� el resultado del servidor como json:"+json+"\n"+e.toString();
			Log.e(TAG, msgError);
			throw new Exception(msgError);
		}
		
		if(jo.has(PARAMETRO_SESION))
			return jo.getString(RESPUESTA_SESION);
		
		//Como no lleg� id sesi�n, asumimos que hay un mensaje detallado en RESPUESTA_INESPERADO
		String inesperado = jo.getString(RESPUESTA_INESPERADO);
		if(inesperado.equalsIgnoreCase(RESPUESTA_INESPERADO_DESACTUALIZADO)){
			DefinirComoDispositivoSinActualizar(jo.getString(RESPUESTA_URL));
			throw new Exception("Esta aplicaci�n requiere actualizarse antes de sincronizarse");
		}
		
		throw new Exception(inesperado);
	}//fin AccionIniciarSesion
	
	
	/**
	 * Esta acci�n manda credenciales y recibe primeros cat�logos a insertar en base de datos
	 * @param idSesion Identificador de la sesi�n con el servidor
	 * @throws Exception 
	 */
	private void AccionPrimerosCatalogos(String idSesion) throws Exception{
		String msgError;
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_PRIMEROS_CATALOGOS));

        publishProgress("Solicitando primeros cat�logos");
		InputStream stream = webHelper.RequestStreamPost(aplicacion.getUrlSincronizacion(), parametros);

		stream = GuardarAbrirStream(stream);
		
		try {
			InterpretarDatosServidor(stream);
			
			EnviarResultado(idSesion, RESULTADO_OK,null);
			
		}catch (SQLiteException e){
			msgError = "Error al interpretar primeros cat�logos en base de datos local:" + e.toString(); 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_SQLITE_EXCEPTION, msgError);
			throw e;
		}catch (NullPointerException e){
			msgError = "Error al interpretar primeros cat�logos. Se intent� accesar algo que no existe:"+ e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_NULL_POINTER_EXCEPTION, msgError);
			throw e;
		} catch (Exception e){
			msgError = "Error desconocido al interpretar primeros cat�logos:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_EXCEPTION, msgError);
			throw e;
		}
		//... validar lleg� lo m�nimo, caso contrario, error
	}
	
	/**
	 * Esta acci�n manda credenciales y recibe primeras tablas transaccionales a insertar en base de datos
	 * @param idSesion Identificador de la sesi�n con el servidor
	 * @throws Exception 
	 */
	private void AccionPrimerosDatos(String idSesion) throws Exception{
		String msgError;
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_PRIMEROS_DATOS));

        publishProgress("Solicitando primeros datos transaccionales");
		InputStream stream = webHelper.RequestStreamPost(aplicacion.getUrlSincronizacion(), parametros);
		
		stream = GuardarAbrirStream(stream);
		
		try {
			InterpretarDatosServidor(stream);
			
			EnviarResultado(idSesion, RESULTADO_OK,null);
			
		}catch (SQLiteException e){
			msgError = "Error al interpretar primeros cat�logos en base de datos local:" + e.toString(); 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_SQLITE_EXCEPTION, msgError);
			throw e;
		}catch (NullPointerException e){
			msgError = "Error al interpretar primeros cat�logos. Se intent� accesar algo que no existe:"+ e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_NULL_POINTER_EXCEPTION, msgError);
			throw e;
		} catch (Exception e){
			msgError = "Error desconocido al interpretar primeros cat�logos:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_EXCEPTION, msgError);
			throw e;
		}
	}
	
	/**
	 * Esta acci�n manda credenciales y recibe cambios a realizar en base de datos
	 * @param idSesion Identificador de la sesi�n con el servidor
	 * @throws Exception 
	 */
	private void AccionRecibirActualizaciones(String idSesion) throws Exception{
		String msgError;
		
		List<NameValuePair> parametros = new ArrayList<NameValuePair>();
        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_RECIBIR_ACTUALIZACIONES));

        publishProgress("Solicitando actualizaciones de datos");
		InputStream stream = webHelper.RequestStreamPost(aplicacion.getUrlSincronizacion(), parametros);
		
		stream = GuardarAbrirStream(stream);
		
		try {
			InterpretarDatosServidor(stream); // aqu� puede cambiar bandera para pedir primeros datos de nuevo
			
			if(pedirPrimerosDatosDeNuevo){
				AccionPrimerosDatos(idSesion); //Hace su propio EnviarResultado();
			}else{
				EnviarResultado(idSesion, RESULTADO_OK,null);
			}
			
			
		}catch (SQLiteException e){
			msgError = "Error al interpretar actualizaciones recibidas para base de datos local:" + e.toString(); 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_SQLITE_EXCEPTION, msgError);
			throw e;
		}catch (NullPointerException e){
			msgError = "Error al interpretar actualizaciones. Se intent� accesar algo que no existe:"+ e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_NULL_POINTER_EXCEPTION, msgError);
			throw e;
		} catch (Exception e){
			msgError = "Error desconocido al interpretar actualizaciones:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_EXCEPTION, msgError);
			throw e;
		}
	}
	
	/**
	 * Interpreta los datos recibidos del servidor que deben estar en formato JSON.
	 * @param stream Stream de datos que contiene la respuesta del servidor
	 * @throws Exception Cuando ocurre alg�n error
	 */
	private void InterpretarDatosServidor(InputStream stream) throws Exception{
		ContentResolver cr = contexto.getContentResolver();
		
		JsonReader reader;
		Gson gson=new Gson();
		String atributo=""; //para iterar atributos json
		
		Uri uri; //helper
		ContentValues fila; //helper
		
		try {
			reader = new JsonReader(new InputStreamReader(stream,"UTF-8"));	
			reader.beginObject(); //Lee objeto principal
			//Lectura de cat�logos/datos fijos
			while(reader.hasNext()){
				atributo=reader.nextName();
				
				if(atributo.equalsIgnoreCase("id_tipo_censo")){
					publishProgress("Asignando tipo de senso");
					int nuevoCenso = reader.nextInt();
					if(aplicacion.getTipoCenso() != nuevoCenso){
						//Habr� cambio de censo as� que borramos TODAS las transaccionales
						RequerirRestaurarTransaccionales();
					}
					aplicacion.setTipoCenso(nuevoCenso);
					
				}else if(atributo.equalsIgnoreCase("id_asu_um")){
					publishProgress("Asignando unidad m�dica");
					int nuevaUM = reader.nextInt();
					if(aplicacion.getUnidadMedica() != nuevaUM){
						//Habr� cambio de unidad m�dica as� que borramos TODAS las transaccionales
						RequerirRestaurarTransaccionales();
					}
					aplicacion.setUnidadMedica( nuevaUM );
					
				}else if(atributo.equalsIgnoreCase("id_nivel_atencion")){
					publishProgress("Asignando nivel de atenci�n");
					int nuevoNivel = reader.nextInt();
					aplicacion.setNivelAtencion(nuevoNivel);
					
				}else if(atributo.equalsIgnoreCase("persona_x_borrar")){
					publishProgress("Borrando Personas espec�ficas");
					reader.beginArray();
					while(reader.hasNext()){
						String idPersona = reader.nextString();
						publishProgress("Borrando persona "+idPersona);
						cr.delete(ProveedorContenido.PERSONA_CONTENT_URI, Persona.ID+"=?", new String[]{idPersona});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Grupo.NOMBRE_TABLA)){
					publishProgress("Interpretando Grupos");
					//Grupos llegan todos o ninguno, as� que hacemos backup, borramos, recibimos los nuevos.
					//Si hay error en parseo/inserci�n, borramos lo realizado y restauramos el backup
					List<Grupo> backup = Grupo.getTodos(contexto);
					cr.delete(ProveedorContenido.GRUPO_CONTENT_URI, null, null);
					try{
						reader.beginArray();
						while(reader.hasNext()){
							Grupo grupo = gson.fromJson(reader, Grupo.class);
							fila = DatosUtil.ContentValuesDesdeObjeto(grupo);
							uri = ProveedorContenido.GRUPO_CONTENT_URI;
							if(cr.insert(uri, fila)==null)
								cr.update(uri, fila, Grupo.ID+"="+grupo._id,null);
						}
						reader.endArray();
					}catch(Exception e){
						publishProgress("Error de parseo � inserci�n en Grupos. Restaurando Grupos viejos");
						cr.delete(ProveedorContenido.GRUPO_CONTENT_URI, null, null);
						Grupo.AgregarRegistros(contexto, backup);
						publishProgress("Permisos viejos restaurados. Arrojando excepci�n");
						throw e;
					}

				}else if(atributo.equalsIgnoreCase(Usuario.NOMBRE_TABLA)){
					publishProgress("Interpretando Usuarios");
					//Usuarios llegan todos o ninguno, as� que hacemos backup, borramos, recibimos los nuevos.
					//Si hay error en parseo/inserci�n, borramos lo realizado y restauramos el backup
					List<Usuario> backup = Usuario.getTodos(contexto);
					cr.delete(ProveedorContenido.USUARIO_CONTENT_URI, null, null);
					try{
						reader.beginArray();
						while(reader.hasNext()){
							Usuario usuario = gson.fromJson(reader, Usuario.class);
							fila = DatosUtil.ContentValuesDesdeObjeto(usuario);
							uri = ProveedorContenido.USUARIO_CONTENT_URI;
							if(cr.insert(uri, fila)==null)
								cr.update(uri, fila, Usuario.ID+"="+usuario._id,null);
						}
						reader.endArray();
					}catch(Exception e){
						publishProgress("Error de parseo � inserci�n en Usuarios. Restaurando Usuarios viejos");
						cr.delete(ProveedorContenido.USUARIO_CONTENT_URI, null, null);
						Usuario.AgregarRegistros(contexto, backup);
						publishProgress("Usuarios viejos restaurados. Arrojando excepci�n");
						throw e;
					}
					
				}else if(atributo.equalsIgnoreCase(Permiso.NOMBRE_TABLA)){
					publishProgress("Interpretando Permisos");
					//Permisos llegan todos o ninguno, as� que hacemos backup, borramos, recibimos los nuevos.
					//Si hay error en parseo/inserci�n, borramos lo realizado y restauramos el backup
					List<Permiso> backup = Permiso.getTodos(contexto);
					cr.delete(ProveedorContenido.PERMISO_CONTENT_URI, null, null);
					try{
						reader.beginArray();
						while(reader.hasNext()){
							Permiso permiso = gson.fromJson(reader, Permiso.class);
							fila = DatosUtil.ContentValuesDesdeObjeto(permiso);
							uri = ProveedorContenido.PERMISO_CONTENT_URI;
							if(cr.insert(uri, fila)==null)
								cr.update(uri, fila, Permiso.ID+"="+permiso._id,null);
						}
						reader.endArray();
					}catch(Exception e){
						publishProgress("Error de parseo � inserci�n en Permisos. Restaurando Permisos viejos");
						cr.delete(ProveedorContenido.PERMISO_CONTENT_URI, null, null);
						Permiso.AgregarRegistros(contexto, backup);
						publishProgress("Permisos viejos restaurados. Arrojando excepci�n");
						throw e;
					}
					
				}else if(atributo.equalsIgnoreCase(Notificacion.NOMBRE_TABLA)){
					publishProgress("Interpretando Notificaciones");
					reader.beginArray();
					while(reader.hasNext()){
						Notificacion notificacion = gson.fromJson(reader, Notificacion.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(notificacion);
						uri = ProveedorContenido.NOTIFICACION_CONTENT_URI;
						cr.insert(uri, fila); //Notificaciones siempre son nuevas
							//cr.update(uri, fila, Notificacion.ID+"="+notificacion._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(TipoSanguineo.NOMBRE_TABLA)){
					publishProgress("Interpretando Tipo Sanguineo");
					reader.beginArray();
					while(reader.hasNext()){
						TipoSanguineo tipoSangre = gson.fromJson(reader, TipoSanguineo.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(tipoSangre);
						uri = ProveedorContenido.TIPO_SANGUINEO_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, TipoSanguineo.ID+"="+tipoSangre._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Vacuna.NOMBRE_TABLA)){
					publishProgress("Interpretando Vacunas");
					reader.beginArray();
					while(reader.hasNext()){
						Vacuna vacuna = gson.fromJson(reader, Vacuna.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(vacuna);
						uri = ProveedorContenido.VACUNA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Vacuna.ID+"="+vacuna._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(AccionNutricional.NOMBRE_TABLA)){
					publishProgress("Interpretando Acciones Nutricionales");
					reader.beginArray();
					while(reader.hasNext()){
						AccionNutricional accion = gson.fromJson(reader, AccionNutricional.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(accion);
						uri = ProveedorContenido.ACCION_NUTRICIONAL_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, AccionNutricional.ID+"="+accion._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Ira.NOMBRE_TABLA)){
					publishProgress("Interpretando Iras");
					reader.beginArray();
					while(reader.hasNext()){
						Ira ira = gson.fromJson(reader, Ira.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(ira);
						uri = ProveedorContenido.IRA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Ira.ID+"="+ira._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Eda.NOMBRE_TABLA)){
					publishProgress("Interpretando Edas");
					reader.beginArray();
					while(reader.hasNext()){
						Eda eda = gson.fromJson(reader, Eda.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(eda);
						uri = ProveedorContenido.EDA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Eda.ID+"="+eda._id,null);
					}
					reader.endArray();
					
				/*}else if(atributo.equalsIgnoreCase(Consulta.NOMBRE_TABLA)){
					publishProgress("Interpretando Consultas de CIE10");
					reader.beginArray();
					while(reader.hasNext()){
						Consulta consulta = gson.fromJson(reader, Consulta.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(consulta);
						uri = ProveedorContenido.CONSULTA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Consulta.ID_CIE10+"=?",new String[]{consulta.id_cie10});
					}
					reader.endArray();*/
				}else if(atributo.equalsIgnoreCase(Consulta.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
						ProveedorContenido.CONSULTA_CONTENT_URI, Consulta.ID_CIE10+"=?", new String[]{Consulta.ID_CIE10}, 
						"Consultas de CIE10", Consulta.class);
					
				}else if(atributo.equalsIgnoreCase(Alergia.NOMBRE_TABLA)){
					publishProgress("Interpretando Alergias");
					reader.beginArray();
					while(reader.hasNext()){
						Alergia alergia = gson.fromJson(reader, Alergia.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(alergia);
						uri = ProveedorContenido.ALERGIA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Alergia.ID+"="+alergia._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Afiliacion.NOMBRE_TABLA)){
					publishProgress("Interpretando Afiliaciones");
					reader.beginArray();
					while(reader.hasNext()){
						Afiliacion afiliacion = gson.fromJson(reader, Afiliacion.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(afiliacion);
						uri = ProveedorContenido.AFILIACION_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Afiliacion.ID+"="+afiliacion._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Nacionalidad.NOMBRE_TABLA)){
					publishProgress("Interpretando Nacionalidades");
					reader.beginArray();
					while(reader.hasNext()){
						Nacionalidad nacionalidad = gson.fromJson(reader, Nacionalidad.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(nacionalidad);
						uri = ProveedorContenido.NACIONALIDAD_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Nacionalidad.ID+"="+nacionalidad._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(OperadoraCelular.NOMBRE_TABLA)){
					publishProgress("Interpretando Operadoras Celulares");
					reader.beginArray();
					while(reader.hasNext()){
						OperadoraCelular operadora = gson.fromJson(reader, OperadoraCelular.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(operadora);
						uri = ProveedorContenido.OPERADORA_CELULAR_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, OperadoraCelular.ID+"="+operadora._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(PendientesTarjeta.NOMBRE_TABLA)){
					publishProgress("Interpretando Pendientes para Tarjetas");
					reader.beginArray();
					while(reader.hasNext()){
						PendientesTarjeta pendiente = gson.fromJson(reader, PendientesTarjeta.class);
						PendientesTarjeta.AgregarNuevoPendienteForaneo(contexto, pendiente);
						//fila = DatosUtil.ContentValuesDesdeObjeto(pendiente);
						//uri = ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI;
						//cr.insert(uri, fila);
							//cr.update(uri, fila, PendientesTarjeta.ID_PERSONA+"=? and "+PendientesTarjeta.REGISTRO_JSON +"=? ",new String[]{pendiente.id_persona,});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ArbolSegmentacion.NOMBRE_TABLA)){
					//InterpretarArbolSegmentacion(gson, reader, cr);
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.ARBOL_SEGMENTACION_CONTENT_URI, 
							ArbolSegmentacion.ID+"=?", new String[]{ArbolSegmentacion.ID}, 
							"Arbol de segmentaci�n", ArbolSegmentacion.class);
					
				}else if(atributo.equalsIgnoreCase(ReglaVacuna.NOMBRE_TABLA)){
					publishProgress("Interpretando Reglas de vacunaci�n");
					reader.beginArray();
					while(reader.hasNext()){
						ReglaVacuna regla = gson.fromJson(reader, ReglaVacuna.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(regla);
						uri = ProveedorContenido.REGLA_VACUNA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ReglaVacuna.ID+"="+regla.id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ViaVacuna.NOMBRE_TABLA)){
					publishProgress("Interpretando V�as Vacuna");
					reader.beginArray();
					while(reader.hasNext()){
						ViaVacuna via = gson.fromJson(reader, ViaVacuna.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(via);
						uri = ProveedorContenido.VIA_VACUNA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ViaVacuna.ID+"="+via._id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(Tratamiento.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.TRATAMIENTO_CONTENT_URI, Tratamiento.ID+"=?", new String[]{Tratamiento.ID}, 
							"Tratamientos", Tratamiento.class);
					
				}else if(atributo.equalsIgnoreCase(PartoMultiple.NOMBRE_TABLA)){
					publishProgress("Interpretando Partos M�ltiples");
					reader.beginArray();
					while(reader.hasNext()){
						PartoMultiple parto = gson.fromJson(reader, PartoMultiple.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(parto);
						uri = ProveedorContenido.PARTO_MULTIPLE_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, PartoMultiple.ID+"="+parto.id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(EstadoVisita.NOMBRE_TABLA)){
					publishProgress("Interpretando Estados de Visitas");
					reader.beginArray();
					while(reader.hasNext()){
						EstadoVisita estado = gson.fromJson(reader, EstadoVisita.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(estado);
						uri = ProveedorContenido.ESTADO_VISITA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, EstadoVisita.ID+"="+estado.id,null);
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(EstadoImc.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.ESTADO_IMC_CONTENT_URI, EstadoImc.ID+"=?", new String[]{EstadoImc.ID}, 
							"Estados de IMC", EstadoImc.class);
					
				}else if(atributo.equalsIgnoreCase(EstadoImcPorEdad.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.ESTADO_IMC_POR_EDAD_CONTENT_URI, 
							EstadoImcPorEdad.SEXO+"=? and "+ EstadoImcPorEdad.EDAD_MESES+"=? and "+EstadoImcPorEdad.ID_ESTADO_IMC+"=?", 
							new String[]{EstadoImcPorEdad.SEXO, EstadoImcPorEdad.EDAD_MESES, EstadoImcPorEdad.ID_ESTADO_IMC}, 
							"Estados de IMC por Edad", EstadoImcPorEdad.class);
					
				}else if(atributo.equalsIgnoreCase(EstadoNutricionAltura.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.ESTADO_NUTRICION_ALTURA_CONTENT_URI, EstadoNutricionAltura.ID+"=?", new String[]{EstadoNutricionAltura.ID}, 
							"Estados de Nutrici�n-Altura", EstadoNutricionAltura.class);
					
				}else if(atributo.equalsIgnoreCase(EstadoNutricionAlturaPorEdad.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.ESTADO_NUTRICION_ALTURA_POR_EDAD_CONTENT_URI, 
							EstadoNutricionAlturaPorEdad.SEXO+"=? and "+ EstadoNutricionAlturaPorEdad.EDAD_MESES+"=? and "+EstadoNutricionAlturaPorEdad.ID_ESTADO_NUTRICION_ALTURA+"=?", 
							new String[]{EstadoNutricionAlturaPorEdad.SEXO, EstadoNutricionAlturaPorEdad.EDAD_MESES, EstadoNutricionAlturaPorEdad.ID_ESTADO_NUTRICION_ALTURA}, 
							"Estados de Nutrici�n-Altura por Edad", EstadoNutricionAlturaPorEdad.class);
				
				}else if(atributo.equalsIgnoreCase(EstadoNutricionPeso.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.ESTADO_NUTRICION_PESO_CONTENT_URI, EstadoNutricionPeso.ID+"=?", new String[]{EstadoNutricionPeso.ID}, 
							"Estados de Nutrici�n-Peso", EstadoNutricionPeso.class);
					
				}else if(atributo.equalsIgnoreCase(EstadoNutricionPesoPorEdad.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.ESTADO_NUTRICION_PESO_POR_EDAD_CONTENT_URI, 
							EstadoNutricionPesoPorEdad.SEXO+"=? and "+ EstadoNutricionPesoPorEdad.EDAD_MESES+"=? and "+EstadoNutricionPesoPorEdad.ID_ESTADO_NUTRICION_PESO+"=?", 
							new String[]{EstadoNutricionPesoPorEdad.SEXO, EstadoNutricionPesoPorEdad.EDAD_MESES, EstadoNutricionPesoPorEdad.ID_ESTADO_NUTRICION_PESO}, 
							"Estados de Nutrici�n-Peso por Edad", EstadoNutricionPesoPorEdad.class);
				
				}else if(atributo.equalsIgnoreCase(EstadoNutricionPesoPorAltura.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.ESTADO_NUTRICION_PESO_POR_ALTURA_CONTENT_URI, 
							EstadoNutricionPesoPorAltura.SEXO+"=? and "+ EstadoNutricionPesoPorAltura.ALTURA+"=? and "+EstadoNutricionPesoPorAltura.ID_ESTADO_NUTRICION_PESO+"=?", 
							new String[]{EstadoNutricionPesoPorAltura.SEXO, EstadoNutricionPesoPorAltura.ALTURA, EstadoNutricionPesoPorAltura.ID_ESTADO_NUTRICION_PESO}, 
							"Estados de Nutrici�n-Peso por Altura", EstadoNutricionPesoPorAltura.class);
				
				}else if(atributo.equalsIgnoreCase(EstadoPerimetroCefalico.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.ESTADO_PERIMETRO_CONTENT_URI, EstadoPerimetroCefalico.ID+"=?", new String[]{EstadoPerimetroCefalico.ID}, 
							"Estados de Per�metro Cef�lico", EstadoPerimetroCefalico.class);
					
				}else if(atributo.equalsIgnoreCase(EstadoPerimetroCefalicoPorEdad.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.ESTADO_PERIMETRO_POR_EDAD_CONTENT_URI, 
							EstadoPerimetroCefalicoPorEdad.SEXO+"=? and "+ EstadoPerimetroCefalicoPorEdad.EDAD_MESES+"=? and "+EstadoPerimetroCefalicoPorEdad.ID_ESTADO_PERI_CEFA+"=?", 
							new String[]{EstadoPerimetroCefalicoPorEdad.SEXO, EstadoPerimetroCefalicoPorEdad.EDAD_MESES, EstadoPerimetroCefalicoPorEdad.ID_ESTADO_PERI_CEFA}, 
							"Estados de Per�metro Cef�lico por Edad", EstadoPerimetroCefalicoPorEdad.class);
					
				}else if(atributo.equalsIgnoreCase(HemoglobinaAltitud.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.HEMOGLOBINA_ALTITUD_CONTENT_URI,
							HemoglobinaAltitud.ID_LOCALIDAD_ASU+"=?", new String[]{HemoglobinaAltitud.ID_LOCALIDAD_ASU}, 
							"Niveles de Hemoglobina", HemoglobinaAltitud.class);
					
				}else if(atributo.equalsIgnoreCase(GrupoAtencion.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.GRUPO_ATENCION_CONTENT_URI, 
							GrupoAtencion.GRUPO_ATENCION+"=? and "+ GrupoAtencion.NIVEL_ATENCION+"=?", 
							new String[]{GrupoAtencion.GRUPO_ATENCION, GrupoAtencion.NIVEL_ATENCION}, 
							"Grupos de atenci�n", GrupoAtencion.class);
					
				}else if(atributo.equalsIgnoreCase(CategoriaCie10.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.CATEGORIA_CIE10_CONTENT_URI, 
							CategoriaCie10.ID+"=?", new String[]{CategoriaCie10.ID}, 
							"Categor�as de CIE10", CategoriaCie10.class);

					
				//////////////////TABLAS TRANSACCIONALES/////////////////
				}else if(atributo.equalsIgnoreCase(Tutor.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.TUTOR_CONTENT_URI, Tutor.ID+"=?", new String[]{Tutor.ID}, 
							"Tutores", Tutor.class);
					/*publishProgress("Interpretando Tutores");
					reader.beginArray();
					while(reader.hasNext()){
						Tutor tutor = gson.fromJson(reader, Tutor.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(tutor);
						uri = ProveedorContenido.TUTOR_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Tutor.ID+"=?",new String[]{tutor.id});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(Persona.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.PERSONA_CONTENT_URI, Persona.ID+"=?", new String[]{Persona.ID}, 
							"Personas", Persona.class);
					/*publishProgress("Interpretando Personas");
					reader.beginArray();
					while(reader.hasNext()){
						Persona persona = gson.fromJson(reader, Persona.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(persona);
						uri = ProveedorContenido.PERSONA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, Persona.ID+"=?",new String[]{persona.id});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(PersonaTutor.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr,
							ProveedorContenido.PERSONA_TUTOR_CONTENT_URI, 
							PersonaTutor.ID_PERSONA+"=?", new String[]{PersonaTutor.ID_PERSONA},
							"Personas con Tutores", PersonaTutor.class);
					/*publishProgress("Interpretando Personas con Tutores");
					reader.beginArray();
					while(reader.hasNext()){
						PersonaTutor persona_tutor = gson.fromJson(reader, PersonaTutor.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(persona_tutor);
						uri = ProveedorContenido.PERSONA_TUTOR_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, PersonaTutor.ID_PERSONA+"=?",new String[]{persona_tutor.id_persona});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(PersonaAlergia.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI, 
							PersonaAlergia.ID_PERSONA+"=?", new String[]{PersonaAlergia.ID_PERSONA},
							"Personas con alergias", PersonaAlergia.class);
					/*publishProgress("Interpretando Personas con alergias");
					reader.beginArray();
					while(reader.hasNext()){
						PersonaAlergia persona_alergia = gson.fromJson(reader, PersonaAlergia.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(persona_alergia);
						uri = ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, PersonaAlergia.ID_PERSONA+"=?",new String[]{persona_alergia.id_persona});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(PersonaAfiliacion.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.PERSONA_AFILIACION_CONTENT_URI, 
							PersonaAfiliacion.ID_PERSONA+"=? and "+PersonaAfiliacion.ID_AFILIACION+"=?", 
							new String[]{PersonaAfiliacion.ID_PERSONA, PersonaAfiliacion.ID_AFILIACION}, 
							"Personas con afiliaciones", PersonaAfiliacion.class);
					/*publishProgress("Interpretando Personas con afiliaciones");
					reader.beginArray();
					while(reader.hasNext()){
						PersonaAfiliacion persona_afiliacion = gson.fromJson(reader, PersonaAfiliacion.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(persona_afiliacion);
						uri = ProveedorContenido.PERSONA_AFILIACION_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, PersonaAfiliacion.ID_PERSONA+"=? and "
									+PersonaAfiliacion.ID_AFILIACION+"=?",
									new String[]{persona_afiliacion.id_persona, persona_afiliacion.id_afiliacion+""});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(RegistroCivil.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.REGISTRO_CIVIL_CONTENT_URI, 
							RegistroCivil.ID_PERSONA+"=?",new String[]{RegistroCivil.ID_PERSONA}, 
							"Registro Civil", RegistroCivil.class);
					/*publishProgress("Interpretando Registro Civil");
					reader.beginArray();
					while(reader.hasNext()){
						RegistroCivil registro = gson.fromJson(reader, RegistroCivil.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(registro);
						uri = ProveedorContenido.REGISTRO_CIVIL_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, RegistroCivil.ID_PERSONA+"=?",new String[]{registro.id_persona});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(AntiguaUM.NOMBRE_TABLA)){
					publishProgress("Interpretando Antiguas unidades m�dicas");
					reader.beginArray();
					while(reader.hasNext()){
						AntiguaUM antiguaUM = gson.fromJson(reader, AntiguaUM.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(antiguaUM);
						uri = ProveedorContenido.ANTIGUA_UM_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, AntiguaUM.ID_PERSONA+"=? and "+ AntiguaUM.FECHA_CAMBIO+"=?", 
									new String[]{antiguaUM.id_persona,antiguaUM.fecha_cambio});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(AntiguoDomicilio.NOMBRE_TABLA)){
					publishProgress("Interpretando Antiguos domicilios");
					reader.beginArray();
					while(reader.hasNext()){
						AntiguoDomicilio domicilio = gson.fromJson(reader, AntiguoDomicilio.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(domicilio);
						uri = ProveedorContenido.ANTIGUO_DOMICILIO_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, AntiguoDomicilio.ID_PERSONA+"=? and "+ AntiguoDomicilio.FECHA_CAMBIO+"=?", 
									new String[]{domicilio.id_persona, domicilio.fecha_cambio});
					}
					reader.endArray();
					
				}else if(atributo.equalsIgnoreCase(ControlVacuna.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, 
							ControlVacuna.ID_VACUNA+"=? and "+ ControlVacuna.FECHA+"=? and "+ControlVacuna.ID_PERSONA+"=?", 
							new String[]{ControlVacuna.ID_VACUNA, ControlVacuna.FECHA, ControlVacuna.ID_PERSONA}, 
							"Controles de Vacunas", ControlVacuna.class);
					/*publishProgress("Interpretando Controles de Vacunas");
					reader.beginArray();
					while(reader.hasNext()){
						ControlVacuna control_vacuna = gson.fromJson(reader, ControlVacuna.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_vacuna);
						uri = ProveedorContenido.CONTROL_VACUNA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlVacuna.ID_PERSONA+"=? and "+ ControlVacuna.FECHA+"=?", 
									new String[]{control_vacuna.id_persona, control_vacuna.fecha});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(ControlIra.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, ProveedorContenido.CONTROL_IRA_CONTENT_URI, 
							ControlIra.ID_IRA+"=? and "+ ControlIra.FECHA+"=? and "+ControlIra.ID_PERSONA+"=?", 
							new String[]{ControlIra.ID_IRA, ControlIra.FECHA, ControlIra.ID_PERSONA}, 
							"Controles de Iras", ControlIra.class);
					/*publishProgress("Interpretando Controles de Iras");
					reader.beginArray();
					while(reader.hasNext()){
						ControlIra control_ira = gson.fromJson(reader, ControlIra.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_ira);
						uri = ProveedorContenido.CONTROL_IRA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlIra.ID_PERSONA+"=? and "+ ControlIra.FECHA+"=?", 
									new String[]{control_ira.id_persona, control_ira.fecha});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(ControlEda.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.CONTROL_EDA_CONTENT_URI, 
							ControlEda.ID_EDA+"=? and "+ ControlEda.FECHA+"=? and "+ControlEda.ID_PERSONA+"=?", 
							new String[]{ControlEda.ID_EDA, ControlEda.FECHA, ControlEda.ID_PERSONA}, 
							"Controles de Edas", ControlEda.class);
					/*publishProgress("Interpretando Controles de Edas");
					reader.beginArray();
					while(reader.hasNext()){
						ControlEda control_eda = gson.fromJson(reader, ControlEda.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_eda);
						uri = ProveedorContenido.CONTROL_EDA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlEda.ID_PERSONA+"=? and "+ ControlEda.FECHA+"=?", 
									new String[]{control_eda.id_persona, control_eda.fecha});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(ControlConsulta.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI, 
							ControlConsulta.CLAVE_CIE10+"=? and "+ ControlConsulta.FECHA+"=? and "+ControlConsulta.ID_PERSONA+"=?", 
							new String[]{ControlConsulta.CLAVE_CIE10, ControlConsulta.FECHA, ControlConsulta.ID_PERSONA}, 
							"Controles de Consultas", ControlConsulta.class);
					/*publishProgress("Interpretando Controles de Consultas");
					reader.beginArray();
					while(reader.hasNext()){
						ControlConsulta control_consulta = gson.fromJson(reader, ControlConsulta.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_consulta);
						uri = ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlConsulta.ID_PERSONA+"=? and "+ ControlConsulta.FECHA+"=?", 
									new String[]{control_consulta.id_persona, control_consulta.fecha});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(ControlAccionNutricional.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_CONTENT_URI, 
							ControlAccionNutricional.ID_ACCION_NUTRICIONAL+"=? and "+ ControlAccionNutricional.FECHA+"=? and "+ControlAccionNutricional.ID_PERSONA+"=?", 
							new String[]{ControlAccionNutricional.ID_ACCION_NUTRICIONAL, ControlAccionNutricional.FECHA, ControlAccionNutricional.ID_PERSONA}, 
							"Controles de Acciones Nutricionales", ControlAccionNutricional.class);
					/*publishProgress("Interpretando Controles de Acciones Nutricionales");
					reader.beginArray();
					while(reader.hasNext()){
						ControlAccionNutricional control_accion = gson.fromJson(reader, ControlAccionNutricional.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_accion);
						uri = ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlAccionNutricional.ID_PERSONA+"=? and "+ ControlAccionNutricional.FECHA+"=?", 
									new String[]{control_accion.id_persona, control_accion.fecha});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(ControlNutricional.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI, 
							ControlNutricional.ID_PERSONA+"=? and "+ ControlNutricional.FECHA+"=?", 
							new String[]{ControlNutricional.ID_PERSONA, ControlNutricional.FECHA},
							"Controles Nutricionales", ControlNutricional.class);
					/*publishProgress("Interpretando Controles Nutricionales");
					reader.beginArray();
					while(reader.hasNext()){
						ControlNutricional control_nutricional = gson.fromJson(reader, ControlNutricional.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(control_nutricional);
						uri = ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI;
						if(cr.insert(uri, fila)==null)
							cr.update(uri, fila, ControlNutricional.ID_PERSONA+"=? and "+ ControlNutricional.FECHA+"=?", 
									new String[]{control_nutricional.id_persona, control_nutricional.fecha});
					}
					reader.endArray();*/
					
				}else if(atributo.equalsIgnoreCase(ControlPerimetroCefalico.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.CONTROL_PERIMETRO_CEFALICO_CONTENT_URI, 
							ControlPerimetroCefalico.ID_PERSONA+"=? and "+ ControlPerimetroCefalico.FECHA+"=?", 
							new String[]{ControlPerimetroCefalico.ID_PERSONA, ControlPerimetroCefalico.FECHA},
							"Controles de Per�metro Cef�lico", ControlPerimetroCefalico.class);
					
				}else if(atributo.equalsIgnoreCase(EsquemaIncompleto.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.ESQUEMA_INCOMPLETO_CONTENT_URI, null, null, 
							"Esquemas incompletos", EsquemaIncompleto.class);
					/*publishProgress("Interpretando Esquemas incompletos");
					reader.beginArray();
					while(reader.hasNext()){
						EsquemaIncompleto esquema = gson.fromJson(reader, EsquemaIncompleto.class);
						fila = DatosUtil.ContentValuesDesdeObjeto(esquema);
						uri = ProveedorContenido.ESQUEMA_INCOMPLETO_CONTENT_URI;
						cr.insert(uri, fila); //No hay updates en caso de haber repetidos (tabla deber�a estar vac�a antes)
					}
					reader.endArray();*/
				
				}else if(atributo.equalsIgnoreCase(SalesRehidratacion.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.SALES_REHIDRATACION_CONTENT_URI, 
							SalesRehidratacion.ID_PERSONA+"=? and "+ SalesRehidratacion.FECHA+"=?", 
							new String[]{SalesRehidratacion.ID_PERSONA, SalesRehidratacion.FECHA},
							"Sales de Rehidrataci�n", SalesRehidratacion.class);
				
				}else if(atributo.equalsIgnoreCase(EstimulacionTemprana.NOMBRE_TABLA)){
					InterpretarAtributoInsertMasivo(gson, reader, cr, 
							ProveedorContenido.ESTIMULACION_TEMPRANA_CONTENT_URI, 
							EstimulacionTemprana.ID_PERSONA+"=? and "+ EstimulacionTemprana.FECHA+"=?", 
							new String[]{EstimulacionTemprana.ID_PERSONA, EstimulacionTemprana.FECHA},
							"Estimulaci�n Temprana", EstimulacionTemprana.class);
					
				}else{
					//Se recibi� algo que NO se puede interpretar (as� pues mal formado)
					//Se intentar� leer esto que no comprendemos pero si hay error, saldr� esto de evidencia
					atributo = "atributo NO ESPERADO "+atributo;
					publishProgress("Interpretando " + atributo);
					//Se intentar� leer lo no reconocido
					if(reader.hasNext())
						reader.skipValue(); //Se salta valor, objeto o arreglo hasta terminarlo
					publishProgress("Se termin� de leer datos no esperados");
				}
			}//fin while reader.hasNext
			
			//reader.endObject();
			reader.close();
			if(!contexto.deleteFile(ARCHIVO_JSON))
				publishProgress("No se borr� archivo temporal de descarga");
			
				
		}catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			//Sucede al intentar leer UTF-8. Nunca deber�a suceder
		}catch (IllegalAccessException e){
			Exception ex = new Exception("Error al generar ContentValues en tabla '"+atributo+"' "+e);
			Log.d(TAG, ex.toString());
			throw ex;
		}catch (IOException e) {
			//Error de lectura json
			Exception ex = new Exception("Error al leer json en atributo '"+atributo+"' "+e);
			Log.d(TAG, ex.toString());
			throw ex;
		}catch (Exception e){
			Exception ex = new Exception("Excepci�n mientras se analizaba atributo '"+atributo+"' "+e);
			Log.d(TAG, ex.toString());
			throw ex;
		}
	}//fin InterpretarDatosServidor
	
	/**
	 * Realiza un insert de elementos en caso de ser instalaci�n nueva. Si es instalaci�n NO nueva
	 * inserta un registro a la vez y en caso de error de base de datos en insersi�n intentar� un
	 * update del registro. En cualquier caso se manda a publicar mensajes de status constantemente.
	 * @param gson Parseador de Json usado para crear <b>reader</b>
	 * @param reader Lector de Gson creado con el parseador <b>gson</b>
	 * @param cr Puente de acceso al ContentProvider de base de datos
	 * @param uriConsulta Uri que describe el script de base de datos a ejecutar
	 * @param updateWhere Se usa en caso de que sea necesasrio ejecutar un UPDATE en base de datos. 
	 * Un valor null indicar� que no se ejecutar� un UPDATE 
	 * @param updateWhereCamposLlave si <b>updateWhere</b> difiere de null este campo debe contener los nombres de
	 * los campos en la fila a actualizar usados como llave primaria para ejecutar correctamente UPDATE
	 * @param atributo T�tulo usado para mostrar mensajes en sincronizaci�n
	 * @param clase Tipo de clase en uso para manejo de los datos en el parseo de Json
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	private <T> void InterpretarAtributoInsertMasivo(Gson gson, JsonReader reader, ContentResolver cr, 
			Uri uriConsulta, String updateWhere, String[] updateWhereCamposLlave, String atributo, Class<T> clase) throws IOException, IllegalAccessException{
		
		publishProgress("Interpretando " + atributo);
		Uri uri = uriConsulta;
		int registros=0; //contador de registros procesados
		int limite=500; //l�mite para acumular bulkinsert (en instalaci�n nueva) o reportar progreso (no nueva)

		reader.beginArray();
		
		if(this.aplicacion.getEsInstalacionNueva() || clase==EsquemaIncompleto.class || pedirPrimerosDatosDeNuevo){
			List<T> lista = new ArrayList<T>(limite);
			
			while(reader.hasNext()){
				T obj = gson.fromJson(reader, clase);
				lista.add(obj);
				if(lista.size()==limite || !reader.hasNext()){
					ContentValues[] filas = new ContentValues[lista.size()];
					publishProgress(atributo+"\nGenerando "+lista.size()+" elementos");
					int n=0;
					for(T elemento : lista)
						filas[n++] = DatosUtil.ContentValuesDesdeObjeto(elemento);
					registros+=lista.size();
					publishProgress(atributo+"\nInsertando "+lista.size()+" elementos de "+registros+" hasta ahora");
					cr.bulkInsert(uri, filas);
					//	cr.update(uri, fila, ArbolSegmentacion.ID+"="+arbol._id,null);
					lista.clear();
				}
			}
		}else{
			
			while(reader.hasNext()){
				T elemento = gson.fromJson(reader, clase);
				ContentValues fila = DatosUtil.ContentValuesDesdeObjeto(elemento);
				if(cr.insert(uri, fila)==null && updateWhere!=null){
					String[] args = null;
					if(updateWhereCamposLlave != null){
						List<String> listaArgs = new ArrayList<String>();
						for(String campo : updateWhereCamposLlave)
							listaArgs.add(fila.getAsString(campo));
						args = new String[listaArgs.size()];
						args = listaArgs.toArray(args);
					}
					cr.update(uri, fila, updateWhere, args);
				}
				if( ++registros % limite == 0 )
					publishProgress(atributo+"\nProcesados "+registros+" elementos");
			}
		}
		
		reader.endArray();
	}//fin InterpretarAtributoInsertMasivo
	
	private void InterpretarArbolSegmentacion(Gson gson, JsonReader reader, ContentResolver cr) throws IOException, IllegalAccessException{
		publishProgress("Interpretando Arbol de Segmentaci�n");
		Uri uri = ProveedorContenido.ARBOL_SEGMENTACION_CONTENT_URI;
		int registros=0; //contador de registros procesados
		int limite=500; //l�mite para acumular bulkinsert (en instalaci�n nueva) o reportar progreso (no nueva)

		reader.beginArray();
		
		if(this.aplicacion.getEsInstalacionNueva()){
			List<ArbolSegmentacion> lista = new ArrayList<ArbolSegmentacion>(limite);
			
			while(reader.hasNext()){
				lista.add((ArbolSegmentacion) gson.fromJson(reader, ArbolSegmentacion.class));
				if(lista.size()==limite || !reader.hasNext()){
					ContentValues[] filas = new ContentValues[lista.size()];
					publishProgress("Generandoo "+lista.size()+" ramas");
					int n=0;
					for(ArbolSegmentacion arbol : lista)
						filas[n++] = DatosUtil.ContentValuesDesdeObjeto(arbol);
					registros+=lista.size();
					publishProgress("Insertando "+lista.size()+" ramas de "+registros+" hasta ahora");
					cr.bulkInsert(uri, filas);
					//	cr.update(uri, fila, ArbolSegmentacion.ID+"="+arbol._id,null);
					lista.clear();
				}
			}
		}else{
			
			while(reader.hasNext()){
				ArbolSegmentacion arbol = gson.fromJson(reader, ArbolSegmentacion.class);
				ContentValues fila = DatosUtil.ContentValuesDesdeObjeto(arbol);
				if(cr.insert(uri, fila)==null)
					cr.update(uri, fila, ArbolSegmentacion.ID+"="+ arbol._id, null);
				if( ++registros % limite == 0 )
					publishProgress("Procesadas "+registros+" ramas");
			}
		}
		
		reader.endArray();
	}//fin InterpretarArbolSegmentacion
	
	/**
	 * Guarda stream en disco. Cierra el stream original y regresa el archivo generado al principio
	 * en un FileInputStream abierto y listo para consumirse
	 * @param stream Stream a guardar en disco.
	 * @return FileInputStream abierto y listo apuntando al archivo generado que contiene informaci�n de stream
	 * @throws Exception
	 */
	private InputStream GuardarAbrirStream(InputStream stream) throws Exception{
		FileOutputStream fsalida = contexto.openFileOutput(ARCHIVO_JSON, 0);
		final byte[] buffer = new byte[1024];
		int read;
		int bloque = 100000, bloqueLeido=0 ;
		try {
			while ((read = stream.read(buffer)) != -1){
			    fsalida.write(buffer, 0, read);
			    bloqueLeido += read;
			    if(bloqueLeido > bloque){
			    	publishProgress("Descargando "+bloqueLeido + "bytes");
			    	bloqueLeido=0;
			    }
			}
			fsalida.flush();
			fsalida.close();
			stream.close();
		} catch (IOException e) {
			String desc = "Hubo un error al descargar stream a "+ARCHIVO_JSON;
			Log.d(TAG, desc);
			throw new Exception(desc, e);
		}
		copiarArchivo(new File(contexto.getFilesDir(),ARCHIVO_JSON), new File(contexto.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),ARCHIVO_JSON));
		return contexto.openFileInput(ARCHIVO_JSON);
	}
	private void copiarArchivo(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
	
	/**
	 * Algunos datos deben vaciarse/borrarse antes de recibir datos del servidor. Aqu� se borran esos datos
	 */
	private void BorrarDatosAntesSinc(){
		ContentResolver cr = contexto.getContentResolver();
		cr.delete(ProveedorContenido.ESQUEMA_INCOMPLETO_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.NOTIFICACION_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI, null, null);
	}
	
	/**
	 * Ejecutado cuando es necesario resetear tablas transaccionales y solicitarlas de nuevo al servidor 
	 */
	private void RequerirRestaurarTransaccionales(){
		publishProgress("Borrando tablas transaccionales");
		ContentResolver cr = aplicacion.getContentResolver();
		cr.delete(ProveedorContenido.PERSONA_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.PERSONA_AFILIACION_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.PERSONA_TUTOR_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.TUTOR_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.ANTIGUO_DOMICILIO_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.ANTIGUA_UM_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.REGISTRO_CIVIL_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.CONTROL_IRA_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.CONTROL_EDA_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.VISITA_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.SALES_REHIDRATACION_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.CONTROL_PERIMETRO_CEFALICO_CONTENT_URI, null, null);
		cr.delete(ProveedorContenido.ESTIMULACION_TEMPRANA_CONTENT_URI, null, null);
		pedirPrimerosDatosDeNuevo=true;
	}
	
	
	/**
	 * Envia al servidor los cambios desde la �ltima actualizaci�n
	 * @param idSesion Identificador de la sesi�n con el servidor
	 * @param ultimaSinc Fecha de la �ltima sincronizaci�n en formato yyyy-MM-dd HH:mm:ss
	 * @throws Exception Cuando ocurre alg�n error
	 */
	private void AccionEnviarCambiosServidor(String idSesion, String ultimaSinc) throws Exception{
		String msgError = null;
		
		//VARIABLES PARA CONSULTAR BASE DE DATOS
		ContentResolver cr = contexto.getContentResolver();
		Cursor cur=null;
		String where="";
		String[] valoresWhere; //contenedor de valores de filtro where
		String[] valoresWhereSincronizacion = {ultimaSinc}; //filtro where m�s com�n por comodidad
		//String[] columnas = null;
		
		JSONObject datosSalida = new JSONObject(); //Contenedor del json final
		publishProgress("Generando datos a enviar al servidor...");
		String[] excepciones= null;
		
		try {
			//TABLA TUTOR
			where = Tutor.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.TUTOR_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{Tutor._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(Tutor.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			
			//TABLA ANTIGUA UM
			where = AntiguaUM.FECHA_CAMBIO + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.ANTIGUA_UM_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{AntiguaUM._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(AntiguaUM.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA ANTIGUO DOMICILIO
			where = AntiguoDomicilio.FECHA_CAMBIO + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.ANTIGUO_DOMICILIO_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{AntiguoDomicilio._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(AntiguoDomicilio.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA PERSONA
			where = Persona.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere = valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.PERSONA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{Persona._ID, Persona.ID_PARTO_MULTIPLE};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(Persona.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA PERSONA X ALERGIA
			where = PersonaAlergia.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{PersonaAlergia._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(PersonaAlergia.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA PERSONA X TUTOR
			where = PersonaTutor.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.PERSONA_TUTOR_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{PersonaTutor._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(PersonaTutor.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA PERSONA X AFILIACION
			where = PersonaAfiliacion.ULTIMA_ACTUALIZACION + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.PERSONA_AFILIACION_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{PersonaAfiliacion._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(PersonaAfiliacion.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA REGISTRO CIVIL
			where = RegistroCivil.FECHA_REGISTRO + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.REGISTRO_CIVIL_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{RegistroCivil._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(RegistroCivil.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL VACUNA
			where = ControlVacuna.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlVacuna._ID, ControlVacuna.ID_INVITADO};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlVacuna.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL IRA
			where = ControlIra.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_IRA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlIra._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlIra.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL EDA
			where = ControlEda.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_EDA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlEda._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlEda.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL CONSULTA
			where = ControlConsulta.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlConsulta._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlConsulta.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL ACCION NUTRICIONAL
			where = ControlAccionNutricional.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_ACCION_NUTRICIONAL_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlAccionNutricional._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlAccionNutricional.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL PERIMETRO CEFALICO
			where = ControlPerimetroCefalico.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_PERIMETRO_CEFALICO_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlPerimetroCefalico._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlPerimetroCefalico.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA CONTROL NUTRICIONAL
			where = ControlNutricional.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ControlNutricional._ID, ControlNutricional.ID_INVITADO};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ControlNutricional.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA SALES REHIDRATACI�N
			where = SalesRehidratacion.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.SALES_REHIDRATACION_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{SalesRehidratacion._ID};//, SalesRehidratacion.ID_INVITADO};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(SalesRehidratacion.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA ESTIMULACI�N TEMPRANA
			where = EstimulacionTemprana.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.ESTIMULACION_TEMPRANA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{EstimulacionTemprana._ID};//, EstimulacionTemprana.ID_INVITADO};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(EstimulacionTemprana.NOMBRE_TABLA, filas);
			}
			cur.close();

			//TABLA VISITA
			where = Visita.FECHA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.VISITA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				//excepciones= new String[]{ControlNutricional._ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(Visita.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TES_PENDIENTES_TARJETA
			cur = PendientesTarjeta.getPendientesPorSincronizar(contexto);
			if(cur.getCount()>0){
				excepciones = new String[]{PendientesTarjeta.ES_PENDIENTE_LOCAL, PendientesTarjeta.RESUELTO};
				JSONArray filas = DatosUtil.CrearJsonArray(cur, excepciones);
				datosSalida.put(PendientesTarjeta.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA BIT�CORA
			where = Bitacora.FECHA_HORA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.BITACORA_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{Bitacora.ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(Bitacora.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			//TABLA ERROR
			where = ErrorSis.FECHA_HORA + ">=?";
			valoresWhere= valoresWhereSincronizacion;
			cur = cr.query(ProveedorContenido.ERROR_SIS_CONTENT_URI, null, where, valoresWhere, null);
			if(cur.getCount()>0){
				excepciones= new String[]{ErrorSis.ID};
				JSONArray filas = DatosUtil.CrearJsonArray(cur,excepciones);
				datosSalida.put(ErrorSis.NOMBRE_TABLA, filas);
			}
			cur.close();
			
			
			//Preparamos POST
			List<NameValuePair> parametros = new ArrayList<NameValuePair>();
	        parametros.add(new BasicNameValuePair(PARAMETRO_SESION, idSesion));
	        parametros.add(new BasicNameValuePair(PARAMETRO_ACCION, ACCION_ENVIAR_SERVIDOR));
	        parametros.add(new BasicNameValuePair(PARAMETRO_DATOS, datosSalida.toString() ));
	        
	        publishProgress("Enviando a servidor datos de "+datosSalida.length()+" tablas");
			String json = webHelper.RequestPost(aplicacion.getUrlSincronizacion(), parametros);
			
			
			//CHECAMOS SI SERVIDOR NOS PIDE ACTUALIZAR LA VERSI�N DEL SOFTWARE
			JSONObject jo;
			try {
				jo = new JSONObject(json);
			} catch (JSONException e) {
				msgError="Se enviaron los datos con �xito a servidor, pero �ste respondi� " +
						"con el siguiente mensaje desconocido:"+json;
				Log.e(TAG, msgError);
				throw new Exception(msgError);
			}
			if(jo.has(RESPUESTA_INESPERADO) && 
					jo.getString(RESPUESTA_INESPERADO).equalsIgnoreCase(RESPUESTA_INESPERADO_DESACTUALIZADO))
				DefinirComoDispositivoSinActualizar(jo.getString(RESPUESTA_URL));
			
		} catch (JSONException e) {
			msgError = "Error en json:"+e.toString(); 
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_JSON_EXCEPTION, msgError);
			throw new Exception(msgError);
		} catch (Exception e){
			msgError = "Error desconocido:"+e.toString();
			Log.e(TAG, msgError);
			EnviarResultado(idSesion, RESULTADO_EXCEPTION, msgError);
			throw new Exception(msgError);
		}finally{
			if(cur!=null)cur.close();
		}
	}//fin AccionEnviarServidor
	
	/**
	 * Cambia el estado de este dispositivo para no permitir uso
	 * @param urlActualizacion Url de la ubicaci�n del APK que se debe descargar
	 */
	private void DefinirComoDispositivoSinActualizar(String urlActualizacion){
		this.aplicacion.setRequiereActualizarApk(true);
		this.aplicacion.setUrlActualizacionApk(urlActualizacion);
	}		

	
	
	/**
	 * Clase helper para m�todos de consulta de servicios en web
	 * @author Axel
	 *
	 */
	private class HttpHelper {
		
		HttpClient cliente;
		//HttpContext contextoHttp;
		
		public HttpHelper(){
			//Para limitar el tiempo de espera...
			final HttpParams parametros = new BasicHttpParams();
			//HttpConnectionParams.setSoTimeout(parametros, 10000);
			HttpConnectionParams.setConnectionTimeout(parametros, 10000);

			cliente = new DefaultHttpClient(parametros);
			//contextoHttp = new BasicHttpContext();
		}
		
		/**
		 * Funci�n helper para mandar request tipo POST y regresar la respuesta del servidor.
		 * @param url Direcci�n a conectar
		 * @param parametros Valores a incluir en request
		 * @return String con resultado del request
		 * @throws Exception 
		 */
		public synchronized String RequestPost(String url, List<NameValuePair> parametros) throws Exception{
			String salida=null;
			byte[] buffer=new byte[1024];

			try{
				//Procesamos respuesta
				InputStream ist = RequestStreamPost(url, parametros);
				ByteArrayOutputStream contenido = new ByteArrayOutputStream();
				
				int bytesLeidos=0;
				while( (bytesLeidos=ist.read(buffer)) != -1 )
					contenido.write(buffer, 0, bytesLeidos);
				//Gson gson=new Gson();gson.
				salida= contenido.toString(); //new String( contenido.toByteArray());

				Log.d(TAG, "POST descarg� datos: "+ (salida.length()>1000000? salida.length()+" caracteres" : "'"+salida+"'"));
			}catch(Exception ex){
				Log.d(TAG, "Error en request tipo POST a url:"+url+"\n"+ex.toString() );
				throw ex;
			}
			return salida;
		}

		/**
		 * Funci�n helper para mandar request tipo POST y regresar la respuesta del servidor.
		 * @param url Direcci�n a conectar
		 * @param parametros Valores a incluir en request
		 * @return InputStream apuntando a la respuesta del servidor
		 * @throws Exception 
		 */
		public synchronized InputStream RequestStreamPost(String url, List<NameValuePair> parametros) throws Exception{
			HttpPost request = new HttpPost(url);

			try{
				//Agregamos par�metros al cuerpo del request (solo en POST tipo application/x-www-form-urlencoded)
				if(parametros!=null)
					request.setEntity(new UrlEncodedFormEntity(parametros, "UTF-8"));
				
				HttpResponse respuesta = cliente.execute(request);
				StatusLine estado = respuesta.getStatusLine();
				
				if(estado.getStatusCode()!= SincronizacionTask.HTTP_STATUS_OK)
					throw new Exception("Error en conexi�n con status: "+estado.toString());
				
				Log.d(TAG, "POST ha obtenido un InputStream");
				return respuesta.getEntity().getContent();
			}catch(Exception ex){
				Exception e2 = new Exception("Error en request tipo POST a url:"+url+"\n"+ex.toString(),ex);
				Log.d(TAG, e2.toString());
				throw e2;
			}
		}

		public synchronized InputStream RequestGet(String url){
			HttpGet request = new HttpGet(url);
			Log.d(TAG, "Inicia request GET en "+url);
			try{
				HttpResponse respuesta = cliente.execute(request);
				StatusLine estado = respuesta.getStatusLine();
				
				if(estado.getStatusCode()!= SincronizacionTask.HTTP_STATUS_OK)
					throw new Exception("Error en conexi�n con status: "+estado.toString());
				
				Log.d(TAG, "GET ha obtenido un InputStream");
				return respuesta.getEntity().getContent();
			}catch(Exception ex){
				Exception e2 = new Exception("Error en request tipo GET a url:"+url+"\n"+ex.toString(),ex);
				Log.d(TAG, e2.toString());
				//throw e2;
			}
			return null;
		}
		
	}//fin HttpHelper
	
	
}//fin clase