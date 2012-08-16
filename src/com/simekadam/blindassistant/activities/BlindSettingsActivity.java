package com.simekadam.blindassistant.activities;

import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.helpers.SpeechHelper;
import com.simekadam.blindassistant.helpers.SpeechHelper.OnSpeechEndedListener;
import com.simekadam.blindassistant.ui.BlindButton;
import com.simekadam.blindassistant.ui.BlindButton.OnDoubleClickListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class BlindSettingsActivity extends Activity {

	private SharedPreferences sp;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.blindassistantmain);
		if(!this.getIntent().hasExtra("reload")){
		SpeechHelper
				.getInstance()
				.say("Jste v nastavení aplikace",
						getApplicationContext(), SpeechHelper.STANDARD_MSG);}
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		sp = getSharedPreferences("com.simekadam.blindassistant",
				Context.MODE_PRIVATE);

		LinearLayout layout = (LinearLayout) findViewById(R.id.blindTableLayout);

		BlindButton btn1 = (BlindButton) findViewById(R.id.firstButton);
		btn1.setSpeechLabel("návrat zpět");
		btn1.setText("zpět");
		btn1.setActionSpeechLabel("zvolena akce zpět");
		btn1.setOnDoubleClickListener(new OnDoubleClickListener() {

			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
				finish();

			}
		});

		BlindButton btn2 = (BlindButton) findViewById(R.id.secondButton);
		//if(sp.getBoolean("data",true)){
		btn2.setSpeechLabel("vypnout odesílání dat přes datové přenosy. používat pouze wi-fi");
		
		btn2.setActionSpeechLabel("Vypínám odesílání dat přes mobilní připojeni, bude používáno pouze Wi-Fi");
		//}else{
			//btn2.setSpeechLabel("použít mobilní připojení pro odesílání dat v reálném čase? ");
			
			//btn2.setActionSpeechLabel("Zapínám odesílání dat přes mobilní připojeni, bude používáno pouze Wi-Fi");
		//}
		btn2.setText("Data/Wi-Fi");
		btn2.setOnDoubleClickListener(new OnDoubleClickListener() {

			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
//				if(sp.getBoolean("data",true)){
//					sp.edit().putBoolean("data", false).commit();
//					
//				}
//				else{
//					sp.edit().putBoolean("data", true).commit();
//				}
				SpeechHelper.getInstance().say(
						"nastavení pro odesílání dat bylo uloženo",
						getApplicationContext(), SpeechHelper.STANDARD_MSG);

			}
		});

		BlindButton btn3 = (BlindButton) findViewById(R.id.thirdButton);
		btn3.setSpeechLabel("Odhlásit uživatele od programu. Aplikace již nebude dále zaznamenávat vaši polohu");
		btn3.setActionSpeechLabel("Odhlašuji");
		btn3.setText("Odhlásit");
		btn3.setOnDoubleClickListener(new OnDoubleClickListener() {

			@Override
			public void onDoubleClick() {
				try {
					sp.edit().putString("userID", "")
							.putBoolean("logged", false).commit();
					SpeechHelper.getInstance().setOnSpeechEndedListener(
							new OnSpeechEndedListener() {

								@Override
								public void speechEnded() {
									// TODO Auto-generated method stub
									Intent closeIntent = new Intent(
											getApplicationContext(),
											ShutdownActivity.class)
											.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(closeIntent);
									finish();

								}
							});
					SpeechHelper.getInstance().say(
							"Odhlášení proběhlo úspěšně",
							getApplicationContext(),
							SpeechHelper.UI_RESPONSE_WITH_CALLBACK);

				} catch (Exception ex) {
					SpeechHelper.getInstance().say("Odhlášení se nezdařilo",
							getApplicationContext(), SpeechHelper.STANDARD_MSG);

				}

			}
		});

	}
	  public void reload() {

		    Intent intent = new Intent(getApplicationContext(), BlindSettingsActivity.class);
		    intent.putExtra("reload", "true");
		    overridePendingTransition(0, 0);
		    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    finish();

		    overridePendingTransition(0, 0);
		    startActivity(intent);
		}
	
}
