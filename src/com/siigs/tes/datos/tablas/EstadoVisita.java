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
public class EstadoVisita {

	public final static String NOMBRE_TABLA = "cns_estado_visita"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String DESCRIPCION = "descripcion";
	public final static String CLAVE = "clave";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + 
		DESCRIPCION + " TEXT NOT NULL, "+
		CLAVE + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	public int id;
	public String descripcion;
	public String clave;
	public int activo;

	public static List<EstadoVisita> getEstadosVisitasActivas(Context context){
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.ESTADO_VISITA_CONTENT_URI, null, ACTIVO + "=1", null, DESCRIPCION);
		List<EstadoVisita> salida = DatosUtil.ObjetosDesdeCursor(cur, EstadoVisita.class);
		cur.close();
		return salida;
	}
}
