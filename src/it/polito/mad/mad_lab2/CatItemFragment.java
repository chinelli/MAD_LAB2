package it.polito.mad.mad_lab2;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class CatItemFragment extends Fragment{
	private Context ctx;
	private DataManager db;				/* Oggetto gestione dati */
	
	/* Grafica */
	private ListView lsCatItem;
	
	/* Costruttore senza argomento di selezione */
    public static CatItemFragment newInstance(int id, int type, String title) {
    	CatItemFragment f = new CatItemFragment();
    	
    	/* Argomenti aggiuntivi */
        Bundle args = new Bundle();
        args.putString(AppConst.TITLE_KEY, title); 			/* Stringa selezione */
        args.putInt(AppConst.TYPE_KEY, type); 				/* Tipo organizzazione */
        args.putInt(AppConst.OBJ_ID_KEY, id); 				/* Selezione */
        f.setArguments(args);

        return f;
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		ctx = activity.getApplicationContext();
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		View view = inflater.inflate(R.layout.cat_item_fragment, container, false);
		
		/* Grafica */
		TextView tvInfo = (TextView) view.findViewById(R.id.tvInfo);
		
    	if(getArgType() == AppConst.TYPE_GEN_KEY){
    		tvInfo.setText(getString(R.string.menu1sub1_string) + " " + getArgTitle());
    	}else{
    		tvInfo.setText(getString(R.string.menu1sub2_string) + " " + getArgTitle());
    	}
		
		/* Oggetti */
		db = new DataManager(ctx);
		
		/* Grafica */
		lsCatItem = (ListView) view.findViewById(R.id.lsCatItem);
		
		/* Imposto adapter listview*/
		lsCatItem.setAdapter(new CatObjectAdapter(getActivity(), ctx, R.layout.list_item, getCatItemData()));
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(db != null)
			db.closeDB();
	}
	
	/* Restituisce i dati degli oggetti del catalogo */
    private List<CatObjectItem> getCatItemData(){    
    	List<CatObjectItem> list = new LinkedList<CatObjectItem>();
    	Cursor c = null; 
    	
    	if(getArgType() == AppConst.TYPE_GEN_KEY){
    		c = db.viewObject("ID_Genre = " + getArgId(), "Name");
    	}else{
    		c = db.viewObject("ID_ProdLine = " + getArgId(), "Name");
    	}
    	
    	if(c != null){
			c.moveToFirst();
			while(!c.isAfterLast()){
				list.add(new CatObjectItem(c.getInt(0), c.getString(6), c.getString(1), c.getString(4) + "x" + c.getString(5) + " cm", c.getString(3) + "EUR"));
				c.moveToNext();
			}
			c.close();
    	}
    	
    	return list;
    }
    
    /* Restituisce argomento titolo */
	private String getArgTitle() {
		return getArguments().getString(AppConst.TITLE_KEY);
    }
	
    /* Restituisce argomento id */
	private int getArgId() {
		return getArguments().getInt(AppConst.OBJ_ID_KEY);
    }
	
    /* Restituisce argomento type */
	private int getArgType() {
		return getArguments().getInt(AppConst.TYPE_KEY);
    }
}