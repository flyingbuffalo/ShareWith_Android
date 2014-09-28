package flying.bufallo.sharewith;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
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
	public static final String LISTENER = "LISTENER";
	public static final String ANI = "ANI";
	
	public final String DEVICE_INDEX = "device_index";
	
	final Animation rotationAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

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
	private boolean READY_FILE_SEND = false;
	
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
        
		/** This is for test **/
		Log.d("TEST DISPLAY", "height : "+displayMetrics.heightPixels+" width : "+displayMetrics.widthPixels);
		Log.d("TEST DISPLAY", "dpheight : "+dpHeight+" dpwidth : "+dpWidth);		
		/** delete it after you use **/       		
		
		// size of button is 1/5 scale of device width
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
				manager.getDevicesAsync();
				
				rotationAnim.setInterpolator(new AccelerateInterpolator());
				rotationAnim.setStartOffset(0);
				rotationAnim.setDuration(2000);
				btnCenter.startAnimation(rotationAnim);
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
	
	// START ANIMATION BLOCK
	
	public void clickCenter() {
		rotationAnim.setInterpolator(new AccelerateInterpolator());
		rotationAnim.setStartOffset(0);
		rotationAnim.setDuration(2000);
		btnCenter.startAnimation(rotationAnim);		
		
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
			
	public void createdCircle(double x, double y, final int index) {
		final View child = new View(getApplicationContext());
		child.setBackground(getResources().getDrawable(R.drawable.btn_android));		
		child.setX((float) x);
		child.setY((float) y);
		child.setVisibility(View.GONE);
		
		child.setId(DYNAMIC_BUTTON_ID+index);		
		
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
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {

			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
		
		aniList.add(alphaAnimation);	
		main.addView(child, 100, 100);				
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
	
	public void startFadeIn() {
		if(1 == aniList.size()) {
			animatorSet.play(aniList.get(0));
		} else if(0 != aniList.size()) {
			for(int i = 0; i < aniList.size() - 1; i++) {
				animatorSet.play(aniList.get(i)).before(aniList.get(i+1));
			}
		}
		Log.d("TEST ANILIST", "anilist size : "+aniList.size());
		animatorSet.start();
		isBtnExist = true;
	}
	
	// END ANIMATION BLOCK
	
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
                            Log.e(FILE_TEST, "File select error", e);
                        }
						
						Log.d(FILE_TEST, "device index : " + _device_index);
						WFDDevice device = _device_list.get(_device_index);

						if(device != null) {
							READY_FILE_SEND = true;		// file send flag
							manager.pairAsync(device);
						}
					}
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
		
	@Override
	public void onDevicesDiscovered(List<WFDDevice> deviceList) {
		Log.d(LISTENER, "onDevicesDiscovered - count : " + deviceList.size());
		if(deviceList.size() > 0) {			
			_device_list = deviceList;
			clickCenter();
		}
	}

	@Override
	public void onDeviceConnected(final WFDPairInfo info) {
		Log.d(LISTENER, "called onDeviceConnected");		
		info.connectSocketAsync(new PairSocketConnectedListener() {

            @Override
            public void onSocketConnected(Socket socket) {
                if (!READY_FILE_SEND && _path == null) {
				    Log.d(FILE_TEST, "Server: connection done.");
				    FileReceiveAsyncTask fileReceiveAsyncTask = new FileReceiveAsyncTask(socket);
				    fileReceiveAsyncTask.execute();
				} else {                    	
				    Log.d(FILE_TEST, "Client: ready to send message");

				    Log.d(FILE_TEST, "when socket connected, _path = " + _path);                        
				    
				    FileSendAsyncTask fileSendAsyncTask = new FileSendAsyncTask(socket, _path);
				    fileSendAsyncTask.execute();
				    
				    READY_FILE_SEND = false;
				}
            }
        });
	}
	
	@Override
	public void onDeviceConnectFailed(int reasonCode) {
		
		switch (reasonCode) {
			case WFDManager.CHANNEL_LOST:
				Log.d(LISTENER, "Channel lost");
				break;
			case WFDManager.DEVICES_RESET:
				Log.d(LISTENER, "Device list need to reset");
				manager.getDevicesAsync();
				break;
			case WFDManager.UPDATE_THIS_DEVICE:
				Log.d(LISTENER, "Change this device status ex)connected");
				TextView myDeviceStatus = (TextView) findViewById(R.id.my_device_status);
				if(manager.mydevice != null) {
					String status = "none";
					switch (manager.mydevice.device.status) {
		            case WifiP2pDevice.AVAILABLE:
		            	status = "Available";
		            case WifiP2pDevice.INVITED:
		            	status = "Invited";
		            case WifiP2pDevice.CONNECTED:
		            	status = "Connected";
		            case WifiP2pDevice.FAILED:
		            	status = "Failed";
		            case WifiP2pDevice.UNAVAILABLE:
		            	status = "Unavailable";
		            default:
		            	status = "Unknown";
					}
					myDeviceStatus.setText(status);
				}
				
				break;
			case WFDManager.WFD_DISABLED:
				Log.d(LISTENER, "Wifi is off");
				break;
	
			default:
				break;
		}
		
	}
	
	@Override
	public void onDevicesDiscoverFailed(int reasonCode) {
		Log.d(LISTENER, "onDevicesDiscoverFailed");
	}
	
	@Override
	public void onDeviceDisconnected() {
		Log.d(LISTENER, "onDeviceDisconnected");
	}
	
	public static boolean copyFile(InputStream inputStream, OutputStream out) {
		 byte buf[] = new byte[1024*8];
		 int len;
		 Log.d(FILE_TEST, "Start copy file");
		 
		 try {
			 int i = 0;
			 while ((len = inputStream.read(buf)) != -1) {
				 out.write(buf, 0, len);
				 Log.d(FILE_TEST, "copy buffer times = " + i++ + "and len = " + len);
			 }
			 out.close();
			 inputStream.close();
		 } catch (IOException e) {
			 Log.d(FILE_TEST, e.toString());
			 return false;
		 }
		  
		 Log.d(FILE_TEST, "End of copy file");
		 return true;
	}
}