package flying.bufallo.sharewith;

import java.io.File;
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

import flying.bafallo.util.Util;
import flying.bufallo.asynctask.FileReceiveAsyncTask;
import flying.bufallo.asynctask.FileSendAsyncTask;

public class MainActivity extends Activity {	

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    
    protected static final String DEVICE_TYPE_PC_WINDOWS = "1";
    protected static final String DEVICE_TYPE_ANDROID = "10";
    
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
	TextView statusView;
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
		statusView = (TextView)findViewById(R.id.status_view);
		
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
				
//				manager.unpair();
				if(manager.mydevice.device.status != WifiP2pDevice.AVAILABLE){
					Log.d("TEST", "unpair");
					manager.unpair();
				}
				
				manager.getDevicesAsync();
				
				rotationAnim.setInterpolator(new AccelerateInterpolator());
				rotationAnim.setStartOffset(0);
				rotationAnim.setDuration(2000);
				btnCenter.startAnimation(rotationAnim);
            }
		});
		
		
		manager = new WFDManager(getApplicationContext(), discoverdListener, connectedListener);
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
		manager.unpair();
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
		String deviceType = _device_list.get(index).device.primaryDeviceType;
		String deType[] = deviceType.split("-");
		Log.d("TEST_TYPE", "device : "+deType[0]);
		Log.d("TEST_TYPE", "DW : "+DEVICE_TYPE_PC_WINDOWS);
		Log.d("TEST_TYPE", "DA : "+DEVICE_TYPE_ANDROID);
		
		
		//select resources by device type
		if(DEVICE_TYPE_PC_WINDOWS.equals(deType[0])){
			child.setBackground(getResources().getDrawable(R.drawable.btn_com));
		}else if(DEVICE_TYPE_ANDROID.equals(deType[0])){
			child.setBackground(getResources().getDrawable(R.drawable.btn_android));
		}else{
			child.setBackground(getResources().getDrawable(R.drawable.btn_unknown));
		}
		child.setX((float) x);
		child.setY((float) y);
		child.setVisibility(View.GONE);
		
		child.setId(DYNAMIC_BUTTON_ID+index);
		
		child.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Toast.makeText(getApplicationContext(), "this is circle " + index, Toast.LENGTH_SHORT).show();
				
				setInformation(index);				
			}
		});
		
		statusView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Toast.makeText(getApplicationContext(), "Clicked!", Toast.LENGTH_SHORT).show();
				
			}
		});
		
		ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(child, View.ALPHA, 0,1);
		alphaAnimation.setDuration(1000);
		alphaAnimation.addListener(new AnimatorListener() {			
			@Override
			public void onAnimationStart(Animator animation) {
				child.setVisibility(View.VISIBLE);		
				Log.d("TEST VISIBLE", "??");
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
		
		statusView.setText(_device_list.get(index).device.deviceName);
		statusView.setVisibility(View.VISIBLE);
		
		_device_index = index;
		
		statusView.setOnClickListener(new OnClickListener() {
			
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
							Log.d("TEST FILE", "Device !null");
							READY_FILE_SEND = true;		// file send flag
							manager.pairAsync(device);
						}											
					}
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}	
	
	WFDDeviceDiscoveredListener discoverdListener = new WFDDeviceDiscoveredListener() {
		
		@Override
		public void onDevicesDiscovered(List<WFDDevice> deviceList) {
			Log.d(LISTENER, "onDevicesDiscovered - count : " + deviceList.size());
			if(deviceList.size() > 0) {			
				_device_list = deviceList;
				clickCenter();
			}
		}
		
		@Override
		public void onDevicesDiscoverFailed(int reasonCode) {
			Log.d(LISTENER, "onDevicesDiscoverFailed");
		}
	};
	
	WFDDeviceConnectedListener connectedListener = new WFDDeviceConnectedListener() {
		
		@Override
		public void onDeviceDisconnected() {
			Log.d(LISTENER, "onDeviceDisconnected");
		}
		
		@Override
		public void onDeviceConnected(final WFDPairInfo info) {
			Log.d(LISTENER, "called onDeviceConnected");		
			info.connectSocketAsync(new PairSocketConnectedListener() {

	            @Override
	            public void onSocketConnected(Socket socket) {
	                if (!READY_FILE_SEND && _path == null) {
					    Log.d(FILE_TEST, "Server: connection done.");
					    FileReceiveAsyncTask fileReceiveAsyncTask = new FileReceiveAsyncTask(socket, new FileReceiveAsyncTask.ReceiveListner() {
							
							@Override
							public void onSuccess(File f) {
								Util.addImageGallery(getApplicationContext(), f);
								Log.d("FILE", (String) getText(R.string.msg_file_receive_success));
//								Toast.makeText(getApplicationContext(), getText(R.string.msg_file_receive_success), Toast.LENGTH_SHORT);
							}
							
							@Override
							public void onFail(Exception e) {
//								Toast.makeText(getApplicationContext(), getText(R.string.msg_file_receive_fail), Toast.LENGTH_SHORT);
								Log.d("FILE", (String) getText(R.string.msg_file_receive_fail));
							}

							@Override
							public void onAlreadyExist() {
//								Toast.makeText(getApplicationContext(), getText(R.string.msg_file_receive_already_exist), Toast.LENGTH_SHORT);
								Log.d("FILE", (String) getText(R.string.msg_file_receive_already_exist));
							}
						});
					    fileReceiveAsyncTask.run();
					} else {                    	
					    Log.d(FILE_TEST, "Client: ready to send message");

					    Log.d(FILE_TEST, "when socket connected, _path = " + _path);                        
					    
					    FileSendAsyncTask fileSendAsyncTask = new FileSendAsyncTask(socket, _path, new FileSendAsyncTask.SendListner() {
							
							@Override
							public void onSuccess() {
//								Toast.makeText(getApplicationContext(), getText(R.string.msg_file_send_success), Toast.LENGTH_SHORT);
								Log.d("FILE", (String) getText(R.string.msg_file_send_success));
							}
							
							@Override
							public void onFail(Exception e) {
//								Toast.makeText(getApplicationContext(), getText(R.string.msg_file_send_fail), Toast.LENGTH_SHORT);
								Log.d("FILE", (String) getText(R.string.msg_file_send_fail));
							}
						});
					    fileSendAsyncTask.run();
					    
					    READY_FILE_SEND = false;
					    _path = null;
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
			            	break;
			            case WifiP2pDevice.INVITED:
			            	status = "Invited";
			            	break;
			            case WifiP2pDevice.CONNECTED:
			            	status = "Connected";
			            	break;
			            case WifiP2pDevice.FAILED:
			            	status = "Failed";
			            	break;
			            case WifiP2pDevice.UNAVAILABLE:
			            	status = "Unavailable";
			            	break;
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
	};
	
}