package com.siigs.tes.datos.vistas;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.siigs.tes.datos.ProveedorContenido;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.PartoMultiple;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaTutor;
import com.siigs.tes.datos.tablas.Tutor;

/**
 * Describe la consulta anidada de Censo con datos de Paciente, Tutor, Vacunas
 * @author Axel
 *
 */
public class Censo {

	public static final String MASCULINO = "M";
	public static final String FEMENINO = "F";
	
	public static CursorLoader getCenso(Context context, String nombre, Integer anoNacimiento, String sexo, String ageb){
		String selection = "1=1";
		if(nombre != null)
			selection += " AND (p."+Persona.NOMBRE + " LIKE '%"+nombre+"%' OR p."+Persona.APELLIDO_PATERNO
			+" LIKE '%"+nombre+"%' OR p."+Persona.APELLIDO_MATERNO+" LIKE '%"+nombre+"%' "
			+ "OR p."+Persona.NOMBRE+" || ' ' || p."+Persona.APELLIDO_PATERNO+" || ' ' || p."+Persona.APELLIDO_MATERNO+" LIKE '%"+nombre+"%')";
		if(anoNacimiento != null)
			selection += " AND '" + anoNacimiento + "'=" + "strftime('%Y', p."+Persona.FECHA_NACIMIENTO+")";
		if(sexo != null)
			selection += " AND " + "p."+Persona.SEXO + "='"+sexo+"'";
		if(ageb != null)
			selection += " AND " + "p."+Persona.AGEB + "='"+ageb+"'";
		return new CursorLoader(context, ProveedorContenido.CENSO_CONTENT_URI, null, selection, null, null);
	}
	
	private final static String SUBCONSULTA = "(select "+ControlVacuna.ID_VACUNA+" from "+ControlVacuna.NOMBRE_TABLA+" where "+ControlVacuna.ID_PERSONA+"=p."+Persona.ID+" and "+ControlVacuna.ID_VACUNA+"=";
	
	//COLUMNAS PARA CURSOR Y PARA QUERY DE CONTENT PROVIDER
	//Cursor de android ridículamente no permite prefijos de tablas estílo "tabla.columna" al momento de
	//consultar sus datos con la forma cursor.getColumnIndex("tabla.columna") Es decir, se come el prefijo "tabla."
	//Por ese motivo, se deben usar variables por un lado para el content provider (que si ocupa estílo "tabla.columna")
	//y por el otro lado con alias para el consumo de datos en la forma cursor.getColumnIndex(NOMBRE_PACIENTE)
	public final static String _ID_PACIENTE = Persona._ID;
	private final static String COL__ID_PACIENTE = "p." + Persona._ID;
	
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

	//Columnas para la consulta del ContentProvider
	public final static String[] COLUMNAS = new String[]{COL__ID_PACIENTE, COL_NOMBRE_PACIENTE, COL_APPAT_PACIENTE, 
		COL_APMAT_PACIENTE, COL_NOMBRE_TUTOR, COL_APPAT_TUTOR, COL_APMAT_TUTOR, COL_CALLE_DOMICILIO, 
		COL_NUMERO_DOMICILIO, COL_COLONIA_DOMICILIO, COL_REFERENCIA_DOMICILIO, COL_AGEB, COL_CURP, COL_FECHA_NACIMIENTO, 
		COL_PARTO, COL_SEXO, COL_BCG, COL_HEPATITIS_1, COL_HEPATITIS_2, COL_HEPATITIS_3, COL_PENTAVALENTE_1, 
		COL_PENTAVALENTE_2, COL_PENTAVALENTE_3,	COL_PENTAVALENTE_4, COL_DPT_R, COL_SRP_1, COL_SRP_2, 
		COL_ROTAVIRUS_1, COL_ROTAVIRUS_2, COL_ROTAVIRUS_3, COL_NEUMOCOCO_1, COL_NEUMOCOCO_2, COL_NEUMOCOCO_3, 
		COL_INFLUENZA_1, COL_INFLUENZA_2, COL_INFLUENZA_R};
	
	//FROM (TABLAS A USAR CON JOINS)
	public final static String TABLAS = Tutor.NOMBRE_TABLA + " t " + 
	" JOIN " + PersonaTutor.NOMBRE_TABLA + " pt ON t."+Tutor.ID + "= pt."+PersonaTutor.ID_TUTOR +
	" JOIN " + Persona.NOMBRE_TABLA + " p ON pt."+PersonaTutor.ID_PERSONA + "= p."+Persona.ID;
}
