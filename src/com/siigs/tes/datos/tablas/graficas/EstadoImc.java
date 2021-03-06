package com.siigs.tes.datos.tablas.graficas;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class EstadoImc {

	public final static String NOMBRE_TABLA = "cns_estado_imc"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id"; //para adaptadores android
	public final static String DESCRIPCION = "descripcion";
	public final static String COLOR = "color";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		DESCRIPCION + " TEXT NOT NULL, "+
		COLOR + " TEXT NOT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public String descripcion;
	public String color;
	
	public static EstadoImc getEstadoImc(Context context, int id) throws InstantiationException, IllegalAccessException{
		Uri uri = Uri.withAppendedPath(ProveedorContenido.ESTADO_IMC_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
		if(!cur.moveToNext()){ //deber�a haber resultados
			cur.close();
			return null;
		}
		
		EstadoImc salida = DatosUtil.ObjetoDesdeCursor(cur, EstadoImc.class);
		cur.close();
		return salida;
	}
	
	public static List<EstadoImc> getEstadosImc(Context context) {
		Cursor cur = context.getContentResolver().query(ProveedorContenido.ESTADO_IMC_CONTENT_URI, 
				null, null, null, null);
		List<EstadoImc> salida = DatosUtil.ObjetosDesdeCursor(cur, EstadoImc.class);
		cur.close();
		return salida;
	}
}
