package com.simekadam.blindassistant;

import com.simekadam.blindassistant.SpeechHelper.OnSpeechEndedListener;
import com.simekadam.blindassistant.ui.BlindAdapter;
import com.simekadam.blindassistant.ui.BlindButton;
import com.simekadam.blindassistant.ui.BlindButton.OnDoubleClickListener;
import com.simekadam.blindassistant.ui.BlindTableLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;

public class BlindAssistantActivity extends Activity{
	
	
	
    


	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.blindassistantmain);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LinearLayout layout = (LinearLayout) findViewById(R.id.blindTableLayout);
        
        BlindButton btn1 = (BlindButton) findViewById(R.id.firstButton);
        btn1.setSpeechLabel("Tuto volbu zvolte, pokud máte problém a chcete vyhledat pomoc");
        btn1.setActionSpeechLabel("Vyhledávám pomoc - prosím čekejte");

        btn1.setText("problém");
        btn1.setOnDoubleClickListener(new OnDoubleClickListener() {
			
        	
			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
				
				startActivity(new Intent(getApplicationContext(), BlindSearchResultActivity.class));
				
				
			}
		});
       
        BlindButton btn2 = (BlindButton) findViewById(R.id.secondButton);
        btn2.setSpeechLabel("Nastavení aplikace");
        btn2.setActionSpeechLabel("Otevírám nastavení aplikace");
        btn2.setOnDoubleClickListener(new OnDoubleClickListener() {
			
			@Override
			public void onDoubleClick() {
				startActivity(new Intent(getApplicationContext(), BlindSettingsActivity.class));
				
			}
		});
        btn2.setText("nastavení");
        
        
        BlindButton btn3 = (BlindButton) findViewById(R.id.thirdButton);
        btn3.setSpeechLabel("souhrnné informace");
        btn3.setText("souhrnné informace");
        btn3.setActionSpeechLabel("Otevírám souhrnné informace");
        btn3.setOnDoubleClickListener(new OnDoubleClickListener() {
			
			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
				SpeechHelper.getInstance().say("Předčítám sougrnné informace.    Délka účasti v programu pro sledování polohy           15dní. Během vaší účasti jste celkem osmkrát využil možnost vyhledání cizí pomoci.", getApplicationContext(), SpeechHelper.STANDARD_MSG);
			}
		});
        
        
     }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		SpeechHelper.getInstance().say("Jste ve výchozí nabídce", getApplicationContext(), SpeechHelper.UI_RESPONSE);
		super.onResume();
	}

	

	
    
    
    
    
    
}