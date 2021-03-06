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
public class EstadoNutricionPesoPorAltura {

	public final static String NOMBRE_TABLA = "cns_edo_nutri_peso_x_altura"; //nombre en BD
	
	//Columnas en la nube
	public final static String SEXO = "sexo";
	public final static String ALTURA = "altura";
	public final static String ID_ESTADO_NUTRICION_PESO = "id_estado_nutricion_peso";
	public final static String PESO = "peso";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + //para adaptadores android
		SEXO + " TEXT NOT NULL, " +
		ALTURA + " NUMERIC NOT NULL, " +
		ID_ESTADO_NUTRICION_PESO + " INTEGER NOT NULL, " +
		PESO + " NUMERIC NOT NULL, " +
		"UNIQUE (" + SEXO + "," + ALTURA + "," + ID_ESTADO_NUTRICION_PESO + ")" +
		"); ";
	
	//POJO
	public String sexo;
	public double altura;
	public int id_estado_nutricion_peso;
	public double peso;
	
	public static List<EstadoNutricionPesoPorAltura> getPorEstado(Context context, int idEstado, String sexo) {
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.ESTADO_NUTRICION_PESO_POR_ALTURA_CONTENT_URI, null, 
				ID_ESTADO_NUTRICION_PESO+"=? and "+SEXO+"=?", new String[]{idEstado+"", sexo}, null);
		List<EstadoNutricionPesoPorAltura> salida = DatosUtil.ObjetosDesdeCursor(cur, EstadoNutricionPesoPorAltura.class);
		cur.close();
		return salida;
	}
}
