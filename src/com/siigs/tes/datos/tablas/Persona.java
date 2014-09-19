package com.siigs.tes.datos.tablas;

import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.ProveedorContenido;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * Esquema de tabla de base de datos
 * @author Axel
 *
 */
public class Persona {

	public final static String NOMBRE_TABLA = "cns_persona"; //nombre en BD
	
	//Columnas en la nube
	public final static String ID = "id";
	public final static String CURP = "curp";
	public final static String NOMBRE = "nombre";
	public final static String APELLIDO_PATERNO = "apellido_paterno";
	public final static String APELLIDO_MATERNO = "apellido_materno";
	public final static String SEXO = "sexo";
	public final static String ID_TIPO_SANGUINEO = "id_tipo_sanguineo";
	public final static String FECHA_NACIMIENTO = "fecha_nacimiento";
	public final static String ID_ASU_LOCALIDAD_NACIMIENTO = "id_asu_localidad_nacimiento";
	public final static String CALLE_DOMICILIO = "calle_domicilio";
	public final static String NUMERO_DOMICILIO = "numero_domicilio";
	public final static String COLONIA_DOMICILIO = "colonia_domicilio";
	public final static String REFERENCIA_DOMICILIO = "referencia_domicilio";
	public final static String AGEB = "ageb";
	public final static String MANZANA = "manzana";
	public final static String SECTOR = "sector";
	public final static String ID_ASU_LOCALIDAD_DOMICILIO= "id_asu_localidad_domicilio";
	public final static String CP_DOMICILIO = "cp_domicilio";
	public final static String TELEFONO_DOMICILIO = "telefono_domicilio";
	public final static String FECHA_REGISTRO = "fecha_registro";
	public final static String ID_ASU_UM_TRATANTE = "id_asu_um_tratante"; //Solo se usa en servidor
	public final static String CELULAR = "celular";
	public final static String ULTIMA_ACTUALIZACION = "ultima_actualizacion";
	public final static String ID_NACIONALIDAD = "id_nacionalidad";
	public final static String ID_OPERADORA_CELULAR = "id_operadora_celular";
	public final static String ID_PARTO_MULTIPLE = "id_parto_multiple";
	public final static String TAMIZ_NEONATAL = "tamiz_neonatal";
	
	//Columnas de control interno
	public final static String _ID = "_id"; //para adaptadores android
	
	//Comandos de base de datos
	public final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NOMBRE_TABLA +"; ";
	
	public final static String CREATE_TABLE =
		"CREATE TABLE IF NOT EXISTS " + NOMBRE_TABLA + " (" +
		_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
		ID + " TEXT NOT NULL , " +
		CURP + " TEXT DEFAULT NULL COLLATE NOCASE, "+
		NOMBRE + " TEXT NOT NULL COLLATE NOCASE, "+
		APELLIDO_PATERNO + " TEXT NOT NULL COLLATE NOCASE, " +
		APELLIDO_MATERNO + " TEXT NOT NULL COLLATE NOCASE, " +
		SEXO + " TEXT NOT NULL COLLATE NOCASE, " +
		ID_TIPO_SANGUINEO + " INTEGER NOT NULL, " +
		FECHA_NACIMIENTO + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_LOCALIDAD_NACIMIENTO + " INTEGER NOT NULL, "+
		CALLE_DOMICILIO + " TEXT NOT NULL COLLATE NOCASE, "+
		NUMERO_DOMICILIO + " TEXT DEFAULT NULL COLLATE NOCASE, "+
		COLONIA_DOMICILIO + " TEXT DEFAULT NULL COLLATE NOCASE, "+
		REFERENCIA_DOMICILIO + " TEXT DEFAULT NULL COLLATE NOCASE, "+
		AGEB + " TEXT DEFAULT NULL COLLATE NOCASE, " +
		MANZANA + " TEXT DEFAULT NULL COLLATE NOCASE, " +
		SECTOR + " TEXT DEFAULT NULL COLLATE NOCASE, " +
		ID_ASU_LOCALIDAD_DOMICILIO + " INTEGER NOT NULL, "+
		CP_DOMICILIO + " INTEGER, "+
		TELEFONO_DOMICILIO + " TEXT DEFAULT NULL, "+
		FECHA_REGISTRO + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_ASU_UM_TRATANTE + " INTEGER NOT NULL, "+
		CELULAR + " TEXT DEFAULT NULL, "+
		ULTIMA_ACTUALIZACION + " INTEGER NOT NULL DEFAULT(strftime('%s','now')), "+
		ID_NACIONALIDAD + " INTEGER NOT NULL, "+
		ID_OPERADORA_CELULAR + " INTEGER DEFAULT NULL, "+
		ID_PARTO_MULTIPLE + " INTEGER DEFAULT 1, "+
		TAMIZ_NEONATAL + " INTEGER DEFAULT 2, "+ //0 = No, 1 = Si, 2 = Se ignora
		" UNIQUE (" + ID + ")" +
		"); "; 
		/*
		, UNIQUE INDEX " + CURP + " ("+ CURP + " ASC), INDEX fk_persona_ece_tipo_sanguineo1_idx` (`id_ece_tipo_sanguineo` ASC),
			  CONSTRAINT `fk_persona_ece_tipo_sanguineo1`
			    FOREIGN KEY (`id_ece_tipo_sanguineo`)
			    REFERENCES `siigs`.`ece_tipo_sanguineo` (`id`)
			    );
			*/
	public final static String INDEX_ASU_LOCALIDAD_DOMICILIO = 
			"CREATE INDEX idx_"+NOMBRE_TABLA+"_"+ID_ASU_LOCALIDAD_DOMICILIO+" ON "+NOMBRE_TABLA+" ( "
			+ID_ASU_LOCALIDAD_DOMICILIO + "); ";
	
