package flying.bufallo.asynctask;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import flying.bufallo.filestream.FileStreamUtil;
import flying.bufallo.sharewith.MainActivity;

import android.util.Log;

public class FileSendAsyncTask extends Thread {
	private Socket socket = null;
	private String path = null;
	private SendListner sendListner;

	public interface SendListner {
		public void onSuccess();
		public void onFail(Exception e);
	}
	
	public FileSendAsyncTask(Socket s, String p, SendListner l) {
		socket = s;
		path = p;
		this.sendListner = l;
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
            
            try {
				sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            FileInputStream fis = new FileInputStream(target);
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

            Log.d(MainActivity.FILE_TEST, "Send file using copyFile");
            FileStreamUtil.copyFile(fis, bos);
            
            sendListner.onSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            sendListner.onFail(e);
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
