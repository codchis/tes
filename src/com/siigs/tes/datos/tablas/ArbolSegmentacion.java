package com.siigs.tes.datos.tablas;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.siigs.tes.R;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class ArbolSegmentacion {

	public final static String NOMBRE_TABLA = "asu_arbol_segmentacion"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id"; //para adaptadores android
	public final static String GRADO_SEGMENTACION = "grado_segmentacion";
	public final static String ID_PADRE = "id_padre";
	public final static String ORDEN = "orden";
	public final static String VISIBLE = "visible";
	public final static String DESCRIPCION = "descripcion";
	
	//Columnas de control interno
	public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		GRADO_SEGMENTACION + " INTEGER NOT NULL, " + //4= localidad & id padre= 
		ID_PADRE + " INTEGER NOT NULL, " +
		ORDEN + " INTEGER DEFAULT NULL, " +
		VISIBLE + " INTEGER NOT NULL, " +
		DESCRIPCION + " TEXT NOT NULL COLLATE NOCASE"+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public int grado_segmentacion;
	public int id_padre;
	public Integer orden;
	public int visible;
	public String descripcion;
	
	public static String getDescripcion(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.ARBOL_SEGMENTACION_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, new String[]{DESCRIPCION}, null, null, null);
		//debería haber resultados
		String salida;
		if(!cur.moveToNext())
			salida = context.getString(R.string.desconocido);
		else salida = cur.getString(cur.getColumnIndex(DESCRIPCION));
		cur.close();
		return salida;
	}
	
	public static ArbolSegmentacion getArbol(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.ARBOL_SEGMENTACION_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
		cur.moveToNext(); //debería haber resultados
		ArbolSegmentacion salida = null;
		try{salida = DatosUtil.ObjetoDesdeCursor(cur, ArbolSegmentacion.class);}catch(Exception e){}
		cur.close();
		return salida;
	}
	
	public static Cursor buscar(Context context, String like, int grado, int id_padre){
		String selection = GRADO_SEGMENTACION + "=? and "+ ID_PADRE + "=? and " + 
				DESCRIPCION + " LIKE '%"+like+"%'";
		String[] selectionArgs = new String[]{grado+"", id_padre+""};
		Cursor cur = context.getContentResolver().query(ProveedorContenido.ARBOL_SEGMENTACION_CONTENT_URI, 
				null, selection, selectionArgs, DESCRIPCION);
		return cur;
	}
}
