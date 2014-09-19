/**
 * 
 */
package com.siigs.tes.controles;


import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import com.example.chartlibrary.Line;
import com.example.chartlibrary.LineGraph;
import com.example.chartlibrary.LinePoint;
import com.siigs.tes.R;
import com.siigs.tes.Sesion;
import com.siigs.tes.TesAplicacion;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.ControlPerimetroCefalico;
import com.siigs.tes.datos.tablas.ErrorSis;
import com.siigs.tes.datos.tablas.Persona;
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
import com.siigs.tes.ui.WidgetUtil;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * @author Axel
 *
 */
public class ControlNutricional extends Fragment {

	private static final String TAG = ControlNutricional.class.getSimpleName();
	private static final float RADIO_PUNTO =7f;
	private static final float ANCHO_LINEA_GRAFICA = 3;
	private static final int EJEX_MINIMO = 0;
	private static final int EJEX_MINIMO_PESOALTURA = 45;
	private static final int COLCHON_EJEX_MAXIMO = 6; 
	
	private TesAplicacion aplicacion;
	private Sesion sesion;
	
	private LineGraph miGrafica = null;
	private Spinner spTipoGrafica = null;
	private LinearLayout llEtiquetas = null;
	private TextView txtEjeX = null;
	private TextView txtEjeY = null;
	
	private LinearLayout llAgregarNutricion = null;
	private LinearLayout llAgregarPerimetro = null;
	
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlNutricional() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.setRetainInstance(true);
		
