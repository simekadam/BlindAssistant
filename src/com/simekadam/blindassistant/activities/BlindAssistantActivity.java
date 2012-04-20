package com.simekadam.blindassistant.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.helpers.SpeechHelper;
import com.simekadam.blindassistant.ui.BlindButton;
import com.simekadam.blindassistant.ui.BlindButton.OnDoubleClickListener;

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
				Intent settingsIntent = new Intent(getApplicationContext(), BlindSettingsActivity.class);
				settingsIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				startActivity(settingsIntent);
				
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
        
        BlindButton btn4 = (BlindButton) findViewById(R.id.fourthButton);
        btn4.setSpeechLabel("vlízt do vývojové verze");
        btn4.setText("vlízt do sekce pro mě");
        btn4.setActionSpeechLabel("dál to neni pro slepouny");
        btn4.setOnDoubleClickListener(new OnDoubleClickListener() {
			
			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
				startActivity(new Intent(getApplicationContext(), DataDisplayActivity.class));
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