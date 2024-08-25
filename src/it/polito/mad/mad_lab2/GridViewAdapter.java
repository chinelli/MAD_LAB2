package it.polito.mad.mad_lab2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter<GridRowItem> {
	private Context ctx;
	private int layoutResourceId;
	
	public GridViewAdapter(Context context, int layoutResourceId, ArrayList<GridRowItem> data) {
		super(context, layoutResourceId, data);
		this.ctx = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RecordHolder viewHolder = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(layoutResourceId, null);
	        viewHolder = new RecordHolder();
	        viewHolder.imageItem = (ImageView)convertView.findViewById(R.id.item_image);
	        viewHolder.txtTitle = (TextView)convertView.findViewById(R.id.item_text);
	        convertView.setTag(viewHolder);
		}else{
			viewHolder = (RecordHolder) convertView.getTag();
	    }
		
		GridRowItem row = getItem(position);
	    Drawable icon = Retrieve(row.getImage());

	    if(icon != null){
		    icon.setFilterBitmap(true);
		    icon.setDither(true);
	    	viewHolder.imageItem.setImageDrawable(icon);
	    }
	    else
	    	viewHolder.imageItem.setBackgroundResource(R.drawable.ic_catalog_menu);
	    viewHolder.txtTitle.setText(row.getTitle());
	    
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
	
	static class RecordHolder {
		TextView txtTitle;
		ImageView imageItem;

	}
}