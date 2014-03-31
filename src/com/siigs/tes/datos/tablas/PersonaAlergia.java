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
public class PersonaAlergia {

	public final static String NOMBRE_TABLA = "cns_persona_x_alergia"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_ALERGIA = "id_alergia";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
		ID_PERSONA + " TEXT NOT NULL, " +
		ID_ALERGIA + " INTEGER NOT NULL, " +
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		"UNIQUE (" + ID_PERSONA + "," + ID_ALERGIA + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public int id_alergia;
	public String ultima_actualizacion;

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof PersonaAlergia))return false;
		PersonaAlergia c = (PersonaAlergia)o;
		return id_alergia == c.id_alergia && id_persona.equals(c.id_persona);
	}
	
	public static Uri AgregarNuevaAlergia(Context context, PersonaAlergia alergia){
		ContentValues cv = new ContentValues();
		cv.put(ID_PERSONA, alergia.id_persona);
		cv.put(ID_ALERGIA, alergia.id_alergia);
		cv.put(ULTIMA_ACTUALIZACION, alergia.ultima_actualizacion);
		Uri salida = context.getContentResolver().insert(ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI, cv);
		if(salida != null)
			Log.d(NOMBRE_TABLA, "Se ha insertado nuevo registro id: "+salida.getLastPathSegment());
		return salida;
	}
	
	public static List<PersonaAlergia> getAlergiasPersona(Context context, String idPersona) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.PERSONA_ALERGIA_CONTENT_URI, null, 
				ID_PERSONA+"=?", new String[]{idPersona}, null);
		List<PersonaAlergia> salida = DatosUtil.ObjetosDesdeCursor(cur, PersonaAlergia.class);
		cur.close();
		return salida;
	}
	
}
