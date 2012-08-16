package com.simekadam.blindassistant.ui;

import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.helpers.SpeechHelper;
import com.simekadam.blindassistant.helpers.SpeechHelper.OnSpeechEndedListener;
import com.simekadam.blindassistant.ui.BlindTableLayout.OnDoubleClickListener;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Display;

;
public class BlindButton extends Button implements OnClickListener,
		OnTouchListener {
	private static BlindButton prevClicked = null;
	private static boolean allowed = false;
	private Intent intent;
	private String speechLabel;
	private String actionSpeechLabel;
	private static Vibrator vibrator;
	private static int currentlyTouched = -1;
	static float width, height, leftFactor, topFactor;
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
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
	}

	public BlindButton(Context context) {
		super(context);
		this.setOnClickListener(this);
		this.speechLabel = context.getString(R.string.nullAction);;
		display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		width = display.getWidth();
		height = display.getHeight();
		leftFactor = width / 2;
		Point point = new Point();
		display.getSize(point);
		topFactor = point.y/10*3;

	}

	public BlindButton(Context context, AttributeSet atributes) {
		super(context, atributes);
		this.setOnClickListener(this);
		this.setOnTouchListener(this);
		this.speechLabel = context.getString(R.string.nullAction);;
		vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		width = display.getWidth();
		height = display.getHeight();
		leftFactor = width / 2;
		Point point = new Point();
		display.getSize(point	);
		topFactor = point.y/10*3;

	}

	private static void checkEdges(float x, float y) {
		if (x < 20 || x > width - 20 || y < 20 || y > topFactor * 3) {
			vibrator.vibrate(100);
		} else {
			int touchedIndex = (int) (Math.floor(x / leftFactor) + (Math
					.floor(y / topFactor) * 2));
//			Log.v("toucheIndex", touchedIndex + "");
			if (currentlyTouched == -1)
				currentlyTouched = touchedIndex;
			else if (touchedIndex < 6 && currentlyTouched != touchedIndex) {
				vibrator.vibrate(100);
				BlindButton.onHoverListener.onHover(touchedIndex);

				currentlyTouched = touchedIndex;
			}
		}
	}

	public BlindButton(Context context, AttributeSet atributes, int defStyle) {
		super(context, atributes, defStyle);
		this.setOnClickListener(this);
		this.speechLabel = context.getString(R.string.nullAction);
		display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		width = display.getWidth();
		height = display.getHeight();
		leftFactor = width / 2;
		Point point = new Point();
		display.getSize(point);
		topFactor = point.y/10*3;

	}

	private void setIntent(Intent intent) {
		this.intent = intent;
	}

	private void say(String text, String utteranceContext) {
		SpeechHelper.getInstance().setOnSpeechEndedListener(
				new OnSpeechEndedListener() {

					@Override
					public void speechEnded() {
						// TODO Auto-generated method stub
						onDoubleClickListener.onDoubleClick();
					}
				});
		SpeechHelper.getInstance().say(text, this.getContext(),
				utteranceContext);
	}

	@Override
	public void onClick(View view) {

		BlindButton clicked = (BlindButton) view;

		if (prevClicked != null && prevClicked.equals(clicked)
				&& allowed == true) {
			prevClicked = null;
			if (clicked.getActionSpeechLabel() != null) {
				this.say(clicked.getActionSpeechLabel(),
						SpeechHelper.UI_RESPONSE_WITH_CALLBACK);
			} else if (clicked.getId() == R.id.seventhButton){
				
			}
			
			else {
				this.say(
						"toto tlačítko neobsahuje žádnou volbu, zvolte prosím nějaké jiné",
						SpeechHelper.UI_RESPONSE);
			}

		} else {
			allowed = true;
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

	public void setOnDoubleClickListener(
			OnDoubleClickListener onDoubleClickListener) {
		this.onDoubleClickListener = onDoubleClickListener;
	}

	public interface OnDoubleClickListener {
		public abstract void onDoubleClick();
	}

	public interface OnHoverListener {
		public abstract void onHover(int index);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		return super.onTouchEvent(event);

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		BlindButton clicked = (BlindButton) v;
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			vibrator.vibrate(50);

			if (prevClicked == null || !prevClicked.equals(clicked)) {
				
				BlindButton.allowed = false;
				if (clicked.getSpeechLabel() != null) {
					this.say(clicked.getSpeechLabel(), SpeechHelper.UI_RESPONSE);
				} 
				else if ((int)clicked.getId() == (int)R.id.seventhButton){
					
				}else {
					this.say("žádná akce", SpeechHelper.UI_RESPONSE);
				}

			}

		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			currentlyTouched = -1;
			BlindButton.prevClicked = clicked;

			return false;
		} else {
			checkEdges(event.getRawX(), event.getRawY());
		}
		return false;
	}
	
	public static float convertDpToPixel(float dp,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi/160f);
	    return px;
	}

}
