package it.polito.mad.mad_lab2;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CatObjectAdapter extends ArrayAdapter<CatObjectItem>{
	private int layoutResourceId;
	private Context ctx;
	private FragmentActivity act;
		
	public CatObjectAdapter(FragmentActivity act, Context context, int layoutResourceId, List<CatObjectItem> data) {
		super(context, layoutResourceId, data);
		this.act = act;
		this.ctx = context;
		this.layoutResourceId = layoutResourceId;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(layoutResourceId, null);
	        viewHolder = new ViewHolder();
	        viewHolder.imgIcon = (ImageView)convertView.findViewById(R.id.imgIcon);
	        viewHolder.TextRow1 = (TextView)convertView.findViewById(R.id.TextRow1);
	        viewHolder.TextRow2 = (TextView)convertView.findViewById(R.id.TextRow2);
	        viewHolder.TextRow3 = (TextView)convertView.findViewById(R.id.TextRow3);
	        viewHolder.btnAdd = (ImageButton)convertView.findViewById(R.id.btnAdd);
	        viewHolder.btnDetail = (Button)convertView.findViewById(R.id.btnDetail);
	        convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
	    }
		
	    final CatObjectItem row = getItem(position);
	    Drawable icon = Retrieve(row.getImage());
	    if(icon != null)
	    	viewHolder.imgIcon.setImageDrawable(icon);
	    else
	    	viewHolder.imgIcon.setBackgroundResource(R.drawable.ic_catalog_menu);
	    viewHolder.TextRow1.setText(row.getTextRow1());
	    viewHolder.TextRow2.setText(row.getTextRow2());
	    viewHolder.TextRow3.setText(row.getTextRow3());
	    viewHolder.btnAdd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (act instanceof ChoiceActivity) {
					Intent intent = act.getIntent();
					intent.putExtra("name", row.getTextRow1());
			    	intent.putExtra("itemId", row.getId());
			    	act.setResult(AppConst.RESULT_OK, intent);
					act.finish();
				}else{
					MainActivity mActivity = (MainActivity) act;
					mActivity.addQuoteItem(row.getId());
					Toast.makeText(ctx, ctx.getString(R.string.added_cart_string, new Object[] {row.getTextRow1()}), Toast.LENGTH_SHORT).show();
				}
			}
		});
	    viewHolder.btnDetail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentTransaction ft;
				Fragment fragment = DetailFragment.newInstance(row.getId());
				ft = act.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.frgContent, fragment, AppConst.CONTENT_FRAGMENT);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.addToBackStack(null);
				ft.commit();
			}
		});
	    
	    return convertView;
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
	
	private class ViewHolder {
		public ImageView imgIcon;
	    public TextView TextRow1;
	    public TextView TextRow2;
	    public TextView TextRow3;
	    public ImageButton btnAdd;
	    public Button btnDetail;
	}
}
