package it.polito.mad.mad_lab2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

public class ChoiceActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choice);
		
		/* Impostazioni ActionBar */
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft;
		Fragment fragment = null;
		fragment = CatalogFragment.newInstance(0);
		ft = fm.beginTransaction();
		ft.replace(R.id.frgContent, fragment, AppConst.CONTENT_FRAGMENT);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				return true;
	
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
