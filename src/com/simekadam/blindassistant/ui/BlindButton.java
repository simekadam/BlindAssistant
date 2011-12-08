package com.simekadam.blindassistant.ui;

import com.simekadam.blindassistant.SpeechHelper;
import com.simekadam.blindassistant.SpeechHelper.OnSpeechEndedListener;
import com.simekadam.blindassistant.ui.BlindTableLayout.OnDoubleClickListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Display;;
public class BlindButton extends Button implements OnClickListener, OnTouchListener{
    private static BlindButton prevClicked = null;
    private Intent intent;
    private String speechLabel;
    private String actionSpeechLabel;
    private static Vibrator vibrator;
    private static int currentlyTouched = -1;
	static float width, height,leftFactor, topFactor;
	static Display display;
	private OnDoubleClickListener onDoubleClickListener;
	private static OnHoverListener onHoverListener;

	


	public static OnHoverListener getOnHoverListener() {
		return onHoverListener;
	}


	public static void setOnHoverListener(OnHoverListener onHoverListener) {
		BlindButton.onHoverListener = onHoverListener;
	}


	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		// TODO Auto-generated method stub
		Log.v("tst", "test");
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}


	public BlindButton(Context context) {
        super(context);
        this.setOnClickListener(this);
        this.speechLabel = "žádná akce";
        display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 

        width = display.getWidth();
	    height = display.getHeight();	    
	    leftFactor = width/2;
		topFactor = height/3;
       
    }
    
    
    public BlindButton(Context context, AttributeSet atributes) {
        super(context, atributes);
        this.setOnClickListener(this);
        this.setOnTouchListener(this);
        this.speechLabel = "žádná akce";
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 

        width = display.getWidth();
	    height = display.getHeight();	    
	    leftFactor = width/2;
		topFactor = height/3;
		
		
		
		
	
    }

    private static void checkEdges(float x, float y){
  	  
  	  int touchedIndex = (int) (Math.floor(x/leftFactor) + (Math.floor( y / topFactor ) * 2));
  	  Log.v("toucheIndex", touchedIndex+"");
  	  if(currentlyTouched == -1) currentlyTouched = touchedIndex;
  	  else if(currentlyTouched != touchedIndex){
  		  vibrator.vibrate(100);
  		  BlindButton.onHoverListener.onHover(touchedIndex);
  		 
  		  currentlyTouched = touchedIndex;
  	  }
    }
    
    public BlindButton(Context context, AttributeSet atributes, int defStyle){
    	super(context, atributes, defStyle);
    	this.setOnClickListener(this);
    	this.speechLabel = null;
    	display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 

        width = display.getWidth();
	    height = display.getHeight();	    
	    leftFactor = width/2;
		topFactor = height/3;

    }

    private void setIntent(Intent intent) {
        this.intent = intent;
    }
    private void say(String text, String utteranceContext) {
    	SpeechHelper.getInstance().setOnSpeechEndedListener(new OnSpeechEndedListener() {
			
			@Override
			public void speechEnded() {
				// TODO Auto-generated method stub
				onDoubleClickListener.onDoubleClick();
			}});
    	SpeechHelper.getInstance().say(text, this.getContext(), utteranceContext);
    }
    
    
    
    @Override
	public void onClick(View view) {
    	
    	BlindButton clicked = (BlindButton) view;
    	
    	if(prevClicked != null){
    		prevClicked = null;
    		if(clicked.getActionSpeechLabel() != null){
    			this.say(clicked.getActionSpeechLabel(), SpeechHelper.UI_RESPONSE_WITH_CALLBACK);
    		}else{
    			this.say("toto tlačítko neobsahuje žádnou volbu, zvolte prosím nějaké jiné", SpeechHelper.UI_RESPONSE);
    		}
    		
    		}
    	else{
    		BlindButton.prevClicked = clicked;
    	}
    		
    	
    	
    }
    
    
    
    public String getSpeechLabel() {
		return speechLabel;
	}


	public void setSpeechLabel(String speechLabel) {
		this.speechLabel = speechLabel;
	}


	public String getActionSpeechLabel() {
		return actionSpeechLabel;
	}


	public void setActionSpeechLabel(String actionSpeechLabel) {
		this.actionSpeechLabel = actionSpeechLabel;
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
	  
	  public interface OnHoverListener{
		  public abstract void onHover(int index);
	  }

	@Override
	public boolean onTouchEvent( MotionEvent event) {
		// TODO Auto-generated method stub
		
		
		return super.onTouchEvent(event);
		
			
	 
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			vibrator.vibrate(20);
		BlindButton clicked = (BlindButton) v;
		
		if(prevClicked == null){
			Log.v("tst","test");
    		if(clicked.getSpeechLabel() != null){
    			this.say(clicked.getSpeechLabel(), SpeechHelper.UI_RESPONSE);
    		}
    		else{
    			this.say("žádná akce", SpeechHelper.UI_RESPONSE);
    		}
    		
    	}
    	
			}
		else if(event.getAction() == MotionEvent.ACTION_UP){
			currentlyTouched = -1;
		}
		else{
			checkEdges(event.getRawX(), event.getRawY());
		}
		return false;
	}


	
    
    
}
    
