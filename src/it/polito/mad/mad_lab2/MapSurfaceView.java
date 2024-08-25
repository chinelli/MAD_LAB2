package it.polito.mad.mad_lab2;

import it.polito.mad.mad_lab2.util.Point;
import it.polito.mad.mad_lab2.util.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
 
public class MapSurfaceView extends View implements OnTouchListener {
	private static final int DRAG = 0;
	private static final int SCROLL = 1;
	private static final int READY = 2;
	private static final int ZOOM = 3;
	private static final int TOP = 0;
	private static final int BOTTOM = 1;
	private static final int LEFT = 2;
	private static final int RIGHT = 3;
	private ScaleGestureDetector mScaleDetector;
	private GestureDetector mGestureDetector;
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Random random;
	private Context ctx;
	private List<FornitureItem> fi_l = new ArrayList<FornitureItem>();
	private List<FornitureItem> wi_l = new ArrayList<FornitureItem>();
	private OnFornitureItemSelection mCallback;
	private float dim_y = 0;
	private float dim_x = 0;

	private float divY;
	private float divX;
	private float divX_or;
	private float pivotX = 0;
	private float pivotY = 0;
	private FornitureItem fiTouched;
	private Matrix drawMatrix;
	private int mode;
	private float mTouchXPosition;
	private float mTouchYPosition;
	private float mOldTouchXPosition;
	private float mOldTouchYPosition;
	private float scrollX;
	private float scrollY;
	private Matrix invertedDrawMtx;
	private FornitureItem wiTouched;
	
	
	public void setOnFornitureItemSelectionListener(OnFornitureItemSelection obj){
		mCallback = obj;
	}
	
