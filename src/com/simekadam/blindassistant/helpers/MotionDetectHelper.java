package com.simekadam.blindassistant.helpers;

import java.util.ArrayList;
import java.util.Iterator;

import com.simekadam.blindassistant.interfaces.ContextAlertListener;
import com.simekadam.blindassistant.interfaces.MotionDetectListener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MotionDetectHelper implements SensorEventListener{

	private static final String TAG = MotionDetectHelper.class.getSimpleName();
	private static MotionDetectHelper instance;
	
	
	private SensorManager sensorManager;
	private Sensor accelerometer;
	
	private ArrayList<MotionDetectListener> motionDetectListeners;
	
	private MotionDetectHelper(Context context){
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		motionDetectListeners = new ArrayList<MotionDetectListener>();
	}
	
	
	public static MotionDetectHelper getInstance(Context context){
		if(instance == null){
			instance = new MotionDetectHelper(context);
		}
		return instance;
	}
	public void startMotionDetection(){
		Log.d(TAG, "motion detection has been started");

		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void startMotionDetection(MotionDetectListener mdl){
		Log.d(TAG, "motion detection has been started");

		addMotionDetectListener(mdl);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		
	}
	public void stopMotionDetection(){
		sensorManager.unregisterListener(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
						
				float vector = (float) test;
				
				motionAssertion(vector);
	}
	
	private void motionAssertion(float vector){
		if(vector > 4){
			stopMotionDetection();
			Log.d(TAG, "motion detected "+vector);
			Iterator<MotionDetectListener> iterator = motionDetectListeners.iterator();
			while(iterator.hasNext()){
				iterator.next().MotionDetected();
			}
			
		}else if(vector < 0.4){
			stopMotionDetection();
			Log.d(TAG, "steady detected "+vector);
			Iterator<MotionDetectListener> iterator = motionDetectListeners.iterator();
			while(iterator.hasNext()){
				iterator.next().SteadyDetected();
			}
		}
	}
	
	
	
	public void addMotionDetectListener(MotionDetectListener mdl){
		if(!motionDetectListeners.contains(mdl)){
		this.motionDetectListeners.add(mdl);
		}
	}
	
	public void removeMotionDetectListener(MotionDetectListener mdl){
		this.motionDetectListeners.remove(mdl);
	}

	
	
}
