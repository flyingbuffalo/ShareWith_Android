package flying.bufallo.sharewith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class FileReceiveAsyncTask extends AsyncTask<Void,Void,Void> {

	private Socket client = null;
	
	public FileReceiveAsyncTask(Socket s) {
		client = s;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		File f = null;
        try {
            Log.d(MainActivity.FILE_TEST, "Receiver : ready to receive");
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            long size = Long.parseLong(in.readLine());
            Log.d(MainActivity.FILE_TEST, "file size : " + size);
            String filename = in.readLine();
            Log.d(MainActivity.FILE_TEST, "file name : " + filename);
            f = new File(Environment.getExternalStorageDirectory() + "/sharewith/" +filename);

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            
//            if(f.exists())
//                f.delete();
            
            f.createNewFile();
            
            Log.d(MainActivity.FILE_TEST, "new file path : " + dirs.toString());

            FileOutputStream output = new FileOutputStream(f);

            MainActivity.copyFile(client.getInputStream(), output);

		} catch (Exception e) {
			Log.d(MainActivity.FILE_TEST, "Server: error\n" + e.toString());
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
		return null;
	} // end of doInBackground

}
