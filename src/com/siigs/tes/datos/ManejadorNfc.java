package com.siigs.tes.datos;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonSyntaxException;
import com.siigs.tes.Sesion;
import com.siigs.tes.Sesion.DatosPaciente;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.tablas.ControlAccionNutricional;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ControlEda;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.ControlNutricional;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaAfiliacion;
import com.siigs.tes.datos.tablas.PersonaAlergia;
import com.siigs.tes.datos.tablas.RegistroCivil;
import com.siigs.tes.datos.tablas.Tutor;

import android.content.Context;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;

/**
 * Manejador de funcionalidades de comunicación con dispositivos NFC.
 * Realiza operaciones de lectura y escritura en TAGs NFC compatibles con estándar NDEF
 * @author Axel
 *
 */
public class ManejadorNfc {
	//CRITERIOS PARA PARSEAR ESTRUCTURAS EN TEXTO DE TARJETA NFC
	private static final String SEPARADOR_TABLA = "~";
	private static final String SEPARADOR_REGISTRO = "°";
	private static final String SEPARADOR_CAMPO = "=";
	private static final String SIMBOLO_NULO = "¬";
	
	//VERSIONES POSIBLES DE DATOS A CONSIDERAR
	private static final String VERSION_1 ="1";
	
	/**
	 * Lee la información del Tag NFC recibido intentando generar historial del paciente en sesión.
	 * Al mismo tiempo analiza si el paciente leído requiere ser actualizado con pendientes encontrados
	 * en el sistema.
	 * @param nfcTag Tag de NFC compatible con estándar NDEF
	 * @param contexto Contexto de la aplicación
	 * @return Una lista de tipo {@link PendientesTarjeta} con 0 o más elementos.
	 * @throws Exception
	 */
	public static List<PendientesTarjeta> LeerDatosNFC(Tag nfcTag, Context contexto) throws Exception{
		TesAplicacion aplicacion = (TesAplicacion)contexto.getApplicationContext();
		
		Sesion.DatosPaciente datosPaciente = getDatosPaciente(nfcTag);
		
		//Checamos si hay pendientes para el paciente
		List<PendientesTarjeta> pendientesResueltos = validarPendientesResueltos(contexto, datosPaciente);
		
		//Ponemos este paciente leído como el actual en atención
		aplicacion.getSesion().setDatosPacienteNuevo(datosPaciente);
		
		return pendientesResueltos;
	}
	
	public static boolean nfcTagPerteneceApersona(String idUsuarioValidar, Tag nfcTag){
		try{
			return getDatosPaciente(nfcTag).persona.id.equals(idUsuarioValidar);
		}catch(Exception e){return false;}
	}
	
	/**
	 * Genera información de paciente con el tag recibido según la versión del contenido.
	 * @param nfcTag tag que contiene la información a leer
	 * @return Historial del paciente representado en {@link DatosPaciente} 
	 * @throws Exception
	 */
	private static Sesion.DatosPaciente getDatosPaciente(Tag nfcTag) throws Exception{
		String contenido = LeerTextoPlano(nfcTag);
		String[] piezas = separar(contenido, SEPARADOR_TABLA);
				
		Sesion.DatosPaciente datosPaciente=null;
		String version = piezas[0];
		if(version.equals(VERSION_1)){
			datosPaciente = LeerVersion1(piezas, version);
		} //else if(version.equals(VERSION_X)){}
		else{
			throw new Exception("Versión de datos no reconocida");
		}
		
		return datosPaciente;
	}
	
