package com.simekadam.blindassistant.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.view.View.OnTouchListener;



public class BlindGridView extends GridView implements OnTouchListener, android.widget.AdapterView.OnItemClickListener{
	private Context mContext;
	Display display;
	final float width, height,leftFactor, topFactor;
	private Vibrator vibrator;
	private int currentlyTouched = -1;
	
	
	
	  public BlindGridView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    this.mContext = context;
	    this.setOnTouchListener(this);
	    this.setOnItemClickListener(this);
	    display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
	    width = display.getWidth();
	    height = display.getHeight();
		leftFactor = width/2;
		topFactor = height/3;
		vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		 

	  }
	  
	  public BlindGridView(Context context) {
		    super(context);
		    this.mContext = context;
		    this.setOnTouchListener(this);
		    this.setOnItemClickListener(this);

		    display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		    width = display.getWidth();
		    height = display.getHeight();	    
		    leftFactor = width/2;
			topFactor = height/3;
			
			vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		  }
	  
	  
	  @Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
		  	//super.onTouchEvent(event);
			if(event.getAction() == MotionEvent.ACTION_UP){
				currentlyTouched = -1;
			}
			checkEdges(event.getRawX(), event.getRawY());
			
			return false;
		}
	  
	  
	  private void checkEdges(float x, float y){
		  
		  int touchedIndex = (int) (Math.floor(x/leftFactor) + (Math.floor( y / topFactor ) * 2));
		  Log.v("toucheIndex", touchedIndex+"");
		  if(currentlyTouched == -1) currentlyTouched = touchedIndex;
		  if(currentlyTouched != touchedIndex){
			  vibrator.vibrate(100);
			  currentlyTouched = touchedIndex;
		  }
		  
	  }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		Log.v("test2","clicked");
	}
	  
	}


