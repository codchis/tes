package com.siigs.tes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.siigs.tes.controles.ContenidoControles;

/**
 * DEPRECATED: Esta clase junto con layout fragment_seccion_detail
 * no son necesarios y se deben eliminar al crear todas las pantallas
 * del menú
 * 
 * A fragment representing a single Seccion detail screen. This fragment is
 * either contained in a {@link PrincipalActivity} in two-pane mode (on
 * tablets) or a {@link ControlActivity} on handsets.
 */
public class ControlFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID_PERSONA that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private ContenidoControles.ItemControl mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ControlFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments()!=null && getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			String id=getArguments().getString(ARG_ITEM_ID);
			Toast.makeText(getActivity(), "id es "+id, Toast.LENGTH_SHORT).show();
			mItem = ContenidoControles.CONTROLES_TODOS_MAP.get(id);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_control,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			//((TextView) rootView.findViewById(R.id.seccion_detail))
					//.setText(mItem.titulo);
		}

		return rootView;
	}
}
