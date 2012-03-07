package com.simekadam.blindassistant;

import com.simekadam.blindassistant.ui.BlindButton;
import com.simekadam.blindassistant.ui.BlindButton.OnDoubleClickListener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class BlindSearchResultActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.blindassistantmain);
        SpeechHelper.getInstance().say("Byly nalezeny 3 osoby, které Vám mohou pomoci. Můžete nějakou vybrat a telefonicky se s ní spojit.", getApplicationContext(), SpeechHelper.STANDARD_MSG);
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
        btn2.setSpeechLabel("Jan Novák - pravděpodobnost pomoci je velmi vysoká");
        btn2.setActionSpeechLabel("Vytáčím uživatele Jan Novák");

        btn2.setText("Jan Novák");
        btn2.setOnDoubleClickListener(new OnDoubleClickListener() {
			
        	
			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
				
				startActivityForResult(new Intent(Intent.ACTION_CALL, Uri.parse("tel:728076881")), 99);
				
				
			}
		});
       
        BlindButton btn3 = (BlindButton) findViewById(R.id.thirdButton);
        btn3.setSpeechLabel("Petr Podhorský - pravděpodobnost pomoci je vysoká");
        btn3.setActionSpeechLabel("Vytáčím uživatele Petr Podhorský");
        btn3.setText("Petr Podhorský");
        btn3.setOnDoubleClickListener(new OnDoubleClickListener() {
			
			@Override
			public void onDoubleClick() {
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:728076881")));
				
			}
		});
        
        
        BlindButton btn4 = (BlindButton) findViewById(R.id.fourthButton);
        btn4.setSpeechLabel("Karel Vomáčka - pravděpodobnost pomoci je mizivá");
        btn4.setActionSpeechLabel("Vytáčím uživatele Karel Vomáčka");
        btn4.setText("Karel Vomáčka");
        btn4.setOnDoubleClickListener(new OnDoubleClickListener() {
			
			@Override
			public void onDoubleClick() {
				// TODO Auto-generated method stub
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:728076881")));
			}
		});
       
     }
}
