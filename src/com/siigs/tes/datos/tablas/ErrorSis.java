package com.siigs.tes.datos.tablas;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

import android.content.ContentValues;
import android.content.Context;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class ErrorSis {

	public final static String NOMBRE_TABLA = "sis_error"; //nombre en BD
	
	public final static int ERROR_DESCONOCIDO = 1000;
	
	//Columnas en la nube
	public final static String ID_USUARIO = "id_usuario";
	public final static String ID_CONTROLADOR_ACCION = "id_controlador_accion";
	public final static String FECHA_HORA = "fecha_hora";
	public final static String DESCRIPCION = "descripcion";
	
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
		DESCRIPCION + " TEXT NOT NULL COLLATE NOCASE"+
		"); ";
	
	//POJO
	public int id_usuario;
	public int id_controlador_accion;
	public String fecha_hora;
	public String descripcion;
	
	public static void AgregarError(Context context, int idUsuario, int ica, String descripcion){
		ContentValues cv = new ContentValues();
		cv.put(ID_USUARIO, idUsuario);
		cv.put(ID_CONTROLADOR_ACCION, ica);
		cv.put(DESCRIPCION, descripcion);
		cv.put(FECHA_HORA, DatosUtil.getAhora());
		context.getContentResolver().insert(ProveedorContenido.ERROR_SIS_CONTENT_URI, cv);
	}
}
