package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Tratamiento {

	public final static String NOMBRE_TABLA = "cns_tratamiento"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String TIPO = "tipo";
	public final static String DESCRIPCION = "descripcion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		TIPO + " TEXT NOT NULL, " +
		DESCRIPCION + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	public int id;
	public String tipo;
	public String descripcion;
	public int activo;
	
	public static String[] getTipos(Context contexto){
		Cursor cur = contexto.getContentResolver().query(ProveedorContenido.TRATAMIENTO_CONTENT_URI,
				new String[]{"distinct "+TIPO}, ACTIVO+"=1", null, TIPO + " ASC");
		String[] salida = new String[cur.getCount()];
		int index = 0;
		while(cur.moveToNext())
			salida[index++] = cur.getString(cur.getColumnIndex(TIPO));
		cur.close();
		return salida;
	}
	
	public static List<Tratamiento> getTratamientosConTipo(Context contexto, String tipo){
		Cursor cur = contexto.getContentResolver().query(ProveedorContenido.TRATAMIENTO_CONTENT_URI, 
				null, ACTIVO+"=1 AND "+TIPO+"=?", new String[]{tipo}, DESCRIPCION +" ASC");
		List<Tratamiento> salida = DatosUtil.ObjetosDesdeCursor(cur, Tratamiento.class);
		cur.close();
		return salida;
	}
	
	public static String getDescripcion(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.TRATAMIENTO_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, new String[]{DESCRIPCION}, null, null, null);
		cur.moveToNext(); //debería haber resultados
		String salida = cur.getString(cur.getColumnIndex(DESCRIPCION));
		cur.close();
		return salida;
	}
}