	/**
	 * Parsea las piezas de elementos recibidos en tarjeta de acuerdo
	 * a esta versión.
	 * @param piezas Cadenas que representan tablas/elementos de acuerdo
	 * modelo de metadatos descrito en los comentarios al fondo de este archivo
	 * @param version No se usa actualmente, pero si en el futuro hubiera una  versión
	 * nueva cuya diferencia al método de parseo de ésta es de unos pocos detalles,
	 * en pocos puntos del código, esta función puede recodificarse agregando 
	 * las funcionalidades correspondientes usando {@lilnk version} para discriminar.
	 * Sin embargo si los cambios de parseo/comportamiento entre una versión y otra
	 * fueran muy amplios, sería mejor escribir una función nueva que haga
	 * sus propias implementaciones y agregar un "else if()" para llamar dicha
	 * función nueva desde LeerDatosNFC()
	 */
	private static Sesion.DatosPaciente LeerVersion1(String[] piezas, String version){
		String[] datosPersona = separar(piezas[1], SEPARADOR_CAMPO);
		Persona persona = new Persona();
		int n=0;
		persona.id = datosPersona[n++];
		persona.curp = existeString(datosPersona[n++]);
		persona.nombre = datosPersona[n++];
		persona.apellido_paterno = datosPersona[n++];
		persona.apellido_materno = datosPersona[n++];
		persona.sexo = datosPersona[n++];
		persona.id_tipo_sanguineo = Integer.parseInt(datosPersona[n++]);
		persona.fecha_nacimiento = datosPersona[n++];
		persona.id_asu_localidad_nacimiento = Integer.parseInt(datosPersona[n++]);
		persona.calle_domicilio = datosPersona[n++];
		persona.numero_domicilio = existeString(datosPersona[n++]);
		persona.colonia_domicilio = existeString(datosPersona[n++]);
		persona.referencia_domicilio = existeString(datosPersona[n++]);
		persona.ageb = existeString(datosPersona[n++]);
		persona.manzana = existeString(datosPersona[n++]);
		persona.sector = existeString(datosPersona[n++]);
		persona.id_asu_localidad_domicilio = Integer.parseInt(datosPersona[n++]);
		persona.cp_domicilio = existeInt(datosPersona[n++]);
		persona.telefono_domicilio = existeString(datosPersona[n++]);
		persona.fecha_registro = datosPersona[n++];
		persona.id_asu_um_tratante = Integer.parseInt(datosPersona[n++]);
		persona.celular = existeString(datosPersona[n++]);
		persona.ultima_actualizacion = datosPersona[n++];
		persona.id_nacionalidad = Integer.parseInt(datosPersona[n++]);
		persona.id_operadora_celular = existeInt(datosPersona[n++]);
		
		String[] datosTutor = separar(piezas[2], SEPARADOR_CAMPO);
		Tutor tutor = new Tutor();
		n=0;
		tutor.id = datosTutor[n++];
		tutor.curp = existeString(datosTutor[n++]);
		tutor.nombre = datosTutor[n++];
		tutor.apellido_paterno = datosTutor[n++];
		tutor.apellido_materno = datosTutor[n++];
		tutor.sexo = datosTutor[n++];
		tutor.telefono = existeString(datosTutor[n++]);
		tutor.celular = existeString(datosTutor[n++]);
		tutor.id_operadora_celular = existeInt(datosTutor[n++]);
		
		String[] datosRegistro = separar(piezas[3], SEPARADOR_CAMPO);
		RegistroCivil registro = new RegistroCivil();
		n=0;
		registro.id_persona = persona.id;
		registro.id_localidad_registro_civil = Integer.parseInt(datosRegistro[n++]);
		registro.fecha_registro = datosRegistro[n++];
		
		String[] listaAlergias = piezas[4].equals("")? new String[]{} : separar(piezas[4], SEPARADOR_REGISTRO);
		List<PersonaAlergia> alergias = new ArrayList<PersonaAlergia>();
		for(String regAlergia : listaAlergias){
			String[] datosAlergia = separar(regAlergia, SEPARADOR_CAMPO);
			PersonaAlergia alergia = new PersonaAlergia();
			alergia.id_persona = persona.id;
			alergia.id_alergia = Integer.parseInt(datosAlergia[0]);
			alergias.add(alergia);
		}
		
		String[] listaAfiliaciones = piezas[5].equals("")? new String[]{} : separar(piezas[5], SEPARADOR_REGISTRO);
		List<PersonaAfiliacion> afiliaciones = new ArrayList<PersonaAfiliacion>();
		for(String regAfiliacion : listaAfiliaciones){
			String[] datosAfiliacion = separar(regAfiliacion, SEPARADOR_CAMPO);
			PersonaAfiliacion afiliacion = new PersonaAfiliacion();
			afiliacion.id_persona = persona.id;
			afiliacion.id_afiliacion = Integer.parseInt(datosAfiliacion[0]);
			afiliaciones.add(afiliacion);
		}
		
		String[] listaVacunas = piezas[6].equals("")? new String[]{} : separar(piezas[6], SEPARADOR_REGISTRO);
		List<ControlVacuna> vacunas = new ArrayList<ControlVacuna>();
		for(String regVacuna : listaVacunas){
			String[] datosVacuna = separar(regVacuna, SEPARADOR_CAMPO);
			ControlVacuna vacuna = new ControlVacuna();
			vacuna.id_persona = persona.id;
			vacuna.id_vacuna = Integer.parseInt(datosVacuna[0]);
			vacuna.fecha = datosVacuna[1];
			vacuna.id_asu_um = Integer.parseInt(datosVacuna[2]);
			vacunas.add(vacuna);
		}
		
		String[] listaIras = piezas[7].equals("")? new String[]{} : separar(piezas[7], SEPARADOR_REGISTRO);
		List<ControlIra> iras = new ArrayList<ControlIra>();
		for(String regIra : listaIras){
			String[] datosIra = separar(regIra, SEPARADOR_CAMPO);
			ControlIra ira = new ControlIra();
			ira.id_persona = persona.id;
			ira.id_ira = Integer.parseInt(datosIra[0]);
			ira.fecha = datosIra[1];
			ira.id_asu_um = Integer.parseInt(datosIra[2]);
			ira.id_tratamiento = Integer.parseInt(datosIra[3]);
			ira.grupo_fecha_secuencial = datosIra[4];
			iras.add(ira);
		}
		
		String[] listaEdas = piezas[8].equals("")? new String[]{} : separar(piezas[8], SEPARADOR_REGISTRO);
		List<ControlEda> edas = new ArrayList<ControlEda>();
		for(String regEda : listaEdas){
			String[] datosEda = separar(regEda, SEPARADOR_CAMPO);
			ControlEda eda = new ControlEda();
			eda.id_persona = persona.id;
			eda.id_eda = Integer.parseInt(datosEda[0]);
			eda.fecha = datosEda[1];
			eda.id_asu_um = Integer.parseInt(datosEda[2]);
			eda.id_tratamiento = Integer.parseInt(datosEda[3]);
			eda.grupo_fecha_secuencial = datosEda[4];
			edas.add(eda);
		}
		
		String[] listaConsultas = piezas[9].equals("")? new String[]{} : separar(piezas[9], SEPARADOR_REGISTRO);
		List<ControlConsulta> consultas = new ArrayList<ControlConsulta>();
		for(String regConsulta : listaConsultas){
			String[] datosConsulta = separar(regConsulta, SEPARADOR_CAMPO);
			ControlConsulta consulta = new ControlConsulta();
			consulta.id_persona = persona.id;
			consulta.id_consulta = Integer.parseInt(datosConsulta[0]);
			consulta.fecha = datosConsulta[1];
			consulta.id_asu_um = Integer.parseInt(datosConsulta[2]);
			consulta.id_tratamiento = Integer.parseInt(datosConsulta[3]);
			consulta.grupo_fecha_secuencial = datosConsulta[4];
			consultas.add(consulta);
		}
		
		String[] listaAcciones = piezas[10].equals("")? new String[]{} : separar(piezas[10], SEPARADOR_REGISTRO);
		List<ControlAccionNutricional> acciones = new ArrayList<ControlAccionNutricional>();
		for(String regAccion : listaAcciones){
			String[] datosAccion = separar(regAccion, SEPARADOR_CAMPO);
			ControlAccionNutricional accion = new ControlAccionNutricional();
			accion.id_persona = persona.id;
			accion.id_accion_nutricional = Integer.parseInt(datosAccion[0]);
			accion.fecha = datosAccion[1];
			accion.id_asu_um = Integer.parseInt(datosAccion[2]);
			acciones.add(accion);
		}
		
		//Si persona no tiene controles, no existirá última localidad, así que la validamos
		String[] listaControles = piezas[11].equals("")? new String[]{} : separar(piezas[11], SEPARADOR_REGISTRO);
		List<ControlNutricional> controles = new ArrayList<ControlNutricional>();
		for(String regControl : listaControles){
			String[] datosControl = separar(regControl, SEPARADOR_CAMPO);
			ControlNutricional control = new ControlNutricional();
			control.id_persona = persona.id;
			control.peso = Double.parseDouble(datosControl[0]);
			control.altura = Integer.parseInt(datosControl[1]);
			control.talla = Integer.parseInt(datosControl[2]);
			control.fecha = datosControl[3];
			control.id_asu_um = Integer.parseInt(datosControl[4]);
			controles.add(control);
		}
		
		return new Sesion.DatosPaciente(persona, tutor, registro, alergias, afiliaciones,
				vacunas, iras, edas, consultas, acciones, controles, true);
	}//fin LeerVersion1
	
