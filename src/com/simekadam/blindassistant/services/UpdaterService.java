package com.simekadam.blindassistant.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.simekadam.blindassistant.activities.DataDisplayActivity;
import com.simekadam.blindassistant.helpers.DatabaseAdapter;
import com.simekadam.blindassistant.helpers.FourierHelper;
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

	private DatabaseAdapter database;
	private SensorManager mSensorManager;

	NotificationManager mNotificationManager;
	private Intent stateUpdateIntent;
	private Intent updateGPSValuesIntent;
	private float[] vectors;
	private float[] outputValues;
	private LocationManager locationManager;
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
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d(TAG, "created");
		super.onCreate();

	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG, "started");
		super.onStart(intent, startId);
		String ns = Context.NOTIFICATION_SERVICE;
		mNotificationManager = (NotificationManager) getSystemService(ns);

		startMotionDetection();
		
		openDatabase();

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

	public void sendDataToServer() {
		Log.d(TAG, "trying to send data to server");
		ServerClient.get("http://www.google.cz", null,
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

		NameValuePair np = new BasicNameValuePair("output",
				jsonObject.toString());
		NameValuePair np2 = new BasicNameValuePair("input",
				mJSONArray2.toString());
		NameValuePair np3 = new BasicNameValuePair("time",
				System.currentTimeMillis() + "");
		Log.d("location", currentLocation.toString());
		NameValuePair np4 = new BasicNameValuePair("location",
				currentLocation.toString());
		List<NameValuePair> nplist = new ArrayList<NameValuePair>();
		nplist.add(np);
		nplist.add(np2);
		nplist.add(np3);
		nplist.add(np4);

		RequestParams params = new RequestParams();

		params.put("output", jsonObject.toString());
		params.put("input", mJSONArray2.toString());
		params.put("time", System.currentTimeMillis() + "");
		params.put("location", currentLocation.toString());

		ServerClient.post("http://www.jssport-giant.cz/android/index.php",
				params, new AsyncHttpResponseHandler() {

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
		// HttpResponse responsePOST = client.execute(post);
		// HttpEntity resEntity = responsePOST.getEntity();
		// if (resEntity != null) {

		// }

	}

	class ResponseHandler extends AsyncHttpResponseHandler {

	};

	private void startGPS() {
		LocationHelper.getLocationHelper(getApplicationContext())
				.registerLocationUpdates(10000);
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
		datadisplayintent = PendingIntent.getActivity(getApplicationContext(),
				0, new Intent(getApplicationContext(),
						DataDisplayActivity.class),
				PendingIntent.FLAG_UPDATE_CURRENT);

		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.contextnotificationlayout);
		contentView.setImageViewResource(R.id.notificationimage,
				R.drawable.icon_simple);
		contentView.setTextViewText(R.id.title, "Context change");
		contentView.setTextViewText(R.id.text, "You are now " + context);

		Notification notification = new Notification(R.drawable.statusbar_icon,
				"Context change", System.currentTimeMillis());
		notification.contentView = contentView;
		notification.contentIntent = datadisplayintent;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		mNotificationManager.notify(42, notification);
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

	private BroadcastReceiver updaterServiceBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
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

				sendDataToServer();
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
		notify("Ukladam polohu", "lat: "+latitude+", lon: "+longitude);
		database.addPositionData(latitude, longitude, time);
		Log.d(TAG, "ukladam do DB");
	}

	
	@Override
	public void contextAlert(int context) {
		MotionContextHelper.getMotionContextHelper(getApplicationContext()).stopContextResolve(this);
		notify(context);
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
			break;
		}

	}

	@Override
	public void MotionDetected() {
		// TODO Auto-generated method stub
		steadyCounter = 0;
		if(++motionCounter > 3 && !loggingActive){
			motionCounter = 0;
			MotionContextHelper.getMotionContextHelper(getApplicationContext()).startContextResolve(this);
			pauseMotionDetection();
		}else{
		startMotionDetection();
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
			startMotionDetection();

		}
	}

}
