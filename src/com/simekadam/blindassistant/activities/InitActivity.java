package com.simekadam.blindassistant.activities;

import com.simekadam.blindassistant.helpers.SpeechHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

public class InitActivity extends Activity{

	private static final int MY_DATA_CHECK_CODE = 55;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		checkTTSData();
	}
	
	private void checkTTSData(){
    	Intent checkIntent = new Intent();
    	checkIntent.setClass(this.getApplicationContext(), SpeechHelper.class);
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivity(checkIntent);
    }
	
	
	protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                SpeechHelper.getInstance().say("Aplikace pro sledování vaší polohy byla zapnuta", getApplicationContext(), SpeechHelper.STANDARD_MSG);
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
