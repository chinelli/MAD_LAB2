package it.polito.mad.mad_lab2;

import it.polito.mad.mad_lab2.pdf.PDFWriter;
import it.polito.mad.mad_lab2.pdf.PaperSize;
import it.polito.mad.mad_lab2.pdf.StandardFonts;
import it.polito.mad.mad_lab2.util.FileProvider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class QuoteFragment extends Fragment {
	private static final String TAG = QuoteFragment.class.getName();			/* TAG per logging */
	private static final int MARGIN_H = 20;										/* Margine pdf altezza */
	private static final int MARGIN_V = 10;										/* Margine pdf verticale */
	private static final int LINE_SIZE = 20;									/* Dimensione linea pdf */
	
	private Context ctx;
	private Activity act;
	private float total;														/* Prezzo totale */
	private ArrayList<QuoteItem> qiList;										/* Lista oggetti nel carello */
	
	/* Grafica */
	private LinearLayout objLayout;
	private TextView tvTot;
	
	/* Costruttore */
    public static QuoteFragment newInstance(ArrayList<QuoteItem> qiList) {
    	QuoteFragment f = new QuoteFragment();
    	
    	/* Argomenti aggiuntivi */
        Bundle args = new Bundle();
        args.putParcelableArrayList(AppConst.OBJ_BUY_KEY, qiList);
        f.setArguments(args);

        return f;
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		act = activity;
		ctx = activity.getApplicationContext();
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		View view = inflater.inflate(R.layout.quote_fragment, container, false);
		
		/* Oggetti */
		qiList = getArgQuoteList();
	    
		/* Grafica */
		objLayout = (LinearLayout) view.findViewById(R.id.llObjPrice);
		tvTot = (TextView) view.findViewById(R.id.tvTot);
		
		total = 0;
		for (QuoteItem quoteItem : qiList) {
			final View childView = LayoutInflater.from(getActivity()).inflate(R.layout.quote_item, objLayout, false);
			
			/* Grafica lista */
			ImageView imgIcon = (ImageView) childView.findViewById(R.id.imgIcon);
			TextView tvObject = (TextView) childView.findViewById(R.id.tvObject);
			TextView tvDescr = (TextView) childView.findViewById(R.id.tvDescr);
			TextView tvPrice = (TextView) childView.findViewById(R.id.tvPrice);
			ImageButton btnRemove = (ImageButton) childView.findViewById(R.id.btnRemove);
			
			Drawable icon = Retrieve(quoteItem.getImage());
		    if(icon != null)
		    	imgIcon.setImageDrawable(icon);
		    else
		    	imgIcon.setBackgroundResource(R.drawable.ic_catalog_menu);
			
			final int id = quoteItem.getId();
			btnRemove.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
					/* Rimuovo dalla lista */
					MainActivity act = (MainActivity) getActivity();
					act.delQuoteItem(id);
					updateTotPrice();
	            	objLayout.removeView(childView);
	            }
	        });
			
			tvObject.setText(quoteItem.getName());
			tvDescr.setText(quoteItem.getDescr());
			tvPrice.setText(String.valueOf(quoteItem.getPrice()));
			total = total + quoteItem.getPrice();
			objLayout.addView(childView, 0);
		}
		
		/* Scrivo la cifra totale dei mobili */
		tvTot.setText(String.valueOf(total));
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.quote_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		PDFWriter pdfWrite = new PDFWriter(PaperSize.FOLIO_WIDTH, PaperSize.FOLIO_HEIGHT);
		pdfWrite.setFont(StandardFonts.SUBTYPE, StandardFonts.TIMES_ROMAN);
		ArrayList<QuoteItem> qiList = getArgQuoteList();
		
		/* Dimensione */
		int beginPage = PaperSize.FOLIO_HEIGHT - MARGIN_H;
		int rightPage = PaperSize.FOLIO_WIDTH - 60;
		
		/* Titolo */
		Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		beginPage = beginPage - icon.getHeight();
		pdfWrite.addImage(MARGIN_V, beginPage, icon);
		pdfWrite.addText(centerObjectPDF(PaperSize.FOLIO_WIDTH, getString(R.string.app_name).length()), PaperSize.FOLIO_HEIGHT - MARGIN_H - (icon.getHeight() / 2), LINE_SIZE, getString(R.string.app_name));
		beginPage = beginPage - 30;
		
		
		MainActivity mainAct = (MainActivity)act;
		Bitmap room = null;
		try {
			MapSurfaceView mMapSurfaceView = new MapSurfaceView(ctx);
			mMapSurfaceView.setDimX(mainAct.getWidth());
			mMapSurfaceView.setDimY(mainAct.getHeight());
			mMapSurfaceView.setWallItemList(mainAct.getWiList());
			mMapSurfaceView.setFornitureItemList(mainAct.getFiList());
			room = mMapSurfaceView.saveView();
		} catch (Exception e) {
			Log.e(TAG, "Exception " + e.getMessage());
		}
		
		/* Immagine stanza */
		
		if(room != null){
			beginPage = beginPage - room.getHeight();			
			pdfWrite.addImage(centerObjectPDF(PaperSize.FOLIO_WIDTH, room.getWidth()), beginPage, room);
		}else{
			pdfWrite.addText(100, beginPage, LINE_SIZE, getString(R.string.msgE2_string));
		}
		
		/* Oggetti carello */
		beginPage = beginPage - 50;
		pdfWrite.addText(MARGIN_V, beginPage, LINE_SIZE, getString(R.string.detail_quote_string));
		beginPage = beginPage - 40;
		for (QuoteItem quoteItem : qiList) {
			pdfWrite.addText(MARGIN_V, beginPage, LINE_SIZE, quoteItem.getName());
			pdfWrite.addText(rightPage, beginPage, LINE_SIZE, String.valueOf(quoteItem.getPrice()));
			beginPage = beginPage - LINE_SIZE;
		}
		
		/* Totale */
		pdfWrite.addLine(MARGIN_V, beginPage, PaperSize.FOLIO_WIDTH - MARGIN_V, beginPage);
		beginPage = beginPage - LINE_SIZE;
		pdfWrite.addText(MARGIN_V, beginPage, LINE_SIZE, getString(R.string.tot_string));
		pdfWrite.addText(rightPage, beginPage, LINE_SIZE, tvTot.getText().toString());
		
		/* Scrittura pdf */
		String pdfString = pdfWrite.asString();
		FileOutputStream outputStream;
		try {
			outputStream = ctx.openFileOutput("Quote.pdf", Context.MODE_PRIVATE);
			outputStream.write(pdfString.getBytes());
			outputStream.close();
		} catch (Exception e) {
			Log.e(TAG, "Exception " + e.getMessage());
		}		
		
		switch (item.getItemId()) {	            
			case R.id.share_menu:		/* Invia una mail */
				Intent sendIntent = new Intent(Intent.ACTION_SEND);
				sendIntent.setType("application/pdf");
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_mail_string));
				sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text_mail_string));
				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(FileProvider.CONTENT_URI + "Quote.pdf")); 
				try {
					startActivity(Intent.createChooser(sendIntent, getString(R.string.send_mail_string)));
				} catch (ActivityNotFoundException ex) {
				    Toast.makeText(ctx, getString(R.string.msgE1_string), Toast.LENGTH_SHORT).show();
				}					
			return true;
			
			case R.id.preview_menu:		/* Visualizza il preview del pdf */
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FileProvider.CONTENT_URI + "Quote.pdf")));
			return true;
			
			default:
			return super.onOptionsItemSelected(item);
		}	
	}
	
	/* Restituisce argomento lista degli oggetti nel carrello */
	private ArrayList<QuoteItem> getArgQuoteList(){
		return getArguments().getParcelableArrayList(AppConst.OBJ_BUY_KEY);
	}

	/* Aggiornamento totale */
	private void updateTotPrice(){
		total = 0;
		for (QuoteItem quoteItem : qiList) {
			total = total + quoteItem.getPrice();
		}
		tvTot.setText(String.valueOf(total));
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
	
	/* Centra oggetto nel pdf */
	private int centerObjectPDF(int pageWidth, int len){
		int wMiddle = pageWidth / 2;
		wMiddle = wMiddle - (len / 2);
		return wMiddle;
	}
}
