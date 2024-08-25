package it.polito.mad.mad_lab2;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends FragmentActivity implements OnItemClickListener{
	private int position = 0;							/* Posizione selezione menu */
	private float width = -1, height = -1;				/* Altezza, larghezza stanza */
	private int numObjCart = 0;							/* Numero di oggetti nel carello */
	private MenuAdapter adapter;						/* MyAdapter menu */
	private View prev = null; 							/* View precedente */
	private ArrayList<QuoteItem> qiList;				/* Lista oggetti per il preventivo */
	private ArrayList<FornitureItem> fiList = null;		/* Lista oggetti per la mappa */
	private ArrayList<FornitureItem> wiList = null;		/* Lista elementi per la mappa */
	private ArrayList<GridRowItem> itemList = null;		/* Lista GridView */
	private int idFi = 0, idElem = 0;					/* ID oggetti e elementi */
	
	/* Grafica */
	private DrawerLayout drawer;
	private ListView navList;
	private ActionBarDrawerToggle drawerToggle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/* Impostazioni ActionBar */
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		/* Recupero posizione menu */
		if (savedInstanceState != null) {
			position = savedInstanceState.getInt(AppConst.POS_MENU_KEY);
			qiList = savedInstanceState.getParcelableArrayList(AppConst.OBJ_BUY_KEY);
			fiList = savedInstanceState.getParcelableArrayList(AppConst.OBJ_MAP_KEY);
			wiList = savedInstanceState.getParcelableArrayList(AppConst.ELEM_MAP_KEY);
			itemList = savedInstanceState.getParcelableArrayList(AppConst.GRID_KEY);
			height = savedInstanceState.getFloat(AppConst.HEIGHT_KEY);
			width = savedInstanceState.getFloat(AppConst.WIDTH_KEY);
			numObjCart = savedInstanceState.getInt(AppConst.NUM_OBJ_CART_KEY);
			idFi = savedInstanceState.getInt(AppConst.ID_OBJ_MAP_KEY);
			idElem = savedInstanceState.getInt(AppConst.ID_ELEM_MAP_KEY);
		}

		/* Riga del menu */
		MenuRow[] menu_data = new MenuRow[]{
				new MenuRow(R.drawable.ic_catalog_menu, getString(R.string.menu1_string)),
				new MenuRow(R.drawable.ic_map_menu, getString(R.string.menu2_string)),
				new MenuRow(R.drawable.ic_cart, getString(R.string.menu3_string)),
		};
		
		/* Oggetti */
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
		
		/* Grafica */
		drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
		navList = (ListView)findViewById(R.id.left_drawer);

		/* Impostazione prima apertura */
		/* TRUE --> programma gia aperto in precedenza */
		/* FALSE --> programma mai aperto */
		Boolean AppOpen = setting.getBoolean(AppConst.APPOPEN_KEY, false); 
		/* Controllo prima apertura */
		if(AppOpen == false){
			
			SharedPreferences.Editor editor = setting.edit();
			DataManager db = new DataManager(this);
			loadGenre(db);
			loadProdLine(db);
			loadElement(db);
			db.loadJSONDB();
			editor.putBoolean(AppConst.APPOPEN_KEY, true);
			editor.commit();
			db.closeDB();
			Intent i = new Intent(this,SplashActivity.class);
			startActivity(i);
		}
		
		drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		adapter = new MenuAdapter(this, R.layout.menu_item, menu_data, position);
		navList.setAdapter(adapter);									/* Setto adapter menu laterale */

		drawerToggle = new ActionBarDrawerToggle(this, drawer, R.drawable.ic_drawer, R.string.open_string, R.string.close_string){
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				invalidateOptionsMenu();
			}
		};
		
		/* Listener */
		drawer.setDrawerListener(drawerToggle);
		navList.setOnItemClickListener(this);

		/* Carico primo frammento */
		if (savedInstanceState == null) {
			updateGUI(0);
			qiList = new ArrayList<QuoteItem>();
			fiList = new ArrayList<FornitureItem>();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(AppConst.POS_MENU_KEY, position);
		outState.putParcelableArrayList(AppConst.OBJ_BUY_KEY, qiList);
		outState.putParcelableArrayList(AppConst.OBJ_MAP_KEY, fiList);
		outState.putParcelableArrayList(AppConst.ELEM_MAP_KEY, wiList);
		outState.putParcelableArrayList(AppConst.GRID_KEY, itemList);
		outState.putInt(AppConst.ID_OBJ_MAP_KEY, idFi);
		outState.putInt(AppConst.ID_ELEM_MAP_KEY, idElem);
		outState.putFloat(AppConst.HEIGHT_KEY, height);
		outState.putFloat(AppConst.WIDTH_KEY, width);
		outState.putFloat(AppConst.WIDTH_KEY, width);
		outState.putInt(AppConst.NUM_OBJ_CART_KEY, numObjCart);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}    

	/* Cambia il colore dello sfondo di una riga della listview */
	public void setBackgroundColor(View v){
		RelativeLayout selLayout = (RelativeLayout)v;
		if(prev == null){
			adapter.resetFirstRow();
			selLayout.setBackgroundColor(Color.rgb(51, 181, 229));
		}else{
			prev.setBackgroundColor(Color.TRANSPARENT);
			selLayout.setBackgroundColor(Color.rgb(51, 181, 229));
		}
		prev = v;
	}
	
	/* Carica generi prodotti */
	private void loadGenre(DataManager db) {
		db.insertGenre("Chairs");
		db.insertGenre("Beds");
		db.insertGenre("Kitchen");
		db.insertGenre("Couches");
		db.insertGenre("Forniture");
	}
	
	/* Carica linee prodotti */
	private void loadProdLine(DataManager db) {
		db.insertProdLine("ProductLine1");
		db.insertProdLine("ProductLine2");
		db.insertProdLine("ProductLine3");
	}
	
	/* Carica elementi */
	private void loadElement(DataManager db) {
		db.insertElement(-1, "Door", 60, 20, "PortaSingMap.png");
		db.insertElement(-2, "Window", 60, 20, "PortaSingMap.png");
		db.insertElement(-3, "Double window", 120, 20, "PortaDoppiaMap.png");
	}
	
	/* Applicazione fragment */
	public void updateGUI(int selectedIndex){
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft;
		Fragment fragment = null;

		switch(selectedIndex){
			case 0:			/* Catalogo */
				fragment = CatalogFragment.newInstance(0);
			break;
			
			case 1:			/* Mappa */
				if(width == -1 && height == -1)
					fragment = new MapFragment1();
				else
					fragment = MapFragment2.newInstance(width, height);
			break;
			
			case 2:			/* Carrello */
				fragment = QuoteFragment.newInstance(qiList);
			break;
		}

		if (fragment != null)	{
			ft = fm.beginTransaction();
			ft.replace(R.id.frgContent, fragment, AppConst.CONTENT_FRAGMENT);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			drawer.closeDrawer(navList);
		}

	}

	/* Aggiunge un elemento nel preventivo */
	public void addQuoteItem(QuoteItem qi){
		if(qi != null){
			qiList.add(qi);
			numObjCart++;
			adapter.setNumObjCart(numObjCart);
		}
	}

	/* Aggiunge un elemento nel preventivo */
	public void addQuoteItem(int id){
		QuoteItem qi = null;
		DataManager db = new DataManager(this);
		Cursor c = db.viewObject("ID_Object = " + id, null);
		if(c != null){
			c.moveToFirst();
			qi = new QuoteItem(c.getInt(0), c.getString(6), c.getString(1), c.getString(4) + "x" + c.getString(5) + " cm", c.getInt(3), -1);
			qi.increment();
			if(itemList != null){
				GridRowItem gridItem = new GridRowItem(c.getInt(0), c.getString(7), c.getString(1), false, 1);
				if (!itemList.contains(gridItem))
					itemList.add(gridItem);
				else
					itemList.get(itemList.indexOf(gridItem)).increment();
			}
			c.close();
		}
		db.closeDB();
		if(qi != null){
			qiList.add(qi);
			numObjCart++;
			adapter.setNumObjCart(numObjCart);
		}
	}

	/* Rimuove un elemento nel preventivo */
	public void delQuoteItem(int id){
		for (QuoteItem qItem : qiList) {
			if(qItem.getId() == id){
				DataManager db = new DataManager(this);
				Cursor c = db.viewObject("ID_Object = " + id, "Name");
				if(c != null){
					c.moveToFirst();
					GridRowItem gridItem = new GridRowItem(c.getInt(0), c.getString(7), c.getString(1), false, 1);
					if (itemList.contains(gridItem)){
						itemList.get(itemList.indexOf(gridItem)).decrement();
						if(itemList.get(itemList.indexOf(gridItem)).getCount() == 0)
							itemList.remove(gridItem);
					}
					c.close();
				}
				if(qItem.getIdFornitureItem() != -1){
					/* Rimuovo oggetto dalla mappa */
			    	for (FornitureItem fi : fiList) {
			    		if(fi.getId() == qItem.getIdFornitureItem()){
			    			fiList.remove(fi);
			    			break;
			    		}
			    	}
				}
				qiList.remove(qItem);
				break;
			}
		}
		numObjCart--;
		adapter.setNumObjCart(numObjCart);
	}
	
	/* Rimuove un elemento nel preventivo */
	public void delQuoteItem(){
		numObjCart--;
		adapter.setNumObjCart(numObjCart);
	}
	
	/* Ottiene la lista del carrello */
	public ArrayList<QuoteItem> getQuoteList(){
		return qiList;
	}
	
	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public int getIdFornitureItem() {
		return idFi;
	}

	public void setIdFornitureItem(int idFi) {
		this.idFi = idFi;
	}

	public int getIdElem() {
		return idElem;
	}

	public void setIdElem(int idElem) {
		this.idElem = idElem;
	}
	
	public ArrayList<FornitureItem> getFiList() {
		return fiList;
	}

	public void setFiList(ArrayList<FornitureItem> fiList) {
		this.fiList = fiList;
	}

	public ArrayList<FornitureItem> getWiList() {
		return wiList;
	}

	public void setWiList(ArrayList<FornitureItem> wiList) {
		this.wiList = wiList;
	}
	
	public ArrayList<GridRowItem> getItemList() {
		return itemList;
	}

	public void setItemList(ArrayList<GridRowItem> itemList) {
		this.itemList = itemList;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int pos, long id) {
		if (pos != position){
			setBackgroundColor(view);
			updateGUI(pos);	
			position = pos;
		}
	}
	

}
