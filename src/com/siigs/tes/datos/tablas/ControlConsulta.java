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
public class ControlConsulta {

	public final static String NOMBRE_TABLA = "cns_control_consulta"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String CLAVE_CIE10 = "clave_cie10";
	public final static String FECHA = "fecha";
	public final static String ID_ASU_UM = "id_asu_um";
	public final static String ID_TRATAMIENTO = "id_tratamiento";
	public final static String GRUPO_FECHA_SECUENCIAL = "grupo_fecha_secuencial";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		CLAVE_CIE10 + " TEXT NOT NULL, "+
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM + " INTEGER NOT NULL, "+
		ID_TRATAMIENTO + " TEXT NOT NULL, "+ //lista de id's de cns_tratamiento separados por ","
		GRUPO_FECHA_SECUENCIAL + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA + "," + CLAVE_CIE10 + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public String clave_cie10;
	public String fecha;
	public int id_asu_um;
	public String id_tratamiento;
	public String grupo_fecha_secuencial;

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ControlConsulta))return false;
		ControlConsulta c = (ControlConsulta)o;
		return clave_cie10.equals(c.clave_cie10) && fecha.equals(c.fecha) && id_persona.equals(c.id_persona);
	}
	
	/**
	 * Regresa una lista de objetos {@link Tratamiento} basado en valores del campo <b>id_tratamiento</b> de esta instancia
	 * @param context
	 * @return Lista de objetos {@link Tratamiento}
	 */
	public  List<Tratamiento> getTratamientos(Context context){
		return Tratamiento.getTratamientosConId(context, id_tratamiento);
	}
	
	public static Uri AgregarNuevoControlConsulta(Context context, ControlConsulta consulta) throws Exception{
		ContentValues cv = DatosUtil.ContentValuesDesdeObjeto(consulta);
		Uri salida = context.getContentResolver().insert(ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI, cv);
		if(salida != null)
			Log.d(NOMBRE_TABLA, "Se ha insertado nuevo registro id: "+salida.getLastPathSegment());
		return salida;
	}
	
	public static List<ControlConsulta> getConsultasPersona(Context context, String idPersona) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI, null, 
				ID_PERSONA+"=?", new String[]{idPersona}, FECHA+" ASC");
		List<ControlConsulta> salida = DatosUtil.ObjetosDesdeCursor(cur, ControlConsulta.class);
		cur.close();
		return salida;
	}

	public static int getTotalCreadosDespues(Context context, String fecha){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.CONTROL_CONSULTA_CONTENT_URI, 
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
