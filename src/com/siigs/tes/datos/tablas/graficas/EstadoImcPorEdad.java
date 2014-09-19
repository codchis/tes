package com.siigs.tes.datos.tablas.graficas;

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
public class EstadoImcPorEdad {

	public final static String NOMBRE_TABLA = "cns_imc_x_edad"; //nombre en BD
	
	//Columnas en la nube
	public final static String SEXO = "sexo";
	public final static String EDAD_MESES = "edad_meses";
	public final static String ID_ESTADO_IMC = "id_estado_imc";
	public final static String IMC = "imc";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		SEXO + " TEXT NOT NULL, " +
		EDAD_MESES + " INTEGER NOT NULL, " +
		ID_ESTADO_IMC + " INTEGER NOT NULL, " +
		IMC + " NUMERIC NOT NULL, " +
		"UNIQUE (" + SEXO + "," + EDAD_MESES + "," + ID_ESTADO_IMC + ")" +
		"); ";
	
	//POJO
	public String sexo;
	public int edad_meses;
	public int id_estado_imc;
	public double imc;
	
	public static List<EstadoImcPorEdad> getPorEstado(Context context, int idEstado, String sexo) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.ESTADO_IMC_POR_EDAD_CONTENT_URI, null, 
				ID_ESTADO_IMC+"=? and "+SEXO+"=?", new String[]{idEstado+"", sexo}, null);
		List<EstadoImcPorEdad> salida = DatosUtil.ObjetosDesdeCursor(cur, EstadoImcPorEdad.class);
		cur.close();
		return salida;
	}
}
