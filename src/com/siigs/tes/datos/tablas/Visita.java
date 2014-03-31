package com.siigs.tes.datos.tablas;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Visita {

	public final static String NOMBRE_TABLA = "cns_visita"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String FECHA = "fecha";
	public final static String ID_ASU_UM = "id_asu_um";
	public final static String ID_ESTADO_VISITA = "id_estado_visita";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID_PERSONA + " TEXT NOT NULL, " + 
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM + " INTEGER NOT NULL, " +
		ID_ESTADO_VISITA + " INTEGER NOT NULL, "+
		"PRIMARY KEY ("+ID_PERSONA+","+FECHA+")" +
		"); ";
	
	//POJO
	public String id_persona;
	public String fecha;
	public int id_asu_um;
	public int id_estado_visita;

	public static Uri AgregarNuevaVisita(Context context, Visita visita) throws Exception{
		ContentValues cv = DatosUtil.ContentValuesDesdeObjeto(visita);
		Uri salida = context.getContentResolver().insert(ProveedorContenido.VISITA_CONTENT_URI, cv);
		if(salida != null)
			Log.d(NOMBRE_TABLA, "Se ha insertado nuevo registro id: "+salida.getLastPathSegment());
		return salida;
	}
}
