package com.siigs.tes.controles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.siigs.tes.ControlFragment;
import com.siigs.tes.R;
import com.siigs.tes.datos.tablas.Permiso;

/**
 * Helper class que provee contenido para listas de menús en panel izquierdo.
 */
public class ContenidoControles {

	//Indica a cada fragmento creado el ICA en esta clase que lo identificó  (normalmente de tipo ICA_XXX_LISTAR)
	//Se aprovecha solo por  {@link CensoCensoNominal} para distinguir entre una vista de Censo y una de Esquema Incompleto
	public final static String ARG_ICA = "parametro_ica";
	
	//Todos los items.
	public static List<ItemControl> CONTROLES_TODOS = new ArrayList<ItemControl>();
	//Todos los items por ID_PERSONA.
	public static Map<String, ItemControl> CONTROLES_TODOS_MAP = new HashMap<String, ItemControl>();

	//items de atención.
	public static List<ItemControl> CONTROLES_ATENCION = new ArrayList<ItemControl>();
	//items de atención por ID_PERSONA.
	public static Map<String, ItemControl> CONTROLES_ATENCION_MAP = new HashMap<String, ItemControl>();

	//items de censo.
	public static List<ItemControl> CONTROLES_CENSO = new ArrayList<ItemControl>();
	//items de censo por ID_PERSONA.
	public static Map<String, ItemControl> CONTROLES_CENSO_MAP = new HashMap<String, ItemControl>();
	
	//items de esquemas.
	public static List<ItemControl> CONTROLES_ESQUEMAS = new ArrayList<ItemControl>();
	//items de esquemas por ID_PERSONA.
	public static Map<String, ItemControl> CONTROLES_ESQUEMAS_MAP = new HashMap<String, ItemControl>();
		
	//items de notificaciones.
	public static List<ItemControl> CONTROLES_NOTIFICACIONES = new ArrayList<ItemControl>();
	//items de notificaciones por ID_PERSONA.
	public static Map<String, ItemControl> CONTROLES_NOTIFICACIONES_MAP = new HashMap<String, ItemControl>();
	
	//items de sincronización.
	public static List<ItemControl> CONTROLES_SINCRONIZACION = new ArrayList<ItemControl>();
	//items de sinronización por ID_PERSONA.
	public static Map<String, ItemControl> CONTROLES_SINCRONIZACION_MAP = new HashMap<String, ItemControl>();

	//items de configuración.
	public static List<ItemControl> CONTROLES_CONFIGURACION = new ArrayList<ItemControl>();
	//items de configuracióin por ID_PERSONA.
	public static Map<String, ItemControl> CONTROLES_CONFIGURACION_MAP = new HashMap<String, ItemControl>();
	
	//items de invitados.
	public static List<ItemControl> CONTROLES_INVITADOS = new ArrayList<ItemControl>();
	//items de invitados por ID_PERSONA.
	public static Map<String, ItemControl> CONTROLES_INVITADOS_MAP = new HashMap<String, ItemControl>();
	
	//Lista de acciones permitibles al usuario que definen si se muestra un control en menú
	//ICA viene de id_controlador_accion
	//
	public final static int ICA_PACIENTE_LISTAR = 96;
		//Acciones internas
		public final static int ICA_PACIENTE_VER = 95;
		public final static int ICA_PACIENTE_EDITAR_DOMICILIO = 98;
		public final static int ICA_PACIENTE_ASIGNAR_UM = 97;
		public final static int ICA_PACIENTE_AGREGAR_ALERGIAS = 99;
		
	public final static int ICA_CONTROLVACUNA_LISTAR = 106;
		//Acciones internas
		public final static int ICA_CONTROLVACUNA_VER = 105;
		public final static int ICA_CONTROLVACUNA_INSERTAR = 104;
		
	public final static int ICA_CONTROLNUTRICIONAL_LISTAR = 89;
		//Acciones internas
		public final static int ICA_CONTROLNUTRICIONAL_VER = 88;
		public final static int ICA_CONTROLNUTRICIONAL_INSERTAR = 87;
		
	public final static int ICA_CONTROLACCIONNUTRICIONAL_LISTAR = 76;
		//Acciones internas
		public final static int ICA_CONTROLACCIONNUTRICIONAL_VER = 75;
		public final static int ICA_CONTROLACCIONNUTRICIONAL_INSERTAR = 74;
		public final static int ICA_CONTROLACCIONNUTRICIONAL_SRO_LISTAR = 1010;
		public final static int ICA_CONTROLACCIONNUTRICIONAL_SRO_INSERTAR = 1011;
		
	public final static int ICA_ESTIMULACIONTEMPRANA_LISTAR = 1014;
		//Acciones internas
		public final static int ICA_ESTIMULACIONTEMPRANA_VER = 1013;
		public final static int ICA_ESTIMULACIONTEMPRANA_INSERTAR = 1012;
		