	/**
	 * Convierte los datos del paciente en String con formato adecuado para guardarse
	 * en nfcTag.
	 * En caso de que la versión de base de datos cambie, se deberá cambiar esta función
	 * al menos en la primera línea que imprime VERSION_1 y lo que sea necesario después.
	 * Así mismo un cambio de versión requerirá cambios en la función de lectura.
	 * @param nfcTag Tag de NFC compatible con estándar NDEF
	 * @param datos Historial del paciente a escribir en nfcTag
	 * @throws Exception 
	 */
	public static void EscribirDatosNFC(Tag nfcTag, Sesion.DatosPaciente datos) throws Exception{
		StringBuilder salida = new StringBuilder();
		salida.append(VERSION_1+SEPARADOR_TABLA);
		
		//Datos de persona
		salida.append(datos.persona.id + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.curp) + SEPARADOR_CAMPO);
		salida.append(datos.persona.nombre + SEPARADOR_CAMPO);
		salida.append(datos.persona.apellido_paterno + SEPARADOR_CAMPO);
		salida.append(datos.persona.apellido_materno + SEPARADOR_CAMPO);
		salida.append(datos.persona.sexo + SEPARADOR_CAMPO);
		salida.append(datos.persona.id_tipo_sanguineo + SEPARADOR_CAMPO);
		salida.append(datos.persona.fecha_nacimiento + SEPARADOR_CAMPO);
		salida.append(datos.persona.id_asu_localidad_nacimiento + SEPARADOR_CAMPO);
		salida.append(datos.persona.calle_domicilio + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.numero_domicilio) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.colonia_domicilio) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.referencia_domicilio) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.ageb) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.manzana) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.sector) + SEPARADOR_CAMPO);
		salida.append(convertirInt(datos.persona.id_asu_localidad_domicilio) + SEPARADOR_CAMPO);
		salida.append(convertirInt(datos.persona.cp_domicilio) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.telefono_domicilio) + SEPARADOR_CAMPO);
		salida.append(datos.persona.fecha_registro + SEPARADOR_CAMPO);
		salida.append(datos.persona.id_asu_um_tratante + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.persona.celular) + SEPARADOR_CAMPO);
		salida.append(datos.persona.ultima_actualizacion + SEPARADOR_CAMPO);
		salida.append(datos.persona.id_nacionalidad + SEPARADOR_CAMPO);
		salida.append(convertirInt(datos.persona.id_operadora_celular) + SEPARADOR_TABLA);
		
		//Datos de tutor
		salida.append(datos.tutor.id + SEPARADOR_CAMPO);
		salida.append(datos.tutor.curp + SEPARADOR_CAMPO);
		salida.append(datos.tutor.nombre + SEPARADOR_CAMPO);
		salida.append(datos.tutor.apellido_paterno + SEPARADOR_CAMPO);
		salida.append(datos.tutor.apellido_materno + SEPARADOR_CAMPO);
		salida.append(datos.tutor.sexo + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.tutor.telefono) + SEPARADOR_CAMPO);
		salida.append(convertirString(datos.tutor.celular) + SEPARADOR_CAMPO);
		salida.append(convertirInt(datos.tutor.id_operadora_celular) + SEPARADOR_TABLA);
		
		//Datos de registro civil
		salida.append(datos.registroCivil.id_localidad_registro_civil + SEPARADOR_CAMPO);
		salida.append(datos.registroCivil.fecha_registro + SEPARADOR_TABLA);
		
		//Datos de alergias
		for(int i=0;i<datos.alergias.size();i++){
			salida.append(datos.alergias.get(i).id_alergia);
			if(i!=datos.alergias.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos afiliación
		for(int i=0;i<datos.afiliaciones.size();i++){
			salida.append(datos.afiliaciones.get(i).id_afiliacion);
			if(i!=datos.afiliaciones.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos vacunas
		for(int i=0;i<datos.vacunas.size();i++){
			salida.append(datos.vacunas.get(i).id_vacuna + SEPARADOR_CAMPO);
			salida.append(datos.vacunas.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.vacunas.get(i).id_asu_um);
			if(i!=datos.vacunas.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos iras
		for(int i=0;i<datos.iras.size();i++){
			salida.append(datos.iras.get(i).id_ira + SEPARADOR_CAMPO);
			salida.append(datos.iras.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.iras.get(i).id_asu_um + SEPARADOR_CAMPO);
			salida.append(datos.iras.get(i).id_tratamiento + SEPARADOR_CAMPO);
			salida.append(datos.iras.get(i).grupo_fecha_secuencial);
			if(i!=datos.iras.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos edas
		for (int i = 0; i < datos.edas.size(); i++) {
			salida.append(datos.edas.get(i).id_eda + SEPARADOR_CAMPO);
			salida.append(datos.edas.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.edas.get(i).id_asu_um + SEPARADOR_CAMPO);
			salida.append(datos.edas.get(i).id_tratamiento + SEPARADOR_CAMPO);
			salida.append(datos.edas.get(i).grupo_fecha_secuencial);
			if (i != datos.edas.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos consultas
		for (int i = 0; i < datos.consultas.size(); i++) {
			salida.append(datos.consultas.get(i).id_consulta + SEPARADOR_CAMPO);
			salida.append(datos.consultas.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.consultas.get(i).id_asu_um + SEPARADOR_CAMPO);
			salida.append(datos.consultas.get(i).id_tratamiento + SEPARADOR_CAMPO);
			salida.append(datos.consultas.get(i).grupo_fecha_secuencial);
			if (i != datos.consultas.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos acciones nutricionales
		for (int i = 0; i < datos.accionesNutricionales.size(); i++) {
			salida.append(datos.accionesNutricionales.get(i).id_accion_nutricional + SEPARADOR_CAMPO);
			salida.append(datos.accionesNutricionales.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.accionesNutricionales.get(i).id_asu_um);
			if (i != datos.accionesNutricionales.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos controles nutricionales
		for (int i = 0; i < datos.controlesNutricionales.size(); i++) {
			salida.append(datos.controlesNutricionales.get(i).peso + SEPARADOR_CAMPO);
			salida.append(datos.controlesNutricionales.get(i).altura + SEPARADOR_CAMPO);
			salida.append(datos.controlesNutricionales.get(i).talla + SEPARADOR_CAMPO);
			salida.append(datos.controlesNutricionales.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.controlesNutricionales.get(i).id_asu_um);
			if (i != datos.controlesNutricionales.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		
		EscribirTextoPlano(nfcTag, salida.toString());
	}

	//HELPERS PARA PARSEO
	private static String existeString(String texto){return SIMBOLO_NULO.equals(texto)? null : texto;}
	private static Integer existeInt(String texto){return SIMBOLO_NULO.equals(texto)? null : Integer.parseInt(texto);}
	private static String convertirString(String texto){return texto == null? SIMBOLO_NULO : texto;}
	private static String convertirInt(Integer numero){return numero == null? SIMBOLO_NULO : numero+"";}
	
	private static String[] separar(String cadenaSeparar, String simbolo){
		List<String> salida = new ArrayList<String>();
		int indexSimbolo=-1, indexInicioCopia=0;
		while( (indexSimbolo=cadenaSeparar.indexOf(simbolo, indexInicioCopia)) >=0 ){
			salida.add(cadenaSeparar.substring(indexInicioCopia, indexSimbolo));
			indexInicioCopia = indexSimbolo+1;
		}
		if(salida.size()>0) //si encontró al menos uno, puede pasar una de dos ...
			if(cadenaSeparar.endsWith(simbolo)) //... acaba con simbolo, y por tanto a la derecha hay vacío
				salida.add(""); //... lo remarcamos así
			else
				salida.add(cadenaSeparar.substring(indexInicioCopia));  //... recuperamos el último pedazo que no capturó
		if(salida.size()==0)return new String[]{cadenaSeparar};
		return salida.toArray(new String[]{});
	}
	
	//HELPERS PARA PENDIENTES
	/**
	 * Verifica los pendientes por resolver para el paciente definido en <b>datosPaciente</b>
	 * @param contexto
	 * @param datosPaciente Historial que contiene al paciente que se le revisará sus pendientes
	 * @return Lista de tipo {@link PendientesTarjeta} con los pendientes que se deben resolver
	 */
	private static List<PendientesTarjeta> validarPendientesResueltos(Context contexto, Sesion.DatosPaciente datosPaciente){
		//Checamos si hay pendientes para el paciente
		List<PendientesTarjeta> pendientesResueltos = new ArrayList<PendientesTarjeta>();
		List<PendientesTarjeta> pendientesResolver = 
				PendientesTarjeta.getPendientesPaciente(contexto, datosPaciente.persona.id);
		if(pendientesResolver.size()>0){
			for(PendientesTarjeta pendiente : pendientesResolver){ 
				try{
					if(pendiente.tabla.equals(ControlVacuna.NOMBRE_TABLA)){
						ControlVacuna nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlVacuna.class);
						if(!InsertarEnLista(nuevo, datosPaciente.vacunas))
							continue;
					}else if(pendiente.tabla.equals(ControlNutricional.NOMBRE_TABLA)){
						ControlNutricional nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlNutricional.class);
						if(!InsertarEnLista(nuevo, datosPaciente.controlesNutricionales))
							continue;
					}else if(pendiente.tabla.equals(ControlAccionNutricional.NOMBRE_TABLA)){
						ControlAccionNutricional nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlAccionNutricional.class);
						if(!InsertarEnLista(nuevo, datosPaciente.accionesNutricionales))
							continue;
					}else if(pendiente.tabla.equals(ControlConsulta.NOMBRE_TABLA)){
						ControlConsulta nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlConsulta.class);
						if(!InsertarEnLista(nuevo, datosPaciente.consultas))
							continue;
					}else if(pendiente.tabla.equals(ControlIra.NOMBRE_TABLA)){
						ControlIra nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlIra.class);
						if(!InsertarEnLista(nuevo, datosPaciente.iras))
							continue;
					}else if(pendiente.tabla.equals(ControlEda.NOMBRE_TABLA)){
						ControlEda nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlEda.class);
						if(!InsertarEnLista(nuevo, datosPaciente.edas))
							continue;
						
						//ESTOS ELEMENTOS SE AGREGAN DIRECTO PUES NO HAY ORDEN ESPECÍFICO REQUERIDO
					}else if(pendiente.tabla.equals(PersonaAlergia.NOMBRE_TABLA)){
						PersonaAlergia nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, PersonaAlergia.class);
						if(datosPaciente.alergias.contains(nuevo))continue;
						else datosPaciente.alergias.add(nuevo);
					}else if(pendiente.tabla.equals(PersonaAfiliacion.NOMBRE_TABLA)){
						PersonaAfiliacion nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, PersonaAfiliacion.class);
						if(datosPaciente.afiliaciones.contains(nuevo))continue;
						else datosPaciente.afiliaciones.add(nuevo);
						//PENDIENTES DE ASIGNACIÓN DIRECTA (SIN LISTA)
					}else if(pendiente.tabla.equals(RegistroCivil.NOMBRE_TABLA)){
						RegistroCivil nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, RegistroCivil.class);
						datosPaciente.registroCivil = nuevo;
					}else if(pendiente.tabla.equals(Tutor.NOMBRE_TABLA)){
						Tutor nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, Tutor.class);
						if(esFechaHoraMenor(datosPaciente.tutor.ultima_actualizacion, nuevo.ultima_actualizacion))
							datosPaciente.tutor = nuevo;
						else continue;
					}else if(pendiente.tabla.equals(Persona.NOMBRE_TABLA)){
						Persona nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, Persona.class);
						if(esFechaHoraMenor(datosPaciente.persona.ultima_actualizacion, nuevo.ultima_actualizacion))
							datosPaciente.persona = nuevo;
						else continue;
					}else{
						continue; //ESTO NUNCA DEBERÍA SUCEDER a menos que se tratara de un tipo de pendiente muy nuevo en app vieja 
					}
					pendientesResueltos.add(pendiente);
					//PendientesTarjeta.MarcarPendienteResuelto(contexto, pendiente);
					
				}catch(JsonSyntaxException jse){
					int idUsuario=((TesAplicacion)contexto.getApplicationContext()).getSesion().getUsuario()._id;
					ErrorSis.AgregarError(contexto, idUsuario, ErrorSis.ERROR_DESCONOCIDO, "Json incorrecto en pendiente de persona:"+
							pendiente.id_persona+", fecha:"+pendiente.fecha+", tabla:"+pendiente.tabla);
				}
				
			}//fin ciclo pendientes
			
			//Esta línea no se puede ejecutar pues la tarjeta fue leída previamente y hay que pasarla de nuevo
			//físicamente antes de poder escribir. En consecuencia tampoco se puede marcar como resuelto su pendiente aún
			//EscribirDatosNFC(nfcTag, datosPaciente);
		}//fin si hay pendientes
		return pendientesResueltos;
	}
	
	/**
	 * Inserta objeto en lista en posición ordenada de menor a mayor.
	 * Si objeto tiene una fecha menor que otros elementos de lista, se insertará en medio
	 * NOTA: Esta implementación improvisada puede cambiarse por una llamada a 
	 * Collections.sort(lista, new InstanciaComparador() ) donde InstanciaComparador es
	 * una clase que implementa Comparator<T>{Compare();}
	 * @param objeto
	 * @param lista
	 */
	private static <T> boolean InsertarEnLista(T objeto, List<T> lista){
		if(lista.contains(objeto))return false; //pues no es necesario agregarlo más veces
		
		int indice;
		for(indice=0; indice < lista.size(); indice++){
			
			if(objeto instanceof ControlVacuna){
				if(esFechaHoraMenor( ((ControlVacuna) objeto).fecha, 
						((ControlVacuna)lista.get(indice)).fecha) )
					break;
			}else if(objeto instanceof ControlNutricional){
				if(esFechaHoraMenor( ((ControlNutricional) objeto).fecha, 
						((ControlNutricional)lista.get(indice)).fecha) )
					break;
			}else if(objeto instanceof ControlAccionNutricional){
				if(esFechaHoraMenor( ((ControlAccionNutricional) objeto).fecha, 
						((ControlAccionNutricional)lista.get(indice)).fecha) )
					break;
			}else if(objeto instanceof ControlConsulta){
				if(esFechaHoraMenor( ((ControlConsulta) objeto).fecha, 
						((ControlConsulta)lista.get(indice)).fecha) )
					break;
			}else if(objeto instanceof ControlIra){
				if(esFechaHoraMenor( ((ControlIra) objeto).fecha, 
						((ControlIra)lista.get(indice)).fecha) )
					break;
			}else if(objeto instanceof ControlEda){
				if(esFechaHoraMenor( ((ControlEda) objeto).fecha, 
						((ControlEda)lista.get(indice)).fecha) )
					break;
			}
		}
		//Inserta objeto en la posición adecuada según su fecha
		lista.add(indice, objeto);
		return true;
	}
	//Helper para convertir tiempo
	private static boolean esFechaHoraMenor(String fecha1, String feccha2){
		try{
			return DatosUtil.esFechaHoraMenor(fecha1, feccha2);
		}catch(Exception e){
			return false;}
	}
	
	/**
	 * Intenta extraer texto plano del tag NFC recibido
	 * @param nfcTag proveedor de los datos
	 * @return cadena de texto contenida en {@link nfcTag}
	 * @throws Exception
	 */
	private static String LeerTextoPlano(Tag nfcTag) throws Exception{
		Ndef ndef = Ndef.get(nfcTag);
		if(ndef==null)return "";
		try {
			ndef.connect();
			NdefMessage mensaje = ndef.getNdefMessage();
			ndef.close();
			NdefRecord record = mensaje.getRecords()[0];
			byte[] payload = record.getPayload();
			
			int bytesHeader = 1 + "en".getBytes("US-ASCII").length;
			int bytesDatos = payload.length - bytesHeader;
			byte[] datos = new byte[bytesDatos];
			System.arraycopy(payload, bytesHeader, datos, 0, bytesDatos);
			
			return new String(datos);
		} catch (IOException e) {
			if(ndef!=null && ndef.isConnected())ndef.close();
			throw new Exception("No se pudo leer la tarjeta:"+e);
		} catch (FormatException e) {
			if(ndef!=null && ndef.isConnected())ndef.close();
			throw new Exception("Mal formato al leer texto plano"+e);
		} catch (Exception e){
			if(ndef!=null && ndef.isConnected())ndef.close();
			throw new Exception("Tarjeta no reconocida al leer:"+e);
		}

	}
	
	/**
	 * Escribe el texto recibido en un tag nfc
	 * @param nfcTag
	 * @param texto
	 * @throws Exception
	 */
	private static void EscribirTextoPlano(Tag nfcTag, String texto) throws Exception{
		NdefRecord[] records = { crearRecordNfc(texto) };
		NdefMessage  mensaje = new NdefMessage(records);
		
		Ndef ndef = Ndef.get(nfcTag);
		// vemos si el tag tiene formato al validar nulo
		if (ndef != null) {
			ndef.connect();

			if (!ndef.isWritable())
				throw new Exception("La tarjeta es de SOLO lectura");
			
			// validamos si hay espacio suficiente
			int size = mensaje.getByteArrayLength();
			if (ndef.getMaxSize() < size)
				throw new Exception("No hay suficiente espacio en tarjeta para guardar información");

			ndef.writeNdefMessage(mensaje);
			ndef.close();
		} else {
			// Intentamos formatear tarjeta
			//Nunca debería ocurrir pues escribimos en tarjetas previamente leídas correctamente
			NdefFormatable format = NdefFormatable.get(nfcTag);
			if (format != null) {
				try {
					format.connect();
					format.format(mensaje);
					format.close();
				} catch (IOException e) {
					format.close();
					throw new Exception("No se pudo formatear tarjeta al intentar escribir:"+ e);
				}
			} else {
				throw new Exception("Tarjeta no parece soportar formato NDEF.");
			}
		}
	}
	
	/**
	 * Crea un record nfc para escribirlo en un tag nfc con el texto recibido
	 * @param texto
	 * @return {@link NdefRecord} estándar que contiene información de <b>texto</b>
	 */
	private static NdefRecord crearRecordNfc(String texto) {
		byte[] bytesTexto  = texto.getBytes();
		byte[] bytesIdioma=null;
		try {
			bytesIdioma = "en".getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// Nunca debería pasar
		}
		byte[] payload    = new byte[1 + bytesIdioma.length + bytesTexto.length];

		// set status byte (see NDEF spec for actual bits)
		payload[0] = (byte) bytesIdioma.length;

		// copia bytesLang y bytesTexto a payload
		System.arraycopy(bytesIdioma,  0, payload, 1                     , bytesIdioma.length);
		System.arraycopy(bytesTexto, 0,   payload, 1 + bytesIdioma.length, bytesTexto.length);

		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
	}
	
	/**
	 * VERSIÓN 1 de metadatos ----------------
	 * VERSIONBD
persona
  `id` CHAR(32)*******
  `curp` VARCHAR(18)
  `nombre` VARCHAR(35)
  `apellido_paterno` VARCHAR(20)
  `apellido_materno` VARCHAR(20)
  `sexo` CHAR(1)
  `id_ece_tipo_sanguineo` INT(1)
  `fecha_nacimiento` DATE
  `id_localidad_nacimiento` INT(10)
  `calle_domicilio` VARCHAR(60)
  `numero_domicilio` VARCHAR(10)
  `colonia_domicilio` VARCHAR(30)
  `referencia_domicilio` VARCHAR(60)
  `id_localidad_domicilio` INT(6)
  `cp_domicilio` INT(5)
  `telefono_domicilio` VARCHAR(20)
  `fecha_registro` DATE 
  `id_um_tratante` INT(10)
  `celuar` VARCHAR(20) 
  `ultima_actualizacion` DATETIME
  `id_ece_nacionalidad` INT(3)
  `id_operadora_celular` INT(2)
  `ultima_actualizacion` INT(2)

tutor
  `id` CHAR(32) 
  `curp` VARCHAR(18)
  `nombre` VARCHAR(35) 
  `apellido_paterno` VARCHAR(20)
  `apellido_materno` VARCHAR(20) 
  `sexo` char(1) 
  `telefono` VARCHAR(20)
  `celular` VARCHAR(20) 
  `id_operadora_celular` INT(2)

registro_civil
  `id_localidad_registro_civil`
  `fecha_registro` DATE

persona_x_alergia*** UNA ENTRADA POR CADA ALERGIA
  `id_alergia` INT(4)

persona_x_afiliacion*** UNA ENTRADA POR CADA AfILIACION
  `id_afiliacion` INT(2)

control_vacuna*** UNA ENTRADA POR CADA VACUNA
  `id_ece_vacuna` INT(2) 
  `fecha` DATE

control_ira*** UNA ENTRADA POR CADA ENFERMEDAD
  `id_ece_ira` INT(3) 
  `fecha` DATE 

control_eda*** UNA ENTRADA POR CADA ENFERMEDAD
  `id_ece_eda` INT(3) 
  `fecha` DATE  

control_consulta*** UNA ENTRADA POR CADA ENFERMEDAD
  `id_ece_consulta` INT(3) 
  `fecha` DATE 

control_accion_nutricional*** UNA ENTRADA POR CADA CONTROL
  `id_ece_accion_nutricional` INT(2) 
  `fecha` DATETIME 

control_nutricional*** UNA ENTRADA POR CADA CONTROL
  `peso` DECIMAL(5,2) 
  `altura` INT(3)
  `talla` INT(3) 
  `fecha` DATETIME
	 */
}
