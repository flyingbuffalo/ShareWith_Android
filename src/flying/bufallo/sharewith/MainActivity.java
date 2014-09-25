package flying.bufallo.sharewith;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
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

import com.ipaulpro.afilechooser.utils.FileUtils;

public class MainActivity extends Activity {
	
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
	int MAX_DEVICE = 3;
	final int CENTER_BTN_SIZE = 180;
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
				clickCenter();				
			}
		});
	}
	
	public void clickCenter() {
		// 페이드 아웃 센터
		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator()); // and this
		fadeOut.setStartOffset(0);
		fadeOut.setDuration(500);
		btnCenter.startAnimation(fadeOut);
		
		
		float x = (float) (btnCenter.getX()+btnSize);
		float y = (float) (btnCenter.getY()+btnSize);
		
		
		Log.d("TEST ANI", "center x : " + x);
		Log.d("TEST ANI", "center y : " + y);
		
		//이미 생성되었던 버튼이 있으면 삭제하고 리스트 초기화
		if(isBtnExist){
			for(int i = 0; i < aniList.size(); i++){
				main.removeView(findViewById(DYNAMIC_BUTTON_ID+i));
			}
			aniList.clear();
			MAX_DEVICE = 5;
		}
		
		// 생성과 페이드 인 아웃터
		for(int i = 0; i < MAX_DEVICE; i++) {
			double circle_x = x + (x-100)*Math.cos(Math.toRadians((360/MAX_DEVICE)*i));
			double circle_y = y + (x-100)*Math.sin(Math.toRadians((360/MAX_DEVICE)*i));
			createdCircle(circle_x, circle_y, i);
			
			Log.d("TEST ANI", "circle " + i + " position : " + circle_x + " , " + circle_y);
		}		
		startFadeIn();
	}
	
	//setText로 기기 정보 설정..?
	public void setInformation(int index){

		btnCenter.setVisibility(View.GONE);
		textCenter.setText("Circle "+index);
		textCenter.setVisibility(View.VISIBLE);
		
		
		//기기 정보 클릭하면 afilechooser
		textCenter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			       Intent target = FileUtils.createGetContentIntent();
                   // Create the chooser Intent
                   Intent intent = Intent.createChooser(
                           target, "aFileChooser");
                   try {
                     startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                   } catch (ActivityNotFoundException e) {
                       // The reason for the existence of aFileChooser
                   }
			}
		});
		
	}
	
	// 원 위치에 그리고 애니메이션 + 뷰 = > 애니메이터를 만들고 저장
	public void createdCircle(double x, double y, final int index) {
		final View child = new View(getApplicationContext());
		child.setBackground(getResources().getDrawable(R.drawable.btn_android));		
		child.setX((float) x);
		child.setY((float) y);
		child.setVisibility(View.GONE);
		
		//동적 생성된 버튼의 ID 설정
		child.setId(DYNAMIC_BUTTON_ID+index);
		
		Log.d("TEST ANI", "index is " + index);	
		
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
	
	// 애니메이터 리스트에서 순차적으로 애니메이션의 순서를 정해줌. 그리고 시작.
	public void startFadeIn() {
		for(int i = 0; i < aniList.size() - 1; i++) {
			animatorSet.play(aniList.get(i)).before(aniList.get(i+1));
		}
		Log.d("TEST ANILIST", "anilist size : "+aniList.size());
		animatorSet.start();
		isBtnExist = true;
	}
	
	
}