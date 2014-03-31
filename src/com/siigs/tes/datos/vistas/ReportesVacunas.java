package com.siigs.tes.datos.vistas;

import android.content.Context;
import android.database.Cursor;

import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.ProveedorContenido;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.Vacuna;

/**
 * Describe la consulta anidada de las Vacunas aplicadas en todo el sistema en un periodo de tiempo
 * @author Axel
 *
 */
public class ReportesVacunas {
	
	public static Cursor getReportesVacunas(Context context, String fechaInicio, String fechaFin){
		String selection = ControlVacuna.ID_ASU_UM+"="
				+((TesAplicacion)context.getApplicationContext()).getUnidadMedica();
		
		if(fechaInicio != null)
			selection += " AND cv."+ControlVacuna.FECHA+">='"+fechaInicio+"'";
		if(fechaFin != null)
			selection += " AND cv."+ControlVacuna.FECHA+"<='"+fechaFin+"'";
		//return new CursorLoader(context, ProveedorContenido.REPORTE_VACUNAS_CONTENT_URI, null, selection, null, null);
		return context.getContentResolver().query(ProveedorContenido.REPORTE_VACUNAS_CONTENT_URI, 
				null, selection, null, null);
	}
	
	//COLUMNAS DE LA CONSULTA
	public final static String ID = Vacuna.ID;
	private final static String COL__ID = "v."+Vacuna.ID;
	
	public final static String DESCRIPCION = Vacuna.DESCRIPCION; //sin versión privada pues no se duplica
	
	public final static String APLICADAS = "aplicadas";
	private final static String COL_APLICADAS = "count(cv."+ControlVacuna.ID_VACUNA+") " + APLICADAS;
	
	public final static String LOTES = "lotes";
	private final static String COL_LOTES = "count(distinct " + "cv."+ControlVacuna.CODIGO_BARRAS +") " + LOTES;
	
	public final static String SIN_LOTE = "sin_lote";
	private final static String COL_SIN_LOTE = "count(cv."+ControlVacuna.ID_VACUNA+") - count(cv."+ControlVacuna.CODIGO_BARRAS+") "+ SIN_LOTE;
	
	
	//ELEMENTOS USADOS PARA CONSTRUIR ESTA CONSULTA
	public final static String[] COLUMNAS = new String[]{COL__ID, DESCRIPCION, COL_APLICADAS, COL_LOTES, COL_SIN_LOTE};

	public final static String TABLAS = ControlVacuna.NOMBRE_TABLA+" cv JOIN "+ Vacuna.NOMBRE_TABLA
			+" v ON cv."+ControlVacuna.ID_VACUNA+"=v."+Vacuna.ID;
	
	public final static String GROUPBY = "v."+Vacuna.ID+", v."+Vacuna.DESCRIPCION;
}
