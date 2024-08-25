package it.polito.mad.mad_lab2.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class AbstractFileProvider extends ContentProvider{
	private final static String[] OPENABLE_PROJECTION= {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
	
	@Override
	public String getType(Uri uri) {
		return (URLConnection.guessContentTypeFromName(uri.toString()));
	}

	@Override
	public boolean onCreate() {
		return false;
	}
	
	protected String getFileName(Uri uri) {
		return(uri.getLastPathSegment());
	}
	
	protected long getDataLength(Uri uri) {
		return(AssetFileDescriptor.UNKNOWN_LENGTH);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		if (projection == null) {
			projection = OPENABLE_PROJECTION;
		}

	    final MatrixCursor cursor = new MatrixCursor(projection, 1);
	    MatrixCursor.RowBuilder b = cursor.newRow();
	    for (String col : projection) {
	    	if (OpenableColumns.DISPLAY_NAME.equals(col)) {
	    		b.add(getFileName(uri));
	    	}else if (OpenableColumns.SIZE.equals(col)) {
	    		b.add(getDataLength(uri));
	    	}else {
	    		b.add(null);
	    	}
	    }
	    
	    return(cursor);
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new RuntimeException("Operation not supported");
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new RuntimeException("Operation not supported");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new RuntimeException("Operation not supported");
	}
	
	static protected void copy(InputStream in, File dst)
            throws IOException {
		FileOutputStream out=new FileOutputStream(dst);
		byte[] buf=new byte[1024];
		int len;

		while ((len=in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}

		in.close();
		out.close();
	}
}
