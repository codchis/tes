package com.siigs.tes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.siigs.tes.controles.ContenidoControles;
import com.siigs.tes.datos.tablas.ControlAccionNutricional;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ControlEda;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.ControlNutricional;
import com.siigs.tes.datos.tablas.ControlPerimetroCefalico;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.EstimulacionTemprana;
import com.siigs.tes.datos.tablas.Permiso;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaAfiliacion;
import com.siigs.tes.datos.tablas.PersonaAlergia;
import com.siigs.tes.datos.tablas.RegistroCivil;
import com.siigs.tes.datos.tablas.SalesRehidratacion;
import com.siigs.tes.datos.tablas.Tutor;
import com.siigs.tes.datos.tablas.Usuario;
import com.siigs.tes.datos.tablas.UsuarioInvitado;

/**
 * Describe atributos de una sesión de uso en la aplicación
 * @author Axel
 *
 */
public class Sesion {

	private Usuario usuario;
	private UsuarioInvitado invitado; //Si es invitado, aquí lo indica
	private List<Permiso> permisos; //Si existe invitado, los permisos deben ser del grupo invitado
	private Calendar fechaInicio;
	
	private DatosPaciente datosPaciente=null; //El paciente en turno que recibe atención
	
	public Sesion(Usuario usuario, UsuarioInvitado invitado, List<Permiso> permisos) {
		this.usuario = usuario;
		this.invitado = invitado;
		this.permisos = permisos;
		this.fechaInicio = Calendar.getInstance();
	}
	
	public Usuario getUsuario(){return usuario;}
	public List<Permiso> getPermisos(){return permisos;}
	public Calendar getFechaInicio(){return fechaInicio;}
	public UsuarioInvitado getUsuarioInvitado(){return invitado;}
	
	public boolean tienePermiso(int id_controlador_accion){
		return ContenidoControles.ExistePermiso(id_controlador_accion, permisos);
	}

	public DatosPaciente getDatosPacienteActual(){return datosPaciente;}
	public void setDatosPacienteNuevo(DatosPaciente datos){this.datosPaciente = datos;}
	
	
	/**
	 * Esta clase mantiene toda la información conocida de un paciente
	 * @author Axel
	 *
	 */
	public static class DatosPaciente{
		private static final String TAG = DatosPaciente.class.getSimpleName();
		
		//Indica si estos datos de paciente son cargados desde una tarjeta NFC
		private boolean cargadoDesdeNfc=true;
		//Indica el bloque de datos del ICSS cuando los datos son cargados de tarjeta NFC y existen datos del ICSS
		public String datosNfcIcss = "";
		
		public Persona persona=null;
		public Tutor tutor=null;
		public RegistroCivil registroCivil=null;
		public List<PersonaAlergia> alergias=new ArrayList<PersonaAlergia>();
		public List<PersonaAfiliacion> afiliaciones = new ArrayList<PersonaAfiliacion>();
		public List<ControlVacuna> vacunas = new ArrayList<ControlVacuna>();
		public List<ControlIra> iras = new ArrayList<ControlIra>();
		public List<ControlEda> edas = new ArrayList<ControlEda>();
		public List<ControlConsulta> consultas = new ArrayList<ControlConsulta>();
		public List<ControlAccionNutricional> accionesNutricionales = new ArrayList<ControlAccionNutricional>();
		public List<ControlNutricional> controlesNutricionales = new ArrayList<ControlNutricional>();
		public List<ControlPerimetroCefalico> perimetrosCefalicos = new ArrayList<ControlPerimetroCefalico>();
		public List<SalesRehidratacion> salesRehidratacion = new ArrayList<SalesRehidratacion>();
		public List<EstimulacionTemprana> estimulacionesTempranas = new ArrayList<EstimulacionTemprana>();
		
