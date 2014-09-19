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
public class EstadoNutricionAlturaPorEdad {

	public final static String NOMBRE_TABLA = "cns_edo_nutri_altura_x_edad"; //nombre en BD
	
	//Columnas en la nube
	public final static String SEXO = "sexo";
	public final static String EDAD_MESES = "edad_meses";
	public final static String ID_ESTADO_NUTRICION_ALTURA = "id_estado_nutricion_altura";
	public final static String ALTURA = "altura";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		SEXO + " TEXT NOT NULL, " +
		EDAD_MESES + " INTEGER NOT NULL, " +
		ID_ESTADO_NUTRICION_ALTURA + " INTEGER NOT NULL, " +
		ALTURA + " NUMERIC NOT NULL, " +
		"UNIQUE (" + SEXO + "," + EDAD_MESES + "," + ID_ESTADO_NUTRICION_ALTURA + ")" +
		"); ";
	
	//POJO
	public String sexo;
	public int edad_meses;
	public int id_estado_nutricion_altura;
	public double altura;
	
	public static List<EstadoNutricionAlturaPorEdad> getPorEstado(Context context, int idEstado, String sexo) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.ESTADO_NUTRICION_ALTURA_POR_EDAD_CONTENT_URI, null, 
				ID_ESTADO_NUTRICION_ALTURA+"=? and "+SEXO+"=?", new String[]{idEstado+"", sexo}, null);
		List<EstadoNutricionAlturaPorEdad> salida = DatosUtil.ObjetosDesdeCursor(cur, EstadoNutricionAlturaPorEdad.class);
		cur.close();
		return salida;
	}
}
