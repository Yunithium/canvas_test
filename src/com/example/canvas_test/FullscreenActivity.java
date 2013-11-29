package com.example.canvas_test;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.example.canvas_test.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
	
	FrameLayout mainView;
	private BallView mBallView = null;
	Handler RedrawHandler = new Handler();
	Timer mTmr = null;
	TimerTask mTsk = null;
	
	private int toastTime = 20;
	
	float mXSpd = 0;
	float mYSpd = 0;
	float mSpeedMul = 1.0f; // 0.04f; (om vi har acc)
	int mCap = 7;
	double bounceX = -0.5;
	double bounceY = -0.7;
	boolean clicksAllowed = true;
	BitmapDrawable playlist;
	BitmapDrawable mainscreen;
	


	Random rand = new Random();
	int width, height;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen);
		
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
		
		Toast.makeText(this, "Starting", Toast.LENGTH_SHORT).show();
		mainView = (android.widget.FrameLayout) findViewById(R.id.painting_place);
		mBallView = new BallView(this, width/2, height/2, 30);
		
		
		mainView.addView(mBallView);
		mBallView.invalidate();
		
		 ((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
		    		new SensorEventListener() {    
		    			@Override  
		    			public void onSensorChanged(SensorEvent event) {  
		    			    //set ball speed based on phone tilt (ignore Z axis)
		    				mXSpd = -event.values[0]*mSpeedMul; //accel?
		    				mYSpd = event.values[1]*mSpeedMul;
		    				
//		    				TextView tv = (TextView) findViewById(R.id.fullscreen_content);
//		    				tv.setText(mXSpd + "");
		    				if(mXSpd>7) mXSpd = 7;
		    				if(mYSpd>7) mYSpd = 7;
		    				//timer event will redraw ball
		    			}
		        		@Override  
		        		public void onAccuracyChanged(Sensor sensor, int accuracy) {} //ignore this event
		        	},
		        	((SensorManager)getSystemService(Context.SENSOR_SERVICE))
		        	.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);
	}
	
	@Override
    public void onResume() //app moved to foreground (also occurs at app startup)
    {
        //create timer to move ball to new position
        mTmr = new Timer(); 
        mTsk = new TimerTask() {
			public void run() {
				//android.util.Log.d("Hej", "d�");
				
				/*mXSpd+=0.1;
				mYSpd+=0.1;*/
				
				float centerYo = height/2;
				float centerXo = width/2;
				float t = 0.001f;
				if(Math.abs(centerXo-mBallView.mX) > 1) mXSpd += t*(centerXo-mBallView.mX);
				if(Math.abs(centerYo-mBallView.mY) > 1) mYSpd += t*(centerYo-mBallView.mY);
				mBallView.mX+=mXSpd;
				mBallView.mY+=mYSpd;
				
				/* H�ger kant */
				if (mBallView.mX > width-30){
					mBallView.mX = width-30; 
					mXSpd *= bounceX;
				}
				else if (mBallView.mX < 30){ // v�nster kant
					mBallView.mX = 30; 
					mXSpd *= bounceX;
				}
				if (mBallView.mY > height-60){ // botten
					mBallView.mY = height-60; 
					mYSpd *= bounceY;
				}
				else if (mBallView.mY < 30){ // topp
					mBallView.mY=30; 
					mYSpd *= bounceY;
				}
				
				//V�ggtr�ffar
				if (mBallView.mX >= width-30 && Math.abs(mBallView.mY-(height*0.624)) < height*0.178){
					long eventtime = SystemClock.uptimeMillis();
					
					Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null); 
					KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0); 
					downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent); 
					sendOrderedBroadcast(downIntent, null);

					
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					v.vibrate(50);
				}
				
				if (mBallView.mX <= 30 && mBallView.mY >= height*0.42 && mBallView.mY <= height*0.75){
					long eventtime = SystemClock.uptimeMillis();
					
					Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null); 
					KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0); 
					downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent); 
					sendOrderedBroadcast(downIntent, null);
					
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					v.vibrate(50);
					
					
				}
				
				if (mBallView.mY >= height-60 && mBallView.mX >= width*0.2 && mBallView.mX <= width*0.8){
					long eventtime = SystemClock.uptimeMillis();
					
					Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
					KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
					downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
					sendOrderedBroadcast(downIntent, null);

				
					
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					v.vibrate(50);
				}
				
		
				//redraw ball. Must run in background thread to prevent thread lock.
				RedrawHandler.post(new Runnable() {
				    public void run() {	
					   mBallView.invalidate();
				  }});
			}}; // TimerTask
        mTmr.schedule(mTsk,1000,10); //start timer
        super.onResume();
    } // onResume
	
	@Override
    public void onDestroy() //main thread stopped
    {
    	super.onDestroy();
    	android.os.Process.killProcess(android.os.Process.myPid());  //remove app from memory 
    }
	
	public void decreaseBounce(View view){
		Toast.makeText(this, "Decreasing Bounce", toastTime).show();
		bounceX += 0.1;
		bounceY += 0.1;
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(400);
	}
	
	public void increaseBounce(View view){
		Toast.makeText(this, "Increasing Bounce", toastTime).show();
		bounceX -= 0.1;
		bounceY -= 0.1;
	
		
	}
	
	public void decreaseSpeed(View view){
		Toast.makeText(this, "Decreasing speed", toastTime).show();
		mSpeedMul -= 0.01f;
		
	}
	
	public void increaseSpeed(View view){
		Toast.makeText(this, "Increasing speed", toastTime).show();
		mSpeedMul += 0.01f;

	}
	
}
