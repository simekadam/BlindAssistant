package com.simekadam.blindassistant.services;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.activities.BlindAssistantStartupActivity;
import com.simekadam.blindassistant.activities.DataDisplayActivity;
import com.simekadam.blindassistant.helpers.DatabaseAdapter;
import com.simekadam.blindassistant.helpers.LocationHelper;
import com.simekadam.blindassistant.helpers.MotionContextHelper;
import com.simekadam.blindassistant.helpers.MotionDetectHelper;
import com.simekadam.blindassistant.helpers.ServerClient;
import com.simekadam.blindassistant.interfaces.ContextAlertListener;
import com.simekadam.blindassistant.interfaces.ContextCountedListener;
import com.simekadam.blindassistant.interfaces.LocationHelperEventsListener;
import com.simekadam.blindassistant.interfaces.MotionDetectListener;

public class UpdaterService extends Service implements 
		LocationHelperEventsListener, ContextAlertListener, MotionDetectListener {

	private static final String TAG = UpdaterService.class.getSimpleName();
	public static final String BROADCAST_MOVING_STATE_UPDATE = "com.simekadam.blindassistant.updatestate";
	private int intervals[] = {5,10,15,20};
	private int currentInterval = 0;
	private DatabaseAdapter database;
	private SensorManager mSensorManager;
	private boolean motionDetected = false;
	private static final int requestCode = 0100;
	NotificationManager mNotificationManager;
	private Intent stateUpdateIntent;
	private Intent updateGPSValuesIntent;
	private float[] vectors;
	private float[] outputValues;
	private LocationManager locationManager;
	public static final String START_SERVICE = "start";
	private JSONArray mJSONArray;
	private JSONArray mJSONArray2;
	private JSONObject jsonObject;
	private JSONObject currentLocation;
	private Sensor mAccelerometer;
	private static int userContext;
	private static PendingIntent datadisplayintent;
	private ContextCountedListener contextCountedListener;
	private boolean loggingActive = false;
	private static int steadyCounter = 0;
	private static int motionCounter = 0;
	private String user;
	private Location lastKnownLocation;
	private Handler motionControlHandler;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d(TAG, "created");
		super.onCreate();

	}

	@Override
	public void onStart(Intent intent, int startId) {
		
		// TODO Auto-generated method stub
		SharedPreferences sp = getSharedPreferences(
				"com.simekadam.blindassistant", Context.MODE_PRIVATE);
		if(sp.getBoolean("logged", false)){
			this.user = sp.getString("userID", "");
			if(this.user == ""){
				redirectToLogin();
			}
		}else{
			redirectToLogin();
		}
		Log.d(TAG, "started");
		super.onStart(intent, startId);
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);
		
		startMotionDetection();
		
		openDatabase();
		//scheduleDetection();
		registerWifiConnectionCallback();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

		Log.d(TAG, "destroyed");
		this.stopSelf();
		database.close();
		MotionContextHelper.getMotionContextHelper(getApplicationContext()).stopContextResolve(this);
		stopLocationLogging();
		stopMotionDetection();
		unregisterWifiConnectionCallback();
		//motionControlHandler.removeMessages(MotionDetectHelper.SCHEDULE_DETECTION);
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "unbinded");
		unregisterWifiConnectionCallback();
		MotionContextHelper.getMotionContextHelper(getApplicationContext()).stopContextResolve(this);
		stopLocationLogging();
		stopMotionDetection();
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void updateGPSValues(Location location) {
		updateGPSValuesIntent.putExtra("lat", location.getLatitude());
		updateGPSValuesIntent.putExtra("long", location.getLongitude());
		updateGPSValuesIntent.putExtra("velocity", location.getSpeed());
		updateGPSValuesIntent.putExtra("acc", location.getAccuracy());
		updateGPSValuesIntent
				.setAction("com.simekadam.blindassistant.UPDATE_GPS_UI");
		sendBroadcast(updateGPSValuesIntent);
	}

	private void updateStateUI(int context) {

		stateUpdateIntent.putExtra("time", new Date().toLocaleString());
		stateUpdateIntent.putExtra("context", context);
		stateUpdateIntent.putExtra("vectors", vectors);
		stateUpdateIntent.putExtra("counted", outputValues);

		stateUpdateIntent
				.setAction("com.simekadam.blindassistant.UPDATE_CONTEXT_UI");
		sendBroadcast(stateUpdateIntent);
	}

	

	private void updateStateUI(String text) {

		stateUpdateIntent.putExtra("time", new Date().toLocaleString());
		stateUpdateIntent.putExtra("context", text);
		stateUpdateIntent.putExtra("vectors", vectors);
		stateUpdateIntent.putExtra("counted", outputValues);
		stateUpdateIntent
				.setAction("com.simekadam.blindassistant.UPDATE_CONTEXT_UI");
		sendBroadcast(stateUpdateIntent);
	}

	private void openDatabase() {
		if (database == null) {
			database = new DatabaseAdapter(getApplicationContext());
			database.open();
		}
	}

	private void closeDatabase() {
		if (database != null) {
			database.close();
		}
	}

	private void registerWifiConnectionCallback() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		registerReceiver(updaterServiceBroadcastReceiver, intentFilter);
	}

	private void unregisterWifiConnectionCallback() {
		unregisterReceiver(updaterServiceBroadcastReceiver);
		
	}

	public void sendDataToServer(RequestParams params) {
		Log.d(TAG, "trying to send data to server");
		ServerClient.post("http://www.jssport-giant.cz/android/index.php", params,
				new AsyncHttpResponseHandler() {
					@Override
					public void onFailure(Throwable arg0) {
						// TODO Auto-generated method stub
						Log.d("httpzkouska", arg0.toString());
						super.onFailure(arg0);
					}

					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						super.onFinish();
					}

					@Override
					public void onStart() {
						// TODO Auto-generated method stub
						super.onStart();
					}

					@Override
					public void onSuccess(String arg0) {
						// TODO Auto-generated method stub
						Log.d("httpzkouska", arg0);
						super.onSuccess(arg0);
					}
				});

		

	}

	class ResponseHandler extends AsyncHttpResponseHandler {

	};

	private void startGPS() {
		LocationHelper.getLocationHelper(getApplicationContext())
				.registerLocationUpdates(5000);
	}

	private void stopGPS() {
		LocationHelper.getLocationHelper(getApplicationContext())
				.unregisterLocationUpdates();
	}

	private void startLocationLogging() {
		if (!loggingActive) {
			startGPS();
			LocationHelper.getLocationHelper(getApplicationContext())
					.addLocationHelperEventListener(this);
			loggingActive = true;
			startMotionDetection();
		}
	}

	private void stopLocationLogging() {
		if (loggingActive) {
			LocationHelper.getLocationHelper(getApplicationContext())
					.removeLocationHelperEventListener(this);
			stopGPS();
			loggingActive = false;
			startMotionDetection();
		}
	}
	
	
	private void startMotionDetection(){
		MotionDetectHelper.getInstance(this.getApplicationContext()).startMotionDetection();
		MotionDetectHelper.getInstance(getApplicationContext()).addMotionDetectListener(this);
	}
	private void stopMotionDetection(){
		Log.d(TAG, "motion detection has been stoppped");
		MotionDetectHelper.getInstance(this.getApplicationContext()).stopMotionDetection();
		MotionDetectHelper.getInstance(getApplicationContext()).removeMotionDetectListener(this);

	}
	
	private void pauseMotionDetection(){
		MotionDetectHelper.getInstance(this.getApplicationContext()).stopMotionDetection();

	}
	
	

	private void notify(int context) {
//		datadisplayintent = PendingIntent.getActivity(getApplicationContext(),
//				0, new Intent(getApplicationContext(),
//						DataDisplayActivity.class),
//				PendingIntent.FLAG_UPDATE_CURRENT);
//
//		RemoteViews contentView = new RemoteViews(getPackageName(),
//				R.layout.contextnotificationlayout);
//		contentView.setImageViewResource(R.id.notificationimage,
//				R.drawable.icon_simple);
//		contentView.setTextViewText(R.id.title, "Context change");
//		contentView.setTextViewText(R.id.text, "You are now " + context);
//
//		Notification notification = new Notification(R.drawable.statusbar_icon,
//				"Context change", System.currentTimeMillis());
//		notification.contentView = contentView;
//		notification.contentIntent = datadisplayintent;
//		notification.flags |= Notification.FLAG_AUTO_CANCEL;
//		notification.defaults |= Notification.DEFAULT_SOUND;
//		notification.defaults |= Notification.DEFAULT_VIBRATE;
//		mNotificationManager.notify(42, notification);
	}
	
	private void notify(String title, String text){
		datadisplayintent = PendingIntent.getActivity(getApplicationContext(),
				0, new Intent(getApplicationContext(),
						DataDisplayActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.contextnotificationlayout);
		contentView.setImageViewResource(R.id.notificationimage,
				R.drawable.icon_simple);
		contentView.setTextViewText(R.id.title, title);
		contentView.setTextViewText(R.id.text, text);

		Notification notification = new Notification(R.drawable.statusbar_icon,
				"Context change", System.currentTimeMillis());
		notification.contentView = contentView;
		notification.contentIntent = datadisplayintent;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		mNotificationManager.notify(42, notification);
	}
	
	private void redirectToLogin(){
		Intent loginIntent = new Intent(getApplicationContext(),
				BlindAssistantStartupActivity.class);
		startActivity(loginIntent);
	}

	private BroadcastReceiver updaterServiceBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "received");
			if (intent.getAction().equals(
					WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
				// String ns = Context.NOTIFICATION_SERVICE;
				// NotificationManager mNotificationManager =
				// (NotificationManager) getSystemService(ns);
				//
				// PendingIntent datadisplayintent =
				// PendingIntent.getActivity(getApplicationContext(), 0, new
				// Intent(getApplicationContext(),DataDisplayActivity.class),
				// PendingIntent.FLAG_UPDATE_CURRENT);
				//
				// RemoteViews contentView = new RemoteViews(getPackageName(),
				// R.layout.contextnotificationlayout);
				// contentView.setImageViewResource(R.id.notificationimage,
				// R.drawable.icon_simple);
				// contentView.setTextViewText(R.id.title, "Context change");
				// contentView.setTextViewText(R.id.text, intent.getAction());
				//
				// Notification notification = new
				// Notification(R.drawable.statusbar_icon, "Context change",
				// System.currentTimeMillis());
				// notification.contentView = contentView;
				// notification.contentIntent = datadisplayintent;
				// notification.flags |= Notification.FLAG_AUTO_CANCEL;
				// notification.defaults |= Notification.DEFAULT_SOUND;
				// notification.defaults |= Notification.DEFAULT_VIBRATE;
				// mNotificationManager.notify(42, notification);
				// // if (intent.getBooleanExtra(, )) {
				// // //do stuff
				// // Log.d("wifi notifications","connected");
				// // } else {
				// // // wifi connection was lost
				// // }

				//sendDataToServer();
				Log.d("xxxx", "wifitoggle");
			}
		}
	};

	@Override
	public void onLocationChanged() {
		double latitude = LocationHelper.getLocationHelper(
				getApplicationContext()).getCurrentLatitude();
		double longitude = LocationHelper.getLocationHelper(
				getApplicationContext()).getCurrentLongitude();
		long time = LocationHelper.getLocationHelper(getApplicationContext())
				.getCurrentLocationTime();
		float velocity = LocationHelper.getLocationHelper(getApplicationContext()).getCurrentVelocity();
		Log.d(TAG, "speed: "+velocity);
		
		// ulozit do DB
		//notify("Ukladam polohu", "lat: "+latitude+", lon: "+longitude);
		database.addPositionData(latitude, longitude, time, this.user);
		if(false){
			RequestParams params = new RequestParams();
			Timestamp timestamp = new Timestamp(time);
			params.put("batch", "0");
			params.put("latitude", latitude+"");
			params.put("longitude", longitude+"");
			params.put("user", this.user);
			params.put("time", timestamp.toString());
			sendDataToServer(params);
			
		}
		Log.d(TAG, "ukladam do DB");
	}

	
	@Override
	public void contextAlert(int context) {
		MotionContextHelper.getMotionContextHelper(getApplicationContext()).stopContextResolve(this);
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        SharedPreferences sp = getSharedPreferences(
				"com.simekadam.blindassistant", Context.MODE_PRIVATE);
        if(lastKnownLocation == null){
        	lastKnownLocation = new Location(LocationManager.GPS_PROVIDER);
        	lastKnownLocation.setAltitude(-1);
        	lastKnownLocation.setLatitude(-1);
        	
        }
		database.addActionData(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), System.currentTimeMillis(), context, sp.getString("userID", "undefined"));
		switch (context) {
		case MotionContextHelper.WALKING:
			startLocationLogging();
			Log.d(TAG, "location monitoring has been started");
			break;
		default:
			stopLocationLogging();
			MotionContextHelper.getMotionContextHelper(getApplicationContext()).stopContextResolve(this);
			Log.d(TAG, "location monitoring has been stopped");
			startMotionDetection();
			//scheduleDetection();
			motionDetected = false;
			break;
		}

	}

	@Override
	public void MotionDetected() {
		// TODO Auto-generated method stub
		
		motionDetected = true;
		steadyCounter = 0;
		if(++motionCounter > 3 && !loggingActive){
			
			//motionControlHandler.removeMessages(MotionDetectHelper.SCHEDULE_DETECTION);
			motionCounter = 0;
			MotionContextHelper.getMotionContextHelper(getApplicationContext()).startContextResolve(this);
			pauseMotionDetection();
		}else{
		//startMotionDetection();
		}
	}
	
	public void SteadyDetected(){
		
		motionCounter = 0;
		if(++steadyCounter>3 && loggingActive){
			steadyCounter = 0;
			MotionContextHelper.getMotionContextHelper(getApplicationContext()).startContextResolve(this);
			pauseMotionDetection();
		}
		else{
			//startMotionDetection();

		}
	}
	
	private void scheduleDetection(){
		motionControlHandler = new Handler(){
			@Override
            public void handleMessage(Message msg)
            {
				if(msg.arg1 == MotionDetectHelper.SCHEDULE_DETECTION){
					if(motionDetected){
						Log.d(TAG, "motion was detected, not stopping now");
						currentInterval = 0;
						msg = Message.obtain();
						msg.arg1 = MotionDetectHelper.SCHEDULE_DETECTION;
						motionControlHandler.removeMessages(MotionDetectHelper.SCHEDULE_DETECTION);
						motionControlHandler.sendMessageDelayed(msg, 5000);
						//motionDetected = false;
					}else{
						currentInterval++;
						if(currentInterval<intervals.length){
							currentInterval = intervals.length-1;
						}
						scheduleStart(currentInterval);
						Log.d(TAG, "stoppped for "+currentInterval);
							stopService(new Intent(getApplicationContext(), UpdaterService.class));
							
					}
				}
            }
			
            
		};
		Message msg = Message.obtain();
		msg.arg1 = MotionDetectHelper.SCHEDULE_DETECTION;
		motionControlHandler.sendMessageDelayed(msg,  5000);
		
	}
	
	private void scheduleStart(int interval){
		// get a Calendar object with current time
				 Calendar cal = Calendar.getInstance();
				 // add 5 minutes to the calendar object
				 cal.add(Calendar.SECOND, 300);
				 Intent intent = new Intent(UpdaterService.START_SERVICE);
				 intent.setAction(UpdaterService.START_SERVICE);
				 PendingIntent sender = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				 
				 // Get the AlarmManager service
				 AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				 am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}
	
	

}
