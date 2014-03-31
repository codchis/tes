package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class PersonaAfiliacion {

	public final static String NOMBRE_TABLA = "cns_persona_x_afiliacion"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_AFILIACION = "id_afiliacion";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_PERSONA + " TEXT NOT NULL, " +
		ID_AFILIACION + " INTEGER NOT NULL, " +
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		"UNIQUE (" + ID_PERSONA + "," + ID_AFILIACION + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public int id_afiliacion;
	public String ultima_actualizacion;

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof PersonaAfiliacion))return false;
		PersonaAfiliacion c = (PersonaAfiliacion)o;
		return id_afiliacion == c.id_afiliacion && id_persona.equals(c.id_persona);
	}
	
	public static List<PersonaAfiliacion> getAfiliacionesPersona(Context context, String idPersona) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.PERSONA_AFILIACION_CONTENT_URI, null, 
				ID_PERSONA+"=?", new String[]{idPersona}, null);
		List<PersonaAfiliacion> salida = DatosUtil.ObjetosDesdeCursor(cur, PersonaAfiliacion.class);
		cur.close();
		return salida;
	}
}
