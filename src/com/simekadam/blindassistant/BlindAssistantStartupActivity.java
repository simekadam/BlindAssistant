package com.simekadam.blindassistant;

import com.simekadam.blindassistant.SpeechHelper.OnSpeechEndedListener;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

public class BlindAssistantStartupActivity extends Activity {
	
	private static final int MY_DATA_CHECK_CODE = 55;
	
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
    	this.startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		super.onCreate(savedInstanceState);
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == MY_DATA_CHECK_CODE) {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	            // success, create the TTS instance
	        		SpeechHelper.getInstance().setOnSpeechEndedListener(new OnSpeechEndedListener() {
					
					@Override
					public void speechEnded() {
						// TODO Auto-generated method stub
						Intent startApp = new Intent(getApplicationContext(),BlindAssistantActivity.class);
			        	startActivity(startApp);
					}
				});
	        	SpeechHelper.getInstance().say("Vítejte v testovací verzi aplikace pro sledování polohy. Já jsem Iveta a budu Vás aplikací provázet. ", this.getApplicationContext(), SpeechHelper.UI_RESPONSE_WITH_CALLBACK);
	        	
	        	
	        } else {
	            // missing data, install it
	            Intent installIntent = new Intent();
	            installIntent.setAction(
	                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installIntent);
	        }
	    }
	}
	
	
	
		 
	
	
}
