package com.siigs.tes.datos.vistas;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.siigs.tes.datos.ProveedorContenido;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.EsquemaIncompleto;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaTutor;
import com.siigs.tes.datos.tablas.Tutor;

/**
 * Describe la consulta anidada de {@link EsquemaIncompleto} con datos de Paciente, Tutor, Vacunas por prioridad
 * Es idéntica a la vista de {@link Censo} pero el origen de datos es la tabla {@link EsquemaIncompleto}
 * en vez de {@link ControlVacuna}
 * @author Axel
 *
 */
public class EsquemasIncompletos {

	public static final String MASCULINO = "M";
	public static final String FEMENINO = "F";
	
	public static CursorLoader getEsquemasIncompletos(Context context, String nombre, Integer anoNacimiento, String sexo, String ageb){
		boolean regresarDatos = true;
		return getEsquemas(context, regresarDatos, nombre, anoNacimiento, sexo, ageb);
	}
	
	public static CursorLoader getPrioridades(Context context, String nombre, Integer anoNacimiento, String sexo, String ageb){
		boolean regresarDatos = false;
		return getEsquemas(context, regresarDatos, nombre, anoNacimiento, sexo, ageb);
	}
	
	/**
	 * Hace una búsqueda de esquemas incompletos con los valores del filtro especificado. Según el valor de <b>regresarDatos</b>
	 * la consulta será para regresar listado de datos ó una fila con totales de prioridades de vacunas.
	 * @param context
	 * @param regresarDatos Valor true regresa los datos del filtro. Valor false regresa una fila con los datos totales del filtro
	 * @param nombre Filtro para búsqueda por nombre
	 * @param anoNacimiento Filtro para búsqueda por año de nacimiento
	 * @param sexo Filtro para búsqueda por sexo
	 * @param ageb Filtro para búsqueda por ageb
	 * @return {@link CursorLoader} con referencia a la consulta de esquemas a realizar
	 */
	private static CursorLoader getEsquemas(Context context, boolean regresarDatos, String nombre, Integer anoNacimiento, String sexo, String ageb){
		String selection = "1=1";
		if(nombre != null)
			selection += " AND ("+NOMBRE_PACIENTE + " LIKE '%"+nombre+"%' OR "+APPAT_PACIENTE
			+" LIKE '%"+nombre+"%' OR "+APMAT_PACIENTE+" LIKE '%"+nombre+"%' "
			+ "OR "+NOMBRE_PACIENTE+" || ' ' || "+APPAT_PACIENTE+" || ' ' || "+APMAT_PACIENTE+" LIKE '%"+nombre+"%')";
		if(anoNacimiento != null)
			selection += " AND '" + anoNacimiento + "'=" + "strftime('%Y', "+FECHA_NACIMIENTO+")";
		if(sexo != null)
			selection += " AND " + SEXO + "='"+sexo+"'";
		if(ageb != null)
			selection += " AND " + AGEB + "='"+ageb+"'";
		
		if(regresarDatos){
			return new CursorLoader(context, ProveedorContenido.VISTA_ESQUEMA_INCOMPLETO_CONTENT_URI, null, selection, null, null);
		}else{
			//Se generan columnas que pediremos a la vista de totales
			String conteo = "count(%s) %s";
			
			String[] columnas = new String[]{
					String.format(conteo, BCG, BCG_P),
					String.format(conteo, HEPATITIS_1, HEPATITIS_1_P),
					String.format(conteo, HEPATITIS_2, HEPATITIS_2_P),
					String.format(conteo, HEPATITIS_3, HEPATITIS_3_P),
					String.format(conteo, PENTAVALENTE_1, PENTAVALENTE_1_P),
					String.format(conteo, PENTAVALENTE_2, PENTAVALENTE_2_P),
					String.format(conteo, PENTAVALENTE_3, PENTAVALENTE_3_P),
					String.format(conteo, PENTAVALENTE_4, PENTAVALENTE_4_P),
					String.format(conteo, DPT_R, DPT_R_P),
					String.format(conteo, SRP_1, SRP_1_P),
					String.format(conteo, SRP_2, SRP_2_P),
					String.format(conteo, ROTAVIRUS_1, ROTAVIRUS_1_P),
					String.format(conteo, ROTAVIRUS_2, ROTAVIRUS_2_P),
					String.format(conteo, ROTAVIRUS_3, ROTAVIRUS_3_P),
					String.format(conteo, NEUMOCOCO_1, NEUMOCOCO_1_P),
					String.format(conteo, NEUMOCOCO_2, NEUMOCOCO_2_P),
					String.format(conteo, NEUMOCOCO_3, NEUMOCOCO_3_P),
					String.format(conteo, INFLUENZA_1, INFLUENZA_1_P),
					String.format(conteo, INFLUENZA_2, INFLUENZA_2_P),
					String.format(conteo, INFLUENZA_R, INFLUENZA_R_P)
			};
			return new CursorLoader(context, ProveedorContenido.VISTA_ESQUEMA_INCOMPLETO_CONTENT_URI, 
					columnas, selection, null, null);
		}
	}
	
