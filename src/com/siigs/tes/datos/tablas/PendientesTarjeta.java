package com.siigs.tes.datos.tablas;

import java.util.List;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class PendientesTarjeta {

	public final static String NOMBRE_TABLA = "tes_pendientes_tarjeta"; //nombre en BD
	
	//Columnas en la nube
	public final static String FECHA = "fecha";
	public final static String ID_PERSONA = "id_persona";
	public final static String TABLA = "tabla";
	public final static String REGISTRO_JSON = "registro_json";
	public final static String ES_PENDIENTE_LOCAL = "es_pendiente_local";
	public final static String RESUELTO = "resuelto";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		//ID_PERSONA + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_PERSONA + " TEXT NOT NULL, "+
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), " +
		TABLA + " TEXT NOT NULL, "+
		REGISTRO_JSON + " TEXT NOT NULL, " +
		ES_PENDIENTE_LOCAL + " INTEGER NOT NULL DEFAULT (0), " +
		RESUELTO + " INTEGER NOT NULL DEFAULT (0) "+
		", UNIQUE (" + ID_PERSONA + "," + FECHA + ") " +
		"); "; 

	//POJO
	public String fecha;
	public String id_persona;
	public String tabla;
	public String registro_json;
	public transient int es_pendiente_local; //para no interpretarlo con gson
	
	/**
	 * Inserta <b>pendiente</b> en base de datos como nuevo registro creado en esta tableta
	 * @param context
	 * @param pendiente Registro a insertar
	 * @return {@link Uri} del registro creado en base de datos
	 */
	public static Uri AgregarNuevoPendienteLocal(Context context, PendientesTarjeta pendiente){
		ContentValues cv = new ContentValues();
		cv.put(ID_PERSONA, pendiente.id_persona);
		cv.put(FECHA, DatosUtil.getAhora());
		cv.put(ES_PENDIENTE_LOCAL, 1);
		cv.put(TABLA, pendiente.tabla);
		cv.put(REGISTRO_JSON, pendiente.registro_json);
		Uri salida = context.getContentResolver().insert(ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI, cv);
		if(salida != null)
			Log.d(NOMBRE_TABLA, "Se ha insertado un nuevo pendiente local: "+salida.getLastPathSegment());
		return salida;
	}
	
	/**
	 * Inserta <b>pendiente</b> en base de datos como nuevo registro recibido de una sincronización con nube.
	 * Todo registro recibido de la nube se considera foráneo independientemente de si originalmente fue creado
	 * en la misma tableta que lo recibe.
	 * @param context
	 * @param pendiente Registro a insertar
	 * @return {@link Uri} del registro creado en base de datos
	 */
	public static Uri AgregarNuevoPendienteForaneo(Context context, PendientesTarjeta pendiente){
		ContentValues cv = new ContentValues();
		cv.put(ID_PERSONA, pendiente.id_persona);
		cv.put(FECHA, pendiente.fecha);
		cv.put(ES_PENDIENTE_LOCAL, 0);
		cv.put(TABLA, pendiente.tabla);
		cv.put(REGISTRO_JSON, pendiente.registro_json);
		Uri salida = context.getContentResolver().insert(ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI, cv);
		if(salida != null)
			Log.d(NOMBRE_TABLA, "Se ha insertado un nuevo pendiente foraneo: "+salida.getLastPathSegment());
		return salida;
	}
	
	/**
	 * Devuelve los pendientes que deben enviarse al servidor basado en si son locales, o si fueron resueltos
	 * @param context
	 * @return {@link Cursor} a lista de resultados
	 */
	public static Cursor getPendientesPorSincronizar(Context context){
		return context.getContentResolver().query(ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI, 
				null, ES_PENDIENTE_LOCAL+"=1 OR "+RESUELTO+"=1", null, null);
	}
	
	public static List<PendientesTarjeta> getPendientesPaciente(Context context, String idPersona){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI, 
				null, ID_PERSONA+"=?", new String[]{idPersona}, FECHA +" asc");
		List<PendientesTarjeta> salida = DatosUtil.ObjetosDesdeCursor(cur, PendientesTarjeta.class);
		cur.close();
		return salida;
	}
	
	/**
	 * De acuerdo a la naturaleza del pendiente lo marca como resuelto para reportar a nube ó lo borra.
	 * @param context
	 * @param pendiente
	 */
	public static void MarcarPendienteResuelto(Context context, PendientesTarjeta pendiente){
		String where = FECHA+"=? AND "+ID_PERSONA+"=?";
		String[] args = new String[]{pendiente.fecha, pendiente.id_persona};
		if(pendiente.es_pendiente_local==1){
			context.getContentResolver().delete(ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI, where, args);
		}else{
			ContentValues cv = new ContentValues();
			cv.put(RESUELTO, 1);
			context.getContentResolver().update(ProveedorContenido.PENDIENTES_TARJETA_CONTENT_URI, cv, where, args);
		}
	}
	
}
