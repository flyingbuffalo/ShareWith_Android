package flying.bufallo.sharewith;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
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
import android.widget.Toast;

public class MainActivity extends Activity {
	
	final int MAX_DEVICE = 5;
	float dpHeight;
	float dpWidth;
	
	RelativeLayout main;
	ImageView btnCenter;	
	AnimatorSet animatorSet = new AnimatorSet();
	List<ObjectAnimator> aniList = new ArrayList<ObjectAnimator>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		main = (RelativeLayout) findViewById(R.id.main);
		btnCenter = (ImageView) findViewById(R.id.center);
		
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
		
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
		
		float x = btnCenter.getX();
		float y = btnCenter.getY();
		
		Log.d("TEST ANI", "center x : " + x);
		Log.d("TEST ANI", "center y : " + y);
		// 생성과 페이드 인 아웃터
		for(int i = 0; i < MAX_DEVICE; i++) {
			double circle_x = x + x*Math.cos(Math.toRadians((360/MAX_DEVICE)*i));
			double circle_y = y + x*Math.sin(Math.toRadians((360/MAX_DEVICE)*i));
			createdCircle(circle_x, circle_y, i);
			
			Log.d("TEST ANI", "circle " + i + " position : " + circle_x + " , " + circle_y);
		}		
		startFadeIn();
	}
	
	// 원 위치에 그리고 애니메이션 + 뷰 = > 애니메이터를 만들고 저장
	public void createdCircle(double x, double y, final int index) {
		final View child = new View(getApplicationContext());
		child.setBackground(getResources().getDrawable(R.drawable.circle));		
		child.setX((float) x);
		child.setY((float) y);
		child.setVisibility(View.GONE);
		
		Log.d("TEST ANI", "index is " + index);	
		
		child.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "this is circle " + index, Toast.LENGTH_SHORT).show();
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
		animatorSet.start();
	}
	
	
}