	//Subconsulta para cada columna de vacuna
	private final static String SUBCONSULTA = "(select "+EsquemaIncompleto.PRIORIDAD+" from "+EsquemaIncompleto.NOMBRE_TABLA+" where "+EsquemaIncompleto.ID_PERSONA+"=p."+Persona.ID+" and "+EsquemaIncompleto.ID_VACUNA+"=";
	
	//COLUMNAS PARA CURSOR Y PARA QUERY DE CONTENT PROVIDER
	//Cursor de android ridículamente no permite prefijos de tablas estílo "tabla.columna" al momento de
	//consultar sus datos con la forma cursor.getColumnIndex("tabla.columna") Es decir, se come el prefijo "tabla."
	//Por ese motivo, se deben usar variables por un lado para el content provider (que si ocupa estílo "tabla.columna")
	//y por el otro lado con alias para el consumo de datos en la forma cursor.getColumnIndex(NOMBRE_PACIENTE)
	public final static String _ID_PACIENTE = Persona._ID;
	private final static String COL__ID_PACIENTE = "p." + Persona._ID + " " + _ID_PACIENTE;
	
	public final static String NOMBRE_PACIENTE = "nombre_paciente";
	private final static String COL_NOMBRE_PACIENTE = "p." + Persona.NOMBRE + " " + NOMBRE_PACIENTE;
	
	public final static String APPAT_PACIENTE = "appat_paciente"; 
	private final static String COL_APPAT_PACIENTE = "p." + Persona.APELLIDO_PATERNO + " " + APPAT_PACIENTE;
	
	public final static String APMAT_PACIENTE = "apmat_paciente"; 
	private final static String COL_APMAT_PACIENTE = "p." + Persona.APELLIDO_MATERNO + " " + APMAT_PACIENTE;
	
	public final static String NOMBRE_TUTOR = "nombre_tutor";
	private final static String COL_NOMBRE_TUTOR = "t." + Tutor.NOMBRE + " " + NOMBRE_TUTOR;
	
	public final static String APPAT_TUTOR = "appat_tutor"; 
	private final static String COL_APPAT_TUTOR = "t." + Tutor.APELLIDO_PATERNO + " " + APPAT_TUTOR;
	
	public final static String APMAT_TUTOR = "apmat_tutor"; 
	private final static String COL_APMAT_TUTOR = "t." + Tutor.APELLIDO_MATERNO + " " + APMAT_TUTOR;

	public final static String CALLE_DOMICILIO = Persona.CALLE_DOMICILIO;
	private final static String COL_CALLE_DOMICILIO = "p." + Persona.CALLE_DOMICILIO + " " + CALLE_DOMICILIO;
	
	public final static String NUMERO_DOMICILIO = Persona.NUMERO_DOMICILIO;
	private final static String COL_NUMERO_DOMICILIO = "p." + Persona.NUMERO_DOMICILIO + " " + NUMERO_DOMICILIO;

	public final static String COLONIA_DOMICILIO = Persona.COLONIA_DOMICILIO;
	private final static String COL_COLONIA_DOMICILIO = "p." + Persona.COLONIA_DOMICILIO + " " + COLONIA_DOMICILIO;
	
	public final static String REFERENCIA_DOMICILIO = Persona.REFERENCIA_DOMICILIO;
	private final static String COL_REFERENCIA_DOMICILIO = "p." + Persona.REFERENCIA_DOMICILIO + " " + REFERENCIA_DOMICILIO;

	public final static String AGEB = Persona.AGEB;
	private final static String COL_AGEB = "p." + Persona.AGEB + " " + AGEB;
	
	public final static String CURP = Persona.CURP;
	private final static String COL_CURP = "p." + Persona.CURP+ " " + CURP;
	
	public final static String FECHA_NACIMIENTO = Persona.FECHA_NACIMIENTO;
	private final static String COL_FECHA_NACIMIENTO = "p." + Persona.FECHA_NACIMIENTO+ " " + FECHA_NACIMIENTO;
	
