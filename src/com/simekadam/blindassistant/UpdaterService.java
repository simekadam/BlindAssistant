package com.simekadam.blindassistant;

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
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class UpdaterService extends Service implements SensorEventListener, LocationListener {
	
	private static final String TAG = UpdaterService.class.getSimpleName();
    public static final String BROADCAST_MOVING_STATE_UPDATE = "com.simekadam.blindassistant.updatestate";
    private static final int WALKING = 1;
    private static final int STEADY = 0;
    private static final int CAR = 2;
    private DatabaseAdapter database;
	private SensorManager mSensorManager;
    private ArrayList<Float> list;
    private final Handler stateUpdatehandler = new Handler();
    NotificationManager mNotificationManager;
    private Intent stateUpdateIntent;
    private Intent updateGPSValuesIntent;
    private float[] vectors;
    private LocationManager locationManager;
	private JSONArray mJSONArray;
	private JSONArray mJSONArray2;
	private JSONObject jsonObject;
	private JSONObject currentLocation;
    private Sensor mAccelerometer;
    private static int userContext;
    private static PendingIntent datadisplayintent; 
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
		stateUpdatehandler.postDelayed(stateCountTask, 5000);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
		Log.d(TAG, "destroyed");
		this.stopSelf();
		stateUpdatehandler.removeCallbacks(stateCountTask);
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
		updateGPSValuesIntent.setAction("dataDisplayActivity.UPDATE_GPS_UI");
		sendBroadcast(updateGPSValuesIntent);
	}
	
	
	private void updateStateUI(float variance) {
 
    	stateUpdateIntent.putExtra("time", new Date().toLocaleString());
    	stateUpdateIntent.putExtra("context", variance);
    	stateUpdateIntent.putExtra("vectors", vectors);
    	stateUpdateIntent.setAction("UPDATE_CONTEXT_UI");
    	sendBroadcast(stateUpdateIntent);
    }
	
	
	
	
	
	private void updateStateUI(String text) {
		 
    	stateUpdateIntent.putExtra("time", new Date().toLocaleString());
    	stateUpdateIntent.putExtra("context", text);
    	stateUpdateIntent.putExtra("vectors", vectors);
    	stateUpdateIntent.setAction("UPDATE_CONTEXT_UI");
    	sendBroadcast(stateUpdateIntent);
    }
	
	private Runnable stateCountTask = new Runnable() {
		public void run() {
			float freq;
			String text;
			float outputData[];
//			float mean = 0;
			 Object[] values =  list.toArray();
			 vectors = new float[values.length];
			 float mean = 0;
			 for(Object f: values){
				 mean  += (Float) f;
			 }
			 mean/=vectors.length;
			 int j = 0;
			 
			 for (Object o : values) {
				 vectors[j++] = Float.parseFloat(""+o);
			}
//		    
//
//			 list.clear();
//			int size = values.length;
//			Log.d(TAG, "size: "+list.size());
//			for(int i = 0; i<size;i++){
//				mean += (Float) values[i];
//			}
//			mean /= size;
//			Log.d(TAG, "mean: "+mean);
//			float squaredValues = 0;
//			for(int i = 0; i<size;i++){
//				final float x = (Float) values[i] - mean;
//				squaredValues += x * x;
//			}
//			
//			float result = squaredValues/size;
//			Log.d(TAG, "xxx: "+mean+" "+result+" "+squaredValues);
//			result = (float) (Math.sqrt(result)/mean*100);
//			Log.d(TAG, "result: "+result);
//			Object[] values =  list.toArray();
//			list.clear();
//			 vectors = new float[values.length];
//			 int j = 0;
//			 
//			 for (Object o : values) {
//				 vectors[j++] = Float.parseFloat(""+o);
//			}
//			float[] counted = new float[vectors.length];
//    		// TODO Auto-generated method stub
//        	for(int i=0;i<vectors.length;i++){
//        		counted[i] = 0;
//        		for(int k = 0; k < vectors.length; k++){
//        			counted[i] += vectors[k]*vectors[((i+k)%vectors.length)];
//        		}
//        	}
//        	
//        	Arrays.sort(counted);
			int length = vectors.length;
			float[] input = new float[length*2];
			Log.d(TAG, length+" delka");
			float max = 0;
			for(int i=0;i<length;i++){
				input[i]=vectors[i]-mean;
				if(input[i]>max) max = input[i];
			}
			if(max>2){
			FloatFFT_1D fftlib = new FloatFFT_1D(length);
			Log.d("delky", length+" "+input.length);
			fftlib.realForward(input);
			outputData = new float[(input.length+1)/2];
			if(true){
				for(int i = 0; i < length; i++){
					
					outputData[i]= (float) Math.sqrt((Math.pow(input[2*i],2))+(Math.pow(input[2*(i)+1], 2)));
				}
			}else{
				for(int i = 0; i < length/2+1; i++){
					
					outputData[i]= (float) Math.sqrt((Math.pow(input[2*i],2))+(Math.pow(input[2*i+1], 2)));
				}
			}

			
			
        	float result = vectors[0];
        	int index = 0;
        	int lowerBound = 0;
        	int upperBound = 0;
        	for(int i = 0; i < outputData.length;i++){
        		if(outputData[i]>max){
        			float tmpFreq = ((float)i)/length*50;
        			if(outputData[i]<500){
        			max = outputData[i];
        			index = i;
        			}
        		}
        	}
        	
        		freq = ((float)index)	/length*50;
			}
			else{
				outputData = input;
				freq = 0;
			}
        	int tmpContext = -1;
        	double coefficient = freq*max;
        	if(coefficient < 600 && coefficient > 100){
        		text = "jdu "+ freq+"; "+max;
        		 tmpContext = WALKING;
        	}else{
        		text = "nejdu "+freq+"; "+max;
        		tmpContext = CAR;
        	}
        	List<Float> output = new ArrayList<Float>();
			for (float f : outputData) {
				output.add(f);
			}
        	mJSONArray = new JSONArray(output);
        	jsonObject = new JSONObject();
        	try {
				jsonObject.accumulate("outputData", mJSONArray);
				jsonObject.accumulate("context", tmpContext);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.d(TAG, e.toString());
			}
			mJSONArray2 = new JSONArray(list);
        	grabURL("http://10.0.0.1/blindassistant/index.php");
        	database.addFourierData(max, freq, (int)System.currentTimeMillis());
        	if (tmpContext != userContext) {
        		userContext = tmpContext;
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
        	stateUpdateIntent.putExtra("counted", outputData);
        	list.clear();
			updateStateUI(text);
			stateUpdatehandler.postDelayed(this, 5000);
		}
		
//		public void postData(List list) {	
//		    // Create a new HttpClient and Post Header
//		    HttpClient httpclient = new DefaultHttpClient();
//		    HttpPost httppost = new HttpPost("http://10.0.0.3/blindassistant/index.php");
//
//		    try {
//		        // Add your data
////		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
////		        nameValuePairs.add(new BasicNameValuePair("id", "12345"));
////		        nameValuePairs.add(new BasicNameValuePair("stringdata", "AndDev is Cool!"));
//		    	JSONArray mJSONArray = new JSONArray(list);
//		    	StringEntity se = new StringEntity(mJSONArray.toString());
//		    	Log.d(TAG,  mJSONArray.toString());
//		        httppost.setEntity(se);
//
//		        // Execute HTTP Post Request
//		        HttpResponse response = httpclient.execute(httppost);
//		        Log.d(TAG, response.getAllHeaders()+"");
//		    } catch (ClientProtocolException e) {
//		        Log.d(TAG, e.toString());
//		    } catch (IOException e) {
//		    	Log.d(TAG, e.toString());
//		        // TODO Auto-generated catch block
//		    }
//		} 
//		¬
		
	};
	
	
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
	
	
	
	public void grabURL(String url) {
	    new GrabURL().execute(url);
	}
	
	
	
	
	private class GrabURL extends AsyncTask<String, String, String> {
	       private final HttpClient client = new DefaultHttpClient();
	       private String content;
	       private boolean error = false;
	       //private ProgressDialog dialog = new ProgressDialog(getApplication().getApplicationContext());

	       protected void onPreExecute() {
//	        dialog.setMessage("Getting your data... Please wait...");
//	        dialog.show();
	    	   
	       }

	   protected String doInBackground(String... url) {
	      
		   try {
		        //HttpClient client = new DefaultHttpClient();  
		        String postURL = "http://www.jssport-giant.cz/android/index.php";
		        HttpPost post = new HttpPost(postURL); 
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
		            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(nplist,HTTP.UTF_8);
		            post.setEntity(ent);
		            HttpResponse responsePOST = client.execute(post);  
		            HttpEntity resEntity = responsePOST.getEntity();  
		            if (resEntity != null) {    
//		            	final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//		     		   emailIntent.setType("text/html");
//		     		   emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"simekadam@gmail.com"});
//
//		     		   emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "blindassistant-debug");
//		     		   emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(EntityUtils.toString(resEntity)));
//		     		   emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		     		   startActivity(emailIntent);
		            }
		    } catch (Exception e) {
		        e.printStackTrace();
		    };
	     
	      return "";
	   }
	   
	   
	   
	   
	   protected void onPostExecute(String content) {
	     // dialog.dismiss();
		   
	     Log.d("returned", content);
	      
	    }

	
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

}
