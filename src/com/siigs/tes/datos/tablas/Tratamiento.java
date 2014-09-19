package com.siigs.tes.datos.tablas;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.siigs.tes.R;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Tratamiento {

	public final static String NOMBRE_TABLA = "cns_tratamiento"; //nombre en BD
	
	//Valores de nivel de atención
	public final static String NIVEL_ATENCION_1 = "CB";
	public final static String NIVEL_ATENCION_2 = "CAT";
	public final static String NIVEL_ATENCION_3 = "CLV";
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String TIPO = "tipo";
	public final static String GRUPO_ATENCION = "grupo_atencion";
	public final static String DESCRIPCION = "descripcion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		TIPO + " TEXT NOT NULL, " +
		GRUPO_ATENCION + " TEXT NOT NULL, " +
		DESCRIPCION + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	public int id;
	public String tipo;
	public String grupo_atencion;
	public String descripcion;
	public int activo;

	/**
	 * @deprecated Conservado para compatibilidad con código viejo. Use {@link Tratamiento#getTipos(Context, int)}
	 */
	@Deprecated
	public static String[] getTipos(Context contexto){
		Cursor cur = contexto.getContentResolver().query(ProveedorContenido.TRATAMIENTO_CONTENT_URI,
				new String[]{"distinct "+TIPO}, ACTIVO+"=1", null, TIPO + " ASC");
		String[] salida = new String[cur.getCount()];
		int index = 0;
		while(cur.moveToNext())
			salida[index++] = cur.getString(cur.getColumnIndex(TIPO));
		cur.close();
		return salida;
	}
	
	/**
	 * Devuelve los tipos de tratamiento disponibles para una unidad médica con el nivel de atención especificado.
	 * @param contexto
	 * @param nivelAtencion El nivel de atención de la unidad médica que está llamando esta función
	 * @return Arreglo con los distintos tipso de tratamiento disponibles ó arreglo vacío si no hay resultados
	 */
	public static String[] getTipos(Context contexto, int nivelAtencion){
		//En vez de crear una consulta JOIN en el ContentProvider extraemos los grupo_atencion de GrupoAtencion
		//y creamos con ellos un filtro para consulta de tratamientos.
		List<GrupoAtencion> grupos = GrupoAtencion.getPorNivel(contexto, nivelAtencion);
		String filtroAtencion = "";
		for(GrupoAtencion grupo : grupos)
			filtroAtencion += (filtroAtencion.equals("") ? "" : " OR ") + GRUPO_ATENCION + "='" + grupo.grupo_atencion + "'";
		if(!filtroAtencion.equals("") )
			filtroAtencion = " AND (" + filtroAtencion + ")";
		
		Cursor cur = contexto.getContentResolver().query(ProveedorContenido.TRATAMIENTO_CONTENT_URI,
				new String[]{"distinct "+TIPO}, ACTIVO+"=1" + filtroAtencion, null, TIPO + " COLLATE UNICODE ASC");
		String[] salida = new String[cur.getCount()];
		int index = 0;
		while(cur.moveToNext())
			salida[index++] = cur.getString(cur.getColumnIndex(TIPO));
		cur.close();
		return salida;
	}
	
	/**
	 * @deprecated Conservado para compatibilidad con código viejo. Use {@link Tratamiento#getTratamientosConTipo(Context, String, int)}
	 */
	@Deprecated
	public static List<Tratamiento> getTratamientosConTipo(Context contexto, String tipo){
		Cursor cur = contexto.getContentResolver().query(ProveedorContenido.TRATAMIENTO_CONTENT_URI, 
				null, ACTIVO+"=1 AND "+TIPO+"=?", new String[]{tipo}, DESCRIPCION +" ASC");
		List<Tratamiento> salida = DatosUtil.ObjetosDesdeCursor(cur, Tratamiento.class);
		cur.close();
		return salida;
	}
	
	/**
	 * Devuelve los tratamientos del tipo especificado y que son permitidos de acuerdo al nivel de atención especificado
	 * @param contexto
	 * @param tipo Tipo de tratamiento.
	 * @param nivelAtencion Nivel de atención de la unidad médica que está llamando esta función.
	 * @return Lista de objetos {@link Tratamiento} ó lista vacía si no hay resultados
	 */
	public static List<Tratamiento> getTratamientosConTipo(Context contexto, String tipo, int nivelAtencion){
		//En vez de crear una consulta JOIN en el ContentProvider extraemos los grupo_atencion de GrupoAtencion
		//y creamos con ellos un filtro para consulta de tratamientos.
		List<GrupoAtencion> grupos = GrupoAtencion.getPorNivel(contexto, nivelAtencion);
		String filtroAtencion = "";
		for(GrupoAtencion grupo : grupos)
			filtroAtencion += (filtroAtencion.equals("") ? "" : " OR ") + GRUPO_ATENCION + "='" + grupo.grupo_atencion + "'";
		if(!filtroAtencion.equals("") )
			filtroAtencion = " AND (" + filtroAtencion + ")";
				
		Cursor cur = contexto.getContentResolver().query(ProveedorContenido.TRATAMIENTO_CONTENT_URI, 
				null, ACTIVO+"=1 AND "+TIPO+"=?" + filtroAtencion, new String[]{tipo}, DESCRIPCION +" COLLATE UNICODE ASC");
		List<Tratamiento> salida = DatosUtil.ObjetosDesdeCursor(cur, Tratamiento.class);
		cur.close();
		return salida;
	}
	
	/**
	 * Regresa una lista de objetos {@link Tratamiento} basado en los id's recibidos en <b>lista_tratamientos</b>
	 * @param context
	 * @param lista_tratamientos Cadena de identificadores de tratamiento separada por comas ","
	 * @return Lista con los tratamientos existentes en base de datos que estén incluídos en <b>lista_tratamientos</b>
	 */
	public static List<Tratamiento> getTratamientosConId(Context context, String lista_tratamientos){
		String[] ids = lista_tratamientos.split(",");
		String filtro ="";		
		for(String id : ids)
			filtro += (filtro.equals("") ? "" : " OR ") + ID + "=" + id;
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.TRATAMIENTO_CONTENT_URI, null, filtro, null, null);
		List<Tratamiento> salida = DatosUtil.ObjetosDesdeCursor(cur, Tratamiento.class);
		cur.close();
		return salida;
	}
	
	/**
	 * Regresa la descripción del tratamiento especificado
	 * @param context
	 * @param id Identificador del tratamiento a buscar
	 * @return String con la descripción del tratamiento ó un String genérico 
	 */
	public static String getDescripcion(Context context, int id){
		Uri uri = Uri.withAppendedPath(ProveedorContenido.TRATAMIENTO_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, new String[]{DESCRIPCION}, null, null, null);
		String salida;
		if(cur.moveToNext() ) //debería haber resultados
			salida = cur.getString(cur.getColumnIndex(DESCRIPCION));
		else salida = context.getString(R.string.desconocido);
		cur.close();
		return salida;
	}
}
