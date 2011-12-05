package com.simekadam.blindassistant.ui;

import com.simekadam.blindassistant.BlindGridView;
import com.simekadam.blindassistant.SpeechHelper;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;



public class BlindAdapter extends BaseAdapter implements OnItemSelectedListener, OnItemClickListener{

	private Context mContext;
	private String[] labels;
	private Button currentlyClicked;
	
	public BlindAdapter(Context c, String[] labels) {  
		  mContext = c;  
		  this.labels = labels;
		  this.currentlyClicked = null;
		  
		 } 
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return labels.length;
	}

	@Override
	public Object getItem(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BlindButton btn;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            btn = new BlindButton(mContext);
            Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
            btn.setLayoutParams(new GridView.LayoutParams(display.getWidth()/2,(int) Math.floor(display.getHeight()/3)));
           btn.setText("ahoj");
            //imageView.setScaleType(ImageView.ScaleType.MATRIX);
            btn.setPadding(8, 8, 8, 8);
            btn.setBackgroundColor(Color.DKGRAY);
            btn.setTextColor(Color.WHITE);
            
            
            
        } else {
        	btn = (BlindButton) convertView;
        }

        btn.setBackgroundColor(Color.LTGRAY);
        return btn;
		  
	}
	
	private void say(String text, String utteranceContext) {
		// TODO Auto-generated method stub
		SpeechHelper.getInstance().say(text, mContext, utteranceContext);
		
	}


	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, int position,
			long id) {
		BlindButton btn = (BlindButton) view;
		btn.setBackgroundColor(Color.RED);
		Log.v("selected", "text: "+btn.getText());
		
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		Log.v("selected","nothing");
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		BlindButton btn = (BlindButton) view;
		btn.setBackgroundColor(Color.RED);
		Log.v("clicked", "text: "+btn.getText());
	}
	
	
		
		
		


		


		
	
	

}