		/**
		 * Carga los datos del paciente desde la base de datos local
		 * @param context Contexto para proveedor de contenido
		 * @param _idPersona Persona a quien pertenecen los datos a cargar
		 * @return {@link DatosPaciente} con los datos cargados de la persona especificada
		 */
		public static DatosPaciente cargarDesdeBaseDatos(Context context, int _idPersona){
			try{
				Persona paciente = Persona.getPersona(context, _idPersona);
				Tutor tutor = Tutor.getTutorDePersona(context, paciente.id);
				RegistroCivil registro = RegistroCivil.getRegistro(context, paciente.id);
				List<PersonaAlergia> alergias = PersonaAlergia.getAlergiasPersona(context, paciente.id);
				List<PersonaAfiliacion> afiliaciones = PersonaAfiliacion.getAfiliacionesPersona(context, paciente.id);
				List<ControlVacuna> vacunas = ControlVacuna.getVacunasPersona(context, paciente.id);
				List<ControlIra> iras = ControlIra.getIrasPersona(context, paciente.id);
				List<ControlEda> edas = ControlEda.getEdasPersona(context, paciente.id);
				List<ControlConsulta> consultas = ControlConsulta.getConsultasPersona(context, paciente.id);
				List<ControlAccionNutricional> accionesNutricionales = 
						ControlAccionNutricional.getAccionesNutricionalesPersona(context, paciente.id);
				List<ControlNutricional> controlesNutricionales = 
						ControlNutricional.getControlesNutricionalesPersona(context, paciente.id);
				List<ControlPerimetroCefalico> perimetrosCefalicos = 
						ControlPerimetroCefalico.getPerimetrosCefalicosPersona(context, paciente.id);
				List<SalesRehidratacion> salesRehidratacion = 
						SalesRehidratacion.getSalesRehidratacionPersona(context, paciente.id);
				List<EstimulacionTemprana> estimulacionesTempranas = 
						EstimulacionTemprana.getEstimulacionesPersona(context, paciente.id);
				
				return new DatosPaciente(paciente, tutor, registro, alergias, afiliaciones, vacunas, iras,
						edas, consultas, accionesNutricionales, controlesNutricionales, perimetrosCefalicos,
						salesRehidratacion, estimulacionesTempranas, "", false);
			}catch(Exception e){
				Log.d(TAG, "No se pudo cargar historial de paciente desde base de datos con _id:"+_idPersona+"\n"+e);
				return null;
			}
		}
		
		public DatosPaciente(Persona p, Tutor t, RegistroCivil rc, List<PersonaAlergia> alergias, 
				List<PersonaAfiliacion> afiliaciones, List<ControlVacuna> vacunas, List<ControlIra> iras,
				List<ControlEda> edas, List<ControlConsulta> consultas,
				List<ControlAccionNutricional> accionesNutricionales, 
				List<ControlNutricional> controlesNutricionales, 
				List<ControlPerimetroCefalico> perimetrosCefalicos,
				List<SalesRehidratacion> salesRehidratacion,
				List<EstimulacionTemprana> estimulacionesTempranas, 
				String datosNfcIcss, boolean cargadoDesdeNfc){
			persona = p;
			tutor = t;
			registroCivil = rc;
			this.alergias = alergias;
			this.afiliaciones = afiliaciones;
			this.vacunas = vacunas;
			this.iras = iras;
			this.edas = edas;
			this.consultas = consultas;
			this.accionesNutricionales = accionesNutricionales;
			this.controlesNutricionales = controlesNutricionales;
			this.perimetrosCefalicos = perimetrosCefalicos;
			this.salesRehidratacion = salesRehidratacion;
			this.estimulacionesTempranas = estimulacionesTempranas;
			this.datosNfcIcss = datosNfcIcss;
			this.cargadoDesdeNfc = cargadoDesdeNfc;
		}
		
		public boolean fueCargadoDesdeNfc(){return cargadoDesdeNfc;}
	}//fin clase DatosPaciente
	
}//fin Sesion