	public MapSurfaceView(Context context) {
		super(context);
		this.ctx = context;
		random = new Random();
		init();
	}

	
	private void init() {
		drawMatrix = new Matrix();
		invertedDrawMtx = new Matrix();
		mGestureDetector = new GestureDetector(ctx, new OnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {

			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				if (fiTouched != null) return; // Avoid selecting multiple object
				float[] vect = { e.getX(), e.getY() };
				invertedDrawMtx.mapPoints(vect);
				int x = (int) vect[0];
				int y = (int) vect[1];
				Log.i("MAP", "LONG - " + x +" "+y);
				fiTouched = getTouchedObject(x, y);
				if (fiTouched != null) {
					Log.i("MAP", "LONG - " + fiTouched.getRotation());
					mode = DRAG;
				}

			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				return false;
			}

			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}
		});
		mScaleDetector = new ScaleGestureDetector(ctx,
				new OnScaleGestureListener() {
					float[] pts = {0,0};
					private float scale;

					@Override
					public void onScaleEnd(ScaleGestureDetector detector) {
					}

					@Override
					public boolean onScaleBegin(ScaleGestureDetector detector) {
	
						pts[0] = detector.getFocusX();
						pts[1] = detector.getFocusY();
						invertedDrawMtx.mapPoints(pts);
						pivotX = pts[0];
						pivotY = pts[1];
						scale = divX;
						return true;
					}

					@Override
					public boolean onScale(ScaleGestureDetector detector) {
						Log.i("MAP",
								"zoom ongoing, scale: "
										+ detector.getScaleFactor());
						divX = detector.getScaleFactor() * scale;

						// Don't let the object get too small or too large.
						divX = Math.max(divX_or*0.9f, Math.min(divX, divX_or*2f));
						pts[0] = detector.getFocusX();
						pts[1] = detector.getFocusY();
						invertedDrawMtx.mapPoints(pts);
						//pivotX = pts[0];
						//pivotY = pts[1];
						return false;
					}
				});

	}

	public MapSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ctx = context;
		random = new Random();
		init();
	}

	public MapSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.ctx = context;
		random = new Random();
		init();
	}

	public float getDimY() {
		return dim_y;
	}

	public void setDimY(float dim_y) {
		this.dim_y = dim_y;
	}

	public float getDimX() {
		return dim_x;
	}

	public void setDimX(float dim_x) {
		this.dim_x = dim_x;
	}

	public void addItem(FornitureItem item) {
		if(item.isWall()){
			
			wi_l.add(item);
		}
		else
			fi_l.add(item);
		invalidate();
	}
	
	public List<FornitureItem> getFornitureItemList() {
		return fi_l;
	}

	public List<FornitureItem> getWallItemList() {
		return wi_l;
	}

	public void setFornitureItemList(List<FornitureItem> fi_l) {
		if (fi_l == null)
			this.fi_l =  new ArrayList<FornitureItem>();
		else 
			this.fi_l =  fi_l;
	}

	public void setWallItemList(List<FornitureItem> wi_l) {
		if (wi_l == null)
			this.wi_l =  new ArrayList<FornitureItem>();
		else 
			this.wi_l =  wi_l;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawMatrix.reset();
		paint.setColor(Color.BLACK);
		//canvas.getClipBounds(clipRect);
		//canvas.drawRect(clipRect, paint);
		//clipRect.inset(-clipRect.width() / 2, -clipRect.height() / 2);
		// SORRY: how to draw partially visible items?
		//canvas.clipRect(clipRect, Op.REPLACE);
	
		if (divX == 0) {
			float w = canvas.getWidth();
			float h = canvas.getHeight();
			if (w != 0) {
				divX = w / (dim_x+100);
			}
			if (h != 0) {
				divY = h / (dim_y+100);
			}
			if (divY<divX)
				divX=divY;
			
			int roomPxsX = (int) (divX*dim_x);
			int roomPxsY = (int) (divX*dim_y);
			scrollX = (w-roomPxsX)/2;
			scrollY = (h-roomPxsY)/2;
			divX_or = divX;
		}
		if (scrollX != 0) {
			drawMatrix.setTranslate(scrollX, scrollY);
			canvas.translate(scrollX, scrollY);
		}

		drawMatrix.preScale(divX, divX, pivotX, pivotY);
		canvas.scale(divX, divX, pivotX, pivotY);
		drawMatrix.invert(invertedDrawMtx);
		updateAnimation(canvas,getWidth(),getHeight());
		drawRoom(canvas,dim_x,dim_y);
	}
	
	
	public float[] roomToPx(float x,float y) {
		float[] pts={x,y};
		drawMatrix.mapPoints(pts);
		return pts;
	}

	private void drawRoom(Canvas canvas,float size_x,float size_y) {
		Paint aux_p = new Paint(Paint.ANTI_ALIAS_FLAG);
		aux_p.setStyle(Paint.Style.STROKE);
		aux_p.setColor(Color.BLACK);
		aux_p.setStrokeWidth(1);
		canvas.save();
//		Matrix mtx = new Matrix();
//		mtx.setTranslate(dim_x / 2, dim_y / 2);
//		canvas.concat(mtx);
		canvas.translate(size_x/2,size_y/2);
		RectF room =new RectF(-size_x / 2, -size_y / 2, size_x / 2, size_y / 2);
		canvas.drawRect(room, aux_p);
		room.inset(-20, -20);
		canvas.drawRect(room, aux_p);
		canvas.restore();
	}

	/* Aggiorna canvas */
	public void updateAnimation(Canvas c,float w, float h) {

		Paint bold = new Paint();
		bold.setColor(Color.LTGRAY);
		bold.setStrokeWidth(0f);
		bold.setFlags(Paint.ANTI_ALIAS_FLAG);
		Paint light = new Paint();
		light.setFlags(Paint.ANTI_ALIAS_FLAG);
		light.setColor(Color.LTGRAY);
		light.setStrokeWidth(0f);
		// Draw the minor grid lines
		float dim_M = 50f;
		float dim_m = 5f;
		
		float major_tile = 50f; 
		float minor_tile = 10f;
		float[] origin = {0,0};
		float[] end = {w,h};
		invertedDrawMtx.mapPoints(origin);
		invertedDrawMtx.mapPoints(end);
		/*
		//DRAW V RIGHT MINOR LINES
		for (float i = 0; i < end[0]; i+=minor_tile) {
			c.drawLine(i, origin[1], i, end[1], light);		
		}
		
		//DRAW V LEFT MINOR LINES
		for (float i = 0; i > origin[0]; i-=minor_tile) {
			c.drawLine(i, origin[1], i, end[1], light);		
		}
		
		//DRAW H BOTTOM MINOR LINES
		for (float i = 0; i < end[1]; i+=minor_tile) {
			c.drawLine(origin[0],i , end[0], i, light);		
		}
		
		//DRAW H TOP MINOR LINES
		for (float i = 0; i > origin[1]; i-=minor_tile) {
			c.drawLine(origin[0], i, end[0], i, light);		
		}
		*/
		//DRAW V RIGHT MAJOR LINES
		for (float i = major_tile; i < end[0]; i+=major_tile) {
			c.drawLine(i, origin[1], i, end[1], bold);		
		}
		
		//DRAW V LEFT MAJOR LINES
		for (float i = 0; i > origin[0]; i-=major_tile) {
			c.drawLine(i, origin[1], i, end[1], bold);		
		}
		
		//DRAW H BOTTOM MAJOR LINES
		for (float i = major_tile; i < end[1]; i+=major_tile) {
			c.drawLine(origin[0],i , end[0], i, bold);		
		}
		
		//DRAW H TOP MAJOR LINES
		for (float i = 0; i > origin[1]; i-=major_tile) {
			c.drawLine(origin[0], i, end[0], i, bold);		
		}
			
		for (FornitureItem f : fi_l) {
			drawItem(c, f);
		}
		for (FornitureItem wi : wi_l) {
			drawItem(c, wi);
		}
		if (fiTouched!=null)
			drawItem(c, fiTouched);
	}
	

	/* Disegna oggetto */
	private void drawItem(Canvas c, FornitureItem f) {
		c.save();
		c.translate(f.getCenter().x, f.getCenter().y);
		c.rotate(f.getRotation());
		
			
		if (f.isCollided())
			paint.setColor(Color.RED);
		else
			paint.setColor(Color.BLUE);
		Drawable d= f.getImage();
		d.setDither(true);
		d.setFilterBitmap(true);
		if(f == fiTouched){
			Rect rAux = new Rect(-f.getLength() / 2, -f.getHeight() / 2, 
					  f.getLength() / 2, f.getHeight() / 2);
			rAux.inset(-3, -3);
			c.drawRect(rAux, paint);
		}
		d.setBounds(new Rect(-f.getLength() / 2, -f.getHeight() / 2, 
							f.getLength() / 2, f.getHeight() / 2));
		d.draw(c);
		c.restore();
	}

	/* Salva il canvas su bitmap */
	public Bitmap saveView(){
		Bitmap  bitmap = Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888);
		Canvas cBitmap = new Canvas(bitmap);
		this.draw4PDF(cBitmap);
		return bitmap;
	}
	private void draw4PDF(Canvas cBitmap) {
		float w = 512;
		float h = 512;
		Paint pdfBack = new Paint();
		pdfBack.setColor(Color.WHITE);
		Rect area =new Rect(0, 0, (int)w, (int)h);
		cBitmap.drawRect(area, pdfBack);
		
		if (divX == 0) {
			
			if (w != 0) {
				divX = (float) (w / (float)(dim_x+100.0f));
			}
			
			if (h != 0) {
				divY = (float) (h / (float)(dim_y+100.0f));
			}
			if (divY<divX)
				divX=divY;
			
			int roomPxsX = (int) (divX*dim_x);
			int roomPxsY = (int) (divX*dim_y);
			scrollX = (w-roomPxsX)/2;
			scrollY = (h-roomPxsY)/2;
		}
		if (scrollX != 0) {
			cBitmap.translate(scrollX, scrollY);
		}
		cBitmap.scale(divX, divX, 0, 0);
		updateAnimation(cBitmap,w,h);
		drawRoom(cBitmap,dim_x,dim_y);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		float[] vect = { event.getX(), event.getY() };
		invertedDrawMtx.mapPoints(vect);
		int x = (int) vect[0];
		int y = (int) vect[1];
		// Log.i("MAP", "TOUCH");
		mScaleDetector.onTouchEvent(event);
		if (mScaleDetector.isInProgress()){
			mode = ZOOM;
			invalidate();
			return true;
		}
			
		
		mGestureDetector.onTouchEvent(event);
		// return true;
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				Log.i("MAP", "DOWN " +x +" "+y );
				if (fiTouched != null && fiTouched.isCollided())
				{
					invalidate();
					return true;
				}
				fiTouched = getTouchedObject(x, y);
				if (fiTouched == null)
					fiTouched = getWallTouchedObject(x, y);
				if (fiTouched != null ) {
					Log.i("MAP", "DOWN - " + fiTouched.getId());
					mode = DRAG;
					mCallback.OnFornitureItemSelected(fiTouched);
				} else {
					mCallback.OnFornitureItemDeselected();
					Log.i("MAP", "DOWN - deselect");
					if (mode != ZOOM ){
						mode = SCROLL;
						mTouchXPosition = event.getX();
						mTouchYPosition = event.getY();
					}
				}
				invalidate();
				return true;
			case MotionEvent.ACTION_MOVE:
				if (fiTouched != null && mode == DRAG && !mScaleDetector.isInProgress()) {
					int[] newxy= {x,y};
//					if (x > dim_x)
//						x= dim_x;
//					if (y > dim_y)
//						y= dim_y;
				
					if (fiTouched.isWall()){
						
						int edge = getNearestSegmentInPerimeter(x, y);
						switch (edge) {
						case TOP:
							fiTouched.setRotation(0);
							newxy[1]=0-fiTouched.getHeight()/2;
							newxy[0] = (int) Math.max(fiTouched.getLength()/2, Math.min(x, dim_x-fiTouched.getLength()/2));
							break;
						case BOTTOM:
							fiTouched.setRotation(180);
							newxy[1]=(int) dim_y+fiTouched.getHeight()/2;
							newxy[0] = (int) Math.max(fiTouched.getLength()/2, Math.min(x, dim_x-fiTouched.getLength()/2));
							break;
						case RIGHT:
							fiTouched.setRotation(90);
							newxy[0]=(int) dim_x+fiTouched.getHeight()/2;
							newxy[1] = (int) Math.max(fiTouched.getLength()/2, Math.min(y, dim_y-fiTouched.getLength()/2));
							break;
						case LEFT:
							
							fiTouched.setRotation(270);
							newxy[0]=0-fiTouched.getHeight()/2;
							newxy[1] = (int) Math.max(fiTouched.getLength()/2, Math.min(y, dim_y-fiTouched.getLength()/2));
							break;
						}
						
						
					}
					else if (!checkbounds(newxy,fiTouched)) {
						Matrix m = new Matrix();
						m.preRotate(fiTouched.getRotation());
						RectF bb = new RectF(-fiTouched.getLength() / 2, -fiTouched.getHeight() / 2, 
								fiTouched.getLength() / 2, fiTouched.getHeight() / 2);
						m.mapRect(bb);
						
						float lenght_2 = (bb.right-bb.left)/2f;
						float height_2 = (bb.bottom-bb.top)/2f;
						Log.i("MAP", "BB "+lenght_2 +" "+height_2);
						if((x-lenght_2)<0) {
							newxy[0] = (int) lenght_2+1;
							Log.i("MAP", "BNDS LEFT ");
						}
						else if ((x+lenght_2)>dim_x)
						{
							newxy[0] = (int) (dim_x-lenght_2);
							Log.i("MAP", "BNDS RIGHT ");
						}
						if((y-height_2)<0) {
							newxy[1] = (int) height_2+1;
							Log.i("MAP", "BNDS TOP ");
						}
						else if ((y+height_2)>dim_y)
						{
							Log.i("MAP", "BNDS BOTTOM ");
							newxy[1] = (int) (dim_y-height_2);
							
						}
						Log.i("MAP", "NEW CNTR - " + newxy[0] +" " + newxy[1]);
					}
					fiTouched.setCenter(new android.graphics.Point(newxy[0], newxy[1]));
					
					if(detectCollision(fiTouched)) {
						fiTouched.setCollided(true);
					}
					else 
						fiTouched.setCollided(false);
					Log.i("MAP", "DRAG");
				} else if (mode == SCROLL && event.getPointerCount() == 1 && !mScaleDetector.isInProgress()) {
					Log.i("MAP", "SCROLL");
					mOldTouchXPosition = mTouchXPosition;
					mOldTouchYPosition = mTouchYPosition;
					mTouchXPosition = event.getX();
					mTouchYPosition = event.getY();
					Math.max(divX_or*0.9f, Math.min(divX, divX_or*2f));
					float scroll = 0f;
					scroll = mTouchXPosition - mOldTouchXPosition + scrollX;
					if ((mTouchXPosition - mOldTouchXPosition + scrollX) > 0)
						scrollX = Math.min(scroll,getWidth()*0.7f);
					else
						scrollX = Math.max(scroll ,-getWidth()*0.7f);
					scroll = mTouchYPosition - mOldTouchYPosition + scrollY;
					if ((scroll) > 0)
						scrollY =  Math.min(scroll,getHeight()*0.7f);
					else
						scrollY =  Math.max(scroll,-getHeight()*0.7f);

					
					Log.i("MAP", "SCROLL - " + scrollX + " " + scrollY);
				}
				invalidate();
			return true;

			case MotionEvent.ACTION_UP:
				Log.i("MAP", "UP");
				if (fiTouched == null || (fiTouched != null && !fiTouched.isCollided()) )
				{
					mode = READY;
				}
				invalidate();
				return true;
		}
		invalidate();
		return false;
	}

	private FornitureItem getWallTouchedObject(int x, int y) {
		float[] vect = { x, y };
		Matrix m = new Matrix();
		// Rotate
		for (FornitureItem wi : wi_l) {
			vect[0]= x;
			vect[1]= y ;
			if (!m.isIdentity())
				Log.i("MAP", "NOT IDENTITY");
			Log.i("MAP", "WT center" + String.valueOf(wi.getCenter().x) + " "
							+ String.valueOf(wi.getCenter().y));
			m.setTranslate(-wi.getCenter().x, -wi.getCenter().y);
			Log.i("MAP", "WT rotation" + String.valueOf(wi.getRotation()));
			m.postRotate(-wi.getRotation());
			m.mapPoints(vect);
			vect[0] = (int) vect[0];
			vect[1] = (int) vect[1];
			Log.i("MAP", "WT coord " + String.valueOf(vect[0])
					+ " " + String.valueOf(vect[1]));
			Log.i("MAP", " WT rect sizes " + wi.getLength() +" "+ wi.getHeight());
			
			if (vect[1] >= -wi.getHeight() / 2 && vect[1] <= wi.getHeight() / 2
					&& vect[0] >= -wi.getLength() / 2
					&& vect[0] <= wi.getLength() / 2)
				return wi;
			m.reset();
		}
		return null;
	}

	private boolean checkbounds(int[] newxy, FornitureItem fi) {
		RectF item = new RectF();
		RectF room = new RectF(0, 0, dim_x, dim_y);
		Matrix m = new Matrix();
		m.setTranslate(newxy[0], newxy[1]);
		m.preRotate(fi.getRotation());
		item.set(-fi.getLength() / 2, -fi.getHeight() / 2,
				fi.getLength() / 2, fi.getHeight() / 2);
		m.mapRect(item);
		RectF originalRoom = new RectF(room);
		room.union(item);
		Log.i("MAP","BNDS "+ room.equals(originalRoom));

		return room.equals(originalRoom);
	}

	private boolean detectCollision(FornitureItem fi) {
		Matrix m = new Matrix();
		
		float[] vtxsT = {-fi.getLength() / 2,-fi.getHeight() / 2,
				 fi.getLength() / 2,-fi.getHeight() / 2,
				 fi.getLength() / 2, fi.getHeight() / 2,
				-fi.getLength() / 2, fi.getHeight() / 2 };
		
		float[] vtxs = {-fi.getLength() / 2,-fi.getHeight() / 2,
				 fi.getLength() / 2,-fi.getHeight() / 2,
				 fi.getLength() / 2, fi.getHeight() / 2,
				-fi.getLength() / 2, fi.getHeight() / 2 };
		

		m.setTranslate(fi.getCenter().x, fi.getCenter().y);
		m.preRotate(fi.getRotation());
		m.mapPoints(vtxsT);
		Polygon touchedPoly = Polygon.Builder()
					        .addVertex(new Point(vtxsT[0], vtxsT[1])) // polygon
					        .addVertex(new Point(vtxsT[2], vtxsT[3]))
					        .addVertex(new Point(vtxsT[4], vtxsT[5]))
					        .addVertex(new Point(vtxsT[6], vtxsT[7]))
					        .close()
					        .build();
		
		Log.i("MAP","COL touchedRect " + fi.getCenter().x + " "+ fi.getCenter().y);
		List<FornitureItem> li = null;
		if (fi.isWall())
			li = wi_l ;
		else
			li = fi_l;
		
		for(FornitureItem f : li) {
			if (f == fi) continue;
			m.reset();
			vtxs[0] = -f.getLength() / 2;
			vtxs[1] = -f.getHeight() / 2;
			vtxs[2] =  f.getLength() / 2;
			vtxs[3] = -f.getHeight() / 2;
			vtxs[4] =  f.getLength() / 2;
			vtxs[5] =  f.getHeight() / 2;
			vtxs[6] = -f.getLength() / 2;
			vtxs[7] =  f.getHeight() / 2;
			
			m.setTranslate(f.getCenter().x, f.getCenter().y);
			m.preRotate(f.getRotation());
			m.mapPoints(vtxs);
			
			Polygon auxPoly = Polygon.Builder()
			        .addVertex(new Point(vtxs[0], vtxs[1])) // polygon
			        .addVertex(new Point(vtxs[2], vtxs[3]))
			        .addVertex(new Point(vtxs[4], vtxs[5]))
			        .addVertex(new Point(vtxs[6], vtxs[7]))
			        .close()
			        .build();
			for(int i= 0 ; i< touchedPoly.getSides().size();i++) {
				int j = i*2;
				if (auxPoly.contains(new Point(vtxsT[j],vtxsT[j+1]))){
					Log.i("MAP","COL "+ f.getCenter().x + " "+ f.getCenter().y);
					return true;
				}
			}
			for(int i= 0 ; i< auxPoly.getSides().size();i++) {
				int j = i*2;
				if (touchedPoly.contains(new Point(vtxs[j],vtxs[j+1]))) {
						Log.i("MAP","COL "+ f.getCenter().x + " "+ f.getCenter().y);
						return true;
				}
			}
			for(int i= 0 ; i< touchedPoly.getSides().size();i++) {
				int j=(i+1)%4;
				int x_i = i*2;
				int y_i = x_i+1;
				int x_j = j*2;
				int y_j= x_j+1;
				//Log.i("MAP","vtxsT "+ x_i + " "+ y_i + "; "+x_j + " "+ y_j);
				for(int k= 0 ; k< auxPoly.getSides().size();k++) {
					int z=(k+1)%4;
					int x_k = k*2;
					int y_k = x_k+1;
					int x_z = z*2;
					int y_z = x_z+1;
					//Log.i("MAP","vtxs "+ x_k + " "+ y_k + "; "+x_z + " "+ y_z);

					if(lineIntersects(vtxsT[x_i],vtxsT[y_i],vtxsT[x_j],vtxsT[y_j],
									  vtxs[x_k],vtxs[y_k],vtxs[x_z],vtxs[y_z]))
						return true;
				}
			}
		}
		return false;
	}

	private FornitureItem getTouchedObject(int x, int y) {
		// Touched outside room
		if (x < 0 || x > dim_x)
			return null;
		if (y < 0 || y > dim_y)
			return null;

		Log.i("MAP", String.valueOf(x) + " " + String.valueOf(y));
		float[] vect = { x, y };
		Matrix m = new Matrix();
		// Rotate
		for (FornitureItem fi : fi_l) {
			vect[0]= x;
			vect[1]= y ;
			if (!m.isIdentity())
				Log.i("MAP", "NOT IDENTITY");
			Log.i("MAP", "new center" + String.valueOf(fi.getCenter().x) + " "
							+ String.valueOf(fi.getCenter().y));
			m.setTranslate(-fi.getCenter().x, -fi.getCenter().y);
			Log.i("MAP", "new rotation" + String.valueOf(fi.getRotation()));
			m.postRotate(-fi.getRotation());
			m.mapPoints(vect);
			vect[0] = (int) vect[0];
			vect[1] = (int) vect[1];
			Log.i("MAP", "new coord " + String.valueOf(vect[0])
					+ " " + String.valueOf(vect[1]));
			Log.i("MAP", "rect sizes " + fi.getLength() +" "+ fi.getHeight());
			
			if (vect[1] >= -fi.getHeight() / 2 && vect[1] <= fi.getHeight() / 2
					&& vect[0] >= -fi.getLength() / 2
					&& vect[0] <= fi.getLength() / 2)
				return fi;
			m.reset();
		}
		return null;
	}
	
	public static boolean lineIntersects(float p0_x, float p0_y, float p1_x, float p1_y, 
		    float p2_x, float p2_y, float p3_x, float p3_y)
		{
		    float s1_x, s1_y, s2_x, s2_y;
		    s1_x = p1_x - p0_x;     s1_y = p1_y - p0_y;
		    s2_x = p3_x - p2_x;     s2_y = p3_y - p2_y;

		    float s, t;
		    s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
		    t = ( s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

		    if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
		    {
		        // Collision detected
		    	return true;
		    }

		    return false; // No collision
		}
	
	
	private double clamp(double x, double lower,double upper){
	  return Math.max(lower, Math.min(upper, x));
	}
	

	private int getNearestSegmentInPerimeter(int x,int y){
	  int dl= Math.abs(x);
	  int dr=  (int) Math.abs(x-dim_x);
	  int dt=  Math.abs(y);
	  int db=   (int) Math.abs(y-dim_y);
	  int  m = Math.min(Math.min(dl, dr), Math.min(dt, db));

	  if (m == dt) return TOP;
	  if (m == db) return BOTTOM; 
	  if (m == dl) return LEFT;
	  return RIGHT;
	}

	public void removeItem(FornitureItem fiSelected) {
		if (fiTouched.equals(fiSelected)) {
			fiTouched=null;
			mode = READY;
		}
		if (fiSelected.isWall()){
			wi_l.remove(fiSelected);
		}
		else {
			fi_l.remove(fiSelected);
		}
		
		
	}

}