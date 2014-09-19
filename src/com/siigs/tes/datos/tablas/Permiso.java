package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.annotations.SerializedName;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Permiso {

	public final static String NOMBRE_TABLA = "sis_permiso"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id";
	public final static String ID_GRUPO = "id_grupo";
	public final static String FECHA = "fecha";
	public final static String ID_CONTROLADOR_ACCION = "id_controlador_accion";
	
	//Columnas de control interno
	public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_GRUPO + " INTEGER NOT NULL, " +
		ID_CONTROLADOR_ACCION + " INTEGER NOT NULL, "+
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')) "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public int id_grupo;
	public int id_controlador_accion;
	public String fecha;
	
	public static Cursor getPermisosGrupo(Context context, int idGrupo){
		return context.getContentResolver().query(ProveedorContenido.PERMISO_CONTENT_URI, 
				null, ID_GRUPO + "="+idGrupo, null, null);
	}
	
	public static List<Permiso> getTodos(Context context){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.PERMISO_CONTENT_URI, 
				null, null, null, null);
		List<Permiso> salida = DatosUtil.ObjetosDesdeCursor(cur, Permiso.class);
		cur.close();
		return salida;
	}
	
	public static void AgregarRegistros(Context context, List<Permiso> permisos) throws Exception{
		ContentValues[] registros = new ContentValues[permisos.size()];
		int i=0;
		for(Permiso permiso : permisos)
			registros[i++]= DatosUtil.ContentValuesDesdeObjeto(permiso);
		context.getContentResolver().bulkInsert(ProveedorContenido.PERMISO_CONTENT_URI,	registros);
	}
	
}
