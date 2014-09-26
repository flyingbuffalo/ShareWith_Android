package flying.bufallo.sharewith;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingbuffalo.wfdmanager.WFDDevice;
import com.flyingbuffalo.wfdmanager.WFDManager;
import com.flyingbuffalo.wfdmanager.WFDManager.WFDDeviceConnectedListener;
import com.flyingbuffalo.wfdmanager.WFDManager.WFDDeviceDiscoveredListener;
import com.flyingbuffalo.wfdmanager.WFDPairInfo.PairSocketConnectedListener;
import com.flyingbuffalo.wfdmanager.WFDPairInfo;
import com.ipaulpro.afilechooser.utils.FileUtils;



public class MainActivity extends Activity implements WFDDeviceDiscoveredListener, WFDDeviceConnectedListener {
	
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	int MAX_DEVICE = 3;
	final int CENTER_BTN_SIZE = 180;

	public static final String FILE_TEST = "FILE_TEST";
	public final String DEVICE_INDEX = "device_index";

	final int DYNAMIC_BUTTON_ID = 0x8000;
	
	float dpHeight;
	float dpWidth;
	
	float btnSize;
	
	boolean isBtnExist = false;
	
	RelativeLayout main;
	TextView textCenter;
	ImageView backgroundCircle;
	ImageView btnCenter;	
	AnimatorSet animatorSet = new AnimatorSet();
	List<ObjectAnimator> aniList = new ArrayList<ObjectAnimator>();
	
	WFDManager manager;	
	List<WFDDevice> _device_list = new ArrayList<WFDDevice>();
	String _path = null;
	private int _device_index = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		main = (RelativeLayout) findViewById(R.id.main);
		backgroundCircle = (ImageView)findViewById(R.id.devices_list_circle);
		btnCenter = (ImageView) findViewById(R.id.center);
		textCenter = (TextView)findViewById(R.id.information);
		
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        
		/*This is for test*/

		Log.d("TEST DISPLAY", "height : "+displayMetrics.heightPixels+" width : "+displayMetrics.widthPixels);
		Log.d("TEST DISPLAY", "dpheight : "+dpHeight+" dpwidth : "+dpWidth);

		
		/*delete it after you use*/       
		
		
		//size of button is 1/5 scale of device width
		btnSize = (float) (displayMetrics.widthPixels*0.2);
		Log.d("TEST SCALE", "Size : "+btnSize); 
		float btnScale = btnSize / displayMetrics.density / CENTER_BTN_SIZE;
		Log.d("TEST SCALE", "Scale : "+btnScale);
		btnCenter.setScaleX(btnScale);
		btnCenter.setScaleY(btnScale);
		
