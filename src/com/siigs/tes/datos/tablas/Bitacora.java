package com.siigs.tes.datos.tablas;

import android.content.ContentValues;
import android.content.Context;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Bitacora {

	public final static String NOMBRE_TABLA = "sis_bitacora"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_USUARIO = "id_usuario";
	public final static String FECHA_HORA = "fecha_hora";
	public final static String PARAMETROS = "parametros";
	public final static String ID_CONTROLADOR_ACCION = "id_controlador_accion";
	
	//Columnas de control interno
	public final static String ID = "_id";
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
		ID_USUARIO + " INTEGER NOT NULL, " +
		ID_CONTROLADOR_ACCION + " INTEGER NOT NULL, "+
		FECHA_HORA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		PARAMETROS + " TEXT DEFAULT NULL COLLATE NOCASE"+
		"); ";
	
	//POJO
	public int id_usuario;
	public int id_controlador_accion;
	public String fecha_hora;
	public String parametros;
	
	public static void AgregarRegistro(Context context, int idUsuario, int ica, String parametros){
		ContentValues cv = new ContentValues();
		cv.put(ID_USUARIO, idUsuario);
		cv.put(ID_CONTROLADOR_ACCION, ica);
		cv.put(PARAMETROS, parametros);
		cv.put(FECHA_HORA, DatosUtil.getAhora());
		context.getContentResolver().insert(ProveedorContenido.BITACORA_CONTENT_URI, cv);
	}
}
