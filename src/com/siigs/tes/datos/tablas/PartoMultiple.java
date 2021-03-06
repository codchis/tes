package com.siigs.tes.datos.tablas;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.siigs.tes.R;
import com.siigs.tes.datos.ProveedorContenido;



/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class PartoMultiple {

	public final static String NOMBRE_TABLA = "cns_parto_multiple"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String DESCRIPCION = "descripcion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + 
		DESCRIPCION + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	public int id;
	public String descripcion;
	public int activo;

	public static String getDescripcion(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.PARTO_MULTIPLE_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, new String[]{DESCRIPCION}, null, null, null);
		//deber�a haber resultados
		String salida;
		if(!cur.moveToNext())
			salida = context.getString(R.string.desconocido);
		else salida = cur.getString(cur.getColumnIndex(DESCRIPCION));
		cur.close();
		return salida;
	}
}
