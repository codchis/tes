package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.siigs.tes.R;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Consulta {

	public final static String NOMBRE_TABLA = "cns_cie10"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_CIE10 = "id_cie10";
	public final static String DESCRIPCION = "descripcion";
	public final static String ID_CATEGORIA = "id_categoria";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
		ID_CIE10 + " TEXT NOT NULL, " +
		DESCRIPCION + " TEXT NOT NULL, "+
		ID_CATEGORIA + " TEXT NOT NULL, " +
		ACTIVO + " INTEGER NOT NULL, "+
		"UNIQUE (" + ID_CIE10 + ")" +
		"); ";
	
	//POJO
	public String id_cie10;
	public String descripcion;
	public String id_categoria;
	public int activo;
	
	public static List<Consulta> getConsultasActivas(Context context){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONSULTA_CONTENT_URI, null, ACTIVO + "=1", null, DESCRIPCION);
		List<Consulta> salida = DatosUtil.ObjetosDesdeCursor(cur, Consulta.class);
		cur.close();
		return salida;
	}
	
	public static String getDescripcion(Context context, String idCie10){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONSULTA_CONTENT_URI, null, ID_CIE10+"=?", new String[]{idCie10}, null);
		String salida;
		if(cur.moveToNext()) //debería haber resultados
			salida = cur.getString(cur.getColumnIndex(DESCRIPCION));
		else salida = context.getString(R.string.desconocido);
		cur.close();
		return salida;
	}
	
	/**
	 * Regresa la categoría del registro que tenga el idCie10 especificado
	 * @param context Contexto
	 * @param idCie10 Clave cie10 que debe tener el registro a buscar
	 * @return Categoría del registro especificado
	 */
	public static String getCategoria(Context context, String idCie10){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONSULTA_CONTENT_URI, null, ID_CIE10+"=?", new String[]{idCie10}, null);
		String salida;
		if(cur.moveToNext()) //debería haber resultados
			salida = cur.getString(cur.getColumnIndex(ID_CATEGORIA));
		else salida = "";
		cur.close();
		return salida;
	}
	
	public static List<Consulta> getConsultasConCategoria(Context context, String idCategoria){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONSULTA_CONTENT_URI, null, ID_CATEGORIA + "=?", new String[]{idCategoria}, DESCRIPCION);
		List<Consulta> salida = DatosUtil.ObjetosDesdeCursor(cur, Consulta.class);
		cur.close();
		return salida;
	}
	
	
	public static Consulta getConsultaConId(Context context, int _id){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.CONSULTA_CONTENT_URI, null, _ID + "=?", new String[]{_id+""}, null);
		if(!cur.moveToFirst()){
			cur.close();
			return null;
		}
		Consulta salida = null;
		try {
			salida = DatosUtil.ObjetoDesdeCursor(cur, Consulta.class);
		} catch (Exception e) {e.printStackTrace();}
		cur.close();
		return salida;
	}
	
	/*public static String getClave(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.CONSULTA_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, new String[]{CLAVE}, null, null, null);
		String salida;
		if(cur.moveToNext()) //debería haber resultados
			salida = cur.getString(cur.getColumnIndex(CLAVE));
		else salida = context.getString(R.string.desconocido);
		cur.close();
		return salida;
	}*/
	
	/**
	 * Busca en las afecciones de acuerdo a al filtro especificado
	 * @param context Contexto
	 * @param like Cadena usada como criterio de búsqueda
	 * @return Cursor apuntando a los resultados existentes de acuerdo a la búsqueda
	 */
	public static Cursor buscar(Context context, String like){
		String selection = DESCRIPCION + " LIKE '%"+like+"%'";
		String[] selectionArgs = null;
		Cursor cur = context.getContentResolver().query(ProveedorContenido.CONSULTA_CONTENT_URI, 
				null, selection, selectionArgs, DESCRIPCION);
		return cur;
	}
}