	public final static String PARTO = Persona.ID_PARTO_MULTIPLE;
	private final static String COL_PARTO = "p." + Persona.ID_PARTO_MULTIPLE+ " " + PARTO;
	
	public final static String SEXO = Persona.SEXO;
	private final static String COL_SEXO = "p." + Persona.SEXO+ " " + SEXO;

	public final static String BCG = "bcg";
	private final static String COL_BCG = SUBCONSULTA + "1) " + BCG;

	public final static String HEPATITIS_1 = "hepatitis_1";
	private final static String COL_HEPATITIS_1 = SUBCONSULTA + "2) " + HEPATITIS_1;
	
	public final static String HEPATITIS_2 = "hepatitis_2";
	private final static String COL_HEPATITIS_2 = SUBCONSULTA + "3) " + HEPATITIS_2;

	public final static String HEPATITIS_3 = "hepatitis_3";
	private final static String COL_HEPATITIS_3 = SUBCONSULTA + "4) " + HEPATITIS_3;

	public final static String PENTAVALENTE_1 = "pentavalente_1";
	private final static String COL_PENTAVALENTE_1 = SUBCONSULTA + "5) " + PENTAVALENTE_1;

	public final static String PENTAVALENTE_2 = "pentavalente_2";
	private final static String COL_PENTAVALENTE_2 = SUBCONSULTA + "6) " + PENTAVALENTE_2;
	
	public final static String PENTAVALENTE_3 = "pentavalente_3";
	private final static String COL_PENTAVALENTE_3 = SUBCONSULTA + "7) " + PENTAVALENTE_3;
	
	public final static String PENTAVALENTE_4 = "pentavalente_4";
	private final static String COL_PENTAVALENTE_4 = SUBCONSULTA + "8) " + PENTAVALENTE_4;

	public final static String DPT_R = "dpt_r";
	private final static String COL_DPT_R = SUBCONSULTA + "9) " + DPT_R;

	public final static String SRP_1 = "srp_1";
	private final static String COL_SRP_1 = SUBCONSULTA + "19) " + SRP_1;

	public final static String SRP_2 = "srp_2";
	private final static String COL_SRP_2 = SUBCONSULTA + "20) " + SRP_2;

	public final static String ROTAVIRUS_1 = "rotavirus_1";
	private final static String COL_ROTAVIRUS_1 = SUBCONSULTA + "10) " + ROTAVIRUS_1;
	
	public final static String ROTAVIRUS_2 = "rotavirus_2";
	private final static String COL_ROTAVIRUS_2 = SUBCONSULTA + "11) " + ROTAVIRUS_2;
	
	public final static String ROTAVIRUS_3 = "rotavirus_3";
	private final static String COL_ROTAVIRUS_3 = SUBCONSULTA + "12) " + ROTAVIRUS_3;
	
	public final static String NEUMOCOCO_1 = "neumococo_1";
	private final static String COL_NEUMOCOCO_1 = SUBCONSULTA + "13) " + NEUMOCOCO_1;
	
	public final static String NEUMOCOCO_2 = "neumococo_2";
	private final static String COL_NEUMOCOCO_2 = SUBCONSULTA + "14) " + NEUMOCOCO_2;
	
	public final static String NEUMOCOCO_3 = "neumococo_3";
	private final static String COL_NEUMOCOCO_3 = SUBCONSULTA + "15) " + NEUMOCOCO_3;
	
	public final static String INFLUENZA_1 = "influenza_1";
	private final static String COL_INFLUENZA_1 = SUBCONSULTA + "16) " + INFLUENZA_1;
	
	public final static String INFLUENZA_2 = "influenza_2";
	private final static String COL_INFLUENZA_2 = SUBCONSULTA + "17) " + INFLUENZA_2;
	
	public final static String INFLUENZA_R = "influenza_r";
	private final static String COL_INFLUENZA_R = SUBCONSULTA + "18) " + INFLUENZA_R;
	//public final static String DPT_1 = "cv9." + ControlVacuna.ID_VACUNA;
	//public final static String DPT_2 = "cv10." + ControlVacuna.ID_VACUNA;

	
	//FROM (TABLAS A USAR CON JOINS)
	private final static String TABLAS = Tutor.NOMBRE_TABLA + " t " + 
		" JOIN " + PersonaTutor.NOMBRE_TABLA + " pt ON t."+Tutor.ID + "= pt."+PersonaTutor.ID_TUTOR +
		" JOIN " + Persona.NOMBRE_TABLA + " p ON pt."+PersonaTutor.ID_PERSONA + "= p."+Persona.ID;
			
	public final static String NOMBRE_VISTA = "vista_esquemas_incompletos";
	
