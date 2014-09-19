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
public class ControlPerimetroCefalico {

	public final static String NOMBRE_TABLA = "cns_control_peri_cefa"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String PERIMETRO_CEFALICO = "perimetro_cefalico";
	public final static String FECHA = "fecha";
	public final static String ID_ASU_UM = "id_asu_um";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		PERIMETRO_CEFALICO + " NUMERIC NOT NULL, "+
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM + " INTEGER NOT NULL, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA + "," + PERIMETRO_CEFALICO + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public double perimetro_cefalico;
	public String fecha;
	public int id_asu_um;

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ControlPerimetroCefalico))return false;
		ControlPerimetroCefalico c = (ControlPerimetroCefalico)o;
		return perimetro_cefalico == c.perimetro_cefalico 
				&& fecha.equals(c.fecha) && id_persona.equals(c.id_persona);
	}
	
	public static Uri AgregarNuevoControlPerimetroCefalico(Context context, 
			ControlPerimetroCefalico accion) throws Exception{
		
		ContentValues cv = DatosUtil.ContentValuesDesdeObjeto(accion);
		Uri salida = context.getContentResolver().insert(ProveedorContenido.CONTROL_PERIMETRO_CEFALICO_CONTENT_URI, cv);
		if(salida != null)
			Log.d(NOMBRE_TABLA, "Se ha insertado nuevo registro id: "+salida.getLastPathSegment());
		return salida;
	}
	
	public static List<ControlPerimetroCefalico> getPerimetrosCefalicosPersona(Context context, String idPersona) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONTROL_PERIMETRO_CEFALICO_CONTENT_URI, null, 
				ID_PERSONA+"=?", new String[]{idPersona}, FECHA+" ASC");
		List<ControlPerimetroCefalico> salida = DatosUtil.ObjetosDesdeCursor(cur, ControlPerimetroCefalico.class);
		cur.close();
		return salida;
	}

	public static int getTotalCreadosDespues(Context context, String fecha){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.CONTROL_PERIMETRO_CEFALICO_CONTENT_URI, 
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
