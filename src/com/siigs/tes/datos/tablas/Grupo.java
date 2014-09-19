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
public class Grupo {

	public final static String NOMBRE_TABLA = "sis_grupo"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id";
	public final static String NOMBRE = "nombre";
	public final static String DESCRIPCION = "descripcion";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		NOMBRE + " TEXT NOT NULL, " +
		DESCRIPCION + " TEXT DEFAULT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public String nombre="";
	public String descripcion;
	
	public static List<Grupo> getTodos(Context context){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.GRUPO_CONTENT_URI, 
				null, null, null, null);
		List<Grupo> salida = DatosUtil.ObjetosDesdeCursor(cur, Grupo.class);
		cur.close();
		return salida;
	}
	
	public static void AgregarRegistros(Context context, List<Grupo> grupos) throws Exception{
		ContentValues[] registros = new ContentValues[grupos.size()];
		int i=0;
		for(Grupo grupo : grupos)
			registros[i++]= DatosUtil.ContentValuesDesdeObjeto(grupo);
		context.getContentResolver().bulkInsert(ProveedorContenido.GRUPO_CONTENT_URI,	registros);
	}
}
