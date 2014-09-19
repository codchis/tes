package com.siigs.tes.datos;

import java.util.ArrayList;

import com.example.chartlibrary.Line;
import com.example.chartlibrary.LinePoint;
import com.siigs.tes.datos.tablas.graficas.EstadoNutricionPesoPorEdad;

import android.graphics.Color;

/**
 * Clase temporal para proveer datos de pesos en niveles de nutrición.
 * Esta clase puede y debe ser reemplazada/complementada por un proveedor de  datos con capacidad
 * dinámica como por ejemplo el catálogo de una base de datos.
 * 
 * @author Axel
 * @deprecated Dada de baja. Use {@link EstadoNutricionPesoPorEdad#getPorEstado(android.content.Context, int, String)}
 */
@Deprecated
public final class NivelNutricion{
	//Sexos disponibles
	public final static int MASCULINO=0;
	public final static int FEMENINO=1;
	//Niveles de peso disponibles
	public final static int PESO_BAJO=1;
	public final static int PESO_NORMAL=2;
	public final static int PESO_ALTO=3;
	public final static int PESO_EXCEDIDO=4;
	//Colores
	public final static int COLOR_PESO_BAJO=Color.argb(128,255, 211, 0);
	public final static int COLOR_PESO_NORMAL=Color.argb(128,0, 255, 0);
	public final static int COLOR_PESO_ALTO=Color.argb(128,255, 102, 0);
	public final static int COLOR_PESO_EXCEDIDO=Color.argb(128,255, 0, 0);

	/**
	 * Genera una línea para graficar basado en niveles de peso y género
	 * a lo largo de 36 meses.
	 * @param genero un valor entre MASCULINO o FEMENINO
	 * @param peso uno de los tipos de PESO disponibles
	 * @param mesesMinimo mínimo de meses a regresar (-1 para no usarlo)
	 * @param mesesMaximo maximo de meses a regresar (-1 para no usarlo)
	 * @return graficable con referencia X(edad en meses), Y(peso en Kg)
	 */
	public static Line getLineaNivelPeso(int genero, int peso,
			int mesesMinimo, int mesesMaximo){
		ArrayList<LinePoint> puntos=new ArrayList<LinePoint>();
		Line linea=new Line();
		linea.setShowingPoints(false);
		linea.setAncho(4);
		
		switch(genero){
		case MASCULINO:
			if(peso==PESO_BAJO){
				linea.setColor(COLOR_PESO_BAJO);
				AgregarPunto(0, 2.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(1, 3.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(2, 4.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(3, 5.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(4, 6.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(5, 6.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(6, 7.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(7, 7.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(8, 7.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(9, 8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(10, 8.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(11, 8.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(12, 8.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(18, 9.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(24, 10.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(30, 11.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(36, 12.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(42, 13.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(48, 14.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(54, 15.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(60, 16f, mesesMinimo, mesesMaximo, puntos);
			}else if(peso==PESO_NORMAL){
				linea.setColor(COLOR_PESO_NORMAL);
				AgregarPunto(0, 3.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(1, 4.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(2, 5.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(3, 6.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(4, 7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(5, 7.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(6, 7.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(7, 8.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(8, 8.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(9, 8.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(10, 9.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(11, 9.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(12, 9.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(18, 10.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(24, 12.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(30, 13.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(36, 14.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(42, 15.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(48, 16.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(54, 17.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(60, 18.3f, mesesMinimo, mesesMaximo, puntos);
			}else if(peso==PESO_ALTO){
				linea.setColor(COLOR_PESO_ALTO);
				AgregarPunto(0, 3.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(1, 5.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(2, 6.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(3, 7.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(4, 7.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(5, 8.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(6, 8.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(7, 9.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(8, 9.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(9, 9.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(10, 10.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(11, 10.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(12, 10.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(18, 12.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(24, 13.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(30, 15.0f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(36, 16.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(42, 17.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(48, 18.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(54, 19.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(60, 21.0f, mesesMinimo, mesesMaximo, puntos);
			}else if(peso==PESO_EXCEDIDO){
				linea.setColor(COLOR_PESO_EXCEDIDO);
				AgregarPunto(0, 4.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(1, 5.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(2, 7.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(3, 8.0f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(4, 8.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(5, 9.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(6, 9.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(7, 10.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(8, 10.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(9, 11.0f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(10, 11.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(11, 11.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(12, 12.0f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(18, 13.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(24, 15.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(30, 16.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(36, 18.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(42, 19.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(48, 21.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(54, 22.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(60, 24.2f, mesesMinimo, mesesMaximo, puntos);
			}
			break;
		case FEMENINO:
			if(peso==PESO_BAJO){
				linea.setColor(COLOR_PESO_BAJO);
				AgregarPunto(0, 2.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(1, 3.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(2, 4.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(3, 5.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(4, 5.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(5, 6.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(6, 6.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(7, 6.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(8, 7.0f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(9, 7.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(10, 7.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(11, 7.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(12, 7.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(18, 9.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(24, 10.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(30, 11.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(36, 12.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(42, 13.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(48, 14.0f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(54, 14.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(60, 15.8f, mesesMinimo, mesesMaximo, puntos);
			}else if(peso==PESO_NORMAL){
				linea.setColor(COLOR_PESO_NORMAL);
				AgregarPunto(0, 3.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(1, 4.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(2, 5.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(3, 5.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(4, 6.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(5, 6.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(6, 7.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(7, 7.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(8, 7.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(9, 8.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(10, 8.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(11, 8.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(12, 8.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(18, 10.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(24, 11.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(30, 12.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(36, 13.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(42, 15.0f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(48, 16.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(54, 17.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(60, 18.2f, mesesMinimo, mesesMaximo, puntos);
			}else if(peso==PESO_ALTO){
				linea.setColor(COLOR_PESO_ALTO);
				AgregarPunto(0, 3.7f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(1, 4.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(2, 5.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(3, 6.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(4, 7.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(5, 7.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(6, 8.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(7, 8.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(8, 9.0f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(9, 9.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(10, 9.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(11, 9.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(12, 10.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(18, 11.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(24, 13.0f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(30, 14.4f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(36, 15.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(42, 17.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(48, 18.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(54, 19.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(60, 21.2f, mesesMinimo, mesesMaximo, puntos);
			}else if(peso==PESO_EXCEDIDO){
				linea.setColor(COLOR_PESO_EXCEDIDO);
				AgregarPunto(0, 4.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(1, 5.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(2, 6.6f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(3, 7.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(4, 8.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(5, 8.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(6, 9.3f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(7, 9.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(8, 10.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(9, 10.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(10, 10.9f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(11, 11.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(12, 11.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(18, 13.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(24, 14.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(30, 16.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(36, 18.1f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(42, 19.8f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(48, 21.5f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(54, 23.2f, mesesMinimo, mesesMaximo, puntos);
				AgregarPunto(60, 24.9f, mesesMinimo, mesesMaximo, puntos);
			}
			break;
		}
		
		linea.setPoints(puntos);
		return linea;
	}//fin NivelPeso
	
	/**
	 * Helper para definir si agrega un punto a lista según meses
	 * @param x
	 * @param y
	 * @param mesesMinimo
	 * @param mesesMaximo
	 * @param lista
	 */
	private static void AgregarPunto(float x, float y, 
			int mesesMinimo, int mesesMaximo, ArrayList<LinePoint> lista){
		if(mesesMinimo>=0 && x< (float)mesesMinimo)return;
		if(mesesMaximo>=0 && x> (float)mesesMaximo)return;
		lista.add(new LinePoint(x, y));
	}
	
}//fin clase
