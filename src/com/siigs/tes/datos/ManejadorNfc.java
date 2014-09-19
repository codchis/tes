package com.siigs.tes.datos;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.JsonSyntaxException;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.siigs.tes.Sesion;
import com.siigs.tes.Sesion.DatosPaciente;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.tablas.ControlAccionNutricional;
import com.siigs.tes.datos.tablas.ControlConsulta;
import com.siigs.tes.datos.tablas.ControlEda;
import com.siigs.tes.datos.tablas.ControlIra;
import com.siigs.tes.datos.tablas.ControlNutricional;
import com.siigs.tes.datos.tablas.ControlPerimetroCefalico;
import com.siigs.tes.datos.tablas.ControlVacuna;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.EstimulacionTemprana;
import com.siigs.tes.datos.tablas.PendientesTarjeta;
import com.siigs.tes.datos.tablas.Persona;
import com.siigs.tes.datos.tablas.PersonaAfiliacion;
import com.siigs.tes.datos.tablas.PersonaAlergia;
import com.siigs.tes.datos.tablas.RegistroCivil;
import com.siigs.tes.datos.tablas.SalesRehidratacion;
import com.siigs.tes.datos.tablas.Tutor;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.widget.Toast;

/**
 * Manejador de funcionalidades de comunicaci�n con dispositivos NFC.
 * Realiza operaciones de lectura y escritura en TAGs NFC compatibles con est�ndar NDEF
 * @author Axel
 *
 */
public class ManejadorNfc {
	//CRITERIOS PARA PARSEAR ESTRUCTURAS EN TEXTO DE TARJETA NFC
	private static final String SEPARADOR_BLOQUE = "^";
	private static final String SEPARADOR_TABLA = "~";
	private static final String SEPARADOR_REGISTRO = "�";
	private static final String SEPARADOR_CAMPO = "=";
	private static final String SIMBOLO_NULO = "�";
	
	//VERSIONES POSIBLES DE DATOS A CONSIDERAR
	private static final String VERSION_1 ="1";
	
	/**
	 * Lee la informaci�n del Tag NFC recibido intentando generar historial del paciente en sesi�n.
	 * Al mismo tiempo analiza si el paciente le�do requiere ser actualizado con pendientes encontrados
	 * en el sistema.
	 * @param nfcTag Tag de NFC compatible con est�ndar NDEF
	 * @param contexto Contexto de la aplicaci�n
	 * @return Una lista de tipo {@link PendientesTarjeta} con 0 o m�s elementos
	 * @throws Exception Cuando ocurre alg�n error
	 */
	public static List<PendientesTarjeta> LeerDatosNFC(Tag nfcTag, Context contexto) throws Exception{
		TesAplicacion aplicacion = (TesAplicacion)contexto.getApplicationContext();
		
		Sesion.DatosPaciente datosPaciente = getDatosPaciente(nfcTag);
		
		//Checamos si hay pendientes para el paciente
		List<PendientesTarjeta> pendientesResueltos = validarPendientesResueltos(contexto, datosPaciente);
		
		//Ponemos este paciente le�do como el actual en atenci�n
		aplicacion.getSesion().setDatosPacienteNuevo(datosPaciente);
		
		return pendientesResueltos;
	}
	
	/**
	 * Lee la informaci�n del metadatos NFC recibido intentando generar historial del paciente en sesi�n.
	 * Al mismo tiempo analiza si el paciente le�do requiere ser actualizado con pendientes encontrados
	 * en el sistema.
	 * @param metadatosNfc Metadatos con la informaci�n a interpretar
	 * @param contexto Contexto de la aplicaci�n
	 * @return Una lista de tipo {@link PendientesTarjeta} con 0 o m�s elementos
	 * @throws Exception Cuando ocurre alg�n error
	 */
	private static List<PendientesTarjeta> LeerDatosNFC(String metadatosNfc, Context contexto) throws Exception{
		TesAplicacion aplicacion = (TesAplicacion)contexto.getApplicationContext();
		
		Sesion.DatosPaciente datosPaciente = getDatosPaciente(metadatosNfc);
		
		//Checamos si hay pendientes para el paciente
		List<PendientesTarjeta> pendientesResueltos = validarPendientesResueltos(contexto, datosPaciente);
		
		//Ponemos este paciente le�do como el actual en atenci�n
		aplicacion.getSesion().setDatosPacienteNuevo(datosPaciente);
		
		return pendientesResueltos;
	}
	
	public static boolean nfcTagPerteneceApersona(String idUsuarioValidar, Tag nfcTag){
		try{
			return getDatosPaciente(nfcTag).persona.id.equals(idUsuarioValidar);
		}catch(Exception e){return false;}
	}
	
	/**
	 * Genera informaci�n de paciente con el tag recibido seg�n la versi�n del contenido.
	 * @param nfcTag tag que contiene la informaci�n a leer
	 * @return Historial del paciente representado en {@link DatosPaciente} 
	 * @throws Exception Si hay error de lectura o la versi�n del metadatos no se reconoce 
	 */
	private static Sesion.DatosPaciente getDatosPaciente(Tag nfcTag) throws Exception{
		String contenido = LeerTextoPlano(nfcTag);
		return getDatosPaciente(contenido);
	}
	
	/**
	 * Genera informaci�n de paciente con el metadatos recibido seg�n la versi�n del metadatos.
	 * @param metadatosNfc Informaci�n que debe cumplir con el metadatos establecido
	 * @return Historial del paciente representado en {@link DatosPaciente}
	 * @throws Exception Si hay error de lectura o la versi�n del metadatos no se reconoce
	 */
	private static Sesion.DatosPaciente getDatosPaciente(String metadatosNfc) throws Exception{
		String[] bloques = separar(metadatosNfc, SEPARADOR_BLOQUE);
		String bloqueComun = bloques[0];
		String bloqueIsech = bloques[1];
		String bloqueIcss = bloques[2];
		String[] piezasComun = separar(bloqueComun, SEPARADOR_TABLA);
				
		Sesion.DatosPaciente datosPaciente=null;
		String version = piezasComun[0];
		if(version.equals(VERSION_1)){
			datosPaciente = LeerVersion1(piezasComun, bloqueIsech, bloqueIcss, version);
		} //else if(version.equals(VERSION_X)){}
		else{
			throw new Exception("Versi�n de datos no reconocida");
		}
		
		return datosPaciente;
	}
	
