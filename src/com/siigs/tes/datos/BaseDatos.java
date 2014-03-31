/**
 * 
 */
package com.siigs.tes.datos;

import com.siigs.tes.datos.tablas.AccionNutricional;
import com.siigs.tes.datos.tablas.Afiliacion;
import com.siigs.tes.datos.tablas.Alergia;
import com.siigs.tes.datos.tablas.AntiguaUM;
import com.siigs.tes.datos.tablas.AntiguoDomicilio;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.Bitacora;
import com.siigs.tes.datos.tablas.Consulta;
import com.siigs.tes.datos.tablas.ControlAccionNutricional;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ControlEda;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.ControlNutricional;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.Eda;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.EsquemaIncompleto;
import com.siigs.tes.datos.tablas.EstadoVisita;
import com.siigs.tes.datos.tablas.Grupo;
import com.siigs.tes.datos.tablas.Ira;
import com.siigs.tes.datos.tablas.Nacionalidad;
import com.siigs.tes.datos.tablas.Notificacion;
import com.siigs.tes.datos.tablas.OperadoraCelular;
import com.siigs.tes.datos.tablas.PartoMultiple;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Permiso;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaAfiliacion;
import com.siigs.tes.datos.tablas.PersonaAlergia;
import com.siigs.tes.datos.tablas.PersonaTutor;
import com.siigs.tes.datos.tablas.RegistroCivil;
import com.siigs.tes.datos.tablas.ReglaVacuna;
import com.siigs.tes.datos.tablas.TipoSanguineo;
import com.siigs.tes.datos.tablas.Tratamiento;
import com.siigs.tes.datos.tablas.Tutor;
import com.siigs.tes.datos.tablas.Usuario;
import com.siigs.tes.datos.tablas.UsuarioInvitado;
import com.siigs.tes.datos.tablas.Vacuna;
import com.siigs.tes.datos.tablas.ViaVacuna;
import com.siigs.tes.datos.tablas.Visita;
import com.siigs.tes.datos.vistas.EsquemasIncompletos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Axel
 * Clase que genera la base de datos de la aplicación y controla
 * acciones en cambios de versión.
 */
public class BaseDatos extends SQLiteOpenHelper {

	private static final String TAG = "BaseDatos";
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "tes_datos.db";
	
	//SCHEMA
	private static final String[] SCRIPTS = {
			//TABLAS
			AccionNutricional.CREATE_TABLE, Afiliacion.CREATE_TABLE,
			Alergia.CREATE_TABLE, AntiguaUM.CREATE_TABLE, AntiguoDomicilio.CREATE_TABLE, 
			ArbolSegmentacion.CREATE_TABLE, Bitacora.CREATE_TABLE, Consulta.CREATE_TABLE, 
			ControlAccionNutricional.CREATE_TABLE, ControlConsulta.CREATE_TABLE, ControlEda.CREATE_TABLE, 
			ControlIra.CREATE_TABLE, ControlNutricional.CREATE_TABLE, ControlVacuna.CREATE_TABLE, 
			Eda.CREATE_TABLE, ErrorSis.CREATE_TABLE, EsquemaIncompleto.CREATE_TABLE, Grupo.CREATE_TABLE, Ira.CREATE_TABLE, 
			Nacionalidad.CREATE_TABLE, Notificacion.CREATE_TABLE, OperadoraCelular.CREATE_TABLE, 
			PendientesTarjeta.CREATE_TABLE,	Permiso.CREATE_TABLE, Persona.CREATE_TABLE, 
			PersonaAfiliacion.CREATE_TABLE, PersonaAlergia.CREATE_TABLE, PersonaTutor.CREATE_TABLE, 
			RegistroCivil.CREATE_TABLE, TipoSanguineo.CREATE_TABLE, Tutor.CREATE_TABLE, 
			Usuario.CREATE_TABLE, UsuarioInvitado.CREATE_TABLE, Vacuna.CREATE_TABLE, ReglaVacuna.CREATE_TABLE,
			ViaVacuna.CREATE_TABLE, Tratamiento.CREATE_TABLE, EstadoVisita.CREATE_TABLE, Visita.CREATE_TABLE,
			PartoMultiple.CREATE_TABLE,
			//Indices
			EsquemaIncompleto.INDEX,
			//Vistas
			EsquemasIncompletos.CREAR_VISTA
			};
	
	private static final String DB_SCHEMA_DROP = "PRAGMA writable_schema = 1;"+
			"delete from sqlite_master where type in ('table', 'index', 'trigger');"+
			"PRAGMA writable_schema = 0;"+
			"VACUUM;"+
			"PRAGMA INTEGRITY_CHECK;";
	
	
	public BaseDatos(Context context){
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "Inicia creación de tablas");
		for(String script : BaseDatos.SCRIPTS){
			Log.d(TAG, script);
			db.execSQL(script);
		}
		Log.d(TAG, "Fin creación de tablas");
		//Hacer cualquier insert default aquí
		//db.execSQL("insert into "+ TABLA1+" values(1,'Red Circle','1',strftime('%s','now') );");
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		if (oldVersion == 3 && newVersion == 4) {
//			//this value is mid february 2011
//			db.execSQL("alter table "+ TABLE_TUTORIALS + " add column " + COL_DATE + " INTEGER NOT NULL DEFAULT '1297728000' ");
//		} else {
//			Log.w(TAG, "Actualizando base datos. Contenido actual será borrado. ["
//					+ oldVersion + "]->[" + newVersion + "]");
//			        db.execSQL("DROP TABLE IF EXISTS " + TABLA1);
//			this.onCreate(db);
//		}
		Log.d(TAG, "Actualizando base datos. Contenido actual será borrado. ["
				+ oldVersion + "]->[" + newVersion + "]");
		        db.execSQL(BaseDatos.DB_SCHEMA_DROP);
		        
		this.onCreate(db);
	}
	
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onDowngrade(db, oldVersion, newVersion);
	}

}
