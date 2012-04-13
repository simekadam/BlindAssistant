package com.simekadam.blindassistant.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;

import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.helpers.SpeechHelper.OnSpeechEndedListener;
import com.simekadam.blindassistant.helpers.SpeechHelper;
import com.simekadam.blindassistant.ui.BlindButton;
import com.simekadam.blindassistant.ui.BlindButton.OnDoubleClickListener;

public class BlindAssistantStartupActivity extends Activity {

	private static final int MY_DATA_CHECK_CODE = 55;
	public static final String TAG = BlindAssistantStartupActivity.class
			.getSimpleName();
	private String userEmail;

	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		checkAuthentication();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		this.startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == MY_DATA_CHECK_CODE) {
			final Intent startApp = new Intent(getApplicationContext(),
					BlindAssistantActivity.class);
			// if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
			// success, create the TTS instance
//			SpeechHelper.getInstance().setOnSpeechEndedListener(
//					new OnSpeechEndedListener() {
//
//						@Override
//						public void speechEnded() {
//
//							// TODO Auto-generated method stub
//							startActivity(startApp);
//							SpeechHelper.getInstance()
//									.removeOnSpeechEndedListener(this);
//						}
//					});
			// SpeechHelper.getInstance().say("Vítejte v testovací verzi aplikace pro sledování polohy. Já jsem Iveta a budu Vás aplikací provázet. ",
			// this.getApplicationContext(),
			// SpeechHelper.UI_RESPONSE_WITH_CALLBACK);

			if (checkAuthentication()) {
				startActivity(startApp);
			} else {
		        

				setContentView(R.layout.blindassistantmain);
				BlindButton btn1 = (BlindButton) findViewById(R.id.firstButton);
				btn1.setSpeechLabel("souhlasím, vstoupit do aplikace");
				btn1.setText("Přihlásit");
				btn1.setActionSpeechLabel("Přihlašuji");
				btn1.setOnDoubleClickListener(new OnDoubleClickListener() {

					@Override
					public void onDoubleClick() {
						// TODO Auto-generated method stub
						if (logIn()) {
							SpeechHelper.getInstance()
									.setOnSpeechEndedListener(
											new OnSpeechEndedListener() {

												@Override
												public void speechEnded() {
													// TODO Auto-generated
													// method stub
													
													startActivity(startApp);

												}
											});
							SpeechHelper.getInstance().say(
									"Přihlášení proběhlo úspěšně.",
									getApplicationContext(),
									SpeechHelper.UI_RESPONSE_WITH_CALLBACK);

						} else {
							SpeechHelper.getInstance()
									.setOnSpeechEndedListener(
											new OnSpeechEndedListener() {

												@Override
												public void speechEnded() {
													// TODO Auto-generated
													// method stub
													Log.d(TAG,
															"a je to rozbitý");
												}
											});
							SpeechHelper.getInstance().say(
									"Vyskytl se problém s přihlášením",
									getApplicationContext(),
									SpeechHelper.UI_RESPONSE);

						}
					}
				});
				BlindButton btn2 = (BlindButton) findViewById(R.id.secondButton);
				btn2.setText("Ukončit");
				btn2.setActionSpeechLabel("Ukončuji aplikaci");
				btn2.setSpeechLabel("Ukončit aplikaci");
				btn2.setOnDoubleClickListener(new OnDoubleClickListener() {
					
					@Override
					public void onDoubleClick() {
						// TODO Auto-generated method stub
						
					}
				});
			}

			// } else {
			// Log.d("voice data", "je to v haji");
			// // missing data, install it
			// Intent installIntent = new Intent();
			// installIntent.setAction(
			// TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
			// startActivity(installIntent);
			// }
		}
	}

	private boolean checkAuthentication() {
		SharedPreferences sp = getSharedPreferences(
				"com.simekadam.blindassistant", Context.MODE_PRIVATE);
		return sp.getBoolean("logged", false);
	}

	private boolean logIn() {

		AccountManager acm = AccountManager.get(getApplicationContext());
		Account[] accounts = acm.getAccountsByType("com.google");
		if (accounts.length == 1) {
			userEmail = accounts[0].name;
			SharedPreferences sp = getSharedPreferences(
					"com.simekadam.blindassistant", Context.MODE_PRIVATE);

			sp.edit().putString("userID", userEmail).putBoolean("logged", true)
					.commit();
			return true;
		} else {
			for (Account acc : accounts) {

			}
			return true;
		}

	}

}
