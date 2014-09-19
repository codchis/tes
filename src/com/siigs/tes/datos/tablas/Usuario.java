package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Usuario {

	public final static String NOMBRE_TABLA = "sis_usuario"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id";
	public final static String NOMBRE_USUARIO = "nombre_usuario";
	public final static String CLAVE = "clave";
	public final static String NOMBRE = "nombre";
	public final static String APELLIDO_PATERNO = "apellido_paterno";
	public final static String APELLIDO_MATERNO = "apellido_materno";
	public final static String CORREO = "correo";
	public final static String ACTIVO = "activo";
	public final static String ID_GRUPO	= "id_grupo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		NOMBRE_USUARIO + " TEXT NOT NULL COLLATE NOCASE, " +
		CLAVE + " TEXT NOT NULL, "+
		NOMBRE + " TEXT NOT NULL COLLATE NOCASE, "+
		APELLIDO_PATERNO + " TEXT DEFAULT NULL COLLATE NOCASE, "+
		APELLIDO_MATERNO + " TEXT DEFAULT NULL COLLATE NOCASE, "+
		CORREO + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL, "+
		ID_GRUPO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public String nombre_usuario;
	public String clave;
	public String nombre;
	public String apellido_paterno;
	public String apellido_materno;
	public String correo;
	public int activo;
	public int id_grupo;
	
	public static Cursor getUsuariosActivos(Context context){
		return context.getContentResolver().query(ProveedorContenido.USUARIO_CONTENT_URI, 
				null, ACTIVO + "=1", null, NOMBRE_USUARIO + " asc");
	}
	
	public static Usuario getUsuarioConId(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.USUARIO_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
		cur.moveToNext(); //debería haber resultados
		Usuario salida = null;
		try {
			salida = DatosUtil.ObjetoDesdeCursor(cur, Usuario.class);
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {}
		cur.close();
		return salida;
	}
	
	public static int getTotalUsuariosActivos(Context context){
		Cursor cur = getUsuariosActivos(context);
		int salida = cur.getCount();
		cur.close();
		return salida;
	}
	
	public static List<Usuario> getTodos(Context context){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.USUARIO_CONTENT_URI, 
				null, null, null, null);
		List<Usuario> salida = DatosUtil.ObjetosDesdeCursor(cur, Usuario.class);
		cur.close();
		return salida;
	}
	
	public static void AgregarRegistros(Context context, List<Usuario> usuarios) throws Exception{
		ContentValues[] registros = new ContentValues[usuarios.size()];
		int i=0;
		for(Usuario usuario : usuarios)
			registros[i++]= DatosUtil.ContentValuesDesdeObjeto(usuario);
		context.getContentResolver().bulkInsert(ProveedorContenido.USUARIO_CONTENT_URI,	registros);
	}
}
