package com.siigs.tes.datos.tablas;

import com.siigs.tes.datos.ProveedorContenido;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class PersonaTutor {

	public final static String NOMBRE_TABLA = "cns_persona_x_tutor"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String ID_TUTOR = "id_tutor";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT UNIQUE NOT NULL, " +
		ID_TUTOR + " TEXT NOT NULL, "+
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')) "+
		"); ";
	
	//POJO
	public String id_persona;
	public String id_tutor;
	public String ultima_actualizacion;
	
	public static String getIdTutorDePersona(Context context, String idPersona){
		Uri uri = ProveedorContenido.PERSONA_TUTOR_CONTENT_URI;
		String where = ID_PERSONA + "=?";
		String[] paramWhere = new String[]{idPersona};
		Cursor cur = context.getContentResolver().query(uri, new String[]{ID_TUTOR}, where, paramWhere, null);
		
		cur.moveToNext(); //debería haber resultados
		String salida = cur.getString(cur.getColumnIndex(ID_TUTOR));
		cur.close();
		return salida;
	}
}