		aplicacion = (TesAplicacion)getActivity().getApplication();
		sesion = aplicacion.getSesion();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.controles_atencion_control_nutricional, container, false);

		final Persona p = sesion.getDatosPacienteActual().persona;

		//Cosas visibles siempre
		WidgetUtil.setDatosBasicosPaciente(rootView, p);
		
		//VISIBILIDAD DE ACCIONES EN PANTALLA SEGÚN PERMISOS
		LinearLayout verNutricion = (LinearLayout)rootView.findViewById(R.id.accion_ver_nutricion);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLNUTRICIONAL_VER))
			verNutricion.setVisibility(View.VISIBLE); else verNutricion.setVisibility(View.GONE);
		
		llAgregarNutricion = (LinearLayout)rootView.findViewById(R.id.accion_agregar_nutricion);
		llAgregarPerimetro = (LinearLayout)rootView.findViewById(R.id.accion_agregar_perimetro);
		
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_ver_nutricion, "Ver Controles Nutricionales", 
				R.string.ayuda_ver_controles_nutricionales, getFragmentManager());

		llEtiquetas = (LinearLayout)rootView.findViewById(R.id.llEtiquetas);
		txtEjeX = (TextView) rootView.findViewById(R.id.txtEjeX);
		txtEjeY = (TextView) rootView.findViewById(R.id.txtEjeY);
		
		//Tipos de gráfica
		spTipoGrafica=(Spinner)rootView.findViewById(R.id.spTipoGrafica);
		final String[] opcionesGrafica = getResources().getStringArray(R.array.opciones_tipo_grafica);
		ArrayAdapter<String> adaptadorTipoGrafica = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_dropdown_item_1line,android.R.id.text1, opcionesGrafica);
		adaptadorTipoGrafica.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		spTipoGrafica.setAdapter(adaptadorTipoGrafica);
		spTipoGrafica.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> av, View view, int position, long id) {
				GenerarGrafica();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
				
		//VER CONTROL
		miGrafica = (LineGraph)rootView.findViewById(R.id.grafica_lineal);
		GenerarGrafica();
		miGrafica.setOnPointClickedListener(new LineGraph.OnPointClickedListener() {
			@Override
			public void onClick(int lineIndex, int pointIndex) {
				//if(lineIndex!=miGrafica.getLines().size()-1)return;
				String puntoY = miGrafica.getLine(lineIndex).getPoint(pointIndex).getY()+"";
				String puntoX = Integer.toString((int)miGrafica.getLine(lineIndex).getPoint(pointIndex).getX());
				
				String tipoGrafica = spTipoGrafica.getSelectedItem()+"";
				String unidadX="", unidadY="";
				if(tipoGrafica.equals(getString(R.string.peso_x_edad))  ){
					unidadX = getString(R.string.edad_meses);
					unidadY = getString(R.string.peso_kg);
				}else if( tipoGrafica.equals(getString(R.string.peso_x_altura)) ){
					unidadX = getString(R.string.altura_cm);
					unidadY = getString(R.string.peso_kg);
					//Recalculamos sin convertir a (int) como se hace arriba para mostrar alturas con decimales
					puntoX = miGrafica.getLine(lineIndex).getPoint(pointIndex).getX()+"";
				}else if(tipoGrafica.equals(getString(R.string.altura_x_edad))){
					unidadX = getString(R.string.edad_meses);
					unidadY = getString(R.string.altura_cm);
				}else if(tipoGrafica.equals(getString(R.string.imc_x_edad)) ){
					unidadX = getString(R.string.edad_meses);
					unidadY = getString(R.string.imc);
				}else if(tipoGrafica.equals(getString(R.string.perimetro_x_edad)) ){
					unidadX = getString(R.string.edad_meses);
					unidadY = getString(R.string.perimetro_cm);
				}else if(tipoGrafica.equals(getString(R.string.concentracion_hemoglobina)) ){
					unidadX = getString(R.string.edad_meses);
					unidadY = getString(R.string.hemoglobina);
				}else{
					return; //No debería pasar
				}
				Toast.makeText(getActivity(), unidadX+": "+puntoX+", "+unidadY+": "+puntoY, Toast.LENGTH_LONG).show();
			}
		});
		
		
		//AGREGAR CONTROL NUTRICIONAL
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_nutricion, R.string.agregar_nutricion, 
				R.string.ayuda_boton_agregar_control_nutricional, getFragmentManager());
		
		Button btnAgregarControlNutricional = (Button)rootView.findViewById(R.id.btnAgregarControl);
		btnAgregarControlNutricional.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Diálogo de nueva vacuna
				ControlNutricionalNuevo dialogo=new ControlNutricionalNuevo();
				Bundle args = new Bundle();
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlNutricional.this, ControlNutricionalNuevo.REQUEST_CODE);
				dialogo.show(ControlNutricional.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlNutricionalNuevo.TAG);
				//Este diálogo avisará su fin en onActivityResult() de llamador
			}
		});
		
		//AGREGAR CONTROL PERÍMETRO
		WidgetUtil.setBarraTitulo(rootView, R.id.barra_titulo_agregar_perimetro, R.string.agregar_perimetro, 
			R.string.ayuda_boton_agregar_control_perimetro, getFragmentManager());
				
		Button btnAgregarControlPerimetro = (Button)rootView.findViewById(R.id.btnAgregarControlPerimetro);
		btnAgregarControlPerimetro.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//Diálogo de nueva vacuna
				ControlPerimetroCefalicoNuevo dialogo=new ControlPerimetroCefalicoNuevo();
				Bundle args = new Bundle();
				dialogo.setArguments(args);
				dialogo.setTargetFragment(ControlNutricional.this, ControlPerimetroCefalicoNuevo.REQUEST_CODE);
				dialogo.show(ControlNutricional.this.getFragmentManager(),
						//.beginTransaction().setCustomAnimations(android.R.animator.fade_out, android.R.animator.fade_in), 
						ControlPerimetroCefalicoNuevo.TAG);
				//Este diálogo avisará su fin en onActivityResult() de llamador
			}
		});
				
		return rootView;
	}
	
	/**
	 * Genera la gráfica de acuerdo al tipo seleccionado por el usuario y pinta la línea del paciente según se 
	 * trate de un control nutricional o un control de perímetro cefálico.
	 */
	private void GenerarGrafica(){
		Persona persona = sesion.getDatosPacienteActual().persona;
		String tipoGrafica = spTipoGrafica.getSelectedItem()+"";
		
		//Visibilidad de secciones para insertar
		llAgregarNutricion.setVisibility(View.GONE);
		llAgregarPerimetro.setVisibility(View.GONE);
		if(sesion.tienePermiso(ContenidoControles.ICA_CONTROLNUTRICIONAL_INSERTAR)){
			if(tipoGrafica.equals(getString(R.string.perimetro_x_edad)) ){
				llAgregarPerimetro.setVisibility(View.VISIBLE);
			}else{
				llAgregarNutricion.setVisibility(View.VISIBLE);
			}
		}
		
		//Calculos
		Period periodo;
		int ejexMinimo = tipoGrafica.equals(getString(R.string.peso_x_altura)) ? 
				EJEX_MINIMO_PESOALTURA : EJEX_MINIMO;
		int ejexMaximo = -1;
		//Calculamos rango de eje X del paciente
		DateTime nacimiento = new DateTime(persona.fecha_nacimiento);
				
		//Sacamos muestras del paciente
		Line lineaPaciente = new Line();
		
		Object[] controlesOrigen;
		if(tipoGrafica.equals(getString(R.string.perimetro_x_edad)) ){
			controlesOrigen = sesion.getDatosPacienteActual().perimetrosCefalicos.toArray();
		}else { // Todas las demás opciones se alimentan del control nutricional
			controlesOrigen = sesion.getDatosPacienteActual().controlesNutricionales.toArray();
		}
		
		//Ciclo sobre los puntos
		for(Object controlOrigen : controlesOrigen){
			//Apuntadores usados según el tipo de gráfica
			com.siigs.tes.datos.tablas.ControlNutricional controlNutricional = null;
			ControlPerimetroCefalico perimetroCefalico = null;
			
			DateTime muestra;
			String fechaControl, idPersona;
			if(controlOrigen instanceof ControlPerimetroCefalico){
				perimetroCefalico = (ControlPerimetroCefalico)controlOrigen;
				fechaControl = perimetroCefalico.fecha;
				idPersona = perimetroCefalico.id_persona;
			}else{
				controlNutricional = (com.siigs.tes.datos.tablas.ControlNutricional)controlOrigen; 
				fechaControl = controlNutricional.fecha;
				idPersona = controlNutricional.id_persona;
				
				if(tipoGrafica.equals(getString(R.string.concentracion_hemoglobina)) 
						&& controlNutricional.hemoglobina == com.siigs.tes.datos.tablas.ControlNutricional.HEMOGLOBINA_NULL)
					continue; //Pues en este control la hemoglobina no fue capturada y no juega para su gráfica
			}
			 
					
			try{
				muestra = DatosUtil.parsearFechaHora(fechaControl);
				periodo = new Interval(nacimiento, muestra).toPeriod();
			}catch(Exception e){
				ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
						ContenidoControles.ICA_CONTROLNUTRICIONAL_VER, 
						"La fecha del control "+ (perimetroCefalico!=null ? "perimetro cefalico" : "nutricional") 
						+" id_persona:"+idPersona +" fecha:"+fechaControl+" no puede ser procesado contra nacimiento:"
								+persona.fecha_nacimiento+". Ex:"+e.getMessage());
				Toast.makeText(getActivity(), 
						"Las fechas control "+(perimetroCefalico!=null ? "perimetro cefalico:" : "nutricional:")
						+ fechaControl + " y nacimiento:" + persona.fecha_nacimiento
						+ " no se pueden procesar. Informar al administrador", 
						Toast.LENGTH_LONG).show();
				return;
			}
			
			//Determinación de puntos X,Y del control en gráfica según el tipo de gráfica seleccionado
			float puntoX, puntoY;
			if(tipoGrafica.equals(getString(R.string.peso_x_altura)) ){
				//X se define por altura
				puntoX = controlNutricional.altura;
			}else{
				//X se define por edad
				int meses = periodo.getYears()*12+periodo.getMonths();
				puntoX = meses;
			}
			if(puntoX>ejexMaximo)ejexMaximo=(int) puntoX;
			
			//Punto Y
			if(tipoGrafica.equals(getString(R.string.peso_x_edad)) || tipoGrafica.equals(getString(R.string.peso_x_altura)) ){
				puntoY = (float) controlNutricional.peso;
			}else if(tipoGrafica.equals(getString(R.string.altura_x_edad)) ){
				puntoY = (float) controlNutricional.altura;
			}else if(tipoGrafica.equals(getString(R.string.imc_x_edad)) ){
				// IMC = peso(kg) / altura(m)*altura(m)
				double alturaM = (double)controlNutricional.altura / 100d;
				puntoY = (float) (controlNutricional.peso / (alturaM*alturaM));
			}else if(tipoGrafica.equals(getString(R.string.perimetro_x_edad)) ){
				puntoY = (float) perimetroCefalico.perimetro_cefalico;
			}else{ //Hemoglobina
				puntoY = (float) controlNutricional.hemoglobina;
			}
			
			lineaPaciente.addPoint(new LinePoint(puntoX, puntoY));
		}//fin ciclo sobre puntos
		lineaPaciente.setColor(getResources().getColor(R.color.grafica_linea_paciente));
		lineaPaciente.setShowingPoints(true);
		lineaPaciente.setRadioPoint(RADIO_PUNTO);
		
		if(ejexMaximo > 0)
			ejexMaximo += COLCHON_EJEX_MAXIMO; //solo para darle un colchón de visibilidad
				
		GenerarLineasBasicas(tipoGrafica, ejexMinimo, ejexMaximo, persona.sexo, persona.id_asu_localidad_domicilio);
		
		if(lineaPaciente.getSize()>0) //Si no tiene puntos puede tener comportamiento extraño
			miGrafica.addLine(lineaPaciente);
		//miGrafica.setRangeY(0, 11);
		//miGrafica.setLineToFill(2);
	}
	
	/**
	 * Genera las líneas básicas de referencia de cada tipo de gráfica, define el texto descriptivo de los ejes X, Y
	 * También visualiza las etiquetas que describen las líneas básicas de referencia.
	 * @param tipoGrafica El tipo de gráfica que se visualiza con el cual se cargarán las líneas adecuadas
	 * @param ejexMinimo El punto en eje X que cualquier punto de una línea debe ser mayor. Si vale -1 el valor no se usa
	 * @param ejexMaximo El punto en eje X que cualquier punto de una línea debe ser inferior. Si vale -1 el valor no se usa
	 * @param sexo El sexo que define las líneas a graficar. Valores M, F
	 * @param idLocalidad Cuando tipoGrafica es de hemoglobina este valor se usa para obtener el punto de corte a pintar
	 */
	private void GenerarLineasBasicas(String tipoGrafica, int ejexMinimo, int ejexMaximo, String sexo, int idLocalidad){
		llEtiquetas.removeAllViews();
		AgregarEtiqueta("Paciente", null);
		
		//Genereación de líneas básicas
		miGrafica.removeAllLines();
		
		Object[] lineasBasicas;
		if(tipoGrafica.equals(getString(R.string.peso_x_edad)) || tipoGrafica.equals(getString(R.string.peso_x_altura)) ){
			txtEjeX.setText(R.string.edad_meses);
			if(tipoGrafica.equals(getString(R.string.peso_x_altura)))txtEjeX.setText(R.string.altura_cm);
			txtEjeY.setText(R.string.peso_kg);
			lineasBasicas = EstadoNutricionPeso.getEstadosNutricionPeso(getActivity()).toArray();
		}else if(tipoGrafica.equals(getString(R.string.altura_x_edad))){
			txtEjeX.setText(R.string.edad_meses);
			txtEjeY.setText(R.string.altura_cm);
			lineasBasicas = EstadoNutricionAltura.getEstadosNutricionAltura(getActivity()).toArray();
		}else if(tipoGrafica.equals(getString(R.string.imc_x_edad)) ){
			txtEjeX.setText(R.string.edad_meses);
			txtEjeY.setText(R.string.imc);
			lineasBasicas = EstadoImc.getEstadosImc(getActivity()).toArray();
		}else if(tipoGrafica.equals(getString(R.string.perimetro_x_edad)) ){
			txtEjeX.setText(R.string.edad_meses);
			txtEjeY.setText(R.string.perimetro_cm);
			lineasBasicas = EstadoPerimetroCefalico.getEstadosPerimetro(getActivity()).toArray();
		}else if(tipoGrafica.equals(getString(R.string.concentracion_hemoglobina)) ){
			txtEjeX.setText(R.string.edad_meses);
			txtEjeY.setText(R.string.hemoglobina);
			HemoglobinaAltitud base;
			try{
				base = HemoglobinaAltitud.getPorLocalidad(getActivity(), idLocalidad);
				if(base == null)
					throw new Exception("La localidad "+idLocalidad +" no existe en tabla "+HemoglobinaAltitud.NOMBRE_TABLA);
			}catch(Exception e){
				ErrorSis.AgregarError(getActivity(), sesion.getUsuario()._id, 
						ContenidoControles.ICA_CONTROLNUTRICIONAL_VER, 
						"No se puede obtener la altitud de Hemoglobina para la localidad: "+idLocalidad+". Ex:"+e.getMessage());
				Toast.makeText(getActivity(), 
						"No se puede obtener la altitud de Hemoglobina para la localidad: "+idLocalidad+". Informar al administrador", 
						Toast.LENGTH_LONG).show();
				return;
			}
			//Línea de corte de hemoglobina
			Line lineaHemoglobina = new Line();
			lineaHemoglobina.setShowingPoints(false);
			lineaHemoglobina.setAncho(ANCHO_LINEA_GRAFICA);
			lineaHemoglobina.setColor(Color.RED);
			AgregarEtiqueta("Punto de corte", "#ff0000");
			float puntoX1 = ejexMinimo == -1 ? 0 : ejexMinimo;
			float puntoX2 = ejexMaximo == -1 ? puntoX1+60 : ejexMaximo;
			float puntoY = (float) base.mujer_embarazada_ninio_6_59_meses;
			//Toast.makeText(getActivity(), "X1 = "+puntoX1+", X2="+puntoX2+", Y="+puntoY, Toast.LENGTH_SHORT).show();
			lineaHemoglobina.addPoint(new LinePoint(puntoX1, puntoY));
			lineaHemoglobina.addPoint(new LinePoint(puntoX2, puntoY));
			//Línea en 0 del eje Y para dimensionar mejor los resultados al graficar
			Line lineaCero = new Line();
			lineaCero.setShowingPoints(false);
			lineaCero.setAncho(ANCHO_LINEA_GRAFICA);
			lineaCero.setColor(Color.GRAY);
			lineaCero.addPoint(new LinePoint(puntoX1, 0));
			lineaCero.addPoint(new LinePoint(puntoX2, 0));
			
			miGrafica.addLine(lineaCero);
			miGrafica.addLine(lineaHemoglobina);
			return;
		}else{
			return; //No debería pasar
		}
		
		for(Object lineaBasica : lineasBasicas){
			int idLinea=0;
			String colorLinea = "#ff0000";
			String descripcionLinea = "test";
			if(tipoGrafica.equals(getString(R.string.peso_x_edad)) || tipoGrafica.equals(getString(R.string.peso_x_altura)) ){
				EstadoNutricionPeso linea =(EstadoNutricionPeso)lineaBasica;
				idLinea = linea._id; colorLinea = linea.color; descripcionLinea = linea.descripcion;
			}else if(tipoGrafica.equals(getString(R.string.altura_x_edad))){
				EstadoNutricionAltura linea =(EstadoNutricionAltura)lineaBasica;
				idLinea = linea._id; colorLinea = linea.color; descripcionLinea = linea.descripcion;
			}else if(tipoGrafica.equals(getString(R.string.imc_x_edad)) ){
				EstadoImc linea = (EstadoImc)lineaBasica;
				idLinea = linea._id; colorLinea = linea.color; descripcionLinea = linea.descripcion;
			}else if(tipoGrafica.equals(getString(R.string.perimetro_x_edad)) ){
				EstadoPerimetroCefalico linea = (EstadoPerimetroCefalico)lineaBasica;
				idLinea = linea._id; colorLinea = linea.color; descripcionLinea = linea.descripcion;
			}
			
			Line lineaGrafica = new Line();
			lineaGrafica.setShowingPoints(false);
			lineaGrafica.setAncho(ANCHO_LINEA_GRAFICA);
			lineaGrafica.setColor(Color.RED);
			try{lineaGrafica.setColor(Color.parseColor(colorLinea));}catch(Exception e){}
			AgregarEtiqueta(descripcionLinea, colorLinea);
			
			Object[] puntosLinea;
			
			if(tipoGrafica.equals(getString(R.string.peso_x_edad)) ){
				puntosLinea = EstadoNutricionPesoPorEdad.getPorEstado(getActivity(), idLinea, sexo).toArray();
			}else if(tipoGrafica.equals(getString(R.string.peso_x_altura)) ){
				puntosLinea = EstadoNutricionPesoPorAltura.getPorEstado(getActivity(), idLinea, sexo).toArray();
			}else if(tipoGrafica.equals(getString(R.string.altura_x_edad))){
				puntosLinea = EstadoNutricionAlturaPorEdad.getPorEstado(getActivity(), idLinea, sexo).toArray();
			}else if(tipoGrafica.equals(getString(R.string.imc_x_edad)) ){
				puntosLinea = EstadoImcPorEdad.getPorEstado(getActivity(), idLinea, sexo).toArray();
			}else{ //perimetro_x_edad
				puntosLinea = EstadoPerimetroCefalicoPorEdad.getPorEstado(getActivity(), idLinea, sexo).toArray();
			}
			
			for(Object puntoLinea : puntosLinea){
				float puntoX, puntoY;
				if(tipoGrafica.equals(getString(R.string.peso_x_edad)) ){
					puntoX = ((EstadoNutricionPesoPorEdad)puntoLinea).edad_meses;
					puntoY = (float) ((EstadoNutricionPesoPorEdad)puntoLinea).peso;
				}else if(tipoGrafica.equals(getString(R.string.peso_x_altura)) ){
					puntoX = (float) ((EstadoNutricionPesoPorAltura)puntoLinea).altura;
					puntoY = (float) ((EstadoNutricionPesoPorAltura)puntoLinea).peso;
				}else if(tipoGrafica.equals(getString(R.string.altura_x_edad))){
					puntoX = (float) ((EstadoNutricionAlturaPorEdad)puntoLinea).edad_meses;
					puntoY = (float) ((EstadoNutricionAlturaPorEdad)puntoLinea).altura;
				}else if(tipoGrafica.equals(getString(R.string.imc_x_edad)) ){
					puntoX = (float) ((EstadoImcPorEdad)puntoLinea).edad_meses;
					puntoY = (float) ((EstadoImcPorEdad)puntoLinea).imc;
				}else{ //perimetro_x_edad
					puntoX = (float) ((EstadoPerimetroCefalicoPorEdad)puntoLinea).edad_meses;
					puntoY = (float) ((EstadoPerimetroCefalicoPorEdad)puntoLinea).perimetro;
				}
				
				if( (ejexMaximo!=-1 && puntoX > ejexMaximo) || (ejexMinimo!=-1 && puntoX < ejexMinimo) )continue;
				lineaGrafica.addPoint(new LinePoint(puntoX, puntoY) );
			}
			if(lineaGrafica.getSize()>0) //Si no tiene puntos puede tener comportamiento extraño
				miGrafica.addLine(lineaGrafica);
		}
	}

	/**
	 * Agrega UN view en llEtiquetas que representa un texto de título con color de fondo especificado
	 * @param nombre
	 * @param color Color en formato hexadecimal #rrggbb o null para usar el color definido en R.color.grafica_linea_paciente
	 */
	private void AgregarEtiqueta(String nombre, String color){
		TextView etiqueta = new TextView(getActivity());
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int marginLeft = llEtiquetas.getChildCount() > 0 ? 20 : 5;
		params.setMargins(marginLeft, 0, 5, 0);
		etiqueta.setLayoutParams(params);
		etiqueta.setText(nombre);
		etiqueta.setTextColor(Color.WHITE);
		etiqueta.setShadowLayer(5, 0, 0, Color.BLACK);
		if(color == null){
			etiqueta.setBackgroundResource(R.color.grafica_linea_paciente);
		}else {
			etiqueta.setBackgroundColor(Color.RED);
			try{etiqueta.setBackgroundColor(Color.parseColor(color));}catch(Exception e){}
		}
		llEtiquetas.addView(etiqueta);
		/*TextView tvColor = new TextView(getActivity());
		params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
		tvColor.setLayoutParams(params);
		if(color == null){
			tvColor.setBackgroundResource(R.color.grafica_linea_paciente);
			etiqueta.setBackgroundResource(R.color.grafica_linea_paciente);
		}else {tvColor.setBackgroundColor(Color.RED);
			try{tvColor.setBackgroundColor(Color.parseColor(color));}catch(Exception e){}
			etiqueta.setBackgroundColor(Color.parseColor(color));
		}*/
		//llEtiquetas.addView(tvColor);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case ControlNutricionalNuevo.REQUEST_CODE:
		case ControlPerimetroCefalicoNuevo.REQUEST_CODE:
			//if(resultCode==ControlNutricionalNuevo.RESULT_OK){
				GenerarGrafica();
			//}
			break;
		}
	}

	
	
	
}//fin clase
