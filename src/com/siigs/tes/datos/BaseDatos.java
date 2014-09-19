/**
 * 
 */
package com.siigs.tes.datos;

import java.io.File;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.siigs.tes.datos.tablas.AccionNutricional;
import com.siigs.tes.datos.tablas.Afiliacion;
import com.siigs.tes.datos.tablas.Alergia;
import com.siigs.tes.datos.tablas.AntiguaUM;
import com.siigs.tes.datos.tablas.AntiguoDomicilio;
import com.siigs.tes.datos.tablas.ArbolSegmentacion;
import com.siigs.tes.datos.tablas.Bitacora;
import com.siigs.tes.datos.tablas.CategoriaCie10;
import com.siigs.tes.datos.tablas.Consulta;
import com.siigs.tes.datos.tablas.ControlAccionNutricional;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ControlEda;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.ControlNutricional;
import com.siigs.tes.datos.tablas.ControlPerimetroCefalico;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.Eda;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.EsquemaIncompleto;
import com.siigs.tes.datos.tablas.EstadoVisita;
import com.siigs.tes.datos.tablas.EstimulacionTemprana;
import com.siigs.tes.datos.tablas.Grupo;
import com.siigs.tes.datos.tablas.GrupoAtencion;
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
import com.siigs.tes.datos.tablas.SalesRehidratacion;
import com.siigs.tes.datos.tablas.TipoSanguineo;
import com.siigs.tes.datos.tablas.Tratamiento;
import com.siigs.tes.datos.tablas.Tutor;
import com.siigs.tes.datos.tablas.Usuario;
import com.siigs.tes.datos.tablas.UsuarioInvitado;
import com.siigs.tes.datos.tablas.Vacuna;
import com.siigs.tes.datos.tablas.ViaVacuna;
import com.siigs.tes.datos.tablas.Visita;
import com.siigs.tes.datos.tablas.graficas.EstadoImc;
import com.siigs.tes.datos.tablas.graficas.EstadoImcPorEdad;
import com.siigs.tes.datos.tablas.graficas.EstadoNutricionAltura;
import com.siigs.tes.datos.tablas.graficas.EstadoNutricionAlturaPorEdad;
import com.siigs.tes.datos.tablas.graficas.EstadoNutricionPeso;
import com.siigs.tes.datos.tablas.graficas.EstadoNutricionPesoPorAltura;
import com.siigs.tes.datos.tablas.graficas.EstadoNutricionPesoPorEdad;
import com.siigs.tes.datos.tablas.graficas.EstadoPerimetroCefalico;
import com.siigs.tes.datos.tablas.graficas.EstadoPerimetroCefalicoPorEdad;
import com.siigs.tes.datos.tablas.graficas.HemoglobinaAltitud;
import com.siigs.tes.datos.vistas.EsquemasIncompletos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Axel
 * Clase que genera la base de datos de la aplicación y controla
 * acciones en cambios de versión.
 * Esta versión implementa a la clase {@link SQLiteAssetHelper} en vez de la versión antigua 
 * con la clase {@link SQLiteOpenHelper}. Este cambio es con el objetivo de incorporar un archivo con la base de datos
 * precargada en el APK que {@link SQLiteAssetHelper} copia de forma transparente en el folder adecuado al usar
 * la base de datos por primera vez.
 * Los scripts para la creación de la base de datos siguen siendo válidos pues la base de datos precargada en la
 * carpeta "assets" no contiene todas las tablas y los scripts aún son ejecutados para crear el resto de las tablas.
 * Ver <b>https://github.com/jgilfelt/android-sqlite-asset-helper</b>
 */
public class BaseDatos extends SQLiteAssetHelper {

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
			PartoMultiple.CREATE_TABLE, EstadoNutricionPeso.CREATE_TABLE, EstadoNutricionPesoPorEdad.CREATE_TABLE,
			EstadoNutricionPesoPorAltura.CREATE_TABLE, EstadoNutricionAltura.CREATE_TABLE, 
			EstadoNutricionAlturaPorEdad.CREATE_TABLE, EstadoImc.CREATE_TABLE, EstadoImcPorEdad.CREATE_TABLE,
			EstadoPerimetroCefalico.CREATE_TABLE, EstadoPerimetroCefalicoPorEdad.CREATE_TABLE,
			HemoglobinaAltitud.CREATE_TABLE, SalesRehidratacion.CREATE_TABLE, EstimulacionTemprana.CREATE_TABLE,
			GrupoAtencion.CREATE_TABLE, ControlPerimetroCefalico.CREATE_TABLE, CategoriaCie10.CREATE_TABLE,
			//Indices
			EsquemaIncompleto.INDEX, Persona.INDEX_ASU_LOCALIDAD_DOMICILIO,
			//Vistas
			EsquemasIncompletos.CREAR_VISTA
			};
	
	private static final String DB_SCHEMA_DROP = "PRAGMA writable_schema = 1;"+
			"delete from sqlite_master where type in ('table', 'index', 'trigger');"+
			"PRAGMA writable_schema = 0;"+
			"VACUUM;"+
			"PRAGMA INTEGRITY_CHECK;";
	
	/**
	 * Crea el objeto manejador de la base de datos y verifica si la base de datos <b>DB_NAME</b> está creada (con estílo assets).
	 * Si el archivo de base de datos no ha sido copiado aún desde el assets, hace la llamada a <b>getWritableDatabase()</b> 
	 * que se encarga en fondo de copiar la base de datos desde carpeta assets.
	 * El resultado de la llamada a getWritableDatabase() es usado como parámetro para {@link onCrear()} quien 
	 * ejecuta los scripts para crear el resto de las tablas.
	 * @param context Contexto de la aplicación
	 */
	public BaseDatos(Context context){
		super(context, DB_NAME, null, DB_VERSION);
		
		File archivoBaseDatos = context.getDatabasePath(DB_NAME);
		if(!archivoBaseDatos.exists())
			onCrear(getWritableDatabase());
	}
	
	/**
	 * Ejecuta los scripts para crear las tablas, vistas e índices de la base de datos.
	 * @param db Base de datos de SQLite que debe tener permisos de escritura
	 */
	public void onCrear(SQLiteDatabase db) {
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
		        
		this.onCrear(db);
	}
	
	
	/* No soportado en SQLiteAssetHelper
	 * @Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onDowngrade(db, oldVersion, newVersion);
	}*/	
	
}//fin BaseDatos
