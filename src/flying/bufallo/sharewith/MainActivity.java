package flying.bufallo.sharewith;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	final int MAX_DEVICE = 5;
	float dpHeight;
	float dpWidth;
	
	RelativeLayout main;
	ImageView btnCenter;
	
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
		btnCenter.setAnimation(fadeOut);
		
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
	}
	
	public void createdCircle(double x, double y, final int index) {
		View child = new View(getApplicationContext());
		child.setBackground(getResources().getDrawable(R.drawable.circle));		
		child.setX((float) x);
		child.setY((float) y);
		
		child.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "this is circle " + index, Toast.LENGTH_SHORT).show();
			}
		});
		
		main.addView(child, 100, 100);
		
		Animation fadeIn_first = new AlphaAnimation(0, 1);
		fadeIn_first.setInterpolator(new DecelerateInterpolator()); // add this
		fadeIn_first.setDuration(1000);
			
		child.setAnimation(fadeIn_first);		
	}
}