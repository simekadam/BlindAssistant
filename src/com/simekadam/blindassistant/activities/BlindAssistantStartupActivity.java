package com.simekadam.blindassistant.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.simekadam.blindassistant.helpers.SpeechHelper;
import com.simekadam.blindassistant.helpers.SpeechHelper.OnSpeechEndedListener;

/**
 * @author simekadam
 *
 */
public class BlindAssistantStartupActivity extends Activity implements OnClickListener{

	private static final int MY_DATA_CHECK_CODE = 55;
	public static final String TAG = BlindAssistantStartupActivity.class
			.getSimpleName();
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		checkAuthentication();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		
		this.startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

		super.onCreate(savedInstanceState);

	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == MY_DATA_CHECK_CODE) {
			Log.d(TAG, resultCode+" "+TextToSpeech.Engine.CHECK_VOICE_DATA_PASS);
		 if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS || resultCode == 0) {
			 //success, create the TTS instance
			SpeechHelper.getInstance().setOnSpeechEndedListener(
					new OnSpeechEndedListener() {

						@Override
						public void speechEnded() {

							// TODO Auto-generated method stub
							processStartup();
							SpeechHelper.getInstance()
									.removeOnSpeechEndedListener();
						}
					});
			 SpeechHelper.getInstance().say("Vítejte v aplikaci pro sledování polohy.",
			 this.getApplicationContext(),
			 SpeechHelper.UI_RESPONSE_WITH_CALLBACK);

			

			 } else {
			 Log.d("voice data", "je to v haji");
			 // missing data, install it
			 Intent installIntent = new Intent();
			 installIntent.setAction(
			 TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
			 startActivity(installIntent);
			 }
		}
	}

	/**
	 * @return 
	 */
	private boolean checkAuthentication() {
		SharedPreferences sp = getSharedPreferences(
				"com.simekadam.blindassistant", Context.MODE_PRIVATE);
		return sp.getBoolean("logged", false);
	}
	
	private void processStartup(){
		Log.d(TAG, "processing startup");
		if (checkAuthentication()) {
			Intent startApp = new Intent(getApplicationContext(),
					BlindAssistantActivity.class);
			startApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(startApp);
		} else {
	        
			Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
			loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(loginIntent);

			
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		SpeechHelper.getInstance().stopTTS();
	}

	

}
