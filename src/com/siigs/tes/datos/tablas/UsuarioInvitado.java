package com.siigs.tes.datos.tablas;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class UsuarioInvitado {

	public final static String NOMBRE_TABLA = "invitado"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_INVITADO = "_id";
	public final static String ID_USUARIO_CREADOR = "id_usuario_creador";
	public final static String NOMBRE = "nombre";
	public final static String FECHA_CREACION = "fecha_creacion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID_INVITADO + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_USUARIO_CREADOR + " INTEGER NOT NULL, " +
		NOMBRE + " TEXT NOT NULL COLLATE NOCASE, "+
		FECHA_CREACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ACTIVO + " INTEGER NOT NULL DEFAULT 1"+
		"); ";

	//POJO (NO USADO PARA CONVERTIR A JSON, SOLO PARA TENER UN DATAOBJECT)
	public int _id;
	public int id_usuario_creador;
	public String nombre;
	public String fecha_creacion;
	public int activo;

	public static int ID_GRUPO = 6; //DIRECTO DE BASE DE DATOS FINAL
	
	public static Cursor getUsuariosInvitadosActivos(Context context){
		return context.getContentResolver().query(ProveedorContenido.USUARIO_INVITADO_CONTENT_URI, 
				null, ACTIVO + "=1", null, NOMBRE + " asc");
	}
	
	public static UsuarioInvitado getUsuarioInvitadoConId(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.USUARIO_INVITADO_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
		cur.moveToNext(); //Debería haber resultados
		UsuarioInvitado salida=null;
		try {
			salida = DatosUtil.ObjetoDesdeCursor(cur, UsuarioInvitado.class);
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {}
		cur.close();
		return salida;
	}
}