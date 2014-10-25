package flying.bufallo.filestream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import flying.bufallo.sharewith.MainActivity;

import android.util.Log;

public class FileStreamUtil {

	public static boolean copyFile(InputStream inputStream, OutputStream out) {
		 byte buf[] = new byte[1024*1024];
		 int len;
		 Log.d(MainActivity.FILE_TEST, "Start copy file");
		 
		 try {
			 int i = 0;
			 while ((len = inputStream.read(buf)) != -1) {
				 out.write(buf, 0, len);
				 Log.d(MainActivity.FILE_TEST, "copy buffer times = " + i++ + "and len = " + len);
			 }
			 out.close();
			 inputStream.close();
		 } catch (IOException e) {
			 Log.d(MainActivity.FILE_TEST, e.toString());
			 return false;
		 }
		  
		 Log.d(MainActivity.FILE_TEST, "End of copy file");
		 return true;
	}

}