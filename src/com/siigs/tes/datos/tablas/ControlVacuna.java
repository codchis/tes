package com.siigs.tes.datos.tablas;

import java.util.List;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class ControlVacuna {

	public final static String NOMBRE_TABLA = "cns_control_vacuna"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_VACUNA = "id_vacuna";
	public final static String FECHA = "fecha";
	public final static String ID_ASU_UM = "id_asu_um";
	public final static String CODIGO_BARRAS = "codigo_barras";
	public final static String ID_INVITADO = "id_invitado";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		ID_VACUNA + " INTEGER NOT NULL, "+
		FECHA + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM + " INTEGER NOT NULL, "+
		CODIGO_BARRAS + " TEXT DEFAULT NULL, "+
		ID_INVITADO + " INTEGER DEFAULT NULL, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA + "," + ID_VACUNA + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public int id_vacuna;
	public String fecha;
	public int id_asu_um;
	public String codigo_barras;
	public transient Integer id_invitado; //transient pues no se envía en JSON
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ControlVacuna))return false;
		ControlVacuna c = (ControlVacuna)o;
		return id_vacuna == c.id_vacuna && fecha.equals(c.fecha) && id_persona.equals(c.id_persona);
	}

	public static Uri AgregarNuevoControlVacuna(Context context, ControlVacuna vacuna) throws Exception{
		ContentValues cv = DatosUtil.ContentValuesDesdeObjeto(vacuna);
		Uri salida = context.getContentResolver().insert(ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, cv);
		if(salida != null)
			Log.d(NOMBRE_TABLA, "Se ha insertado nuevo registro id: "+salida.getLastPathSegment());
		return salida;
	}

	public static List<ControlVacuna> getVacunasPersona(Context context, String idPersona) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, null, 
				ID_PERSONA+"=?", new String[]{idPersona}, FECHA+" ASC");
		List<ControlVacuna> salida = DatosUtil.ObjetosDesdeCursor(cur, ControlVacuna.class);
		cur.close();
		return salida;
	}
	
	public static int getTotalCreadosDespues(Context context, String fecha){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.CONTROL_VACUNA_CONTENT_URI, 
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
