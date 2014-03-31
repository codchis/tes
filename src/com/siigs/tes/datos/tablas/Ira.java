package com.siigs.tes.datos.tablas;

import java.util.List;

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
public class Ira {

	public final static String NOMBRE_TABLA = "cns_ira"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id";
	public final static String ID_CIE10 = "id_cie10";
	public final static String DESCRIPCION = "descripcion";
	public final static String CLAVE = "clave";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_CIE10 + " INTEGER NOT NULL, " +
		DESCRIPCION + " TEXT NOT NULL, "+
		CLAVE + " TEXT NOT NULL, " +
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public int id_cie10;
	public String descripcion;
	public String clave;
	public int activo;
	
	public static List<Ira> getIrasActivas(Context context){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.IRA_CONTENT_URI, null, ACTIVO + "=1", null, DESCRIPCION);
		List<Ira> salida = DatosUtil.ObjetosDesdeCursor(cur, Ira.class);
		cur.close();
		return salida;
	}
	
	public static String getDescripcion(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.IRA_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, new String[]{DESCRIPCION}, null, null, null);
		cur.moveToNext(); //debería haber resultados
		String salida = cur.getString(cur.getColumnIndex(DESCRIPCION));
		cur.close();
		return salida;
	}
	
	public static String getClave(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.IRA_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, new String[]{CLAVE}, null, null, null);
		String salida;
		if(cur.moveToNext()) //debería haber resultados
			salida = cur.getString(cur.getColumnIndex(CLAVE));
		else salida = context.getString(R.string.desconocido);
		cur.close();
		return salida;
	}

}
