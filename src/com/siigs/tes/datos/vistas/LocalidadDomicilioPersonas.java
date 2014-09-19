package com.siigs.tes.datos.vistas;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.siigs.tes.R;
import com.siigs.tes.datos.ProveedorContenido;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.Persona;

/**
 * Regresa lista de {@link ArbolSegmentacion} de id_asu_localidad_domicilio de cada persona 
 * @author Axel
 *
 */
public class LocalidadDomicilioPersonas {
	
	public static List<ArbolSegmentacion> getLocalidadDomicilio(Context context){		
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.VISTA_LOCALIDAD_DOMICILIO_PERSONA_CONTENT_URI, 
				null, null, null, DESCRIPCION + " ASC");
		List<ArbolSegmentacion> salida = new ArrayList<ArbolSegmentacion>();
		ArbolSegmentacion dummy = new ArbolSegmentacion();
		dummy._id = 0;
		dummy.descripcion = context.getString(R.string.indistinto);
		salida.add(dummy);
		while(cur.moveToNext()){
			dummy = new ArbolSegmentacion();
			dummy._id = cur.getInt(cur.getColumnIndex(ID));
			dummy.descripcion = cur.getString(cur.getColumnIndex(DESCRIPCION));
			salida.add(dummy);
		}
		cur.close();
		return salida;
	}
	
	//COLUMNAS DE LA CONSULTA
	public final static String ID = ArbolSegmentacion.ID;
	private final static String COL__ID = "a."+ArbolSegmentacion.ID;
	
	public final static String DESCRIPCION = ArbolSegmentacion.DESCRIPCION; //sin versión privada pues no se duplica
	
	
	//ELEMENTOS USADOS PARA CONSTRUIR ESTA CONSULTA
	public final static String[] COLUMNAS = new String[]{COL__ID, DESCRIPCION};

	public final static String TABLAS = ArbolSegmentacion.NOMBRE_TABLA+" a JOIN "+ Persona.NOMBRE_TABLA
			+" p ON a."+ArbolSegmentacion.ID+"=p."+Persona.ID_ASU_LOCALIDAD_DOMICILIO;
	
}