		btnCenter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.d("TEST ANI", "click center");
                Toast.makeText(getApplicationContext(), "野껓옙源�餓ο옙..", Toast.LENGTH_LONG).show();
				manager.getDevicesAsync();
            }
		});
		
		manager = new WFDManager(getApplicationContext(),this, this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		manager.registerReceiver();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		manager.unregisterReceiver();
	}
	
	public void clickCenter() {
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
		fadeOut.setStartOffset(0);
		fadeOut.setDuration(500);
		btnCenter.startAnimation(fadeOut);
		
		
		float x = (float) (btnCenter.getX()+btnSize);
		float y = (float) (btnCenter.getY()+btnSize);
		
		
		Log.d("TEST ANI", "center x : " + x);
		Log.d("TEST ANI", "center y : " + y);
		

		if(isBtnExist){
			for(int i = 0; i < aniList.size(); i++){
				main.removeView(findViewById(DYNAMIC_BUTTON_ID+i));
			}
			aniList.clear();
		}
		

		for(int i = 0; i < _device_list.size(); i++) {
			double circle_x = x + (x-100)*Math.cos(Math.toRadians((360/_device_list.size())*i));
			double circle_y = y + (x-100)*Math.sin(Math.toRadians((360/_device_list.size())*i));

			createdCircle(circle_x, circle_y, i);
			
			Log.d("TEST ANI", "circle " + i + " position : " + circle_x + " , " + circle_y);
		}		
		startFadeIn();
	}
	
	public void setInformation(final int index){

		btnCenter.setVisibility(View.GONE);

		textCenter.setText(_device_list.get(index).device.deviceName);
		textCenter.setVisibility(View.VISIBLE);
		
		_device_index = index;
		
		textCenter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			       Intent target = FileUtils.createGetContentIntent();
                   // Create the chooser Intent
                   Intent intent = Intent.createChooser(
                           target, "aFileChooser");
                   intent.putExtra(DEVICE_INDEX, index);
                   Log.d(FILE_TEST, "Click index!!! " + index);
                   try {
                	   startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                   } catch (ActivityNotFoundException e) {
                       // The reason for the existence of aFileChooser
                   }
			}
		});
		
	}
	
	// �좎룞���좎룞�숈튂�좎룞���좎뙎紐뚯삕�좎룞���좎뙇�덈챿�쇿뜝�깆눦��+ �좎룞��= > �좎뙇�덈챿�쇿뜝�숈삕�좎떢紐뚯삕 �좎룞�쇿뜝�숈삕�좑옙�좎룞�쇿뜝�숈삕
	public void createdCircle(double x, double y, final int index) {
		final View child = new View(getApplicationContext());
		child.setBackground(getResources().getDrawable(R.drawable.btn_android));		
		child.setX((float) x);
		child.setY((float) y);
		child.setVisibility(View.GONE);
		
		child.setId(DYNAMIC_BUTTON_ID+index);
		
		Log.d("TEST ANI", "index is " + index);	
		
		child.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "this is circle " + index, Toast.LENGTH_SHORT).show();
				
				setInformation(index);
			}
		});
		
		ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(child, View.ALPHA, 0,1);
		alphaAnimation.setDuration(1000);
		alphaAnimation.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				child.setVisibility(View.VISIBLE);				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				//child.setVisibility(View.GONE);
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		
		aniList.add(alphaAnimation);	
		main.addView(child, 100, 100);				
	}
		
	public void startFadeIn() {
		if(aniList.size() > 1) {
			for(int i = 0; i < aniList.size() - 1; i++) {
				animatorSet.play(aniList.get(i)).before(aniList.get(i+1));
			}
		} else if(aniList.size() == 0) {
			animatorSet.play(aniList.get(0));
		}
		Log.d("TEST ANILIST", "anilist size : "+aniList.size());
		animatorSet.start();
		isBtnExist = true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CHOOSE_FILE_RESULT_CODE :
				if (resultCode == RESULT_OK) {
					if (data != null) {
						// Get the URI of the selected file
						final Uri uri = data.getData();
						Log.i(FILE_TEST, "Uri = " + uri.toString());
						try {
                            // Get the file path from the URI
                            _path = FileUtils.getPath(this, uri);
                            Toast.makeText(this,
                                    "File Selected: " + _path, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e("FileSelectorTestActivity", "File select error", e);
                        }
						
						Log.d(FILE_TEST, "device index : " + _device_index);
						WFDDevice device = _device_list.get(_device_index);

						if(device != null && device.device != null) {
							manager.pairAsync(device);
						}
					}
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	 public static boolean copyFile(InputStream inputStream, OutputStream out, long size) {
		 byte buf[] = new byte[1024*1024];
		 int len;
		 Log.d(FILE_TEST, "Start copy file");
		 try {
			 int i = 0;
			 while ((len = inputStream.read(buf)) != -1) {
				 out.write(buf, 0, len);
			 }
			 out.close();
			 inputStream.close();
		 } catch (IOException e) {
			 Log.d(FILE_TEST, e.toString());
			 return false;
		 }
		  
		 Log.d(FILE_TEST, "file copy success");
		 return true;
	 }

	@Override
	public void onDeviceConnected(final WFDPairInfo info) {
		Log.d(FILE_TEST, "called onDeviceConnected");
		info.connectSocketAsync(new PairSocketConnectedListener() {

            @Override
            public void onSocketConnected(Socket socket) {
                try {
                    if (info.info.groupFormed && info.info.isGroupOwner) {
                        Log.d("TEST", "Server: connection done.");
                        MessageAsyncTask m = new MessageAsyncTask(socket);
                        m.execute();
                    } else if (info.info.groupFormed) {
                        Log.d("TEST", "Client: ready to send message");

                        File target = new File(getPathFromUri(Uri.parse(_path)));
                        long fileSize = target.length();
                        Log.d(FILE_TEST, "Send file size : " + fileSize);

                        String titleWithPath = target.toString();
                        String title = "";
                        int i = 0;

                        String tmp = titleWithPath;
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

                            DataInputStream dis = new DataInputStream(new FileInputStream(new File(titleWithPath)));
                            OutputStream dos = new DataOutputStream(socket.getOutputStream());

                            Log.d(FILE_TEST, "Send file using copyFile");
                            copyFile(dis, dos, fileSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }finally{
                            socket.close();
                        }
                    } // end of client send file
                } catch (IOException e) {
                    Log.e(FILE_TEST, e.getMessage());
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                Log.d(FILE_TEST, "end send file");
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
	}

    public String getPathFromUri(Uri uri){

        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();

        return path;
    }

	@Override
	public void onDeviceConnectFailed(int reasonCode) {
		Log.d("TEST", "onDeviceConnectFailed");
		
	}

	@Override
	public void onDeviceDisconnected() {
		Log.d("TEST", "onDeviceDisconnected");
		
	}

	@Override
	public void onDevicesDiscovered(List<WFDDevice> deviceList) {
		Log.d("TEST", "onDevicesDiscovered - count : " + deviceList.size());
		if(deviceList.size() > 0) {			
			_device_list = deviceList;
			clickCenter();	
		}
	}

	@Override
	public void onDevicesDiscoverFailed(int reasonCode) {
		Log.d("TEST", "onDevicesDiscoverFailed");
		
	}
	
	public class MessageAsyncTask extends AsyncTask<Void,Void,Void> {
		private Socket client = null;
		
		public MessageAsyncTask(Socket s) {
			client = s;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			File f = null;
            try {
                Log.d(FILE_TEST, "Receiver : ready to receive");
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                long size = Long.parseLong(in.readLine());
                Log.d(FILE_TEST, "file size : " + size);
                String filename = in.readLine();
                Log.d(FILE_TEST, "file name : " + filename);
                f = new File(Environment.getExternalStorageDirectory() + "/"
                        + getPackageName() +filename);

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();

                f.createNewFile();

                FileOutputStream output = new FileOutputStream(f);

                copyFile(client.getInputStream(), output, size);

			} catch (Exception e) {
				Log.d(FILE_TEST, "Server: error");
			}
			return null;
		}
		
	}
}