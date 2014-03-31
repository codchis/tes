package com.siigs.tes.datos.tablas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.siigs.tes.R;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Alergia {

	public final static String NOMBRE_TABLA = "cns_alergia"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "_id"; //para adaptadores android
	public final static String TIPO = "tipo";
	public final static String DESCRIPCION = "descripcion";
	public final static String ACTIVO = "activo";
	
	//Columnas de control interno
	//public final static String _REMOTO_ID = "id"; //mapeo campo id en base de datos remota
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " + //para adaptadores android
		TIPO + " TEXT NOT NULL, "+
		DESCRIPCION + " TEXT NOT NULL, "+
		ACTIVO + " INTEGER NOT NULL "+
		"); ";
	
	//POJO
	@SerializedName("id")
	public int _id;
	public String tipo;
	public String descripcion;
	public int activo;
	
	
	public static List<Alergia> getAlergias(Context context){
		Uri uri = ProveedorContenido.ALERGIA_CONTENT_URI;
		Cursor cur = context.getContentResolver().query(uri, null, ACTIVO+"=1", null, null);
		
		List<Alergia> salida = DatosUtil.ObjetosDesdeCursor(cur, Alergia.class);
		cur.close();
		return salida;
	}
	
	/**
	 * Devuelve todas las alergias que Están ó NO Están en alergiasDePersona, según el valor
	 * de enAlergiasDePersona
	 * @param context
	 * @param alergiasDePersona
	 * @param enAlergiasDePersona true, devuelve alergias que están en <b>alergiasDePersona</b> false, lo contrario.
	 */
	public static List<Alergia> getAlergiasConLista(Context context, List<PersonaAlergia> alergiasDePersona, boolean enAlergiasDePersona){
		String whereIn = ACTIVO +"=1 and " + ID + 
				(!enAlergiasDePersona ? " not " : "") + " in (";
		for(PersonaAlergia pa : alergiasDePersona)
			whereIn += pa.id_alergia + ",";
		if(alergiasDePersona.size()>0)
			whereIn = whereIn.substring(0, whereIn.length()-1);
		whereIn += ")";
		
		Uri uri = ProveedorContenido.ALERGIA_CONTENT_URI;
		Cursor cur = context.getContentResolver().query(uri, null, whereIn, null, TIPO + "," +DESCRIPCION);
		List<Alergia> salida = new ArrayList<Alergia>();
		salida = DatosUtil.ObjetosDesdeCursor(cur, Alergia.class);
		cur.close();
		return salida;
	}
	
	/**
	 * Pequeño helper para determinar el id del icono del tipo de alergia definido poro <b>tipoAlergia</b>
	 * @param tipoAlergia El tipo de alergia en tabla Alergia
	 * @return resId de la imagen que representa a <b>tipoAlergia</b>
	 */
	public static int getResourceImagenTipoAlergia(String tipoAlergia){
		if(tipoAlergia.toLowerCase().equals("antibioticos"))return R.drawable.antibiotico;
		else if(tipoAlergia.toLowerCase().equals("antihipertensivos"))return R.drawable.antihipertensivos;
		else if(tipoAlergia.toLowerCase().equals("antiinflamatorios"))return R.drawable.antiinflamatorios;
		else if(tipoAlergia.toLowerCase().equals("quirurgicos"))return R.drawable.quirurgicos;
		else if(tipoAlergia.toLowerCase().equals("vitaminas"))return R.drawable.vitaminas;
		else if(tipoAlergia.toLowerCase().equals("alimentos"))return R.drawable.alimentos;
		else if(tipoAlergia.toLowerCase().equals("otros medicamentos"))return R.drawable.otrosmedicamentos;
		else return android.R.drawable.btn_radio;
	}
	
}
