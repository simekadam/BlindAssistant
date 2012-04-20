package com.simekadam.blindassistant.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.helpers.DatabaseAdapter;
import com.simekadam.blindassistant.helpers.SpeechHelper;
import com.simekadam.blindassistant.ui.BlindButton;
import com.simekadam.blindassistant.ui.BlindButton.OnDoubleClickListener;

public class BlindSearchResultActivity extends Activity {
	
	private static final int HELP_ME = 1000;
	
	private DatabaseAdapter database;

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 if(processStartup()){
		 
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
       
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        SharedPreferences sp = getSharedPreferences(
				"com.simekadam.blindassistant", Context.MODE_PRIVATE);
        openDatabase();
        database.addActionData(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), System.currentTimeMillis(), HELP_ME, sp.getString("userID", "undefined"));
        SpeechHelper.getInstance().say("Byly nalezeny 3 osoby, které Vám mohou pomoci. Můžete nějakou vybrat a telefonicky se s ní spojit.", getApplicationContext(), SpeechHelper.STANDARD_MSG);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.blindassistantmain);
        
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
				 LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			        SharedPreferences sp = getSharedPreferences(
							"com.simekadam.blindassistant", Context.MODE_PRIVATE);
			        openDatabase();
			        database.addActionData(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), System.currentTimeMillis(), HELP_ME, sp.getString("userID", "undefined"));
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
				 LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			        SharedPreferences sp = getSharedPreferences(
							"com.simekadam.blindassistant", Context.MODE_PRIVATE);
			        openDatabase();
			        database.addActionData(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), System.currentTimeMillis(), HELP_ME, sp.getString("userID", "undefined"));
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
				 LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			        SharedPreferences sp = getSharedPreferences(
							"com.simekadam.blindassistant", Context.MODE_PRIVATE);
			        openDatabase();
			        database.addActionData(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), System.currentTimeMillis(), HELP_ME, sp.getString("userID", "undefined"));
				startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:728076881")));
			}
		});
		 }else{
			 finish();
		 }
     }
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		closeDatabase();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		closeDatabase();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		closeDatabase();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		processStartup();
		openDatabase();
		super.onResume();
	}

	private void openDatabase() {
		if (database == null) {
			database = new DatabaseAdapter(getApplicationContext());
			
		}
		database.open();
	}

	private void closeDatabase() {
		if (database != null) {
			database.close();
		}
	}
	
	private boolean checkAuthentication() {
		SharedPreferences sp = getSharedPreferences(
				"com.simekadam.blindassistant", Context.MODE_PRIVATE);
		return sp.getBoolean("logged", false);
	}
	
	private boolean processStartup(){
		if (!checkAuthentication()) {
			
	        
			Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(loginIntent);
			finish();
			return false;
			
		}
		else{
			return true;
		}
	}
}
