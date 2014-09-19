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
public class ControlNutricional {
	
	public static double HEMOGLOBINA_NULL = 0.0; //Valor para el cual hemoglobina será considerado vacío

	public final static String NOMBRE_TABLA = "cns_control_nutricional"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String FECHA = "fecha";
	public final static String ID_ASU_UM = "id_asu_um";
	public final static String PESO = "peso";
	public final static String ALTURA = "altura"; //estatura
	public final static String TALLA = "talla"; //no usado (cintura)
	public final static String HEMOGLOBINA = "hemoglobina";
	public final static String ID_INVITADO = "id_invitado";
	
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
		PESO + " NUMERIC NOT NULL, "+
		ALTURA + " INTEGER NOT NULL, "+
		TALLA + " INTEGER NOT NULL, "+
		HEMOGLOBINA + " NUMERIC NOT NULL, "+
		ID_INVITADO + " INTEGER DEFAULT NULL, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public String fecha;
	public int id_asu_um;
	public double peso;
	public int altura;
	public int talla;
	public double hemoglobina;
	public transient Integer id_invitado; //transient pues no se envía en JSON

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ControlNutricional))return false;
		ControlNutricional c = (ControlNutricional)o;
		return fecha.equals(c.fecha) && id_persona.equals(c.id_persona);
	}
	
	public static Uri AgregarNuevoControlNutricional(Context context, ControlNutricional control) throws Exception{
		ContentValues cv = DatosUtil.ContentValuesDesdeObjeto(control);
		Uri salida = context.getContentResolver().insert(ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI, cv);
		if(salida != null)
			Log.d(NOMBRE_TABLA, "Se ha insertado nuevo registro id: "+salida.getLastPathSegment());
		return salida;
	}
	
	public static List<ControlNutricional> getControlesNutricionalesPersona(Context context, String idPersona) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI, null, 
				ID_PERSONA+"=?", new String[]{idPersona}, FECHA+" ASC");
		List<ControlNutricional> salida = DatosUtil.ObjetosDesdeCursor(cur, ControlNutricional.class);
		cur.close();
		return salida;
	}

	public static int getTotalCreadosDespues(Context context, String fecha){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.CONTROL_NUTRICIONAL_CONTENT_URI, 
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