	public final static String CREAR_VISTA = "CREATE VIEW " + NOMBRE_VISTA + " AS SELECT " +
			COL__ID_PACIENTE + ", " + COL_NOMBRE_PACIENTE + ", " + COL_APPAT_PACIENTE + ", " + COL_APMAT_PACIENTE + ", " +
			COL_NOMBRE_TUTOR + ", " + COL_APPAT_TUTOR + ", " + COL_APMAT_TUTOR + ", " + COL_CALLE_DOMICILIO + ", " +
			COL_NUMERO_DOMICILIO + ", " + COL_COLONIA_DOMICILIO + ", " + COL_REFERENCIA_DOMICILIO + ", " +
			COL_AGEB + ", " + COL_CURP + ", " + COL_FECHA_NACIMIENTO + ", " + COL_PARTO +", " + COL_SEXO + ", " + COL_BCG + ", " + 
			COL_HEPATITIS_1 + ", " + COL_HEPATITIS_2 + ", " + COL_HEPATITIS_3 + ", " + COL_PENTAVALENTE_1 + ", " +
			COL_PENTAVALENTE_2 + ", " + COL_PENTAVALENTE_3 + ", " + COL_PENTAVALENTE_4 + ", " +
			COL_DPT_R + ", " + COL_SRP_1 + ", " + COL_SRP_2 + ", " + COL_ROTAVIRUS_1 + ", " + COL_ROTAVIRUS_2 + ", " +
			COL_ROTAVIRUS_3 + ", " + COL_NEUMOCOCO_1 + ", " + COL_NEUMOCOCO_2 + ", " + COL_NEUMOCOCO_3 + ", " +
			COL_INFLUENZA_1 + ", " + COL_INFLUENZA_2 + ", " + COL_INFLUENZA_R +
			" FROM " + TABLAS + 
			" WHERE exists(select "+EsquemaIncompleto.ID_PERSONA+" FROM "
				+EsquemaIncompleto.NOMBRE_TABLA+" WHERE "+EsquemaIncompleto.ID_PERSONA+"=p."+Persona.ID+")";
	
	//NOMBRES DE COLUMNAS DE LAS VACUNAS REGRESADAS EN DATOS DE ESQUEMAS
	/*public final static String[] VACUNAS = new String[]{BCG, HEPATITIS_1, HEPATITIS_2, HEPATITIS_3,
		PENTAVALENTE_1, PENTAVALENTE_2, PENTAVALENTE_3, PENTAVALENTE_4, DPT_R, SRP_1, SRP_2, ROTAVIRUS_1,
		ROTAVIRUS_2, ROTAVIRUS_3, NEUMOCOCO_1, NEUMOCOCO_2, NEUMOCOCO_3, INFLUENZA_1, INFLUENZA_2, INFLUENZA_R};*/
	
	
	//COLUMNAS DE PRIORIDADES POR VACUNA EN CONSULTA DE TOTALES DE ESQUEMAS
	public final static String BCG_P = BCG + "_p";
	public final static String HEPATITIS_1_P = HEPATITIS_1 + "_p";
	public final static String HEPATITIS_2_P = HEPATITIS_2 + "_p";;
	public final static String HEPATITIS_3_P = HEPATITIS_3 + "_p";
	public final static String PENTAVALENTE_1_P = PENTAVALENTE_1 + "_p";
	public final static String PENTAVALENTE_2_P = PENTAVALENTE_2 + "_p";
	public final static String PENTAVALENTE_3_P = PENTAVALENTE_3 + "_p";
	public final static String PENTAVALENTE_4_P = PENTAVALENTE_4 + "_p";
	public final static String DPT_R_P = DPT_R + "_p";
	public final static String SRP_1_P = SRP_1 + "_p";
	public final static String SRP_2_P = SRP_2 + "_p";
	public final static String ROTAVIRUS_1_P = ROTAVIRUS_1 + "_p";
	public final static String ROTAVIRUS_2_P = ROTAVIRUS_2 + "_p";
	public final static String ROTAVIRUS_3_P = ROTAVIRUS_3 + "_p";
	public final static String NEUMOCOCO_1_P = NEUMOCOCO_1 + "_p";
	public final static String NEUMOCOCO_2_P = NEUMOCOCO_2 + "_p";
	public final static String NEUMOCOCO_3_P = NEUMOCOCO_3 + "_p";
	public final static String INFLUENZA_1_P = INFLUENZA_1 + "_p";
	public final static String INFLUENZA_2_P = INFLUENZA_2 + "_p";
	public final static String INFLUENZA_R_P = INFLUENZA_R + "_p";
}
