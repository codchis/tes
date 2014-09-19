package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class GrupoAtencion {

	public final static String NOMBRE_TABLA = "cns_grupo_atencion"; //nombre en BD
	
	//Columnas en la nube
	public final static String _ID = "_id"; //para adaptadores android
	public final static String NIVEL_ATENCION = "nivel_atencion";
	public final static String GRUPO_ATENCION = "grupo_atencion";
	public final static String DESCRIPCION = "descripcion";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		NIVEL_ATENCION + " INTEGER NOT NULL, "+
		GRUPO_ATENCION + " TEXT NOT NULL, "+
		DESCRIPCION + " TEXT NOT NULL, "+
		"UNIQUE (" + NIVEL_ATENCION + "," + GRUPO_ATENCION + ")" +
		"); ";
	
	//POJO
	public int nivel_atencion;
	public String grupo_atencion;
	public String descripcion;
	
	public static List<GrupoAtencion> getPorNivel(Context context, int nivelAtencion){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.GRUPO_ATENCION_CONTENT_URI, null, 
					NIVEL_ATENCION + "=?", new String[]{nivelAtencion+""}, null);
		List<GrupoAtencion> salida = DatosUtil.ObjetosDesdeCursor(cur, GrupoAtencion.class);
		cur.close();
		return salida;
	}
	
}
