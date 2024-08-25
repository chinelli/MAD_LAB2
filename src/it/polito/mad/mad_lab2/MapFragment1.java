package it.polito.mad.mad_lab2;

import android.app.Activity;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class MapFragment1 extends Fragment {
	private static final String TAG = MapFragment1.class.getName(); /*
																	 * TAG per
																	 * logging
																	 */

	/* Grafica */
	private EditText etWidth;
	private EditText etHeight;

	private Context ctx;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setHasOptionsMenu(true);
		ctx = (Context) activity.getApplicationContext();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		int lastFid = getFragmentManager().getBackStackEntryCount() - 1;
		if (lastFid >= 0) {
			BackStackEntry bse = getFragmentManager().getBackStackEntryAt(
					lastFid);
			if (bse != null) {
				Fragment f2 = getFragmentManager().findFragmentByTag(
						bse.getName());
				if (f2 != null && f2 instanceof MapFragment2) {
					getFragmentManager().popBackStack();
					getFragmentManager().popBackStack();
				}
			}
		}
		View view = inflater.inflate(R.layout.map1_fragment, container, false);

		/* Grafica */
		etWidth = (EditText) view.findViewById(R.id.etWidth);
		etHeight = (EditText) view.findViewById(R.id.etHeight);

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.save_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			try {
				InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(getActivity()
						.getCurrentFocus().getWindowToken(), 0);
			} catch (Exception e) {
				Log.e(TAG, "Errore! keyboard hidden");
			}
			float width = 0;
			float height = 0;
			try{
			width = Float.valueOf(etWidth.getText().toString());
			height = Float.valueOf(etHeight.getText().toString());
			}catch (Exception e) {
				Toast.makeText(ctx, getString(R.string.short_dim), Toast.LENGTH_SHORT).show();
				return true;
			}
			
			if (width <= 0 || height <= 0 ) {
				Toast.makeText(ctx, getString(R.string.negative_dim), Toast.LENGTH_SHORT).show();
				return true;
			}
			if (width < 200 || height < 200 ) {
				Toast.makeText(ctx, getString(R.string.short_dim), Toast.LENGTH_SHORT).show();
				return true;
			}
			if (width > 2000 || height > 2000 ) {
				Toast.makeText(ctx, getString(R.string.big_dim), Toast.LENGTH_SHORT).show();
				return true;
			}
			/* Salvo la dimensione */
			MainActivity act = (MainActivity) getActivity();
			act.setWidth(width);
			act.setHeight(height);

			FragmentTransaction ft;
			Fragment fragment = MapFragment2.newInstance(
					Float.valueOf(etWidth.getText().toString()),
					Float.valueOf(etHeight.getText().toString()));
			ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.frgContent, fragment, AppConst.CONTENT_FRAGMENT);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
