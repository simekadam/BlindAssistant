package com.simekadam.blindassistant.activities;

import com.simekadam.blindassistant.helpers.SpeechHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ShutdownActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		final Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		SpeechHelper.getInstance().say("Pro využívání aplikace se musíte přihlásit. Přihlášením souhlasíte s využitím Vaší e-mailové pro účely identifikace nasbíraných údajů. Pokud nesouhlasíte můžete aplikaci ukončit.", getApplicationContext(), SpeechHelper.STANDARD_MSG);
		startActivity(intent);
		finish();
	}

	
	
}
