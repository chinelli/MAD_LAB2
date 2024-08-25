package it.polito.mad.mad_lab2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import it.polito.mad.mad_lab2.pdf.PDFWriter;
import it.polito.mad.mad_lab2.pdf.PaperSize;
import it.polito.mad.mad_lab2.pdf.StandardFonts;
import it.polito.mad.mad_lab2.util.FileProvider;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class MapFragment2 extends Fragment implements AdapterView.OnItemClickListener, OnFornitureItemSelection,
		OnClickListener {
	private static final String TAG = MapFragment2.class.getName(); /* TAG per logging */
	private static final int ITEM_CHOICE = 0;
	private Context ctx;
	private MainActivity act;
	private DataManager db; 				/* Oggetto gestione DB */
	private int idItem; 					/* ID oggetto nella mappa */
	private int idElement; 					/* ID elemento nella mappa */
	private ArrayList<QuoteItem> qiList; 	/* Lista oggetti per il preventivo */
	private ArrayList<GridRowItem> itemList;
	private GridRowItem saveItem = null;
	private boolean save = false;
	private FornitureItem fiSelected; 		/* Elemento/Oggetto selezionato */
	private FornitureItem fiDelete;
	
	/* Grafica */
	private GridView gvObject;
	private MapSurfaceView mMapSurfaceView;
	private LinearLayout mBtnBar;
	private ImageButton mBtnRotateDx;
	private ImageButton mBtnRotateSx;
	private ImageButton mBtnDelete;

	private static final int MARGIN_H = 20;										/* Margine pdf altezza */
	private static final int MARGIN_V = 10;										/* Margine pdf verticale */
	private static final int LINE_SIZE = 20;									/* Dimensione linea pdf */
	
	/* Costruttore */
	public static MapFragment2 newInstance(float width, float height) {
		MapFragment2 f = new MapFragment2();

		/* Argomenti aggiuntivi */
		Bundle args = new Bundle();
		args.putFloat(AppConst.WIDTH_KEY, width); /* Larghezza */
		args.putFloat(AppConst.HEIGHT_KEY, height); /* Altezza */
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}

		View view = inflater.inflate(R.layout.map2_fragment, container, false);

		/* Oggetti */
		act = (MainActivity) getActivity();
		db = new DataManager(ctx);

		if (savedInstanceState != null) {
			idItem = savedInstanceState.getInt("idItem");
			idElement = savedInstanceState.getInt("idElement");
			save = savedInstanceState.getBoolean("save");
		} else {
			idItem = act.getIdFornitureItem();
			idElement = act.getIdElem();
		}

		/* Grafica */
		mMapSurfaceView = (MapSurfaceView) view.findViewById(R.id.sfView);
		gvObject = (GridView) view.findViewById(R.id.gvObject);
		mBtnBar = ((LinearLayout) view.findViewById(R.id.btnBar));
		mBtnRotateDx = (ImageButton) view.findViewById(R.id.btnRotateDx);
		mBtnRotateSx = (ImageButton) view.findViewById(R.id.btnRotateSx);
		mBtnDelete = ((ImageButton) view.findViewById(R.id.btnDelete));
		gvObject = ((GridView) view.findViewById(R.id.gvObject));

		// FornitureItem window = new FornitureItem(ctx, 0, new
		// Point((int)getArgWidth()/3, (int)getArgHeight()/3), 100, 20, 0,
		// "FornelloMap.png");
		// window.setWall(true);
		// mMapSurfaceView.addItem(window);
		//
		// mMapSurfaceView.addItem(new FornitureItem(ctx, 0, new
		// Point((int)getArgWidth()/3, (int)getArgHeight()/3), 200, 200, 45,
		// "FornelloMap.png"));

		mMapSurfaceView.setOnTouchListener(mMapSurfaceView);
		mMapSurfaceView.setOnFornitureItemSelectionListener(this);

		/* Imposto listener */
		gvObject.setOnItemClickListener(this);
		mBtnRotateSx.setOnClickListener(this);
		mBtnRotateDx.setOnClickListener(this);
		mBtnDelete.setOnClickListener(this);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		qiList = act.getQuoteList();
		itemList = act.getItemList();
		if(itemList == null){
			itemList = getObjData();
			act.setItemList(itemList);
			save = false;
		}else
			save = true;
		
		ArrayList<FornitureItem> fi_l = act.getFiList();
		ArrayList<FornitureItem> wi_l = act.getWiList();
		mMapSurfaceView.setFornitureItemList(fi_l);
		mMapSurfaceView.setWallItemList(wi_l);
		mMapSurfaceView.setDimX(getArgWidth());
		mMapSurfaceView.setDimY(getArgHeight());
		gvObject.setAdapter(new GridViewAdapter(this.ctx,
				R.layout.grid_row_item, itemList));
	}
	@Override
	public void onPause() {
		super.onPause();
		act.setIdElem(idElement);
		act.setIdFornitureItem(idItem);
		act.setFiList((ArrayList<FornitureItem>) mMapSurfaceView
				.getFornitureItemList());
		act.setWiList((ArrayList<FornitureItem>) mMapSurfaceView
				.getWallItemList());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null)
			db.closeDB();
	}

	/* Caricamento GridView */
	private ArrayList<GridRowItem> getObjData() {
		ArrayList<GridRowItem> item = new ArrayList<GridRowItem>();

		/* Carico oggetti del carrello */
		for (QuoteItem qi : qiList) {
			if (qi.getCount() > 0) {
				Cursor c = db.viewObject("ID_Object = " + qi.getId(), "Name");
				if (c != null) {
					c.moveToFirst();
					while (!c.isAfterLast()) {
						GridRowItem gridItem = new GridRowItem(c.getInt(0), c.getString(7), c.getString(1), false, 1);
						if (!item.contains(gridItem))
							item.add(gridItem);
						else
							item.get(item.indexOf(gridItem)).increment();
						c.moveToNext();
					}
					c.close();
				}
			}
		}

		/* Carico elementi (Porte, Finestre) */
		Cursor c = db.viewElement(null, "Name");
		if (c != null) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				item.add(new GridRowItem(c.getInt(0), c.getString(4), c
						.getString(1), true));
				c.moveToNext();
			}
			c.close();
		}

		return item;
	}

	/* Restituisce argomento posizione tab */
	private float getArgWidth() {
		return getArguments().getFloat(AppConst.WIDTH_KEY);
	}

	/* Restituisce argomento posizione tab */
	private float getArgHeight() {
		return getArguments().getFloat(AppConst.HEIGHT_KEY);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.map_menu, menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("idItem", idItem);
		outState.putInt("idElement", idElement);
		outState.putBoolean("save", save);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_item_menu: /* Aggiungi al carello */
			Intent i = new Intent(ctx, ChoiceActivity.class);
			startActivityForResult(i, ITEM_CHOICE);
			return true;

		case R.id.reset_room_menu: /* Reset della stanza */
			act.setWidth(-1f);
			act.setHeight(-1f);
			fiSelected = null;
			act.getFiList().clear();
			act.getWiList().clear();
			
			FragmentTransaction ft;
			Fragment f1 = new  MapFragment1();
			ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.frgContent, f1, AppConst.CONTENT_FRAGMENT);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
