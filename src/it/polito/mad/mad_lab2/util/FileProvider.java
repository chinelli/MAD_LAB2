package it.polito.mad.mad_lab2.util;

import java.io.File;
import java.io.FileNotFoundException;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class FileProvider extends AbstractFileProvider {
	public static final Uri CONTENT_URI = Uri.parse("content://it.polito.mad.mad_lab2.util/");
	
	@Override
	public boolean onCreate() {
		File file = new File(getContext().getFilesDir().getAbsolutePath() + "/" + "Quote.pdf");

	    if (file.exists()) {
	    	return true;
	    }else{
	    	return false;
	    }
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		File f = new File(getContext().getFilesDir(), uri.getPath());

	    if (f.exists()) {
	    	return ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
	    }
	    
	    throw new FileNotFoundException(uri.getPath());
	}

	@Override
	protected long getDataLength(Uri uri) {
	    File f = new File(getContext().getFilesDir(), uri.getPath());

	    return f.length();
	}	
}