	/**
	 * Parsea las piezas de elementos recibidos en tarjeta de acuerdo
	 * a esta versi�n.
	 * @param piezasComun Cadenas que representan tablas/elementos de acuerdo
	 * modelo de metadatos descrito en los comentarios al fondo de este archivo
	 * @param bloqueIsech Bloque de texto interpretable correspondiente al Isech
	 * @param bloqueIcss Bloque de texto interpretable correspondiente al Icss
	 * @param version No se usa actualmente, pero si en el futuro hubiera una  versi�n
	 * nueva cuya diferencia al m�todo de parseo de �sta es de unos pocos detalles,
	 * en pocos puntos del c�digo, esta funci�n puede recodificarse agregando 
	 * las funcionalidades correspondientes usando <b>version</b> para discriminar.
	 * Sin embargo si los cambios de parseo/comportamiento entre una versi�n y otra
	 * fueran muy amplios, ser�a mejor escribir una funci�n nueva que haga
	 * sus propias implementaciones y agregar un "else if()" para llamar dicha
	 * funci�n nueva desde LeerDatosNFC()
	 * @return Una instancia de {@link DatosPaciente} con los datos le�dos del paciente
	 */
	private static Sesion.DatosPaciente LeerVersion1(String[] piezasComun, String bloqueIsech, String bloqueIcss, String version){
		String[] datosPersona = separar(piezasComun[1], SEPARADOR_CAMPO);
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
		persona.tamiz_neonatal = Integer.parseInt(datosPersona[n++]);
		
		String[] datosTutor = separar(piezasComun[2], SEPARADOR_CAMPO);
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
		
		RegistroCivil registro = null;
		if(!piezasComun[3].equals("")){
			String[] datosRegistro = separar(piezasComun[3], SEPARADOR_CAMPO);
			registro = new RegistroCivil();
			n=0;
			registro.id_persona = persona.id;
			registro.id_localidad_registro_civil = Integer.parseInt(datosRegistro[n++]);
			registro.fecha_registro = datosRegistro[n++];
		}
		
		String[] listaAlergias = piezasComun[4].equals("")? new String[]{} : separar(piezasComun[4], SEPARADOR_REGISTRO);
		List<PersonaAlergia> alergias = new ArrayList<PersonaAlergia>();
		for(String regAlergia : listaAlergias){
			String[] datosAlergia = separar(regAlergia, SEPARADOR_CAMPO);
			PersonaAlergia alergia = new PersonaAlergia();
			alergia.id_persona = persona.id;
			alergia.id_alergia = Integer.parseInt(datosAlergia[0]);
			alergias.add(alergia);
		}
		
		String[] listaAfiliaciones = piezasComun[5].equals("")? new String[]{} : separar(piezasComun[5], SEPARADOR_REGISTRO);
		List<PersonaAfiliacion> afiliaciones = new ArrayList<PersonaAfiliacion>();
		for(String regAfiliacion : listaAfiliaciones){
			String[] datosAfiliacion = separar(regAfiliacion, SEPARADOR_CAMPO);
			PersonaAfiliacion afiliacion = new PersonaAfiliacion();
			afiliacion.id_persona = persona.id;
			afiliacion.id_afiliacion = Integer.parseInt(datosAfiliacion[0]);
			afiliaciones.add(afiliacion);
		}
		
		String[] listaVacunas = piezasComun[6].equals("")? new String[]{} : separar(piezasComun[6], SEPARADOR_REGISTRO);
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
		
		/*String[] listaIras = piezas[7].equals("")? new String[]{} : separar(piezas[7], SEPARADOR_REGISTRO);
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
		}*/
		
		String[] piezasIsech = separar(bloqueIsech, SEPARADOR_TABLA);
		
		String[] listaConsultas = piezasIsech[0].equals("")? new String[]{} : separar(piezasIsech[0], SEPARADOR_REGISTRO);
		List<ControlConsulta> consultas = new ArrayList<ControlConsulta>();
		for(String regConsulta : listaConsultas){
			String[] datosConsulta = separar(regConsulta, SEPARADOR_CAMPO);
			ControlConsulta consulta = new ControlConsulta();
			consulta.id_persona = persona.id;
			consulta.clave_cie10 = datosConsulta[0];
			consulta.fecha = datosConsulta[1];
			consulta.id_asu_um = Integer.parseInt(datosConsulta[2]);
			consulta.id_tratamiento = datosConsulta[3];//Integer.parseInt(datosConsulta[3]);
			consulta.grupo_fecha_secuencial = datosConsulta[4];
			consultas.add(consulta);
		}
		
		String[] listaAcciones = piezasIsech[1].equals("")? new String[]{} : separar(piezasIsech[1], SEPARADOR_REGISTRO);
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
		
		String[] listaControles = piezasIsech[2].equals("")? new String[]{} : separar(piezasIsech[2], SEPARADOR_REGISTRO);
		List<ControlNutricional> controles = new ArrayList<ControlNutricional>();
		for(String regControl : listaControles){
			String[] datosControl = separar(regControl, SEPARADOR_CAMPO);
			ControlNutricional control = new ControlNutricional();
			control.id_persona = persona.id;
			control.peso = Double.parseDouble(datosControl[0]);
			control.altura = Integer.parseInt(datosControl[1]);
			control.talla = Integer.parseInt(datosControl[2]);
			control.hemoglobina = datosControl[3].equals("") ? 0 : Double.parseDouble(datosControl[3]); //convertirInt(datosControl[3]);
			control.fecha = datosControl[4];
			control.id_asu_um = Integer.parseInt(datosControl[5]);
			controles.add(control);
		}
		
		String[] listaPerimetro = piezasIsech[3].equals("")? new String[]{} : separar(piezasIsech[3], SEPARADOR_REGISTRO);
		List<ControlPerimetroCefalico> perimetros = new ArrayList<ControlPerimetroCefalico>();
		for(String regPerimetro : listaPerimetro){
			String[] datosPerimetro = separar(regPerimetro, SEPARADOR_CAMPO);
			ControlPerimetroCefalico perimetro = new ControlPerimetroCefalico();
			perimetro.id_persona = persona.id;
			perimetro.perimetro_cefalico = Double.parseDouble(datosPerimetro[0]);
			perimetro.fecha = datosPerimetro[1];
			perimetro.id_asu_um = Integer.parseInt(datosPerimetro[2]);
			perimetros.add(perimetro);
		}
		
		String[] listaSales = piezasIsech[4].equals("")? new String[]{} : separar(piezasIsech[4], SEPARADOR_REGISTRO);
		List<SalesRehidratacion> sales = new ArrayList<SalesRehidratacion>();
		for(String regSal : listaSales){
			String[] datosSal = separar(regSal, SEPARADOR_CAMPO);
			SalesRehidratacion sal = new SalesRehidratacion();
			sal.id_persona = persona.id;
			sal.cantidad = Integer.parseInt(datosSal[0]);
			sal.fecha = datosSal[1];
			sal.id_asu_um = Integer.parseInt(datosSal[2]);
			sales.add(sal);
		}
		
		String[] listaEstimulaciones = piezasIsech[5].equals("")? new String[]{} : separar(piezasIsech[5], SEPARADOR_REGISTRO);
		List<EstimulacionTemprana> estimulaciones = new ArrayList<EstimulacionTemprana>();
		for(String regEstimulacion : listaEstimulaciones){
			String[] datosEstimulacion = separar(regEstimulacion, SEPARADOR_CAMPO);
			EstimulacionTemprana estimulacion = new EstimulacionTemprana();
			estimulacion.id_persona = persona.id;
			estimulacion.tutor_capacitado = Integer.parseInt(datosEstimulacion[0]);
			estimulacion.fecha = datosEstimulacion[1];
			estimulacion.id_asu_um = Integer.parseInt(datosEstimulacion[2]);
			estimulaciones.add(estimulacion);
		}

		//El espacio correspondiente a iras/edas se manda nulo
		return new Sesion.DatosPaciente(persona, tutor, registro, alergias, afiliaciones, vacunas, null, null, 
				consultas, acciones, controles, perimetros, sales, estimulaciones, bloqueIcss, true);
	}//fin LeerVersion1
	
	/**
	 * Escribe los datos del paciente en el lector NFC nativo de Android.
	 * @param nfcTag Tag de NFC compatible con est�ndar NDEF
	 * @param datos Historial del paciente a escribir en nfcTag
	 * @throws Exception 
	 */
	public static void EscribirDatosNFC(Tag nfcTag, Sesion.DatosPaciente datos) throws Exception{
		String salida = getFormatoNfc(datos);
		EscribirTextoPlano(nfcTag, salida);
	}
	
