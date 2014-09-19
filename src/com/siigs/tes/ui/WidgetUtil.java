package com.siigs.tes.ui;

import com.siigs.tes.DialogoAyuda;
import com.siigs.tes.R;
import com.siigs.tes.datos.DatosUtil;
import com.siigs.tes.datos.tablas.Persona;

import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Pequeñas funciones helper para tareas repetitivas relacionadas con widgets en los layout xml
 * @author Axel
 *
 */
public class WidgetUtil {

	public static String TAG = WidgetUtil.class.getSimpleName();
	

	/**
	 * Configura una barra de título dentro de <b>contenedor</b> con el texto especificado pero sin ayuda visible
	 * @param contenedor Widget arriba en la jerarquía de la barra de título a configurar
	 * @param idBarraTitulo identificador de la barra ubicada dentro del layout de <b>contenedor</b> 
	 * @param idResTextoTitulo El id del recurso para cargar el texto de título desde <b>R.string</b>
	 */
	public static void setBarraTitulo(View contenedor, int idBarraTitulo, int idResTextoTitulo){
		setBarraTitulo(contenedor, idBarraTitulo, idResTextoTitulo, 0, null);
	}
	/**
	 * Configura una barra de título dentro de <b>contenedor</b> con el texto especificado pero sin ayuda visible
	 * @param contenedor Widget arriba en la jerarquía de la barra de título a configurar
	 * @param idBarraTitulo identificador de la barra ubicada dentro del layout de <b>contenedor</b> 
	 * @param textoTitulo Texto que se coloca como título
	 */
	public static void setBarraTitulo(View contenedor, int idBarraTitulo, String textoTitulo){
		setBarraTitulo(contenedor, idBarraTitulo, textoTitulo, 0, null);
	}
	
	/**
	 * Configura una barra de título dentro de <b>contenedor</b> con el texto y botón de ayuda especificados
	 * @param contenedor Widget arriba en la jerarquía de la barra de título a configurar
	 * @param idBarraTitulo identificador de la barra ubicada dentro del layout de <b>contenedor</b> 
	 * @param idResTextoTitulo El id del recurso para cargar el texto de título desde <b>R.string</b>
	 * @param idResTextoAyuda id en <b>R.string</b> que aparecerá como texto al presionar botón ayuda. Valor 0 indica que no hay ayuda
	 * @param fm El manager que controlará el salto a la ayuda. Valor null indica que no hay ayuda
	 */
	public static void setBarraTitulo(View contenedor, int idBarraTitulo, int idResTextoTitulo, int idResTextoAyuda, FragmentManager fm){
		setBarraTitulo(contenedor, idBarraTitulo, 
				contenedor.getResources().getString(idResTextoTitulo), idResTextoAyuda, fm);
	}
	/**
	 * Configura una barra de título dentro de <b>contenedor</b> con el texto y botón de ayuda especificados
	 * @param contenedor Widget arriba en la jerarquía de la barra de título a configurar
	 * @param idBarraTitulo identificador de la barra ubicada dentro del layout de <b>contenedor</b> 
	 * @param textoTitulo El texto a poner en el título
	 * @param idResTextoAyuda id en <b>R.string</b> que aparecerá como texto al presionar botón ayuda. Valor 0 indica que no hay ayuda
	 * @param fm El manager que controlará el salto a la ayuda. Valor null indica que no hay ayuda
	 */
	public static void setBarraTitulo(View contenedor, int idBarraTitulo, String textoTitulo, 
			final int idResTextoAyuda, final FragmentManager fm){
		//Asignación de título
		TextView titulo =
				(TextView)contenedor.findViewById(idBarraTitulo).findViewById(R.id.txtTituloBarra);
		titulo.setText(textoTitulo);
		
		//Asignación de botón ayuda
		ImageButton ayuda = 
				(ImageButton)contenedor.findViewById(idBarraTitulo).findViewById(R.id.btnAyuda);
		if(fm==null || idResTextoAyuda == 0){
			ayuda.setVisibility(TextView.INVISIBLE);
			return;
		}
		ayuda.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogoAyuda.CrearNuevo(fm, idResTextoAyuda);
			}
		});
	}

	/**
	 * Define contenido del TextView que debe existir en contenedor y que debe tener id R.id.txtDatosBasicos
	 * @param contenedor Destino del contenido
	 * @param p {@link Persona} origen de datos
	 */
	public static void setDatosBasicosPaciente(View contenedor, Persona p){
		((TextView)contenedor.findViewById(R.id.txtDatosBasicos))
				.setText(p.getNombreCompleto()+"\nSexo: "+p.sexo
						+"   Edad: "+DatosUtil.calcularEdad(p.fecha_nacimiento));
	}
}//fin clase
