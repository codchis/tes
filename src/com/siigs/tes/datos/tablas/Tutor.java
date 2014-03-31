package com.siigs.tes.datos.tablas;

import android.content.Context;
import android.database.Cursor;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Tutor {

	public final static String NOMBRE_TABLA = "cns_tutor"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String CURP = "curp";
	public final static String NOMBRE = "nombre";
	public final static String APELLIDO_PATERNO = "apellido_paterno";
	public final static String APELLIDO_MATERNO = "apellido_materno";
	public final static String SEXO = "sexo";
	public final static String TELEFONO = "telefono";
	public final static String CELULAR = "celular";
	public final static String ID_OPERADORA_CELULAR = "id_operadora_celular";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+
		ID + " TEXT NOT NULL , " +
		CURP + " TEXT NOT NULL COLLATE NOCASE, "+
		NOMBRE + " TEXT NOT NULL COLLATE NOCASE, "+
		APELLIDO_PATERNO + " TEXT NOT NULL COLLATE NOCASE, " +
		APELLIDO_MATERNO + " TEXT NOT NULL COLLATE NOCASE, " +
		SEXO + " TEXT NOT NULL COLLATE NOCASE, " +
		TELEFONO + " TEXT DEFAULT NULL, "+
		CELULAR + " TEXT DEFAULT NULL, "+
		ID_OPERADORA_CELULAR + " INTEGER DEFAULT NULL, "+
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		" UNIQUE (" + ID + ")"+
		"); ";
	
	//POJO
	public String id;
	public String curp;
	public String nombre;
	public String apellido_paterno;
	public String apellido_materno;
	public String sexo;
	public String telefono;
	public String celular;
	public Integer id_operadora_celular;
	public String ultima_actualizacion;
	
	
	public static Tutor getTutorDePersona(Context context, String idPersona){
		String idTutor = PersonaTutor.getIdTutorDePersona(context, idPersona);
		
		Cursor cur = context.getContentResolver().query(ProveedorContenido.TUTOR_CONTENT_URI, null, 
				ID+"=?", new String[]{idTutor}, null);
		if(!cur.moveToNext()){
			cur.close();
			return null;
		}
		Tutor salida = null;
		try {
			salida = DatosUtil.ObjetoDesdeCursor(cur, Tutor.class);
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {}
		cur.close();
		return salida;
	}
}
