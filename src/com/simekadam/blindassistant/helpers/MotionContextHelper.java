package com.simekadam.blindassistant.helpers;

import java.util.ArrayList;
import java.util.Iterator;

import com.simekadam.blindassistant.interfaces.ContextAlertListener;
import com.simekadam.blindassistant.interfaces.ContextCountedListener;
import com.simekadam.blindassistant.services.UpdaterService;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MotionContextHelper implements SensorEventListener, ContextCountedListener{

	
	 public static final String TAG = MotionContextHelper.class.getSimpleName();
		public static final int WALKING = 1;
		public static final int STEADY  = 0;
		public static final int CAR     = 2;
		public static final int COUNT_CONTEXT = 100;
		public static final int COUNT_CONTEXT_FOREGROUND = 110;
		public static final int COUNT_CONTEXT_VERIFY = 120;
		public static final int COUNT_CONTEXT_WHILE_MEASURING = 130;
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private static MotionContextHelper instance;
    private Handler contextUpdatehandler;
    private static int lastVerifiedContext = 0;
    private static int contextRank = 1;
    private static int lastMeasuredContext = -1;
   
	
    private ArrayList<Float> list;
    private ArrayList<ContextAlertListener> contextAlertListeners;
	
	private MotionContextHelper(Context context){
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		contextAlertListeners = new ArrayList<ContextAlertListener>();
	}
	
	public static MotionContextHelper getMotionContextHelper(Context context){
		
		if(MotionContextHelper.instance == null){
			MotionContextHelper.instance = new MotionContextHelper(context);
		}
		return MotionContextHelper.instance;
		
		
	}
	
	public void registerAccelerometerUpdates(){
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	public void unregisterAccelerometerUpdates(){
		sensorManager.unregisterListener(this);
	}
	
	
	
	
	
	
	
	
	
	
	
	public void startContextResolve(ContextAlertListener ccl){
		list = new ArrayList<Float>();
		this.setOnContextAlertListener(ccl);
		FourierHelper.setOnContextCountedListener(this);
		registerAccelerometerUpdates();

		contextUpdatehandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
            	Bundle dataBundle = new Bundle();
        		float vectors[] = new float[list.size()];
        		int i = 0;
        		Iterator<Float> inputConverter = list.iterator();
        		
        		while(inputConverter.hasNext()){
        			vectors[i++] = inputConverter.next();
        		}
        		list.clear();

        		dataBundle.putFloatArray("vectors", vectors);
        		msg.setData(dataBundle);
                FourierHelper.processFourierData(msg);
                //stopContextResolve();

            }
        };
		Message msg = Message.obtain();
		msg.arg1 = COUNT_CONTEXT_FOREGROUND;
		
		contextUpdatehandler.sendMessageDelayed(msg, 4000);
		
		
	}
	
	public void stopContextResolve(ContextAlertListener cal){
		contextUpdatehandler.removeMessages(COUNT_CONTEXT);
		FourierHelper.removeOnContextCountedListener(this);

		unregisterAccelerometerUpdates();
		contextAlertListeners.remove(cal);

	}
	
	public void pauseContextResolve(){
		
	}
	
	public void setOnContextAlertListener(ContextAlertListener ccl){
		if(!contextAlertListeners.contains(ccl)){
		contextAlertListeners.add(ccl);
		}
	}
	
	
	
	
	
	
	
	
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		
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
				
		float vector = (float) test;
	        	//database.addAccelerometerValue(vector, event.values[0], event.values[1], event.values[2],(int) System.currentTimeMillis());

				//Log.d(TAG,event.values[0]+" "+event.values[1]+" "+event.values[2]);

		list.add(vector);
		
	}
	
	
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void verifyContext(int context){
		Log.d(TAG, "verifying context..current contextRank is: "+contextRank);
		if(contextRank == -10){
			contextRank = 0;
			lastMeasuredContext = context;
		}
		
		if(context == lastMeasuredContext){
			contextRank++;
		}
		else{
			contextRank--;
			lastMeasuredContext = context;
		}
		//lastMeasuredContext = context;
			if(contextRank > 2 || contextRank < -2){
					lastVerifiedContext = context;
				
					lastVerifiedContext = context;
					Log.d(TAG, "calling context alert listeners");
					for(ContextAlertListener ccl : contextAlertListeners){
						ccl.contextAlert(lastVerifiedContext);
					}
					
					
				
				contextRank = -10;
				//contextUpdatehandler.removeMessages(COUNT_CONTEXT_VERIFY);
			}
			else{
			Message msg = Message.obtain();
			msg.arg1 = COUNT_CONTEXT_VERIFY;
			msg.obj = list;
			contextUpdatehandler.sendMessageDelayed(msg, 5000);
			}
		
		
	}

	@Override
	public void contextCounted(ArrayList<Float> outputData, int context, int intent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "context has been counted: "+context);
		switch(intent){
		case COUNT_CONTEXT_FOREGROUND:

			verifyContext(context);

			break;
			
		case COUNT_CONTEXT_VERIFY:
			verifyContext(context);
			break;
		
		case COUNT_CONTEXT_WHILE_MEASURING:
			if(context != MotionContextHelper.WALKING){
				verifyContext(context);
			}
			break;
		}
	}

	@Override
	public void contextCounted(ArrayList<Float> outputData,
			ArrayList<Float> inputData, int context, int intent) {
		switch(intent){
		case COUNT_CONTEXT_FOREGROUND:

			verifyContext(context);

			break;
			
		case COUNT_CONTEXT_VERIFY:
			verifyContext(context);
			break;
		}
		
		
		Log.d(TAG, "context has been counted and now I should do something about it...woooo");

	}

	
}
