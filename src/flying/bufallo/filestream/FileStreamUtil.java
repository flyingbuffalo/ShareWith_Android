package flying.bufallo.filestream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import flying.bufallo.sharewith.MainActivity;

import android.util.Log;

public class FileStreamUtil {

	public static boolean copyFile(InputStream inputStream, OutputStream out) {
		 byte buf[] = new byte[1024*1024];
		 int len;
		 int sum = 0;
		 Log.d(MainActivity.FILE_TEST, "Start copy file");
		 
		 try {
			 int i = 0;
			 while ((len = inputStream.read(buf)) != -1) {
				 ByteBuffer bb = ByteBuffer.wrap(buf, 0, len);
				 bb.order(ByteOrder.LITTLE_ENDIAN);				 
				 out.write(bb.array(), 0, len);
				 sum+=len;
				 Log.d(MainActivity.FILE_TEST, "copy buffer times = " + i++ + "and len = " + len + " sum : "+sum);
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
