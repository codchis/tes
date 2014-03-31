package com.siigs.tes.datos.tablas;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class AntiguoDomicilio {

	public final static String NOMBRE_TABLA = "cns_antiguo_domicilio"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID_PERSONA = "id_persona";
	public final static String FECHA_CAMBIO = "fecha_cambio";
	public final static String ID_ASU_LOCALIDAD_DOMICILIO = "id_asu_localidad_domicilio";
	public final static String CALLE_DOMICILIO = "calle_domicilio";
	public final static String NUMERO_DOMICILIO = "numero_domicilio";
	public final static String COLONIA_DOMICILIO = "colonia_domicilio";
	public final static String REFERENCIA_DOMICILIO = "referencia_domicilio";
	public final static String CP_DOMICILIO = "cp_domicilio";
	
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		ID_PERSONA + " TEXT NOT NULL, "+
		FECHA_CAMBIO + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_LOCALIDAD_DOMICILIO + " INTEGER NOT NULL, "+
		CALLE_DOMICILIO + " TEXT NOT NULL, " +
		NUMERO_DOMICILIO + " TEXT DEFAULT NULL, " +
		COLONIA_DOMICILIO + " TEXT DEFAULT NULL, "+
		REFERENCIA_DOMICILIO + " TEXT DEFAULT NULL, " +
		CP_DOMICILIO + " INTEGER, "+
		"UNIQUE (" + ID_PERSONA + "," + FECHA_CAMBIO + ")" +
		"); ";
	
	//POJO
	public String id_persona;
	public String fecha_cambio;
	public int id_asu_localidad_domicilio;
	public String calle_domicilio;
	public String numero_domicilio;
	public String colonia_domicilio;
	public String referencia_domicilio;
	public Integer cp_domicilio;
	
	public static Uri AgregarAntiguoDomicilio(Context context, AntiguoDomicilio dom) throws Exception {
		ContentValues cv = DatosUtil.ContentValuesDesdeObjeto(dom);
		return context.getContentResolver().insert(ProveedorContenido.ANTIGUO_DOMICILIO_CONTENT_URI, cv);
	}
	
	public static AntiguoDomicilio DePersona(Persona p, String fechaCambio){
		AntiguoDomicilio ad = new AntiguoDomicilio();
		ad.fecha_cambio = fechaCambio;
		ad.id_asu_localidad_domicilio = p.id_asu_localidad_domicilio;
		ad.calle_domicilio = p.calle_domicilio;
		ad.colonia_domicilio = p.colonia_domicilio;
		ad.numero_domicilio = p.numero_domicilio;
		ad.cp_domicilio = p.cp_domicilio;
		ad.referencia_domicilio = p.referencia_domicilio;
		ad.id_persona = p.id;
		return ad;
	}
}
