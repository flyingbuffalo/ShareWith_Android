package flying.bufallo.sharewith;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
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
    
    protected static final String DEVICE_TYPE_PC_WINDOWS = "1";
    protected static final String DEVICE_TYPE_ANDROID = "10";
    
	final int CENTER_BTN_SIZE = 180;

	public static final String FILE_TEST = "FILE_TEST";
	public final String DEVICE_INDEX = "device_index";

	final int DYNAMIC_BUTTON_ID = 0x8000;
	
	float dpHeight;
	float dpWidth;
	
	float btnSize;
	
	boolean isBtnExist = false;
	
	final Animation rotationAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, 
			Animation.RELATIVE_TO_SELF, 0.5f);
	
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
		// ���̵� �ƿ� ����

		rotationAnim.setInterpolator(new AccelerateInterpolator());
		rotationAnim.setStartOffset(0);
		rotationAnim.setDuration(2000);
		btnCenter.startAnimation(rotationAnim);
		
		
		float x = (float) (btnCenter.getX()+btnSize);
		float y = (float) (btnCenter.getY()+btnSize);
		
		
		Log.d("TEST ANI", "center x : " + x);
		Log.d("TEST ANI", "center y : " + y);
		
		//�̹� ��Ǿ�� ��ư�� ������ �����ϰ� ����Ʈ �ʱ�ȭ
		if(isBtnExist){
			for(int i = 0; i < aniList.size(); i++){
				main.removeView(findViewById(DYNAMIC_BUTTON_ID+i));
			}
			aniList.clear();
		}
		
		// ��� ���̵� �� �ƿ���
		for(int i = 0; i < _device_list.size(); i++) {
			double circle_x = x + (x-100)*Math.cos(Math.toRadians((360/_device_list.size())*i));
			double circle_y = y + (x-100)*Math.sin(Math.toRadians((360/_device_list.size())*i));

			createdCircle(circle_x, circle_y, i);
			
			Log.d("TEST ANI", "circle " + i + " position : " + circle_x + " , " + circle_y);
		}		
		startFadeIn();
	}
	
	//setText�� ��� ���� ����..?
	public void setInformation(final int index){

		btnCenter.setVisibility(View.GONE);
		textCenter.setText(_device_list.get(index).device.deviceName);
		textCenter.setVisibility(View.VISIBLE);
		
		_device_index = index;
		
		//��� ���� Ŭ���ϸ� afilechooser
		textCenter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
	
	// �� ��ġ�� �׸��� �ִϸ��̼� + �� = > �ִϸ����͸� ����� ����
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
		
		Log.d("TEST_TYPE", "Device type : "+_device_list.get(index).device.primaryDeviceType);
		//���� ��� ��ư�� ID ����
		child.setId(DYNAMIC_BUTTON_ID+index);
		
		Log.d("TEST ANI", "index is  " + index);	
		
		child.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "this is circle " + index, Toast.LENGTH_SHORT).show();
				
				setInformation(index);				
			}
		});
		
		textCenter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
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
	
	// �ִϸ����� ����Ʈ���� ���������� �ִϸ��̼��� �� ������. �׸��� ����.
	public void startFadeIn() {
		if(1 == aniList.size()){
			animatorSet.play(aniList.get(0));
		}else if(0 != aniList.size()){
			for(int i = 0; i < aniList.size() - 1; i++) {
				animatorSet.play(aniList.get(i)).before(aniList.get(i+1));
			}
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
						
						if(device != null) {
							manager.pairAsync(device);
						}											
					}
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	 public static boolean copyFile(InputStream inputStream, OutputStream out) {
		 byte buf[] = new byte[1024*1024];
		 int len;
		 Log.d("ī������", "ī������");
		 try {
			 int i = 0;
			 while ((len = inputStream.read(buf)) != -1) {
				 out.write(buf, 0, len);
				 Log.d("ī������ ���?",""+i++);
			 }
			 out.close();
			 inputStream.close();
		 } catch (IOException e) {
			 Log.d(FILE_TEST, e.toString());
			 Log.d("ī�����ϳ�", "����");
			 return false;
		 }
		  
		 Log.d("ī�����ϳ�", "����");
		 return true;
	 }

	@Override
	public void onDeviceConnected(final WFDPairInfo info) {
		
		info.connectSocketAsync(new PairSocketConnectedListener() {
			
			@Override
			public void onSocketConnected(Socket s) {
				try{
					if (info.info.groupFormed && info.info.isGroupOwner) {
						Log.d("TEST", "Server: connection done");
						MessageAsyncTask m = new MessageAsyncTask(s);
						m.execute();
					} else if (info.info.groupFormed) {
						Log.d("TEST", "Client: ready to send message");
						while (true) {
							BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
							PrintWriter out = new PrintWriter(w, true);
		                    String return_msg = "PING";
		                    out.println(return_msg);
		                    Log.d("TEST", "result :" + return_msg);
		                    
		                    Thread.sleep(1000);
						}
					}
				} catch(IOException e) {
					Log.e("TEST ERROR", e.getMessage());
				} catch (InterruptedException e) {					
					e.printStackTrace();
				}
			}
		});			
	}

	@Override
	public void onDeviceConnectFailed(int reasonCode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDeviceDisconnected() {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}
	
	public class MessageAsyncTask extends AsyncTask<Void,Void,Void> {
		private Socket client = null;
		
		public MessageAsyncTask(Socket s) {
			client = s;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				while(true) {
					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String str = in.readLine();
                    Log.d(FILE_TEST,"S: Received: '" + str + "'");
				}
			} catch (Exception e) {
				Log.d(FILE_TEST, "Server: error");
			}
			return null;
		}
		
	}
}