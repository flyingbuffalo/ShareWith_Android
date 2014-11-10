package flying.bufallo.asynctask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import flying.bafallo.util.EndianUtil;
import flying.bufallo.filestream.FileStreamUtil;
import flying.bufallo.sharewith.MainActivity;

import android.os.Environment;
import android.util.Log;

public class FileReceiveAsyncTask extends Thread {

	private Socket client = null;
	private ReceiveListner receiveListner;		
	
	public interface ReceiveListner {
		public void onSuccess(File f);
		public void onFail(Exception e);
		public void onAlreadyExist();
	}
	
	public FileReceiveAsyncTask(Socket s, ReceiveListner l) {
		client = s;
		this.receiveListner = l;	
	}
	
	@Override
	public void run() {
		File f = null;
        try {
            Log.d(MainActivity.FILE_TEST, "Receiver : ready to receive");
            InputStream in = client.getInputStream();
            
            byte[] buffer = new byte[4];
			in.read(buffer);
            int nameLength = EndianUtil.byteArrayToInt(buffer);
            Log.d(MainActivity.FILE_TEST, "file name length : " + nameLength);
            
            buffer = new byte[nameLength];
            in.read(buffer);
            String filename = new String(buffer);
            Log.d(MainActivity.FILE_TEST, "file name : " + filename);
            
            buffer = new byte[8];
            in.read(buffer);
            long size = EndianUtil.byteArrayToLong(buffer);
            Log.d(MainActivity.FILE_TEST, "file size : " + size);
            
            f = new File(Environment.getExternalStorageDirectory() + "/sharewith/" +filename);
                        
            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            
            if(f.exists()) {
//            	f.delete();
        		receiveListner.onAlreadyExist();
            }
            	                       
            f.createNewFile();
            
            Log.d(MainActivity.FILE_TEST, "new file path : " + dirs.toString());

            FileOutputStream output = new FileOutputStream(f);

            FileStreamUtil.copyFile(client.getInputStream(), output);
                        
            receiveListner.onSuccess(f);            

		} catch (Exception e) {
			Log.d(MainActivity.FILE_TEST, "Server: error\n" + e.toString());
			receiveListner.onFail(e);			
		} finally {
			if(client != null) {
				if(client.isConnected()) {
					try {
						Log.d("TEST", "client.close");
						client.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}			
		}
	} // end of run

}
