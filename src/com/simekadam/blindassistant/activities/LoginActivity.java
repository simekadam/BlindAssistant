package com.simekadam.blindassistant.activities;

import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.helpers.SpeechHelper;
import com.simekadam.blindassistant.helpers.SpeechHelper.OnSpeechEndedListener;
import com.simekadam.blindassistant.ui.BlindButton;
import com.simekadam.blindassistant.ui.BlindButton.OnDoubleClickListener;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class LoginActivity extends Activity {

	public static final String TAG = LoginActivity.class.getSimpleName();
	private String userEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blindassistantmain);
		BlindButton btn1 = (BlindButton) findViewById(R.id.firstButton);
		btn1.setSpeechLabel("Souhlasím, vstoupit do aplikace.");
		btn1.setText("Přihlásit");
		btn1.setActionSpeechLabel("Přihlašuji");
		btn1.setOnDoubleClickListener(new OnDoubleClickListener() {

			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
				if (logIn()) {
					final Intent startApp = new Intent(getApplicationContext(),
							BlindAssistantActivity.class);
					startActivity(startApp);
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
													"Přihlášení se nezdařilo");
										}
									});
					SpeechHelper.getInstance().say(
							"Přihlášení se nezdařilo",
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

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
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