	public final static int ICA_EDA_LISTAR = 83;
		//Acciones internas
		public final static int ICA_EDA_VER = 82;
		public final static int ICA_EDA_INSERTAR = 81;
		
	public final static int ICA_IRA_LISTAR = 86;
		//Acciones internas
		public final static int ICA_IRA_VER = 85;
		public final static int ICA_IRA_INSERTAR = 84;
		
	public final static int ICA_CONTROLCONSULTA_LISTAR = 80;
		//Acciones internas
		public final static int ICA_CONTROLCONSULTA_VER = 79;
		public final static int ICA_CONTROLCONSULTA_INSERTAR = 78;
	
	public final static int ICA_CENSO_LISTAR = 131;
	
	public final static int ICA_ESQUEMAS_LISTAR = 133;
		public final static int ICA_AGREGAR_VISITA = 167;
	
	public final static int ICA_NOTIFICACION_LISTAR = 94;
	public final static int ICA_NOTIFICACION_REPORTE_LISTAR = 101;
	
	public final static int ICA_CONFIGURAR__CONFIGURAR = 77;
	
	public final static int ICA_SINCRONIZAR_LISTAR = 102;
	
	public final static int ICA_INVITADOS_LISTAR = 92;
	public final static int ICA_INVITADOS_VALIDAR_LISTAR = 136;
	
	
	static {		
		CrearControles(null);
	}

	/**
	 * @author Axel
	 * Crea los arreglos y mapas para los menús de navegación.
	 * Esta función es llamada al crear la aplicación pero debe ser
	 * llamada nuevamente en la función de login y aquí se debe implementar
	 * verificación de roles para solo agregar los menús permitidos.
	 */
	private static void CrearControles(List<Permiso> permisos){
		ItemControl item;
		
		//Controles de Atención
		CONTROLES_ATENCION.clear();CONTROLES_ATENCION_MAP.clear();
		if(permisos == null)return;
		
		if(ExistePermiso(ICA_PACIENTE_LISTAR, permisos)){
			item=new ItemControl(ICA_PACIENTE_LISTAR, "Paciente", AtencionPaciente.class, R.drawable.paciente);
			addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		}
		if(ExistePermiso(ICA_CONTROLVACUNA_LISTAR, permisos)){
			item=new ItemControl(ICA_CONTROLVACUNA_LISTAR, "Control de Vacunación", ControlVacunas.class, R.drawable.vacunacion);
			addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		}
		if(ExistePermiso(ICA_CONTROLNUTRICIONAL_LISTAR, permisos)){
			item=new ItemControl(ICA_CONTROLNUTRICIONAL_LISTAR, "Control Nutricional", ControlNutricional.class, R.drawable.nutricion);
			addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		}
		if(ExistePermiso(ICA_CONTROLACCIONNUTRICIONAL_LISTAR, permisos)){
			item=new ItemControl(ICA_CONTROLACCIONNUTRICIONAL_LISTAR, "Acciones Nutricionales", ControlAccionNutricional.class, R.drawable.nutricionales);
			addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		}
		if(ExistePermiso(ICA_ESTIMULACIONTEMPRANA_LISTAR, permisos)){
			item=new ItemControl(ICA_ESTIMULACIONTEMPRANA_LISTAR, "Estimulación Temprana", ControlEstimulacionTemprana.class, R.drawable.estimulacion_temprana);
			addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		}
		/*if(ExistePermiso(ICA_IRA_LISTAR, permisos)){
			item=new ItemControl(ICA_IRA_LISTAR, "Control IRAs", ControlIras.class, R.drawable.iras);
			addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		}
		if(ExistePermiso(ICA_EDA_LISTAR, permisos)){
			item=new ItemControl(ICA_EDA_LISTAR, "Control EDAs", ControlEdas.class, R.drawable.edas);
			addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		}*/
		if(ExistePermiso(ICA_CONTROLCONSULTA_LISTAR, permisos)){
			item=new ItemControl(ICA_CONTROLCONSULTA_LISTAR, "Control Consultas", ControlConsultas.class, R.drawable.consultas);
			addItem(item, CONTROLES_ATENCION, CONTROLES_ATENCION_MAP);
		}
		
		//Controles de Censo
		CONTROLES_CENSO.clear();CONTROLES_CENSO_MAP.clear();
		if(ExistePermiso(ICA_CENSO_LISTAR, permisos)){
			item=new ItemControl(ICA_CENSO_LISTAR, "Censo", CensoCensoNominal.class);
			addItem(item, CONTROLES_CENSO, CONTROLES_CENSO_MAP);
		}
		
		//Controles de Esquemas incompletos
		CONTROLES_ESQUEMAS.clear();CONTROLES_ESQUEMAS_MAP.clear();
		if(ExistePermiso(ICA_ESQUEMAS_LISTAR, permisos)){
			item=new ItemControl(ICA_ESQUEMAS_LISTAR, "Esquemas incompletos", CensoCensoNominal.class);
			addItem(item, CONTROLES_ESQUEMAS, CONTROLES_ESQUEMAS_MAP);
		}
		
		//Controles de Notificaciones
		CONTROLES_NOTIFICACIONES.clear();CONTROLES_NOTIFICACIONES_MAP.clear();
		if(ExistePermiso(ICA_NOTIFICACION_LISTAR, permisos)){
			item=new ItemControl(ICA_NOTIFICACION_LISTAR, "Notificaciones", Notificaciones.class, R.drawable.notificacion_ver);
			addItem(item, CONTROLES_NOTIFICACIONES, CONTROLES_NOTIFICACIONES_MAP);
		}
		if(ExistePermiso(ICA_NOTIFICACION_REPORTE_LISTAR, permisos)){
			item=new ItemControl(ICA_NOTIFICACION_REPORTE_LISTAR, "Reportes de desempeño", Reportes.class, R.drawable.reportes);
			addItem(item, CONTROLES_NOTIFICACIONES, CONTROLES_NOTIFICACIONES_MAP);
		}
		
		//Controles de Sincronización
		CONTROLES_SINCRONIZACION.clear();CONTROLES_SINCRONIZACION_MAP.clear();
		if(ExistePermiso(ICA_SINCRONIZAR_LISTAR, permisos)){
			item=new ItemControl(ICA_SINCRONIZAR_LISTAR, "Sincronización", Sincronizacion.class);
			addItem(item, CONTROLES_SINCRONIZACION, CONTROLES_SINCRONIZACION_MAP);
		}
		
		//Controles de Configuracion
		CONTROLES_CONFIGURACION.clear();CONTROLES_CONFIGURACION_MAP.clear();
		if(ExistePermiso(ICA_CONFIGURAR__CONFIGURAR, permisos)){
			item=new ItemControl(ICA_CONFIGURAR__CONFIGURAR, "Configuración", Configuracion.class);
			addItem(item, CONTROLES_CONFIGURACION, CONTROLES_CONFIGURACION_MAP);
		}
		
		//Controles de invitados
		CONTROLES_INVITADOS.clear();CONTROLES_INVITADOS_MAP.clear();
		if(ExistePermiso(ICA_INVITADOS_LISTAR, permisos)){
			item=new ItemControl(ICA_INVITADOS_LISTAR, "Listar", ControlFragment.class, R.drawable.listar);
			addItem(item, CONTROLES_INVITADOS, CONTROLES_INVITADOS_MAP);
		}
		if(ExistePermiso(ICA_INVITADOS_VALIDAR_LISTAR, permisos)){
			item=new ItemControl(ICA_INVITADOS_VALIDAR_LISTAR, "Validar", ControlFragment.class, R.drawable.validar);
			addItem(item, CONTROLES_INVITADOS, CONTROLES_INVITADOS_MAP);
		}
		
	}
	