//			for(int i = 0; i < getFragmentManager().getBackStackEntryCount(); ++i) {    
//				getFragmentManager().popBackStack();
//			}
			return true;
			
		case R.id.share_menu:		/* Invia una mail */
			createPDF();
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
			createPDF();
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FileProvider.CONTENT_URI + "Quote.pdf")));
		return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void createPDF(){
		PDFWriter pdfWrite = new PDFWriter(PaperSize.FOLIO_WIDTH, PaperSize.FOLIO_HEIGHT);
		pdfWrite.setFont(StandardFonts.SUBTYPE, StandardFonts.TIMES_ROMAN);
		
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
		float tot= 0.0f;
		for (QuoteItem quoteItem : qiList) {
			pdfWrite.addText(MARGIN_V, beginPage, LINE_SIZE, quoteItem.getName());
			pdfWrite.addText(rightPage, beginPage, LINE_SIZE, String.valueOf(quoteItem.getPrice()));
			tot+= quoteItem.getPrice();
			beginPage = beginPage - LINE_SIZE;
		}
		
		/* Totale */
		pdfWrite.addLine(MARGIN_V, beginPage, PaperSize.FOLIO_WIDTH - MARGIN_V, beginPage);
		beginPage = beginPage - LINE_SIZE;
		pdfWrite.addText(MARGIN_V, beginPage, LINE_SIZE, getString(R.string.tot_string));
		pdfWrite.addText(rightPage, beginPage, LINE_SIZE, String.valueOf(tot));
		
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
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(fiSelected != null && fiSelected.isCollided()){
			Toast.makeText(ctx, getString(R.string.msgE3_string), Toast.LENGTH_SHORT).show();
			return;
		}
		GridRowItem sel = (GridRowItem) gvObject.getItemAtPosition(position);
		FornitureItem fi = null;

		if (sel.isElement()) {
			/* Elemento selezionato (Info) */
			Cursor c = this.db.viewElement("ID_Element = " + sel.getId(), null);
			if (c != null) {
				c.moveToFirst();
				fi = new FornitureItem(ctx, idElement, new Point(
						(int) getArgWidth() / 2, (int) getArgHeight() / 2),
						c.getInt(2), c.getInt(3), 0, c.getString(4), -1);
				c.close();
				fi.setWall(true);
				idElement++;
			}
		} else {
			save = false;
			/* Oggetto selezionato (Info) */
			Cursor c = this.db.viewObject("ID_Object = " + sel.getId(), null);
			if (c != null) {
				c.moveToFirst();
				fi = new FornitureItem(ctx, idItem, new Point(
						(int) getArgWidth() / 2, (int) getArgHeight() / 2),
						c.getInt(4), c.getInt(5), 0, c.getString(7), c.getInt(0));
				c.close();
				idItem++;
				sel.decrement();
				saveItem = sel;
				if(sel.getCount() == 0){
					itemList.remove(sel);
				}
				/* Imposto id oggetto mappa nella lista carrello */
				for (QuoteItem qi : qiList) {
					if (qi.getId() == sel.getId() && qi.getIdFornitureItem() == -1) {
						qi.setIdFornitureItem(fi.getId());
						break;
					}
				}
				gvObject.setAdapter(new GridViewAdapter(ctx,
						R.layout.grid_row_item, itemList));
			}
		}

		/* Aggiunta nella lista e nella stanza */
		if (fi != null) {
			
			long downTime = SystemClock.uptimeMillis();
			long eventTime = SystemClock.uptimeMillis() + 100;
			float[] pts = { 0f, 0f };

			// List of meta states found here:
			// developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
			int metaState = 0;

			if (fi.isWall()){
				fi.setCenter(new android.graphics.Point(0+fi.getLength()/2,0-fi.getHeight()/2));
			}
			
			pts = mMapSurfaceView.roomToPx(fi.getCenter().x,
						fi.getCenter().y);
			MotionEvent motionEventDown = MotionEvent.obtain(downTime, eventTime,
					MotionEvent.ACTION_DOWN, pts[0], pts[1], metaState);
			MotionEvent motionEventMove = MotionEvent.obtain(downTime, eventTime,
					MotionEvent.ACTION_MOVE, pts[0], pts[1], metaState);
			MotionEvent motionEventUp = MotionEvent.obtain(downTime, eventTime,
					MotionEvent.ACTION_UP, pts[0], pts[1], metaState);
			
			mMapSurfaceView.addItem(fi);
			mMapSurfaceView.dispatchTouchEvent(motionEventDown);
			mMapSurfaceView.dispatchTouchEvent(motionEventMove);
			mMapSurfaceView.dispatchTouchEvent(motionEventUp);
			motionEventDown.recycle();
			motionEventMove.recycle();
			motionEventUp.recycle();
			
			
		}
	}

	@Override
	public void OnFornitureItemSelected(FornitureItem fi) {
		AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setDuration(300);
		fiSelected = fi;
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				mBtnBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		if (fiSelected.isWall()) {
			mBtnRotateDx.setVisibility(View.INVISIBLE);
			mBtnRotateSx.setVisibility(View.INVISIBLE);
		} else {
			mBtnRotateDx.setVisibility(View.VISIBLE);
			mBtnRotateSx.setVisibility(View.VISIBLE);
		}
		if (mBtnBar.getVisibility() == View.GONE)
			mBtnBar.startAnimation(anim);
	}

	@Override
	public void OnFornitureItemDeselected() {
		AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
		anim.setDuration(300);
		fiSelected = null;
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mBtnBar.setVisibility(View.GONE);
			}
		});

		if (mBtnBar.getVisibility() == View.VISIBLE)
			mBtnBar.startAnimation(anim);

	}

	/* Listener dialog elimina oggetto */
	DialogInterface.OnClickListener dialogDelObjectListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				if(saveItem != null){
					if(saveItem.getCount() == 0){
						itemList.add(saveItem);
						gvObject.setAdapter(new GridViewAdapter(ctx,
							R.layout.grid_row_item, itemList));
					}
					saveItem.increment();
				}
				if(save){
					gvObject.setAdapter(new GridViewAdapter(ctx,
							R.layout.grid_row_item, itemList));
				}
				fiDelete = null;
				dialog.dismiss();
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				if(save){
					Cursor c = db.viewObject("ID_Object = " + fiDelete.getIdDB(), "Name");
					if (c != null) {
						c.moveToFirst();
						GridRowItem gridItem = new GridRowItem(c.getInt(0), c.getString(7), c.getString(1), false, 1);
						if (itemList.contains(gridItem) && itemList.get(itemList.indexOf(gridItem)).getCount() == 0)
							itemList.remove(gridItem);
						c.close();
					}
				}
				/* Elimino oggetto dal carrello */
				for (QuoteItem qi : qiList) {
					if (qi.getIdFornitureItem() == fiDelete.getId()) {
						qiList.remove(qi);
						break;
					}
				}
				fiDelete = null;
				act.delQuoteItem();
				dialog.dismiss();
				break;
			}

		}
	};

	@Override
	public void onClick(View v) {
		long downTime = SystemClock.uptimeMillis();
		long eventTime = SystemClock.uptimeMillis() + 100;
		float[] pts = { 0f, 0f };

		// List of meta states found here:
		// developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
		int metaState = 0;

		if (fiSelected != null) {
			pts = mMapSurfaceView.roomToPx(fiSelected.getCenter().x,
					fiSelected.getCenter().y);

		}
		MotionEvent motionEventDown = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_DOWN, pts[0], pts[1], metaState);
		MotionEvent motionEventMove = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_MOVE, pts[0], pts[1], metaState);
		MotionEvent motionEventUp = MotionEvent.obtain(downTime, eventTime,
				MotionEvent.ACTION_UP, pts[0], pts[1], metaState);
		switch (v.getId()) {
		case R.id.btnRotateSx:
			if (fiSelected != null && !fiSelected.isWall()) {
				fiSelected.setRotation(fiSelected.getRotation() + 15);
				mMapSurfaceView.dispatchTouchEvent(motionEventDown);
				mMapSurfaceView.dispatchTouchEvent(motionEventMove);
				mMapSurfaceView.dispatchTouchEvent(motionEventUp);
			}
			break;
		case R.id.btnRotateDx:
			if (fiSelected != null && !fiSelected.isWall()) {
				fiSelected.setRotation(fiSelected.getRotation() - 15);
				mMapSurfaceView.dispatchTouchEvent(motionEventDown);
				mMapSurfaceView.dispatchTouchEvent(motionEventMove);
				mMapSurfaceView.dispatchTouchEvent(motionEventUp);
			}
			break;
			
		case R.id.btnDelete:
			if (fiSelected != null) {
				mMapSurfaceView.dispatchTouchEvent(motionEventDown);
				mMapSurfaceView.dispatchTouchEvent(motionEventMove);
				mMapSurfaceView.dispatchTouchEvent(motionEventUp);
				if (!fiSelected.isWall()) {
					if(save){
						Cursor c = db.viewObject("ID_Object = " + fiSelected.getIdDB(), "Name");
						if (c != null) {
							c.moveToFirst();
							GridRowItem gridItem = new GridRowItem(c.getInt(0), c.getString(7), c.getString(1), false, 1);
							if (!itemList.contains(gridItem))
								itemList.add(gridItem);
							else
								itemList.get(itemList.indexOf(gridItem)).increment();
							c.close();
						}
					}
					AlertDialog.Builder buildDel = new AlertDialog.Builder(
							getActivity());
					buildDel.setTitle(getString(R.string.dialog1_title_string));
					buildDel.setIcon(R.drawable.ic_action_warning);
					buildDel.setMessage(getString(R.string.dialog1_msg_string))
							.setPositiveButton(
									getString(R.string.dialog1_yes_string),
									dialogDelObjectListener)
							.setNegativeButton(
									getString(R.string.dialog1_no_string),
									dialogDelObjectListener).show();
				}
				mMapSurfaceView.removeItem(fiSelected);
				fiDelete = fiSelected;
				mMapSurfaceView.dispatchTouchEvent(motionEventDown);
				mMapSurfaceView.dispatchTouchEvent(motionEventMove);
				mMapSurfaceView.dispatchTouchEvent(motionEventUp);
			}
			break;
			
		default:
			break;
			
		}
		motionEventDown.recycle();
		motionEventMove.recycle();
		motionEventUp.recycle();

	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null && requestCode == ITEM_CHOICE){
			int itemId = data.getIntExtra("itemId", -1);
			String name = data.getStringExtra("name");
			if (itemId > 0) {
				act.addQuoteItem(itemId);
				if(name != null)
					Toast.makeText(ctx, getString(R.string.added_cart_string, new Object[] {name}), Toast.LENGTH_SHORT).show();
				else
					Toast.makeText(ctx, getString(R.string.added_cart_string), Toast.LENGTH_SHORT).show();
			}
			gvObject.setAdapter(new GridViewAdapter(ctx, R.layout.grid_row_item, itemList));
		}
	}
	
	/* Centra oggetto nel pdf */
	private int centerObjectPDF(int pageWidth, int len){
		int wMiddle = pageWidth / 2;
		wMiddle = wMiddle - (len / 2);
		return wMiddle;
	}

}
