package it.polito.mad.mad_lab2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

public class CatalogFragment extends Fragment implements OnChildClickListener, TextWatcher, OnItemClickListener{
	private static final String TAG = CatalogFragment.class.getName();			/* TAG per logging */
	private Context ctx;
	private DataManager db;													/* Oggetto gestione dati */
	private CatExpandableListAdapter listAdapter;							/* Adapter x lista espandibile */
	private List<String> listDataHeader;									/* Lista per header lista espandibile */
    private HashMap<String, List<CatExpandableListItem>> listDataChild;		/* Mappa per lista espandibile*/
    
	/* Grafica */
	private ExpandableListView elvType;
	private EditText etSearch;
	private ListView lvSearch;
	private TextView tvInfo;
	
	/* Costruttore */
    public static CatalogFragment newInstance(int tabPos) {
    	CatalogFragment f = new CatalogFragment();
    	
    	/* Argomenti aggiuntivi */
        Bundle args = new Bundle();
        args.putInt(AppConst.TAB_POS_KEY, tabPos); 				/* Posizione TAB */
        f.setArguments(args);

        return f;
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		ctx = activity.getApplicationContext();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		View view = inflater.inflate(R.layout.catalog_fragment,container, false);
		
		/* Oggetti */
		db = new DataManager(ctx);
		
		/* Grafica */
		elvType = (ExpandableListView)view.findViewById(R.id.elvType);
		etSearch = (EditText)view.findViewById(R.id.etSearch);
		lvSearch = (ListView)view.findViewById(R.id.lvSearch);
		tvInfo = (TextView)view.findViewById(R.id.tvInfo);
		
		/* Caricamento/impostazioni lista */
		loadListData();
		listAdapter = new CatExpandableListAdapter(ctx, listDataHeader, listDataChild);
		elvType.setAdapter(listAdapter);
		
		/* Imposto listener */
		lvSearch.setOnItemClickListener(this);
		elvType.setOnChildClickListener(this);
		etSearch.addTextChangedListener(this);
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(db != null)
			db.closeDB();
	}
	
	/* Carica dati nella lista */
	private void loadListData(){
		listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<CatExpandableListItem>>();
        Cursor c = null;
        
        /* Header */
        listDataHeader.add(getString(R.string.menu1sub1_string));
        listDataHeader.add(getString(R.string.menu1sub2_string));
        
        /* Lista generi */
        List<CatExpandableListItem> listSub1 = new ArrayList<CatExpandableListItem>();
    	c = db.viewGenre(null, "Name");
    	if(c != null){
			c.moveToFirst();
			while(!c.isAfterLast()){
				listSub1.add(new CatExpandableListItem(c.getInt(0), AppConst.TYPE_GEN_KEY, c.getString(1)));
				c.moveToNext();
			}
			c.close();
    	}
    	
        /* Lista linee prodotti */
        List<CatExpandableListItem> listSub2 = new ArrayList<CatExpandableListItem>();
    	c = db.viewProdLine(null, "Name");
    	if(c != null){
			c.moveToFirst();
			while(!c.isAfterLast()){
				listSub2.add(new CatExpandableListItem(c.getInt(0), AppConst.TYPE_PL_KEY, c.getString(1)));
				c.moveToNext();
			}
			c.close();
    	}
    	
        /* Aggiunta alla mappa */
        listDataChild.put(listDataHeader.get(0), listSub1);
        listDataChild.put(listDataHeader.get(1), listSub2);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
		FragmentTransaction ft;
		CatExpandableListItem sel = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
		Fragment fragment = CatItemFragment.newInstance(sel.getId(), sel.getType(), sel.getName());
		ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.frgContent, fragment, AppConst.CONTENT_FRAGMENT);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(null);
		ft.commit();
		return false;
	}
	
	/* Mostra i risultati della ricerca */
	private void showResults(String query) {    	
		List<CatObjectItem> list = new LinkedList<CatObjectItem>();

		Cursor cName = db.getWordMatches(query, AppConst.KEY_NAME, null);

		if (cName == null) {
			lvSearch.setVisibility(View.GONE);
			elvType.setVisibility(View.VISIBLE);
			tvInfo.setText(getString(R.string.no_results, new Object[] {query}));
		}else{
			lvSearch.setVisibility(View.VISIBLE);
			elvType.setVisibility(View.GONE);
			tvInfo.setText(getString(R.string.search_nores_string) + " " + query);
			if(cName != null){
				if(cName.getCount() > 0){
					cName.moveToFirst();
					while(!cName.isAfterLast()){
						list.add(new CatObjectItem(cName.getInt(0), cName.getString(4), cName.getString(1), cName.getString(2) + " cm", cName.getString(3) + " EUR"));
						cName.moveToNext();
					}
				}
				cName.close();
			}
			/* Info su risultati di ricerca */
			int count = list.size();
			String countString = getResources().getQuantityString(R.plurals.search_results,
					count, new Object[] {count, query});
			tvInfo.setText(countString);
			lvSearch.setAdapter(new CatObjectAdapter(getActivity(), ctx, R.layout.list_item, list));
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if(s.length() >= 1)
			showResults(s.toString());
		else{
			lvSearch.setVisibility(View.GONE);
			tvInfo.setText(getString(R.string.search_instructions));
			elvType.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {	
		try{
			InputMethodManager inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
		}catch (Exception e) {
			Log.e(TAG, "Errore! keyboard hidden");
        }
		FragmentTransaction ft;
		CatObjectItem sel = (CatObjectItem)parent.getItemAtPosition(position);
		Fragment fragment = DetailFragment.newInstance(sel.getId());
		ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.frgContent, fragment, AppConst.CONTENT_FRAGMENT);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(null);
		ft.commit();
	}
}
