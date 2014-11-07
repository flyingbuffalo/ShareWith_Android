package flying.bafallo.util;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;

public class Util {

	public static void addImageGallery(Context context, File file) {
	    ContentValues values = new ContentValues();
	    values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
	    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // setar isso
	    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}

}
