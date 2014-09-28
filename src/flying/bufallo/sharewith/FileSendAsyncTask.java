package flying.bufallo.sharewith;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

public class FileSendAsyncTask extends Thread {
	private Socket socket = null;
	private String path = null;
	
	public FileSendAsyncTask(Socket s, String p) {
		socket = s;
		path = p;
	}
	
	@Override
	public void run() {		        
        File target = new File(path);
        long fileSize = target.length();
        Log.d(MainActivity.FILE_TEST, "FileSendAsyncTask : Send file size : " + fileSize);

        String title = "";
        int i = 0;

        String tmp = path;
        while(true){
            if('/' == tmp.charAt(i)){
                tmp = tmp.substring(i+1);
                i = 0;
            }
            if(tmp.length()-1 == i){
                break;
            }
            i++;
        }
        title = tmp;

        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new
            OutputStreamWriter(socket.getOutputStream())), true);

            // file size
            out.println(fileSize);
            out.flush();

            // file name
            out.println(title);
            out.flush();

            FileInputStream fis = new FileInputStream(target);
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

            Log.d(MainActivity.FILE_TEST, "Send file using copyFile");
            MainActivity.copyFile(fis, bos);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
        	 if (socket != null) {
                 if (socket.isConnected()) {
                     try {
                         Log.d(MainActivity.FILE_TEST, "end send file");
                         socket.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
        }
   
	} // end of run

}
