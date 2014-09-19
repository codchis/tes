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
public class CategoriaCie10 {

	public final static String NOMBRE_TABLA = "cns_categoria_cie10"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String DESCRIPCION = "descripcion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		ID + " TEXT NOT NULL, " +
		DESCRIPCION + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL, "+
		"UNIQUE (" + ID + ")" + 
		"); ";
	
	public String id;
	public String descripcion;
	public int activo;
	
	public static List<CategoriaCie10> getActivas(Context context){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CATEGORIA_CIE10_CONTENT_URI, null, ACTIVO + "=1", null, DESCRIPCION);
		List<CategoriaCie10> salida = DatosUtil.ObjetosDesdeCursor(cur, CategoriaCie10.class);
		cur.close();
		return salida;
	}
}
