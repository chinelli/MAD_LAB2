package it.polito.mad.mad_lab2;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuAdapter extends ArrayAdapter<MenuRow>{
	private Context context; 
    private int layoutResourceId;    
    private MenuRow data[] = null; 	/* Dati delle righe menu (Immagine, Titolo) */
    private int position;		 	/* Posizione selezione */
    private View save_view; 		/* View da selezionare */
    private View cart_view;			/* View Carello */
    
	public MenuAdapter(Context context, int layoutResourceId, MenuRow[] data, int position) {
		super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        this.position = position;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		MenuHolder holder = null;
        
		if(row == null)
        {
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);   
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new MenuHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.MenuTextRow1 = (TextView)row.findViewById(R.id.MenuTextRow1);
            
            if(position == this.position){
            	save_view = row;
            	row.setBackgroundColor(Color.rgb(51, 181, 229));
            }
            
            if(position == 2){
            	cart_view = row;
            }
            
            row.setTag(holder);
        }else{
        	holder = (MenuHolder)row.getTag();
        }
		
        holder.MenuTextRow1.setText(data[position].text);
        holder.imgIcon.setImageResource(data[position].icon);
        
		return row;
	}
	
	public void resetFirstRow(){
		save_view.setBackgroundColor(Color.TRANSPARENT);
	}
	
	public void setNumObjCart(int NumObjCart){
		TextView tvNumObjCart = (TextView)cart_view.findViewById(R.id.MenuNumObjCart);
		tvNumObjCart.setText(String.valueOf(NumObjCart));
	}
	
    static class MenuHolder
    {
		ImageView imgIcon;
        TextView MenuTextRow1;
        TextView MenuNumObjCart;
    }
}
