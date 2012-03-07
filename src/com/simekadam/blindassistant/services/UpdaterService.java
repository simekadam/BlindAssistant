package com.simekadam.blindassistant.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.simekadam.blindassistant.activities.DataDisplayActivity;
import com.simekadam.blindassistant.helpers.DatabaseAdapter;
import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.helpers.FourierHelper;
import com.simekadam.blindassistant.interfaces.ContextCountedListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class UpdaterService extends Service implements SensorEventListener, LocationListener, ContextCountedListener  {
	
	private static final String TAG = UpdaterService.class.getSimpleName();
    public static final String BROADCAST_MOVING_STATE_UPDATE = "com.simekadam.blindassistant.updatestate";
    private static final int WALKING = 1;
    private static final int STEADY = 0;
    private static final int CAR = 2;
    private DatabaseAdapter database;
	private SensorManager mSensorManager;
    private ArrayList<Float> list;
    
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
    private Handler stateUpdatehandler;
    
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
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		FourierHelper.setOnNewsUpdateListener(this);
		//dummy object because of compatibility with server
		currentLocation = new JSONObject();
		try{
		currentLocation.accumulate("long", "");
		currentLocation.accumulate("lat", "");
		currentLocation.accumulate("alt", "");
		currentLocation.accumulate("acc", "");
		currentLocation.accumulate("time", "");
		}catch(Exception e){
			Log.d(TAG, e.toString());
		}
		
		
		
		mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		stateUpdateIntent = new Intent();
		updateGPSValuesIntent = new Intent();
		openDatabase();
		list = new ArrayList<Float>();
		stateUpdatehandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                FourierHelper.processFourierData(msg);
            }
        };
		Message msg = Message.obtain();
		msg.arg1 = FourierHelper.COUNT_CONTEXT;
		msg.obj = list;
		stateUpdatehandler.sendMessageDelayed(msg, 5000);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
		Log.d(TAG, "destroyed");
		this.stopSelf();
		stateUpdatehandler.removeMessages(FourierHelper.COUNT_CONTEXT);
		database.close();
		mSensorManager.unregisterListener(this);
		super.onDestroy();
	}

	

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "unbinded");
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	
	
	
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
		
		//ramp-speed - play with this value until satisfied

		//last result storage - keep definition outside of this function, eg. in wrapping object
		float result[] = new float[3];
		float accel[] = new float[3]; 

		//acceleration.x,.y,.z is the input from the sensor

		//result.x,.y,.z is the filtered result

		final float kFilteringFactor = 0.1f;

		//last result storage - keep definition outside of this function, eg. in wrapping object

		//acceleration.x,.y,.z is the input from the sensor

		//result.x,.y,.z is the filtered result

		accel[0] = event.values[0] * kFilteringFactor + accel[0] * (1.0f - kFilteringFactor);
		accel[1] = event.values[1] * kFilteringFactor + accel[1] * (1.0f - kFilteringFactor);
		accel[2] = event.values[2] * kFilteringFactor + accel[2] * (1.0f - kFilteringFactor);
		result[0] = event.values[0];
		result[1] = event.values[1];
		result[2] = event.values[2];
		
				double test =  Math.sqrt(result[0]*result[0]+result[1]*result[1]+result[2]*result[2]);
				
				float vector =(float) test;
	        	//database.addAccelerometerValue(vector, event.values[0], event.values[1], event.values[2],(int) System.currentTimeMillis());

				//Log.d(TAG,event.values[0]+" "+event.values[1]+" "+event.values[2]);
		list.add(vector);
		
	}
	
	private void updateGPSValues(Location location){
		updateGPSValuesIntent.putExtra("lat", location.getLatitude());
		updateGPSValuesIntent.putExtra("long", location.getLongitude());
		updateGPSValuesIntent.putExtra("velocity", location.getSpeed());
		updateGPSValuesIntent.putExtra("acc", location.getAccuracy());
		updateGPSValuesIntent.setAction("com.simekadam.blindassistant.UPDATE_GPS_UI");
		sendBroadcast(updateGPSValuesIntent);
	}
	
	
	private void updateStateUI(int context) {
 
    	stateUpdateIntent.putExtra("time", new Date().toLocaleString());
    	stateUpdateIntent.putExtra("context", context);
    	stateUpdateIntent.putExtra("vectors", vectors);
    	stateUpdateIntent.setAction("com.simekadam.blindassistant.UPDATE_CONTEXT_UI");
    	sendBroadcast(stateUpdateIntent);
    }
	
	
	public final void contextComputedHandler(ArrayList<Float> output, int context){
            
			
		
		mJSONArray = new JSONArray(output);
    	jsonObject = new JSONObject();
    	try {
			jsonObject.accumulate("outputData", mJSONArray);
			jsonObject.accumulate("context", context);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.toString());
		}
		mJSONArray2 = new JSONArray(list);
    	//grabURL("http://10.0.0.1/blindassistant/index.php");
    	
		//database.addFourierData(max, freq, (int)System.currentTimeMillis());
    	
		
		
    	
    	if (context != userContext) {
    		userContext = context;
    		datadisplayintent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(),DataDisplayActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    		
    		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.contextnotificationlayout);
    		contentView.setImageViewResource(R.id.notificationimage, R.drawable.icon_simple);
    		contentView.setTextViewText(R.id.title, "Context change");
    		contentView.setTextViewText(R.id.text, "You are now "+userContext);
    		
			Notification notification = new Notification(R.drawable.statusbar_icon, "Context change", System.currentTimeMillis());
			notification.contentView = contentView;
			notification.contentIntent = datadisplayintent;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			mNotificationManager.notify(42, notification);


		}
    	if(userContext == WALKING){
    		startGPS();

    	}
    	//stateUpdateIntent.putExtra("counted", outputData);
		//updateStateUI(text);
		
		
		outputValues = new float[output.size()];
		for(int iter = 0; iter < output.size(); iter++){
			outputValues[iter] = (float) output.get(iter);
		}
		vectors = new float[list.size()];
		for(int iter = 0; iter < list.size(); iter++){
			vectors[iter] = (float) list.get(iter);
		}
		
		
		
        updateStateUI(userContext);
        Message msg = Message.obtain();
		msg.arg1 = FourierHelper.COUNT_CONTEXT;
		msg.obj = list;
		stateUpdatehandler.sendMessageDelayed(msg, 5000);
    	list.clear();

    }
	
	
	private void updateStateUI(String text) {
		 
    	stateUpdateIntent.putExtra("time", new Date().toLocaleString());
    	stateUpdateIntent.putExtra("context", text);
    	stateUpdateIntent.putExtra("vectors", vectors);
    	stateUpdateIntent.putExtra("counted", outputValues);
    	stateUpdateIntent.setAction("com.simekadam.blindassistant.UPDATE_CONTEXT_UI");
    	sendBroadcast(stateUpdateIntent);
    }
	
	
		

	
	
	private void openDatabase(){
		if(database==null){
			database = new DatabaseAdapter(getApplicationContext());
			database.open();
		}
	}
	private void closeDatabase(){
		if(database!=null){
			database.close();
		}
	}
	
	
	
	public void sendDataToServer(){
		NameValuePair np = new BasicNameValuePair("output",jsonObject.toString());
    	NameValuePair np2 = new BasicNameValuePair("input", mJSONArray2.toString());
    	NameValuePair np3 = new BasicNameValuePair("time", System.currentTimeMillis()+"");
    	Log.d("location", currentLocation.toString());
    	NameValuePair np4 = new BasicNameValuePair("location", currentLocation.toString());
    	List<NameValuePair> nplist = new ArrayList<NameValuePair>();
    	nplist.add(np);
    	nplist.add(np2);
    	nplist.add(np3);
    	nplist.add(np4);
        //HttpResponse responsePOST = client.execute(post);  
        //HttpEntity resEntity = responsePOST.getEntity();  
        //if (resEntity != null) {    

        //}
		
	}
	
	
	
	
	
		        	
		   
	     


	public void startGPS(){
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 0, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		currentLocation = new JSONObject();
		try{
		currentLocation.accumulate("long", location.getLongitude());
		currentLocation.accumulate("lat", location.getLatitude());
		currentLocation.accumulate("alt", location.getAltitude());
		currentLocation.accumulate("acc", location.getAccuracy());
		currentLocation.accumulate("time", location.getTime());
		Log.d(TAG, currentLocation.toString());
		}catch(Exception e){
			Log.d(TAG, e.toString());
		}
		updateGPSValues(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	
	

	@Override
	public void contextCounted(ArrayList<Float> outputData, int context) {
		// TODO Auto-generated method stub
		contextComputedHandler(outputData, context);
	}
	
	
	
	
		

}
