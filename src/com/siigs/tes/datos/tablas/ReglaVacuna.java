package com.siigs.tes.datos.tablas;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.siigs.tes.Sesion.DatosPaciente;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class ReglaVacuna {

	public final static String NOMBRE_TABLA = "cns_regla_vacuna"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String ID_VACUNA = "id_vacuna";
	public final static String DIA_INICIO_APLICACION_NACIDO = "dia_inicio_aplicacion_nacido";
	public final static String DIA_FIN_APLICACION_NACIDO = "dia_fin_aplicacion_nacido";
	public final static String ID_VACUNA_SECUENCIAL = "id_vacuna_secuencial";
	public final static String DIA_INICIO_APLICACION_SECUENCIAL = "dia_inicio_aplicacion_secuencial";
	public final static String DIA_FIN_APLICACION_SECUENCIAL = "dia_fin_aplicacion_secuencial";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	public final static String ACTIVO = "activo";
	public final static String ID_VIA_VACUNA = "id_via_vacuna";
	public final static String DOSIS = "dosis";
	public final static String REGION = "region";
	public final static String OBSERVACION_REGION = "observacion_region";
	public final static String ESQ_COM = "esq_com";
	public final static String ORDEN_ESQ_COM = "orden_esq_com";
	public final static String ALERGIAS = "alergias";
	public final static String FORZAR_APLICACION = "forzar_aplicacion"; //Define si se forza la aplicación de la regla
	
	//Columnas de control interno
	
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		ID + " INTEGER PRIMARY KEY NOT NULL, " +
		ID_VACUNA + " INTEGER NOT NULL, " +
		DIA_INICIO_APLICACION_NACIDO + " INTEGER DEFAULT NULL, " +
		DIA_FIN_APLICACION_NACIDO + " INTEGER DEFAULT NULL, " +
		ID_VACUNA_SECUENCIAL + " INTEGER DEFAULT NULL, " +
		DIA_INICIO_APLICACION_SECUENCIAL + " INTEGER DEFAULT NULL, " +
		DIA_FIN_APLICACION_SECUENCIAL + " INTEGER DEFAULT NULL, " +
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ACTIVO + " INTEGER NOT NULL, " +
		ID_VIA_VACUNA + " INTEGER NOT NULL, " +
		DOSIS + " NUMERIC DEFAULT NULL, " +
		REGION + " TEXT DEFAULT NULL, " +
		ESQ_COM + " INTEGER NOT NULL, " +
		OBSERVACION_REGION + " TEXT DEFAULT NULL, " +
		ORDEN_ESQ_COM + " INTEGER DEFAULT NULL, " +
		ALERGIAS + " TEXT DEFAULT NULL, " +
		FORZAR_APLICACION + " INTEGER NOT NULL" +
		"); ";
	
	//POJO
	public int id;
	public int id_vacuna;
	public Integer dia_inicio_aplicacion_nacido;
	public Integer dia_fin_aplicacion_nacido;
	public Integer id_vacuna_secuencial;
	public Integer dia_inicio_aplicacion_secuencial;
	public Integer dia_fin_aplicacion_secuencial;
	public String ultima_actualizacion;
	public int activo;
	public int id_via_vacuna;
	public Double dosis;
	public String region;
	public int esq_com;
	public String observacion_region;
	public Integer orden_esq_com;
	public String alergias; //Separadas por ','
	public int forzar_aplicacion;
	
	public static ReglaVacuna getRegla(Context context, int id) throws Exception {
		Uri uri = Uri.withAppendedPath(ProveedorContenido.REGLA_VACUNA_CONTENT_URI, String.valueOf(id));
		Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
		if(!cur.moveToNext()){
			cur.close();
			return null;
		}
		ReglaVacuna salida = DatosUtil.ObjetoDesdeCursor(cur, ReglaVacuna.class);
		cur.close();
		return salida;
	}
	
	public static ReglaVacuna getReglaDeVacuna(Context context, int idVacuna) throws Exception {
		Cursor cur = context.getContentResolver().query(ProveedorContenido.REGLA_VACUNA_CONTENT_URI, 
				null, ID_VACUNA+"="+idVacuna, null, null);
		if(!cur.moveToNext()){
			cur.close();
			return null;
		}
		ReglaVacuna salida = DatosUtil.ObjetoDesdeCursor(cur, ReglaVacuna.class);
		cur.close();
		return salida;
	}
	
	/**
	 * Valida si idVacuna es aplicable para el paciente recibido y en caso de haber algún motivo
	 * para no aplicarla, lo regresa en formato texto.
	 * @param context
	 * @param idVacuna Vacuna por validar
	 * @param paciente Datos del paciente para la validación
	 * @return Motivo por el que idVacuna no es aplicable. String vacío "" en caso contrario
	 */
	public static String motivoNoEsAplicableVacuna(Context context, int idVacuna, DatosPaciente paciente){
		ReglaVacuna regla;
		try {
			regla = getReglaDeVacuna(context, idVacuna);
		} catch (Exception e) {
			e.printStackTrace();return "";
		}
		if(regla.forzar_aplicacion == 0) return ""; //De entrada al no ser regla forzada permitimos aplicar vacuna
		
		for(ControlVacuna control : paciente.vacunas)
			if(control.id_vacuna == idVacuna)
				return "Esta vacuna ya está aplicada"; //Pues ya se aplicó
		
		if(regla.dia_inicio_aplicacion_nacido!=null && regla.dia_fin_aplicacion_nacido!=null){
			DateTime nacimiento = new DateTime(paciente.persona.fecha_nacimiento);
			DateTime primerDiaAplicacion = nacimiento.plusDays(regla.dia_inicio_aplicacion_nacido);
			DateTime ultimoDiaAplicacion = nacimiento.plusDays(regla.dia_fin_aplicacion_nacido);
			DateTime hoy = DateTime.now();
			if(hoy.isAfter(primerDiaAplicacion) && hoy.isBefore(ultimoDiaAplicacion))
				return "";
			else 
				return "Edad del paciente no está en los días requeridos para aplicar la vacuna";			
		}
		
		if(regla.id_vacuna_secuencial != null){
			//La vacuna se basa en una anterior (secuencial)
			for(ControlVacuna control : paciente.vacunas)
				if(control.id_vacuna == regla.id_vacuna_secuencial)
					return ""; //Si está aplicada la anterior necesaria
			return "Esta vacuna requiere aplicar otra antes"; //No se encontró la anterior requerida
		}
		
		//Checamos alergias
		String[] alergiasRegla = regla.alergias.split(",");
		for(String alergiaRegla : alergiasRegla) //Por cada alergia prohibida para la vacuna ...
			for(PersonaAlergia alergia : paciente.alergias) //... la buscamos en las alergias del paciente
				if(alergiaRegla.equals(alergia.id_alergia+""))
					return "El paciente es alergico a esta vacuna";
		
		return "";
	}
	
}