	//POJO
	public String id;
	public String curp;
	public String nombre;
	public String apellido_paterno;
	public String apellido_materno;
	public String sexo;
	public int id_tipo_sanguineo;
	public String fecha_nacimiento;
	public int id_asu_localidad_nacimiento;
	public String calle_domicilio;
	public String numero_domicilio;
	public String colonia_domicilio;
	public String referencia_domicilio;
	public String ageb;
	public String manzana;
	public String sector;
	public int id_asu_localidad_domicilio;
	public Integer cp_domicilio;
	public String telefono_domicilio;
	public String fecha_registro;
	public int id_asu_um_tratante;
	public String celular;
	public String ultima_actualizacion;
	public int id_nacionalidad;
	public Integer id_operadora_celular;
	public int id_parto_multiple;
	public int tamiz_neonatal;
	
	public final static String SEXO_MASCULINO = "M";
	public final static String SEXO_FEMENINO = "F";
	
	public final static int TAMIZ_NO = 0;
	public final static int TAMIZ_SI = 1;
	public final static int TAMIZ_IGNORA = 2;
	
	public String getNombreCompleto(){return nombre + " "+ apellido_paterno + " " + apellido_materno;}
	
	public static Persona getPersona(Context context, int _id) throws Exception{
		Cursor cur = context.getContentResolver().query(
				ProveedorContenido.PERSONA_CONTENT_URI, null, _ID+"="+_id, null, null);
		if(!cur.moveToNext()){
			cur.close();
			return null;
		}
		
		Persona salida = DatosUtil.ObjetoDesdeCursor(cur, Persona.class);
		cur.close();
		return salida;
	}
	
	public static void AgregarEditar(Context context, Persona p) throws Exception{
		ContentValues cv = DatosUtil.ContentValuesDesdeObjeto(p);
		if(context.getContentResolver().insert(ProveedorContenido.PERSONA_CONTENT_URI, cv)==null)
			context.getContentResolver().update(ProveedorContenido.PERSONA_CONTENT_URI, cv, ID+"=?", new String[]{p.id});
	}
	
	public static int getTotalActualizadosDespues(Context context, String fecha){
		Cursor cur = context.getContentResolver().query(ProveedorContenido.PERSONA_CONTENT_URI, new String[]{"count(*)"}, 
				ULTIMA_ACTUALIZACION + ">=?", new String[]{fecha}, null);
		if(!cur.moveToNext()){
			cur.close();
			return 0;
		}
		int salida = cur.getInt(0);
		cur.close();
		return salida;
	}
}