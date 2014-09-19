package com.siigs.tes.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Este es un adaptador genérico para mapear una lista de objetos genéricos contra 
 * widgets de cualquier clase que visualizarán/recibirán los atributos definidos.
 * @author Axel
 *
 * @param <T> Clase genérica
 */
public class AdaptadorArrayMultiView<T> extends ArrayAdapter<T> {

	private final static String TAG = "AdaptadorArrayMultiView";
	
	private Context contexto;
	private int layoutId;
	private int dropDownLayoutId; //asignable en setDrowDownViewResource
	private List<T> datos;
	
	private Mapeo[] reglasMapeo;

	private ObjectViewBinder<T> miBinder = null;
	
	/**
	 * Crea un adaptador que mapea los atributos definidos en mapeoAtributoView a widgets/views
	 * en el layout del adaptador. Los datos en el mapeo deben ser correctos para ser extraídos 
	 * de la lista datos y asignados a los widgets/views respectivamente.
	 * 
	 * @param c Contexto donde se ejecuta este adaptador
	 * @param layout Identificador del layout XML a cargar como visualizador de cada elemento
	 * @param datos Origen de los datos a representar
	 * @param mapeoAtributoView Reglas de uno o más mapeos a realizar entre atributos de elementos T y widgets/views del layout
	 */
	public AdaptadorArrayMultiView(Context c, int layout, List<T> datos, Mapeo[] mapeoAtributoView ){
		super(c, layout, datos);
		this.contexto = c;
		this.layoutId = layout;
		this.dropDownLayoutId = layout;
		this.datos = datos;
		this.reglasMapeo = mapeoAtributoView;
	}

	public void setViewBinder(ObjectViewBinder<T> binder){this.miBinder = binder;}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return crearView(position, convertView, parent, this.layoutId);
	}
	
	/**
	 * Construye un View con el layout especificado. Usado por getView() y getDropDownView()
	 * @param position
	 * @param convertView
	 * @param parent
	 * @param layout
	 * @return
	 */
	private View crearView(int position, View convertView, ViewGroup parent, int layout){
		View salida = convertView;
		if(salida == null){
			LayoutInflater inflater = LayoutInflater.from(contexto);
			salida = inflater.inflate(layout, parent, false);	
		}
		
		T elemento = datos.get(position);
		
		for(Mapeo regla : reglasMapeo){
			Object valor=null;
			try{
				valor = elemento.getClass().getField(regla.atributoPorCopiar).get(elemento);
			}catch (Exception e){
				e.printStackTrace();
			}
			
			View destino = salida.findViewById(regla.idViewDestino);
			try {
				if(miBinder == null || !miBinder.setViewValue(destino, regla.metodoInvocarEnIdView, elemento, regla.atributoPorCopiar, valor, position))
					destino.getClass().getMethod(regla.metodoInvocarEnIdView, regla.tipoDatoMetodoInvocarEnIdView).invoke(destino, valor);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}catch(Exception e){
				Log.d(TAG, "algo pasó:"+e);
			}
		}
		
		return salida; //super.getView(position, convertView, parent);
	}
	
	

	@Override
	/**
	 * Regresamos lo mismo que getView() pues queremos mostrar mismos datos pero con layout de drop down
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return crearView(position, convertView, parent, this.dropDownLayoutId);
		//return getView(position, convertView, parent);
		//return super.getDropDownView(position, convertView, parent);
	}
	
	@Override
	public void setDropDownViewResource(int resource) {
		this.dropDownLayoutId = resource;
	}

	@Override
	public T getItem(int position) {
		return datos.get(position);
		//return super.getItem(position);
	}

	@Override
	public int getCount() {
		return datos.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	
	/**
	 * Describe reglas de mapeo entre un atributo de un elemento T listado en {@link AdaptadorArrayMultiView}
	 * y un widget/view en el layout del adaptador.
	 * @author Axel
	 *
	 */
	public static class Mapeo{
		public String atributoPorCopiar; //El atributo a copiar en la colección de origen de datos
		public int idViewDestino; //id del widget al que se le mapeará el valor de atributoPorCopiar
		public String metodoInvocarEnIdView; //El método a invocar en widget ej. "setText"
		public Class<?> tipoDatoMetodoInvocarEnIdView; //El tipo de dato en el método a invocar, ej. CharSequence.class
		
		/**
		 * Regla de mapeo entre un atributo de un elemento T y un widget/view
		 * @param atributoPorCopiar El atributo a extraer
		 * @param idViewDestino El id en layout del widget/view destinado a recibir el contenido de atributoPorCopiar
		 * @param metodoInvocarEnIdView El método a invocar para asignar el valor contenido en atributoPorCopiar ej. "setText"
		 * @param tipoParametro El tipo de dato que recibe metodoInvocarEnIdView ej. "CharSequence.class"
		 */
		public Mapeo(String atributoPorCopiar, int idViewDestino, String metodoInvocarEnIdView, Class<?> tipoParametro){
			this.atributoPorCopiar = atributoPorCopiar;
			this.idViewDestino = idViewDestino;
			this.metodoInvocarEnIdView = metodoInvocarEnIdView;
			this.tipoDatoMetodoInvocarEnIdView = tipoParametro;
		}
	}
	
}