	/**
	 * Manda recargar los controles ajustándose a los permisos recibidos
	 * @param permisos Los permisos de un usuario que inicia sesión
	 */
	public static void RecargarControles(List<Permiso> permisos){
		CrearControles(permisos);
	}
	
	private static void addItem(ItemControl item, List<ItemControl> listaDestino, 
			Map<String, ItemControl> mapaDestino) {
		listaDestino.add(item);
		mapaDestino.put(item.id, item);
		
		CONTROLES_TODOS.add(item);
		CONTROLES_TODOS_MAP.put(item.id, item);
	}
	
	/**
	 * Indica si existe un permiso Permiso en permisos que contenga id_controlador_accion
	 * @param id_controlador_accion El dato a buscar en cada Permiso
	 * @param permisos Lista de objetos Permiso en los cuales buscar id_controlador_accion
	 */
	public static boolean ExistePermiso(int id_controlador_accion, List<Permiso> permisos){
		for(Permiso permiso : permisos)
			if(permiso.id_controlador_accion == id_controlador_accion)
				return true;
		return false;
	}

	/**
	 * item que representa un Control (elemento de lista) de un menú.
	 */
	public static class ItemControl implements Serializable {

		//private static final long serialVersionUID = 2260447284334905773L;
		
		public String id;
		public String titulo;
		public Class<?> clase;
		public int resIdIcono; //Id de icono a usar

		public ItemControl(int id, String titulo, Class<?> clase) {
			Construir(id, titulo, clase, android.R.drawable.btn_star_big_on);
		}
		
		public ItemControl(int id, String titulo, Class<?> clase, int idIcono){
			Construir(id, titulo, clase, idIcono);
		}
		
		private void Construir(int id, String titulo, Class<?> clase, int idIcono){
			this.id = id+"";
			this.titulo = titulo;
			this.clase=clase;
			this.resIdIcono = idIcono;
		}

		@Override
		public String toString() {
			return titulo;
		}
	}
}
