package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class SalesRehidratacion {

	public final static String NOMBRE_TABLA = "cns_sales_rehidratacion"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String FECHA = "fecha";
	public final static String ID_ASU_UM = "id_asu_um";
	public final static String CANTIDAD = "cantidad";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM + " INTEGER NOT NULL, "+
		CANTIDAD + " INTEGER NOT NULL, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public String fecha;
	public int id_asu_um;
	public int cantidad;
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof SalesRehidratacion))return false;
		SalesRehidratacion c = (SalesRehidratacion)o;
		return fecha.equals(c.fecha) && id_persona.equals(c.id_persona);
	}
	
	public static Uri AgregarNuevaSalRehidratacion(Context context, SalesRehidratacion control) throws Exception{
		ContentValues cv = DatosUtil.ContentValuesDesdeObjeto(control);
		Uri salida = context.getContentResolver().insert(ProveedorContenido.SALES_REHIDRATACION_CONTENT_URI, cv);
		if(salida != null)
			Log.d(NOMBRE_TABLA, "Se ha insertado nuevo registro id: "+salida.getLastPathSegment());
		return salida;
	}
	
	public static List<SalesRehidratacion> getSalesRehidratacionPersona(Context context, String idPersona) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.SALES_REHIDRATACION_CONTENT_URI, null, 
				ID_PERSONA+"=?", new String[]{idPersona}, FECHA+" ASC");
		List<SalesRehidratacion> salida = DatosUtil.ObjetosDesdeCursor(cur, SalesRehidratacion.class);
		cur.close();
		return salida;
	}

	public static int getTotalCreadosDespues(Context context, String fecha){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.SALES_REHIDRATACION_CONTENT_URI, 
				new String[]{"count(*)"}, FECHA + ">=?", new String[]{fecha}, null);
		if(!cur.moveToNext()){
			cur.close();
			return 0;
		}
		int salida = cur.getInt(0);
		cur.close();
		return salida;
	}
}
