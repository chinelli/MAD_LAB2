package it.polito.mad.mad_lab2;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailFragment extends Fragment{
	private Context ctx;
	private DataManager db;				/* Oggetto gestione dati */
	
	/* Grafica */
	private ImageView imgItem;
	private TextView tvName;
	private TextView tvDescr;
	private TextView tvMeasure;
	private TextView tvPrice;
	
	/* Costruttore */
    public static DetailFragment newInstance(int id) {
    	DetailFragment f = new DetailFragment();
    	
    	/* Argomenti aggiuntivi */
        Bundle args = new Bundle();
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
		View view = inflater.inflate(R.layout.detail_fragment, container, false);
		
		/* Oggetti */
		db = new DataManager(ctx);
		
		/* Grafica */
		imgItem = (ImageView) view.findViewById(R.id.imgItem);
		tvName = (TextView) view.findViewById(R.id.tvName);
		tvDescr = (TextView) view.findViewById(R.id.tvDescr);
		tvMeasure = (TextView) view.findViewById(R.id.tvMeasure);
		tvPrice = (TextView) view.findViewById(R.id.tvPrice);
		
		/* Carico dati */
		Cursor c = db.viewObject("ID_Object = " + getArgId(), null);
		if(c != null){
			c.moveToFirst();
			/* Carico dati */
			Drawable icon = Retrieve(c.getString(6));
		    if(icon != null)
		    	imgItem.setImageDrawable(icon);
		    else
		    	imgItem.setBackgroundResource(R.drawable.ic_catalog_menu);
			tvName.setText(c.getString(1));
			tvDescr.setText(c.getString(2));
			tvMeasure.setText(c.getString(4) + "x" + c.getString(5) + " (LxP)");
			tvPrice.setText(c.getString(3) + " Euro");
			c.close();
		}
		
		return view;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(db != null)
			db.closeDB();
	}
		
	private Drawable Retrieve(String name){
		Drawable d = null;
		
		try {
			InputStream ims = ctx.getAssets().open(name);
			d = Drawable.createFromStream(ims, null);
		} catch (IOException e) {
			return null;
		}
	    
	    return d;
	}
	
    /* Restituisce argomento id */
	private int getArgId() {
		return getArguments().getInt(AppConst.OBJ_ID_KEY);
    }
}
