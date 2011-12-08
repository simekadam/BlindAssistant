package com.simekadam.blindassistant.ui;

import com.simekadam.blindassistant.SpeechHelper;
import com.simekadam.blindassistant.ui.BlindButton.OnHoverListener;

import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;


public class BlindTableLayout extends LinearLayout implements OnTouchListener, OnHoverListener{

	Context mContext;
	Display display;
	final float width, height,leftFactor, topFactor;
	private Vibrator vibrator;
	private int currentlyTouched = -1;
	private OnDoubleClickListener onDoubleClickListener;
	
	

	public BlindTableLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
	    width = display.getWidth();
	    height = display.getHeight();	    
	    leftFactor = width/2;
		topFactor = height/3;
		this.setOnTouchListener(this);
		BlindButton.setOnHoverListener(new OnHoverListener() {
			
			@Override
			public void onHover(int index) {
				int row = (int)Math.floor(index/2);
				LinearLayout lin = (LinearLayout) getChildAt(row);
				BlindButton btn = (BlindButton ) lin.getChildAt(index-row*2);
				SpeechHelper.getInstance().say(btn.getSpeechLabel(), getContext(), SpeechHelper.UI_RESPONSE);
			}
		});
		this.setClickable(true);
		this.setEnabled(true);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		// TODO Auto-generated constructor stub
	}
	
	public BlindTableLayout(Context context){
		super(context);
		this.mContext = context;
		display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
	    width = display.getWidth();
	    height = display.getHeight();	    
	    leftFactor = width/2;
		topFactor = height/3;
		this.setOnTouchListener(this);
		this.setClickable(true);
		this.setEnabled(true);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		
	}
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
	  	//super.onTouchEvent(event);
		Log.v("touch", event.getAction()+" xxx "+MotionEvent.ACTION_UP);
		if(event.getAction() == MotionEvent.ACTION_UP){
			currentlyTouched = -1;
			return false;
		}
		checkEdges(event.getRawX(), event.getRawY());
		
		return false;
	}
  
	
  
  private void checkEdges(float x, float y){
	  
	  int touchedIndex = (int) (Math.floor(x/leftFactor) + (Math.floor( y / topFactor ) * 2));
	  if(currentlyTouched == -1) currentlyTouched = touchedIndex;
	  else if(currentlyTouched != touchedIndex){
		  vibrator.vibrate(100);
		  BlindButton btn = (BlindButton) this.getChildAt(touchedIndex);
		  Log.v("test", btn+"test");
		  SpeechHelper.getInstance().say(btn.getSpeechLabel(), getContext(), SpeechHelper.UI_RESPONSE);
		  currentlyTouched = touchedIndex;
	  }
	  
	  
	
	  
  }
  
  public OnDoubleClickListener getOnDoubleClickListener() {
		return onDoubleClickListener;
	}

	public void setOnDoubleClickListener(OnDoubleClickListener onDoubleClickListener) {
		this.onDoubleClickListener = onDoubleClickListener;
	}
  
  public interface OnDoubleClickListener{
		public abstract void onDoubleClick();
	}

@Override
public void onHover(int index) {
	// TODO Auto-generated method stub
	
	
}



}
