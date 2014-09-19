package com.siigs.tes.datos.tablas.graficas;

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
public class HemoglobinaAltitud {

	public final static String NOMBRE_TABLA = "asu_hemoglobina_altitud"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_LOCALIDAD_ASU = "id_localidad_asu";
	public final static String ALTITUD = "altitud";
	public final static String MUJER_NO_EMBARAZADA = "mujer_no_embarazada";
	public final static String MUJER_EMBARAZADA_NINIO_6_59_MESES = "mujer_embarazada_ninio_6_59_meses";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_LOCALIDAD_ASU + " INTEGER NOT NULL, "+
		ALTITUD + " INTEGER NOT NULL, "+
		MUJER_NO_EMBARAZADA + " NUMERIC NOT NULL, "+
		MUJER_EMBARAZADA_NINIO_6_59_MESES + " NUMERIC NOT NULL, "+
		"UNIQUE (" + ID_LOCALIDAD_ASU + ")" +
		"); ";
	
	//POJO
	public int id_localidad_asu;
	public int altitud;
	public double mujer_no_embarazada;
	public double mujer_embarazada_ninio_6_59_meses;
	
	public static HemoglobinaAltitud getPorLocalidad(Context context, int id_asu) throws InstantiationException, IllegalAccessException{
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.HEMOGLOBINA_ALTITUD_CONTENT_URI, null, 
				ID_LOCALIDAD_ASU+"=?", new String[]{id_asu+""}, null);
		if(!cur.moveToNext()){ //debería haber resultados
			cur.close();
			return null;
		}
		
		HemoglobinaAltitud salida = DatosUtil.ObjetoDesdeCursor(cur, HemoglobinaAltitud.class);
		cur.close();
		return salida;
	}
}
