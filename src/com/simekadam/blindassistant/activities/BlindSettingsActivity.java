package com.simekadam.blindassistant.activities;

import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.helpers.SpeechHelper;
import com.simekadam.blindassistant.ui.BlindButton;
import com.simekadam.blindassistant.ui.BlindButton.OnDoubleClickListener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class BlindSettingsActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.blindassistantmain);
        SpeechHelper.getInstance().say("Jste v nastavení aplikace vyberte položku, kterou chcete nastavit", getApplicationContext(), SpeechHelper.STANDARD_MSG);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LinearLayout layout = (LinearLayout) findViewById(R.id.blindTableLayout);
        
        
        BlindButton btn1 = (BlindButton) findViewById(R.id.firstButton);
        btn1.setSpeechLabel("návrat zpět");
        btn1.setText("zpět");
        btn1.setActionSpeechLabel("zvolena akce zpět");
        btn1.setOnDoubleClickListener(new OnDoubleClickListener() {
			
			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
				onBackPressed();

			}
		});
        
        
        BlindButton btn2 = (BlindButton) findViewById(R.id.secondButton);
        btn2.setSpeechLabel("použít mobilní připojení pro odesílání dat? Aktuálně nastaveno na volbu ano");
        btn2.setActionSpeechLabel("Vypínám odesílání dat přes mobilní připojeni, bude používáno pouze Wi-Fi");

        btn2.setText("Data/Wi-Fi");
        btn2.setOnDoubleClickListener(new OnDoubleClickListener() {
			
        	
			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
				
				SpeechHelper.getInstance().say("nastavení pro odesílání dat bylo uloženo", getApplicationContext(), SpeechHelper.STANDARD_MSG);
				
				
			}
		});
       
        BlindButton btn3 = (BlindButton) findViewById(R.id.thirdButton);
        btn3.setSpeechLabel("Odhlásit uživatele od programu");
        btn3.setActionSpeechLabel("Odhlašuji");
        btn3.setText("Odhlásit");
        btn3.setOnDoubleClickListener(new OnDoubleClickListener() {
			
			@Override
			public void onDoubleClick() {
				SpeechHelper.getInstance().say("Odhlášení proběhlo úspěšně", getApplicationContext(), SpeechHelper.STANDARD_MSG);
				
			}
		});
       
     }
}
