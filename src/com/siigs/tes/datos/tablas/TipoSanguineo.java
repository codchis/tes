package com.siigs.tes.datos.tablas;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.siigs.tes.R;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class TipoSanguineo {

	public final static String NOMBRE_TABLA = "cns_tipo_sanguineo"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id"; //para adaptadores android
	public final static String DESCRIPCION = "descripcion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		DESCRIPCION + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public String descripcion;
	public int activo;
	
	public static String getTipoSanguineo(Context context, int idTipoSanguineo){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.TIPO_SANGUINEO_CONTENT_URI, String.valueOf(idTipoSanguineo));
		Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
		if(!cur.moveToNext()){ //debería haber resultados
			cur.close();
			return context.getString(R.string.desconocido);
		}
		String salida = cur.getString(cur.getColumnIndex(DESCRIPCION));
		cur.close();
		return salida;
	}
}