	/**
	 * Convierte los datos del paciente en String con metadatos adecuado para guardarse en dispositivos NFC.
	 * En caso de que la versi�n de base de datos cambie, se deber� cambiar esta funci�n
	 * al menos en la primera l�nea que imprime VERSION_1 y lo que sea necesario despu�s.
	 * As� mismo un cambio de versi�n requerir� cambios en la funci�n de lectura.
	 * @param datos Historial del paciente a convertir en metadatos para NFC
	 * @return String con el formato del metadatos definido para ser escrito en los dispositivos NFC.
	 */
	private static String getFormatoNfc(Sesion.DatosPaciente datos){
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
		salida.append(convertirInt(datos.persona.id_operadora_celular) + SEPARADOR_CAMPO);
		salida.append(datos.persona.tamiz_neonatal + SEPARADOR_TABLA);
		
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
		if(datos.registroCivil != null){
			salida.append(datos.registroCivil.id_localidad_registro_civil + SEPARADOR_CAMPO);
			salida.append(datos.registroCivil.fecha_registro + SEPARADOR_TABLA);
		}else{
			salida.append(SEPARADOR_TABLA);
		}
		
		//Datos de alergias
		for(int i=0;i<datos.alergias.size();i++){
			salida.append(datos.alergias.get(i).id_alergia);
			if(i!=datos.alergias.size()-1)salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos afiliaci�n
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
		
		//salida.append(SEPARADOR_TABLA);
		salida.append(SEPARADOR_BLOQUE); //en lugar de separador tabla
		
		/*
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
		salida.append(SEPARADOR_TABLA);*/
		
		//Datos consultas
		for (int i = 0; i < datos.consultas.size(); i++) {
			salida.append(datos.consultas.get(i).clave_cie10 + SEPARADOR_CAMPO);
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
			salida.append(datos.controlesNutricionales.get(i).hemoglobina + SEPARADOR_CAMPO);
			salida.append(datos.controlesNutricionales.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.controlesNutricionales.get(i).id_asu_um);
			if (i != datos.controlesNutricionales.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);
		
		//Datos perimetro cef�lico
		for (int i = 0; i < datos.perimetrosCefalicos.size(); i++) {
			salida.append(datos.perimetrosCefalicos.get(i).perimetro_cefalico + SEPARADOR_CAMPO);
			salida.append(datos.perimetrosCefalicos.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.perimetrosCefalicos.get(i).id_asu_um);
			if (i != datos.perimetrosCefalicos.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);

		//Datos sales rehidrataci�n
		for (int i = 0; i < datos.salesRehidratacion.size(); i++) {
			salida.append(datos.salesRehidratacion.get(i).cantidad + SEPARADOR_CAMPO);
			salida.append(datos.salesRehidratacion.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.salesRehidratacion.get(i).id_asu_um);
			if (i != datos.salesRehidratacion.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		salida.append(SEPARADOR_TABLA);

		//Datos estimulaciones tempranas
		for (int i = 0; i < datos.estimulacionesTempranas.size(); i++) {
			salida.append(datos.estimulacionesTempranas.get(i).tutor_capacitado + SEPARADOR_CAMPO);
			salida.append(datos.estimulacionesTempranas.get(i).fecha + SEPARADOR_CAMPO);
			salida.append(datos.estimulacionesTempranas.get(i).id_asu_um);
			if (i != datos.estimulacionesTempranas.size() - 1)
				salida.append(SEPARADOR_REGISTRO);
		}
		
		salida.append(SEPARADOR_BLOQUE + datos.datosNfcIcss);
		
		return salida.toString();
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
		if(salida.size()>0) //si encontr� al menos uno, puede pasar una de dos ...
			if(cadenaSeparar.endsWith(simbolo)) //... acaba con simbolo, y por tanto a la derecha hay vac�o
				salida.add(""); //... lo remarcamos as�
			else
				salida.add(cadenaSeparar.substring(indexInicioCopia));  //... recuperamos el �ltimo pedazo que no captur�
		if(salida.size()==0)return new String[]{cadenaSeparar};
		return salida.toArray(new String[]{});
	}
	
	//HELPERS PARA PENDIENTES
	/**
	 * Verifica los pendientes por resolver para el paciente definido en <b>datosPaciente</b>
	 * @param contexto
	 * @param datosPaciente Historial que contiene al paciente que se le revisar� sus pendientes
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
					}else if(pendiente.tabla.equals(ControlPerimetroCefalico.NOMBRE_TABLA)){
						ControlPerimetroCefalico nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlPerimetroCefalico.class);
						if(!InsertarEnLista(nuevo, datosPaciente.perimetrosCefalicos))
							continue;
					}else if(pendiente.tabla.equals(SalesRehidratacion.NOMBRE_TABLA)){
						SalesRehidratacion nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, SalesRehidratacion.class);
						if(!InsertarEnLista(nuevo, datosPaciente.salesRehidratacion))
							continue;
					}else if(pendiente.tabla.equals(EstimulacionTemprana.NOMBRE_TABLA)){
						EstimulacionTemprana nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, EstimulacionTemprana.class);
						if(!InsertarEnLista(nuevo, datosPaciente.estimulacionesTempranas))
							continue;
					/*}else if(pendiente.tabla.equals(ControlIra.NOMBRE_TABLA)){
						ControlIra nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlIra.class);
						if(!InsertarEnLista(nuevo, datosPaciente.iras))
							continue;
					}else if(pendiente.tabla.equals(ControlEda.NOMBRE_TABLA)){
						ControlEda nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, ControlEda.class);
						if(!InsertarEnLista(nuevo, datosPaciente.edas))
							continue;*/
						
						//ESTOS ELEMENTOS SE AGREGAN DIRECTO PUES NO HAY ORDEN ESPEC�FICO REQUERIDO
					}else if(pendiente.tabla.equals(PersonaAlergia.NOMBRE_TABLA)){
						PersonaAlergia nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, PersonaAlergia.class);
						if(datosPaciente.alergias.contains(nuevo))continue;
						else datosPaciente.alergias.add(nuevo);
					}else if(pendiente.tabla.equals(PersonaAfiliacion.NOMBRE_TABLA)){
						PersonaAfiliacion nuevo = DatosUtil.ObjetoDesdeJson(pendiente.registro_json, PersonaAfiliacion.class);
						if(datosPaciente.afiliaciones.contains(nuevo))continue;
						else datosPaciente.afiliaciones.add(nuevo);
						//PENDIENTES DE ASIGNACI�N DIRECTA (SIN LISTA)
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
						continue; //ESTO NUNCA DEBER�A SUCEDER a menos que se tratara de un tipo de pendiente muy nuevo en app vieja 
					}
					pendientesResueltos.add(pendiente);
					//PendientesTarjeta.MarcarPendienteResuelto(contexto, pendiente);
					
				}catch(JsonSyntaxException jse){
					int idUsuario=((TesAplicacion)contexto.getApplicationContext()).getSesion().getUsuario()._id;
					ErrorSis.AgregarError(contexto, idUsuario, ErrorSis.ERROR_DESCONOCIDO, "Json incorrecto en pendiente de persona:"+
							pendiente.id_persona+", fecha:"+pendiente.fecha+", tabla:"+pendiente.tabla);
				}
				
			}//fin ciclo pendientes
			
			//Esta l�nea no se puede ejecutar pues la tarjeta fue le�da previamente y hay que pasarla de nuevo
			//f�sicamente antes de poder escribir. En consecuencia tampoco se puede marcar como resuelto su pendiente a�n
			//EscribirDatosNFC(nfcTag, datosPaciente);
		}//fin si hay pendientes
		return pendientesResueltos;
	}
	
	/**
	 * Inserta objeto en lista en posici�n ordenada de menor a mayor.
	 * Si objeto tiene una fecha menor que otros elementos de lista, se insertar� en medio
	 * NOTA: Esta implementaci�n improvisada puede cambiarse por una llamada a 
	 * Collections.sort(lista, new InstanciaComparador() ) donde InstanciaComparador es
	 * una clase que implementa Comparator<T>{Compare();}
	 * @param objeto Un control de paciente a validar para inserci�n
	 * @param lista La lista donde se insertar� <b>objeto</b> en caso de cumplir la validaci�n
	 * @return true si <b>objeto</b> fue insertado en <b>lista</b> y false de lo contrario
	 */
	private static <T> boolean InsertarEnLista(T objeto, List<T> lista){
		if(lista.contains(objeto))return false; //pues no es necesario agregarlo m�s veces
		
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
			}else if(objeto instanceof ControlPerimetroCefalico){
				if(esFechaHoraMenor( ((ControlPerimetroCefalico) objeto).fecha, 
						((ControlPerimetroCefalico)lista.get(indice)).fecha) )
					break;
			}else if(objeto instanceof SalesRehidratacion){
				if(esFechaHoraMenor( ((SalesRehidratacion) objeto).fecha, 
						((SalesRehidratacion)lista.get(indice)).fecha) )
					break;
			}else if(objeto instanceof EstimulacionTemprana){
				if(esFechaHoraMenor( ((EstimulacionTemprana) objeto).fecha, 
						((EstimulacionTemprana)lista.get(indice)).fecha) )
					break;
			/*}else if(objeto instanceof ControlIra){
				if(esFechaHoraMenor( ((ControlIra) objeto).fecha, 
						((ControlIra)lista.get(indice)).fecha) )
					break;
			}else if(objeto instanceof ControlEda){
				if(esFechaHoraMenor( ((ControlEda) objeto).fecha, 
						((ControlEda)lista.get(indice)).fecha) )
					break;*/
			}
		}
		//Inserta objeto en la posici�n adecuada seg�n su fecha
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
	 * @param nfcTag Proveedor de los datos
	 * @return Cadena de texto contenida en <b>nfcTag</b>
	 * @throws Exception Cuando sucede alg�n error
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
	 * @param nfcTag Tag destino para escribir <b>texto</b>
	 * @param texto Contenido a escribir en <b>nfcTag</b>
	 * @throws Exception Cuando sucede alg�n error
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
				throw new Exception("No hay suficiente espacio en tarjeta para guardar informaci�n");

			ndef.writeNdefMessage(mensaje);
			ndef.close();
		} else {
			// Intentamos formatear tarjeta
			//Nunca deber�a ocurrir pues escribimos en tarjetas previamente le�das correctamente
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
	 * @return {@link NdefRecord} est�ndar que contiene informaci�n de <b>texto</b>
	 */
	private static NdefRecord crearRecordNfc(String texto) {
		byte[] bytesTexto  = texto.getBytes();
		byte[] bytesIdioma=null;
		try {
			bytesIdioma = "en".getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// Nunca deber�a pasar
		}
		byte[] payload    = new byte[1 + bytesIdioma.length + bytesTexto.length];

		// set status byte (see NDEF spec for actual bits)
		payload[0] = (byte) bytesIdioma.length;

		// copia bytesIdioma y bytesTexto a payload
		System.arraycopy(bytesIdioma,  0, payload, 1                     , bytesIdioma.length);
		System.arraycopy(bytesTexto, 0,   payload, 1 + bytesIdioma.length, bytesTexto.length);

		return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
	}
	
	/**
	 * VERSI�N 1 de metadatos ----------------
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
	
	/**
	 * Lector de dispositivo USB con capacidad de leer y escribir en tags NFC Mifare Classic 1k y 4k
	 * @author Axel
	 *
	 */
	public static class LectorUsb{ 
		private static final int VELOCIDAD_SERIAL = 115200;
		private static final String ACCESS_KEY = "D3F7D3F7D3F7";
		
		private static final String NO_HAY_TAG_NFC = "E#@50_1"+new String(new byte[]{13}); //Regresado por lector al intentar identificar tarjeta y no encontrarla
		private static final String DELIMITADOR_TEXTO_SIN_NDEF = "}"; //Para identificar cuando se ha le�do todo el texto al no ser Ndef
		private static final String FIN_COMANDO = "\n"; //Indica al lector USB que el comando est� listo (al enviar datos). Usado para determinar que el lector ha terminado de devolver datos (al recibir)
		private static final byte FIN_TLV = (byte)0xFE; //S�mbolo de fin de mensaje Ndef
		//private static final String FIN_TLV_str = new String(new byte[]{FIN_TLV});
		private static final String ISO_8859_1 = "ISO-8859-1"; //Usado para conversi�n byte[]/String sin mal formar datos
		private static final String UTF_8 = "UTF-8"; //Para convertir string ISO a UTF
		
		private static final int BYTES_ALMACENAMIENTO_1K = 752; //Para Mifare Classic 1k
		private static final int BYTES_ALMACENAMIENTO_4K = 3440; //Para Mifare Classic 4k
		private static final int ULTIMO_SECTOR_4K = 39; //�ltimo sector usable 0-39
		private static final int BYTES_POR_BLOQUE = 16; //Los bloques de tarjetas 1k, 4k siempre tienen 16 bytes
		
		//Comandos disponibles para enviar a la lectora
		private static final String COMANDO_IDENTIFICAR_TARJETA = "getcard";
		private static final String COMANDO_LEER_TARJETA = "readcard";
		private static final String COMANDO_ESCRIBIR_TARJETA = "writecard";
		
		//Variables de estado general
		private String comandoActual = null; //identifica el comando m�s reciente enviado al lector usb 
		private String bufferMensaje = new String(new byte[0], ISO_8859_1); //Acumula datos recibidos por USB en recibirDatos()
		private String idTarjetaEnLectura = null; //Indica la tarjeta que actualmente se est� leyendo
		private String idTarjetaEnEscritura = null; //Indica la tarjeta que actualmente se est� escribiendo
		private byte[] bytesEscribir = null; //Guarda el mensaje que se pretende enviar desde EscribirTarjeta()
		private int numBytesEscritos = 0; //Indica cuantos bytes de bytesEscribir se han escrito
		
		//Variables de estado al avanzar en bloques de tarjeta
		private int sectorActual = 1; //Siempre a partir de 1. Los sectores posibles cambian seg�n tarjetas 1k, 4k
		private int bloqueInicial = 4; //Siempre a partir de 4. Los bloques posibles cambian seg�n tarjetas 1k, 4k
		private int bloqueFinal = 6; //var�a seg�n bloqueInicial y sectorActual y tarjetas 1k, 4k
		private int bloquesUsablesSector; //Seg�n el sector este dato puede variar entre 3 y 15
		private int bloquesLeidosEscritos; //Indica cuantos bloques han sido le�dos o escritos de sectorActual
		
		//Variables de estado al usar Ndef
		private boolean tagNfcActualUsaNdef = false;
		private int numBytesTagNdefActual = -1; //si usamos un tag con Ndef esta variable guarda el tama�o del mensaje
		private int numBytesLeidosNdef = 0; 
		
		private Activity miActivity = null;
		
		//Variables para comunicaci�n USB
		private UsbManager manager;
		private UsbSerialDriver sDriver = null;
		private SerialInputOutputManager mSerialIoManager;
		private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
	    
		/**
		 * Eventos de naturaleza callback para reportar fin de eventos de lectura/escritura/identificaci�n de usb
		 * @author Axel
		 *
		 */
		public interface EventosLector{
			/**
			 * Sucede cuando el lector termina de identificar una tarjeta.
			 * @param lector Lector que identific� la tarjeta
			 * @param idTarjeta Identificador de la tarjeta � <b>null</b> si no se detect� una.
			 */
			public void onGetIdTarjeta(LectorUsb lector, String idTarjeta);
			public void onLeerTarjeta(LectorUsb lector, List<PendientesTarjeta> pendientes);
			public void onEscribirTarjeta(LectorUsb lector);
		}
		
		private EventosLector misEventos;
		
		//Listener de eventos IO en serial
		private final SerialInputOutputManager.Listener mListener =
			new SerialInputOutputManager.Listener() {
			    @Override
			    public void onRunError(Exception e) {
			    	msg("Error de SerialManager:"+e, false);
			    }
			
			    @Override
			    public void onNewData(final byte[] data) {
			    	try {
						recibirDatos(data);
					} catch (UnsupportedEncodingException e) {
						msg("No fue posible convertir datos a "+ISO_8859_1, false);
					}
			    }
			};
		
		
		/**
		 * Inicia un lector USB con capacidad de leer/escribir en tarjetas Mifare Classic de 1k y 4k
		 * @param contenedor Actividad que contiene directa o indirectamente esta instancia de lector
		 * @param eventos Eventos a usar para reportar resultados de lectura/escritura
		 * @throws IOException Si ocurre un error al iniciar la comunicaci�n con dispositivo usb
		 */
		public LectorUsb(Activity contenedor, EventosLector eventos) throws IOException{
			misEventos = eventos;
			miActivity = contenedor;
			
			manager = (UsbManager) contenedor.getSystemService(Context.USB_SERVICE);

			// Buscar primer driver disponible
			sDriver = UsbSerialProber.acquire(manager);
			sDriver.open();
			sDriver.setBaudRate(VELOCIDAD_SERIAL);
		}
		
		/**
		 * Bandera consultable para saber si existe alg�n comando en ejecuci�n de forma que no se intente llamar otro
		 * @return
		 */
		public boolean hayComandoEnEjecucion(){return comandoActual != null;}
		
		/**
		 * Pide al lector usb identificar una tarjeta NFC. El resultado de esta llamada debe recibirse
		 * en el listener onGetIdTarjeta() del callback declarado en el constructor de esta clase
		 * @throws IOException 
		 */
		public void IdentificarTarjeta() throws IOException{
			EnviarComando(COMANDO_IDENTIFICAR_TARJETA);
		}
		
		/**
		 * Pide al lecctor usb leer el contenido de la tarjeta NFC con <b>idTarjeta</b>.
		 * El resultado de esta llamada debe recibirse en el listener onLeerTarjeta() del callback declarado
		 * en el constructor de esta clase
		 * @param idTarjeta Id de la tarjeta NFC a leer obtenido previamente al llamar a IdentificarTarjeta()
		 * @throws IOException 
		 */
		public void LeerTarjeta(String idTarjeta) throws IOException{
			reiniciarBloques();
			idTarjetaEnLectura = idTarjeta;
			EnviarComando(COMANDO_LEER_TARJETA + " " + idTarjeta + " " + ACCESS_KEY + " " + bloqueInicial + " " + bloqueFinal);
		}
		
		/**
		 * Pide al lector usb escribir el historial en la tarjeta NFC con <b>idTarjeta</b>.
		 * El resultado de esta llamada debe recibirse en el listener onEscribirTarjeta() del callback declarado
		 * en el constructor de esta clase
		 * @param idTarjeta Id de la tarjeta NFC a escribir obtenido previamente al llamar a IdentificarTarjeta()
		 * @param historial Informaci�n del paciente que se escribir� en la tarjeta NFC
		 * @param usarNdefTexto Escribe el historial en formato Ndef tipo Texto (recomendado) o como texto simple (no compatible con implementaci�n de {@link ManejadorNfc})
		 * @throws IOException 
		 */
		public void EscribirTarjeta(String idTarjeta, DatosPaciente historial, boolean usarNdefTexto) throws IOException{
			String salida = getFormatoNfc(historial);
			byte[] datos;
			if(usarNdefTexto){
				datos = EncapsularNdefTexto(salida);
			}else{
				salida += DELIMITADOR_TEXTO_SIN_NDEF;				
				datos = salida.getBytes();
			}
			//Debido a que no hay forma de saber si usamos 1k o 4k, asumimos 4k.
			if(datos.length > BYTES_ALMACENAMIENTO_4K)
				throw new IOException("Los datos a guardar ("+datos.length+" bytes) superan los "+BYTES_ALMACENAMIENTO_4K+" bytes");

			//Preparamos variables generales para iniciar escritura
			reiniciarBloques();
			idTarjetaEnEscritura = idTarjeta; 
			bytesEscribir = datos;
			numBytesEscritos = 0;
			EscribirBytesRestantes();
			/*int totalBytesEscritos = 0;
			while(totalBytesEscritos < datos.length){				
				EnviarComando(COMANDO_ESCRIBIR_TARJETA + " " + idTarjeta + " " + ACCESS_KEY + " " + bloqueInicial + " " + bloqueFinal);
				//Ahora el lector est� esperando tanta informaci�n como se especific� en (bloqueFinal-bloqueInicial)
				int numBytesEscribir = (1 + bloqueFinal - bloqueInicial) * BYTES_POR_BLOQUE;
				byte[] buffer = new byte[numBytesEscribir];
				int numBytesRestantes = datos.length - totalBytesEscritos;
				int numBytesCopiar = numBytesRestantes > buffer.length ? buffer.length : numBytesRestantes;
				System.arraycopy(datos, totalBytesEscritos, buffer, 0, numBytesCopiar);
				
				EnviarDatos(buffer);
				totalBytesEscritos += numBytesEscribir;
				//Generalmente la �ltima llamada a escribir de este ciclo escribir� m�s datos de los requeridos pues
				//usamos un buffer con el tama�o del sector y no del �ltimo pedazo de informaci�n. Se puede ajustar
				//pero dejar bytes vac�os a la derecha de la verdadera informaci�n es bueno.
				
				avanzarBloques(); //Si los datos ocupaban m�s de 1 sector, en pr�ximo ciclo se usar� este
			}*/
		}
		
		private void EscribirBytesRestantes() throws IOException{
			EnviarComando(COMANDO_ESCRIBIR_TARJETA + " " + idTarjetaEnEscritura + " " + ACCESS_KEY + " " + bloqueInicial + " " + bloqueFinal);
			//Ahora el lector est� esperando tanta informaci�n como se especific� en (bloqueFinal-bloqueInicial)
			int numBytesEscribir = (1 + bloqueFinal - bloqueInicial) * BYTES_POR_BLOQUE;
			byte[] buffer = new byte[numBytesEscribir];
			int numBytesRestantes = bytesEscribir.length - numBytesEscritos;
			int numBytesCopiar = numBytesRestantes > buffer.length ? buffer.length : numBytesRestantes;
			System.arraycopy(bytesEscribir, numBytesEscritos, buffer, 0, numBytesCopiar);
			
			//Guardamos el contexto actual del avanzado de bloques por si escritura fallara (solo para logeo)
			int tmpSectorActual = sectorActual, tmpBloquesLeidosEscritos = bloquesLeidosEscritos, 
					tmpBloqueInicial = bloqueInicial, tmpBloqueFinal = bloqueFinal, 
					tmpBloquesUsables = bloquesUsablesSector;
			
			//Asignamos variables antes de escribir asumiendo que saldr� bien pues no sabemos si el env�o de datos
			//terminar� antes de asignar estas variables, lo cual ser�a inconveniente
			numBytesEscritos += numBytesEscribir;
			avanzarBloques();
			try{
				EnviarDatos(buffer);
			}catch(IOException e){
				//Restauramos variables para que posibles log muestren el contexto real
				numBytesEscritos -= numBytesEscribir;
				sectorActual = tmpSectorActual; bloquesLeidosEscritos = tmpBloquesLeidosEscritos; 
				bloqueInicial = tmpBloqueInicial; bloqueFinal = tmpBloqueFinal; 
				bloquesUsablesSector = tmpBloquesUsables;
				throw e;
			}
			/**
			 * En una cadena de llamadas a esta funci�n, generalmente la �ltima llamada escribir� m�s datos de 
			 * los requeridos pues usamos un buffer con el tama�o del sector y no del �ltimo pedazo de informaci�n. 
			 * Es necesario pues el lector espera recibir tantos bytes como bloques se declar� en el comando.
			 */
		}
		
		/**
		 * Env�a un comando al lector usb que eventualmente debe generar respuesta en listener hasta recibirDatos()
		 * @param comando Cadena con el comando a enviar al lector usb
		 * @throws IOException 
		 */
		private void EnviarComando(String comando) throws IOException{
			if(sDriver == null){
				msg("El driver USB no est� disponible", false);
				return;
			}
			if(comando.startsWith(COMANDO_LEER_TARJETA) && sectorActual > ULTIMO_SECTOR_4K)
				throw new IOException("Se pide leer en un sector de tarjeta que no existe");
			//msg("mandando comando:'"+comando+"' \ncomactual:"+comandoActual+", driver="+sDriver+" serioalIOmanager="+mSerialIoManager);
			
			comandoActual = comando;
			comando += FIN_COMANDO;

			byte datos[] = comando.getBytes();
			EnviarDatos(datos);
		}

		/**
		 * Escribe datos en el lector USB intentando escribir los datos cuando no se logra con �xito al primer intento.
		 * @param datos Informaci�n a escribir en el lector USB
		 * @throws IOException Cuando <b>datos</b> tiene 0 bytes. Cuando el n�mero de intentos para escribir los datos es sobrepasado.
		 */
		private void EnviarDatos(byte[] datos) throws IOException{
			if(datos.length == 0)
				throw new IOException("Se intent� escribir 0 datos");
			
			//if(datos.length > UsbSerialDriver.DEFAULT_WRITE_BUFFER_SIZE)
			//	sDriver.setWriteBufferSize(datos.length); //Dificilmente pasar�a pues default es 16k
			
			int totalBytesEscritos = sDriver.write(datos, 0);
			if(totalBytesEscritos == datos.length)
				return; //Todo bien
			
			//Es necesario usar t�cnica para escribir los bytes restantes a escribir
			int numIntentos = 0; //Cuenta los intentos que han habido de escribir sin �xito
			while(totalBytesEscritos < datos.length){
				byte[] buffer = new byte[datos.length - totalBytesEscritos];
				System.arraycopy(datos, totalBytesEscritos, buffer, 0, datos.length - totalBytesEscritos);
				int numBytesEscritos = sDriver.write(buffer, 0);
				totalBytesEscritos += numBytesEscritos;
				if(numBytesEscritos <= 0){ //Nada logr�
					numIntentos++;
					if(numIntentos > 3)
						throw new IOException("Por alguna raz�n no es posible enviar datos al lector USB. Puede intentarlo de nuevo.");
				}else numIntentos = 0; //Reiniciamos contador
			}
		}
		
		/**
		 * Define variables de estado para los bloques inicial y final en los cuales se podr� leer/escribir.
		 * Las asignaciones son en relaci�n a la estructura de tarjetas Mifare Classic 1k y Mifare Classic 4k.
		 * Por esa raz�n no se debe usar esta funci�n si se trabaja sobre tarjetas con estructura de memoria distinta.
		 * Esta funci�n tampoco valida si el sector existe o si es correcto en relaci�n al usado anteriormente.
		 */
		private void avanzarBloques(){
			bloquesLeidosEscritos += 3; //Nos piden avanzar porque 3 ya se usaron
			if(bloquesLeidosEscritos >= bloquesUsablesSector){
				//Ya usamos todo lo que el sector ofrec�a, ahora cambiamos
				sectorActual++;
				bloquesLeidosEscritos = 0;
				if(sectorActual < 32){
					bloquesUsablesSector = 3;
					bloqueInicial = 4 * sectorActual;
				}else{
					bloquesUsablesSector = 15;
					bloqueInicial = 128 + (16*(sectorActual-32));
				}
				bloqueFinal = bloqueInicial + 2; //siempre rango de 3 bloques
			}else{
				//Seguimos en el mismo sector as� que solo avanzamos 3 bloques 
				bloqueInicial += 3;
				bloqueFinal += 3;
			}
		}
		
		/**
		 * Define las variables de estado de para los bloques inicial y final en el primer rango de bloques usables
		 */
		private void reiniciarBloques(){
			bloqueInicial = 4; bloqueFinal = 6; sectorActual = 1; bloquesUsablesSector = 3; bloquesLeidosEscritos = 0;
		}
		
		/**
		 * Convierte <b>texto</b> en bytes y lo encapsula en un mensaje Ndef de un solo record tipo Texto que incluye su byte TLV de terminaci�n 0xFE.
		 * https://learn.adafruit.com/adafruit-pn532-rfid-nfc/ndef
		 * @param texto Cadena a convertir en mensaje Ndef
		 * @return texto encapsulado en mensaje Ndef con un record tipo Texto
		 */
		private byte[] EncapsularNdefTexto(String texto){
			byte recordType = (byte)0x54; //igual a UTF8 "T" que indica tipo de record Texto
			//Preparamos los bytes del record (su payload)
			String payload = " en" + texto; //" ":lo cambiaremos adelante, "en": UTF8-EN
			byte[] bytesTexto = payload.getBytes();
			bytesTexto[0] = (byte)0x02; //siempre es as� con Ndef texto
			
			//header del payload (https://learn.adafruit.com/adafruit-pn532-rfid-nfc/ndef#tlv-blocks)
			byte header;
			byte[] payloadLength;
			if(bytesTexto.length <= 255){ 
				header = (byte) 0xD1;
				//payloadLength de 1 byte
				payloadLength = new byte[]{(byte) bytesTexto.length};
			}else{
				header = (byte)0xC1;
				byte[] longitud = BigInteger.valueOf(bytesTexto.length).toByteArray();
				//payloadLength de 4 bytes 
				payloadLength = new byte[4];
				System.arraycopy(longitud, 0, payloadLength, 4 - longitud.length, longitud.length);
			}
			byte typeLength = (byte)0x01; //siempre es as�
			
			// header + typeLength + payloadLength + recordType + bytesTexto
			int numBytesRecord = 2 + payloadLength.length + 1 + bytesTexto.length;
			
			byte[] tlv;
			if(numBytesRecord < 255){
				//2 bytes ignorados, 0x03 = Mensaje Ndef, longitud Record
				tlv = new byte[]{(byte)0x00, (byte)0x00, (byte)0x03, (byte) numBytesRecord}; 
			}else{
				byte[] longitud = BigInteger.valueOf(numBytesRecord).toByteArray();
				// 0x03 = Mensaje Ndef, 0xFF = Usamos formato longitud 3 bytes (0xFF reservado y 2 bytes de longitud Record)
				tlv = new byte[]{(byte)0x03, (byte)255, longitud.length>1? (byte)longitud[0] : 0, longitud.length>1? (byte)longitud[1] : (byte)longitud[0]};
			}
			
			//Ahora ensamblamos todo el mensaje Ndef
			byte[] bytesNdef = new byte[tlv.length + numBytesRecord + 1]; //+1 por terminador TLV 0xFE
			System.arraycopy(tlv, 0, bytesNdef, 0, tlv.length);
			bytesNdef[tlv.length] = header;
			bytesNdef[tlv.length+1] = typeLength;
			System.arraycopy(payloadLength, 0, bytesNdef, tlv.length+2, payloadLength.length);
			bytesNdef[tlv.length+2+payloadLength.length] = recordType;
			System.arraycopy(bytesTexto, 0, bytesNdef, tlv.length+2+payloadLength.length+1, bytesTexto.length);
			bytesNdef[bytesNdef.length-1] = FIN_TLV;
			
			return bytesNdef;
		}
		
		/**
		 * Determina si el bloque de bytes recibido son el comienzo de un mensaje Ndef con un record de Texto
		 * https://learn.adafruit.com/adafruit-pn532-rfid-nfc/ndef#tlv-blocks
		 * @param primerBloque Primeros bytes de una lectura de tag NFC
		 * @return true Si el bloque representa un mensaje Ndef de tipo texto, false caso contrario
		 */
		private boolean existeNdefTexto(byte[] primerBloque){
			if(primerBloque[0] == (byte)0 && primerBloque[1] == (byte)0 && primerBloque[2] == (byte)0x03)
				if(primerBloque[4] == (byte)0xD1)
					return true;
			if(primerBloque[0] == (byte)0x03 && primerBloque[1] == (byte)0xFF )
				if(primerBloque[4] == (byte)0xD1 || primerBloque[4] == (byte)0xC1)
					return true;
			return false;
		}
		
		/**
		 * Calcula el n�mero de bytes que ocupa un mensaje Ndef basado en <b>inicioMensajeNdef</b>
		 * 
		 * El n�mero de bytes NO incluye el byte de fin de mensaje Ndef 0xFE el cual a veces puede
		 * estar presente o no en un tag NFC dependiendo de si el mensaje Ndef ocupa bloques completos
		 * � si el �ltimo "chunk" de datos no ocupa por completo el �ltimo bloque en que escribe.
		 * Ver https://learn.adafruit.com/adafruit-pn532-rfid-nfc/ndef#tlv-blocks
		 * @param inicioMensajeNdef Cadena en ISO-8859-1 con el mensaje Ndef. Se espera que tenga al menos 10 bytes de datos para poder determinar el n�mero de bytes que el mensaje Ndef declara
		 * @return n�mero de byetes que ocupa un mensaje Ndef sin contar el byte de fin de mensaje 0xFE
		 * @throws UnsupportedEncodingException 
		 */
		private int getNumBytesNdef(String inicioMensajeNdef) throws UnsupportedEncodingException {
			byte[] datos = inicioMensajeNdef.getBytes(ISO_8859_1);

			if(datos[4] == (byte)0xD1){
				//El tama�o del contenido del record se contiene en 1 byte
				int largoContenido = new BigInteger(1, new byte[]{datos[6]}).intValue(); //(int)datos[6];
				//8 bytes del mensaje Ndef + contenido del record
				return 8 + largoContenido;
			}else{ 
				//se supone que datos[4] = 0xC1 indicando tama�o del contenido del record de 4 bytes
				byte[] bytesContenido = new byte[4];
				bytesContenido[0] = datos[6];
				bytesContenido[1] = datos[7];
				bytesContenido[2] = datos[8];
				bytesContenido[3] = datos[9];
				int largoContenido = new BigInteger(1, bytesContenido).intValue();
				//11 bytes del mensaje Ndef + contenido del record
				return 11 + largoContenido;
			}
		}
		
		/**
		 * Recibe lo que se asume es la cadena de bytes de un mensaje Ndef con un record tipo Texto codificado con UTF8.
		 * En consecuencia regresa la cadena de texto contenida en el record tipo Texto.
		 * Se asume tambi�n que el mensaje Ndef <b>NO</b> contiene el byte de terminaci�n TLV <b>0xFE</b> al final de la cadena de bytes.
		 * Ver https://learn.adafruit.com/adafruit-pn532-rfid-nfc/ndef#tlv-blocks
		 * @param datos Bytes de un mensaje Ndef con un solo record tipo texto codificado en UTF8
		 * @return Cadena de texto contenida en el record del mensaje Ndef recibido.
		 */
		private String LeerNdefTexto(byte[] datos){
			int localidadInicial;
			if(datos[4] == (byte)0xD1){
				//En este mensaje Ndef tipo Texto codificado en UTF8-EN el contenido a leer est� a partir 
				//del byte 12 (localidad 11)
				localidadInicial = 11;
			}else{ 
				//En este mensaje Ndef tipo Texto codificado en UTF8-EN el contenido a leer est� a partir
				//del byte 15 (localidad 14)
				localidadInicial = 14;
			}
			//En teor�a pedimos que datos no tenga fin de tlv pero lo checamos por si acaso
			int byteFinTlv = datos[datos.length-1] == FIN_TLV ? 1 : 0;
			return new String(datos, localidadInicial, datos.length-localidadInicial-byteFinTlv);
		}
	
		
		/**
		 * Llamado por el listener de IO serial al recibir una porci�n de respuesta por ejecutar un comando
		 * @param datos Porci�n de datos regresado por el lector USB
		 * @throws UnsupportedEncodingException No deber�a suceder pues se usa ISO-8859-1 y UTF-8
		 */
		private void recibirDatos(byte[] datos) throws UnsupportedEncodingException{
			
			//Validamos si estamos en modo lectura Y recibiendo el primer bloque de datos de la lectura
			if(comandoActual.startsWith(COMANDO_LEER_TARJETA) && bufferMensaje.isEmpty()){
				tagNfcActualUsaNdef = existeNdefTexto(datos); //Funciona porque asumimos que siempre recibiremos al menos 5 bytes en primera lectura
				//Reiniciamos variables de estado para uso de Ndef
				numBytesTagNdefActual = -1;
				numBytesLeidosNdef = 0;
			}
			
			String message = new String(datos, ISO_8859_1);
			bufferMensaje += message; //acumulamos respuesta
			
			if(tagNfcActualUsaNdef && comandoActual.startsWith(COMANDO_LEER_TARJETA))
				numBytesLeidosNdef += datos.length; //datos puede incluir byte de terminaci�n de comando as� que ese se resta abajo
			
			//Si leemos un Ndef cuyo # de bytes no ha sido determinado a�n Y ya hemos le�do suficientes bytes para saberlo
			if(comandoActual.startsWith(COMANDO_LEER_TARJETA) && tagNfcActualUsaNdef 
					&& numBytesTagNdefActual == -1 && bufferMensaje.getBytes(ISO_8859_1).length >= 10 )
				numBytesTagNdefActual = getNumBytesNdef(bufferMensaje);
		    
			
			boolean recepcionDatosTerminada = false;
			if(message.indexOf(FIN_COMANDO) >= 0){
				//Estamos suponiendo que sin importar lo que est� escrito en la tarjeta, FIN_COMANDO siempre 
				//vendr� anexado al final de la cadena regresada de datos 
				//incluso si en caso de READCARD se leyeron menos bloques que los necesarios para obtener el texto guardado
				//De lo contrario la total implementaci�n de esta zona de c�digo est� mal.
				recepcionDatosTerminada = true;
				bufferMensaje = bufferMensaje.substring(0, (bufferMensaje.length() - FIN_COMANDO.length() ));
				
				//Si al terminar de recibir datos ejecutabamos lectura Y el contenido es un mensaje Ndef
				if(tagNfcActualUsaNdef && comandoActual.startsWith(COMANDO_LEER_TARJETA) && numBytesTagNdefActual != -1 ){
					numBytesLeidosNdef -= FIN_COMANDO.getBytes().length; //Debe ser igual que bufferMensaje.getBytes(ISO).length
					//La lectura de bloques pudo haber terminado pero debemos ver si se ley� el Ndef completo
					if(numBytesTagNdefActual-numBytesLeidosNdef<=0){
						//Ya est� le�do el Ndef. Rehacemos buffer para que guarde SOLO el mensaje Ndef (sin TLV final)
						byte[] tmp = bufferMensaje.getBytes(ISO_8859_1);
						byte[] bytesNdef = new byte[numBytesTagNdefActual];
						System.arraycopy(tmp, 0, bytesNdef, 0, bytesNdef.length);
						bufferMensaje = new String(bytesNdef, ISO_8859_1);
						//El c�digo seguir� en validaci�n de fin de recepci�n de datos abajo
					}else{
						//Lectura de bloques termin�, pero no as� lectura del mensaje
						//Tenemos que ejecutar nuevamente un comando lectura en  los siguientes bloques a�n no le�dos
						avanzarBloques();
						try {
							EnviarComando(COMANDO_LEER_TARJETA + " " + idTarjetaEnLectura + " " + ACCESS_KEY + " " + bloqueInicial + " " + bloqueFinal);
						} catch (IOException e) {
							msg("Al continuar con lectura Ndef en sector:" + sectorActual+", bloqueInicial:"+bloqueInicial+", bloqueFinal:"+bloqueFinal+"\nSucedi�:"+e, false);
						}
						//Necesitamos que bufferMensaje se mantenga para leer el pr�ximo pedazo de bloques ...
						return; //...As� el c�digo de verificaci�n de fin abajo no ejecutar� pues la lectura en realidad contin�a
					}
				}else if(!tagNfcActualUsaNdef && comandoActual.startsWith(COMANDO_LEER_TARJETA)){
					//La lectura de bloques pudo haber terminado pero debemos ver si se ley� el texto completo
					int inicioSobrante = bufferMensaje.indexOf(DELIMITADOR_TEXTO_SIN_NDEF);
					if(inicioSobrante >= 0){
						bufferMensaje = bufferMensaje.substring(0, inicioSobrante);
						//El c�digo seguir� en validaci�n de fin de recepci�n de datos abajo
					}else{
						//Lectura de bloques termin�, pero no as� lectura del texto
						//Tenemos que ejecutar nuevamente un comando lectura en  los siguientes bloques a�n no le�dos
						avanzarBloques();
						if(sectorActual > ULTIMO_SECTOR_4K){
							//Aparentemente esta tarjeta no fue escrita con este lector � no tiene el formato esperado
							msg("Contenido de tarjeta no compatible con este lector USB", false);
							return;
							//...As� el c�digo de verificaci�n de fin abajo no ejecutar� pues nada funcion�
						}
						try {
							EnviarComando(COMANDO_LEER_TARJETA + " " + idTarjetaEnLectura + " " + ACCESS_KEY + " " + bloqueInicial + " " + bloqueFinal);
						} catch (IOException e) {
							msg("Al continuar con lectura Texto en sector:" + sectorActual+", bloqueInicial:"+bloqueInicial+", bloqueFinal:"+bloqueFinal+"\nSucedi�:"+e, false);
						}
						//Necesitamos que bufferMensaje se mantenga para leer el pr�ximo pedazo de bloques ...
						return; //...As� el c�digo de verificaci�n de fin abajo no ejecutar� pues la lectura en realidad contin�a
					}
				}
			}
			
		    //Si ha terminado de leer los bytes de datos de usb
		    if (recepcionDatosTerminada) {
		    	//Ya lleg� la respuesta completa
			   	//Procesamos mensaje seg�n el comando enviado previamente
		    	if(comandoActual != null){//este if deber�a ser inecesario
		    		//En este punto si alguno de los eventos reportados pide un nuevo comando de lectura a esta
		    		//clase entonces comandoActual y bufferMensaje podr�an ser modificados antes de terminar
		    		//este m�todo (recibirDatos) as� que respaldamos las variables antes de dejarlas limpias
		    		String tmpComando = comandoActual, tmpBuffer = bufferMensaje;
		    		comandoActual = null;
		    		bufferMensaje = new String(new byte[0], ISO_8859_1);
		    		
		    		if(tmpComando.startsWith(COMANDO_IDENTIFICAR_TARJETA) ){
		    			//msg("Llamando a ongetidtarjeta");
		    			String salida = new String(tmpBuffer.getBytes(ISO_8859_1), UTF_8);
		    			if(tmpBuffer.equals(NO_HAY_TAG_NFC))
		    				salida = null;
		    			misEventos.onGetIdTarjeta(this, salida);
		    			
		    		}else if(tmpComando.startsWith(COMANDO_LEER_TARJETA)){
		    			String metadatosPaciente;
		    			if(tagNfcActualUsaNdef)
		    				metadatosPaciente = LeerNdefTexto(tmpBuffer.getBytes(ISO_8859_1));
		    			else
		    				metadatosPaciente = new String(tmpBuffer.getBytes(ISO_8859_1), UTF_8);
		    			List<PendientesTarjeta> pendientes = null;
		    			try {
							pendientes = LeerDatosNFC(metadatosPaciente, miActivity);
						} catch (Exception e) {
							msg("Error al obtener datos del paciente en USB:"+e, false);
							return;
						}
		    			misEventos.onLeerTarjeta(this, pendientes);
		    			
		    		}else if(tmpComando.startsWith(COMANDO_ESCRIBIR_TARJETA)){
		    			if(numBytesEscritos >= bytesEscribir.length)
		    				misEventos.onEscribirTarjeta(this);
						else
							try {
								EscribirBytesRestantes();
							} catch (IOException e) {
								msg("No se pudo continuar escritura en tarjeta. Sector:"+sectorActual+", De bloque "+bloqueInicial+" a "+bloqueFinal, false);
							}
		    		}

		    	}else{
		    		bufferMensaje = new String(new byte[0], ISO_8859_1); //limpiamos buffer
		    	}
		    }
		}
		
		
		private void DetenerIoManager(){
			if (mSerialIoManager != null) {
	            mSerialIoManager.stop();
	            mSerialIoManager = null;
	        }
		}
		
		private void IniciarIoManager(){
			if (sDriver != null) {
	            mSerialIoManager = new SerialInputOutputManager(sDriver, mListener);
	            mExecutor.submit(mSerialIoManager);
	        }
		}
		
		/**
		 * Libera recursos antes del cambio de contexto.
		 * Debe ser llamado por fragmento o actividad contenedora al suceder onPause()
		 */
		public void onPause(){
			DetenerIoManager();
			if (sDriver != null) {
	            try {
	                sDriver.close();
	            } catch (IOException e) {}
	            sDriver = null;
	        }
		}
		
		/**
		 * Reactiva la capacidad de comunicaci�n con el lector usb.
		 * Debe ser llamado por fragmento o actividad contenedora al suceder onResume()
		 */
		public void onResume(){
			if (sDriver != null) {
	            try {
	                sDriver.open();
	                sDriver.setBaudRate(VELOCIDAD_SERIAL);
//	                sDriver.setParameters(9600, 8, UsbSerialDriver. STOPBITS_1, UsbSerialDriver.PARITY_NONE);
	            } catch (IOException e) {
	                try {
	                    sDriver.close();
	                } catch (IOException e2) {}
	                sDriver = null;
	                return;
	            }
	        }
			
			DetenerIoManager();
			IniciarIoManager();
		}

		public void msg(String mensaje){msg(mensaje, true);}
		public void msg(final String mensaje, final boolean duracionCorta){
			miActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(miActivity, mensaje, duracionCorta ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
				}
			});;
		}		

	}//fin clase LectorUsb
	
	
	
}//fin clase ManejadorNfc
