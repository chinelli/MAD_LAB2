package it.polito.mad.mad_lab2;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class DataManager {
	private static final String TAG = DataManager.class.getName();	/* TAG per logging */
	
	private static final String DB_NAME = "Forniture.db"; 	/* Nome DB */
	private static final int DB_VERSION = 1; 				/* Versione DB */
	
	/* Tabella Generi */
	static final String GENRE_TABLE = "Genre";
    static final String GENRE_TABLE_CREATE = "CREATE TABLE " + GENRE_TABLE + " (ID_Genre INTEGER PRIMARY KEY, Name TEXT)";    
    
    /* Tabella Oggetti (Virtuale) */
    private static final String FTS_VIRTUAL_TABLE = "FTSObject";
    private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE + " USING fts3 (" + AppConst.KEY_ID + ", " + AppConst.KEY_NAME + ", "  + ", " + AppConst.KEY_DIM + ", " + AppConst.KEY_PRICE + ", " + AppConst.KEY_IMAGE + ");";
    
	/* Tabella Oggetti */
	static final String OBJECT_TABLE = "Object";
    static final String OBJECT_TABLE_CREATE = "CREATE TABLE " + OBJECT_TABLE + " (ID_Object INTEGER, Name TEXT, "
    		+ "Description TEXT, Price TEXT, Width TEXT, Depth TEXT, ObjImage TEXT, MapImage TEXT, ID_Genre INTEGER, ID_ProdLine INTEGER)";
    
	/* Tabella Linee Prodotti */
	static final String PROD_LINE_TABLE = "ProdLine";
    static final String PROD_LINE_TABLE_CREATE = "CREATE TABLE " + PROD_LINE_TABLE + " (ID_ProdLine INTEGER PRIMARY KEY, Name TEXT)";
    
    /* Tabella Elementi */
	static final String ELEMENT_TABLE = "Element";
    static final String ELEMENT_TABLE_CREATE = "CREATE TABLE " + ELEMENT_TABLE + " (ID_Element INTEGER, Name TEXT, Width INTEGER, Depth INTEGER, MapImage TEXT)";
    
    private Context ctx;
	private SQLiteDatabase db;
	private OpenHelper oh;
	
	/* Costruttore */
	public DataManager(Context ctx){
		this.ctx = ctx;
		oh = new OpenHelper(ctx);
		db = oh.getWritableDatabase();
	}
	
	/* Carica file json */
	private String loadJSONFromAsset() {
	    String json = null;
	    try {
	        InputStream is = ctx.getAssets().open("products.json");
	        int size = is.available();
	        byte[] buffer = new byte[size];
	        is.read(buffer);
	        is.close();
	        json = new String(buffer, "UTF-8");
	    } catch (IOException e) {
	        Log.e(TAG, e.getMessage());
	        return null;
	    }
	    
	    return json;
	}
	
	/* Carica file json nel db */
	public void loadJSONDB() {
		JSONObject objJSON = null;		/* Oggetto json */
    	JSONArray jsonArray = null;
    	
		/* Carico oggetto json */
		try {
			objJSON = new JSONObject(loadJSONFromAsset());
			jsonArray = objJSON.getJSONArray("products");
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		int genre;
		int prodLine;
    	for (int i = 0; i < jsonArray.length(); i++){
    		try {
				JSONObject objData = jsonArray.getJSONObject(i);
				/* Genere */
				if(objData.isNull("genre")){
					genre = 5;
				}else{
					genre = objData.getInt("genre");
				}
				
				/* Product Line */
				if(objData.isNull("productLine")){
					prodLine = 1;
				}else{
					prodLine = objData.getInt("productLine");
				}				
				
				insertObject(objData.getInt("productID"), objData.getString("name"), objData.getString("description"), 
						objData.getString("price"), objData.getString("width"), objData.getString("depth"), 
						objData.getString("imagePath"), objData.getString("mapPath"), genre, prodLine);
				loadVirtualTable(objData.getInt("productID"), objData.getString("name"), 
						objData.getString("width") + "x" + objData.getString("depth"), objData.getString("price"), objData.getString("imagePath"));
			} catch (JSONException e) {
				Log.e(TAG, "JSONException " + e.getMessage());
			}
    	}
	}
		
	/* Aggiunge genere (GENRE_TABLE) */
	public void insertGenre(String Name){
		ContentValues cv = new ContentValues();
		cv.put("Name", Name);
		db.insert(GENRE_TABLE, null, cv);
	}
	
	/* Modifica genere (GENRE_TABLE) */
	public void editGenre(String filter, ContentValues cv){
		db.update(GENRE_TABLE, cv, filter, null);
	}
	
	/* Cancella genere (GENRE_TABLE) */
	public void deleteGenre(String filter){
		db.delete(GENRE_TABLE, filter, null);
	}
	
	/* Visualizza genere (GENRE_TABLE) */
	public Cursor viewGenre(String filter, String order){
		return db.query(GENRE_TABLE, null, filter, null, null, null, order);
	}
	
	/* Aggiunge oggetto (OBJECT_TABLE) */
	public void insertObject(int ID_Object, String Name, String Description, String Price, 
			String Width, String Depth, String ObjImage, String MapImage, int ID_Genre, int ID_ProdLine){
		ContentValues cv = new ContentValues();
		cv.put("ID_Object", ID_Object);
		cv.put("Name", Name);
		cv.put("Description", Description);
		cv.put("Price", Price);
		cv.put("Width", Width);
		cv.put("Depth", Depth);
		cv.put("ObjImage", ObjImage);
		cv.put("MapImage", MapImage);
		cv.put("ID_Genre", ID_Genre);
		cv.put("ID_ProdLine", ID_ProdLine);
		db.insert(OBJECT_TABLE, null, cv);
	}
	
	/* Modifica oggetto (OBJECT_TABLE) */
	public void editObject(String filter, ContentValues cv){
		db.update(OBJECT_TABLE, cv, filter, null);
	}
	
	/* Cancella oggetto (OBJECT_TABLE) */
	public void deleteObject(String filter){
		db.delete(OBJECT_TABLE, filter, null);
	}
	
	/* Visualizza oggetto (OBJECT_TABLE) */
	public Cursor viewObject(String filter, String order){
		return db.query(OBJECT_TABLE, null, filter, null, null, null, order);
	}
	
	/* Aggiunge linea prodotto (PROD_LINE_TABLE) */
	public void insertProdLine(String Name){
		ContentValues cv = new ContentValues();
		cv.put("Name", Name);
		db.insert(PROD_LINE_TABLE, null, cv);
	}
	
	/* Modifica linea prodotto (PROD_LINE_TABLE) */
	public void editProdLine(String filter, ContentValues cv){
		db.update(PROD_LINE_TABLE, cv, filter, null);
	}
	
	/* Cancella linea prodotto (PROD_LINE_TABLE) */
	public void deleteProdLine(String filter){
		db.delete(GENRE_TABLE, filter, null);
	}
	
	/* Visualizza linea prodotto (PROD_LINE_TABLE) */
	public Cursor viewProdLine(String filter, String order){
		return db.query(PROD_LINE_TABLE, null, filter, null, null, null, order);
	}	
	
	/* Caricamento Tabella virtuale ricerca (FTS_VIRTUAL_TABLE) */
	public void loadVirtualTable(int ID_Object, String Name, String dim, String price, String image){
        ContentValues cv = new ContentValues();
        cv.put(AppConst.KEY_ID, ID_Object);
        cv.put(AppConst.KEY_NAME, Name);
        cv.put(AppConst.KEY_DIM, dim);
        cv.put(AppConst.KEY_PRICE, price);
        cv.put(AppConst.KEY_IMAGE, image);
        db.insert(FTS_VIRTUAL_TABLE, null, cv);	
	}
	
	/* Modifica Tabella virtuale ricerca (FTS_VIRTUAL_TABLE) */
	public void editVirtualTable(String filter, ContentValues cv){
		db.update(FTS_VIRTUAL_TABLE, cv, filter, null);
	}
	
	/* Ricerca nella tabella virtuale (FTS_VIRTUAL_TABLE) */
    public Cursor getWordMatches(String query, String field, String[] columns) {
        String selection = field + " MATCH ?";
        String[] selectionArgs = new String[] {query + "*"};

        return query(selection, selectionArgs, columns);
    }
	
    /* Esegui la query utilizzando la tabella viruale (FTS_VIRTUAL_TABLE) */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        Cursor cursor = builder.query(db, columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
	
    /* Aggiunge elemento (ELEMENT_TABLE) */
	public void insertElement(int id, String Name, int Width, int Depth, String MapImage){
		ContentValues cv = new ContentValues();
		cv.put("ID_Element", id);
		cv.put("Name", Name);
		cv.put("Width", Width);
		cv.put("Depth", Depth);
		cv.put("MapImage", MapImage);
		db.insert(ELEMENT_TABLE, null, cv);
	}
	
	/* Modifica elemento (ELEMENT_TABLE) */
	public void editElement(String filter, ContentValues cv){
		db.update(ELEMENT_TABLE, cv, filter, null);
	}
	
	/* Cancella elemento (ELEMENT_TABLE) */
	public void deleteElement(String filter){
		db.delete(ELEMENT_TABLE, filter, null);
	}
	
	/* Visualizza elemento (ELEMENT_TABLE) */
	public Cursor viewElement(String filter, String order){
		return db.query(ELEMENT_TABLE, null, filter, null, null, null, order);
	}
	
	/* Chiudi il db */
	public void closeDB(){
		if (oh != null) {
			oh.close();
		}
	}
	
	/* Classe creazione DB */
	private static class OpenHelper extends SQLiteOpenHelper{
		OpenHelper(Context context) {
	    	 super(context, DB_NAME, null, DB_VERSION);
		}	    	  

		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "onCreate");
			db.execSQL(GENRE_TABLE_CREATE);
			db.execSQL(OBJECT_TABLE_CREATE);
			db.execSQL(PROD_LINE_TABLE_CREATE);
			db.execSQL(ELEMENT_TABLE_CREATE);
			db.execSQL(FTS_TABLE_CREATE);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "onUpgrade");
			db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
		}
		
	} 	
}
