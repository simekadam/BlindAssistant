package com.simekadam.blindassistant.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.simekadam.blindassistant.helpers.DatabaseAdapter;
import com.simekadam.blindassistant.services.UpdaterService;
import com.simekadam.blindassistant.interfaces.ContextCountedListener;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class FourierHelper {

	private static final String TAG = FourierHelper.class.getSimpleName();

	

	private static float[] vectors;
	private static float[] input;
	private static float[] outputData;
	private static float freq;
	private static float max;
	private static boolean processing = false;
	private static ArrayList<ContextCountedListener> ccListeners = new ArrayList<ContextCountedListener>();

	public static void processFourierData(Message msg) {
		// TODO Auto-generated method stub
		// super.handleMessage(msg);
		processing = true;
		Log.d(TAG, "processing vector data");
		
			int context = getContext((float[]) msg.getData().getFloatArray(
					"vectors"));
		
			ArrayList<Float> output = new ArrayList<Float>();

			for (float f : FourierHelper.outputData) {
				output.add(f);
			}
			ArrayList<Float> input = new ArrayList<Float>();
			for (float f : FourierHelper.vectors) {
				input.add(f);
			}
			Iterator<ContextCountedListener> listenerIterator = ccListeners.iterator();
			while (listenerIterator.hasNext()) {
				ContextCountedListener ccl = listenerIterator.next();
				switch (msg.arg1) {
				case MotionContextHelper.COUNT_CONTEXT_FOREGROUND:
					ccl.contextCounted(output, input, context, msg.arg1);

					break;

				default:
					ccl.contextCounted(output, context, msg.arg1);
					break;
				}
				// ccl.conte
			}
		

	}

	private static int getContext(float[] vectors) {

		FourierHelper.vectors = vectors;
		// Log.d(TAG, list.get(1)+"");
		countFourierTransform();
		double coefficient = freq * max;
		int context;
		Log.d(TAG, "frequency: "+freq+" max: "+max);
		if (coefficient < 600 && coefficient > 100) {
			context = MotionContextHelper.WALKING;
		} else {
			context = MotionContextHelper.STEADY;
		}
		
		return context;
	}

	private static void countFourierTransform() {

		substractMean();
		stateCountTask.run();

	}

	private static void substractMean() {
		float mean = 0;
		for (float f : vectors) {
			mean += f;
		}
		mean /= vectors.length;
		input = new float[vectors.length * 2];

		for (int i = 0; i < vectors.length; i++) {
			input[i] = vectors[i] - mean;
		}
		
	}

	private static float getMax(float array[]) {
		float max = 0;
		for (float f : array) {
			if (f > max)
				max = f;
		}
		return max;
	}

	private static Runnable stateCountTask = new Runnable() {
		public void run() {

			// vectors = new float[values.length];
			max = getMax(input);

			int length = vectors.length;

			if (max > 2) {
				FloatFFT_1D fftlib = new FloatFFT_1D(length);
				fftlib.realForward(input);
				outputData = new float[(input.length + 1) / 2];

				// WTF zjistit co je to za sracku

				if (true) {
					for (int i = 0; i < length; i++) {

						outputData[i] = (float) Math.sqrt((Math
								.pow(input[2 * i], 2))
								+ (Math.pow(input[2 * (i) + 1], 2)));
					}
				} else {
					for (int i = 0; i < length / 2 + 1; i++) {

						outputData[i] = (float) Math.sqrt((Math
								.pow(input[2 * i], 2))
								+ (Math.pow(input[2 * i + 1], 2)));
					}
				}

				// konec WTF zjistit co je to za sracku

				int index = 0;

				for (int i = 0; i < outputData.length; i++) {
					if (outputData[i] > max) {
						if (outputData[i] < 500) {
							max = outputData[i];
							index = i;
						}
					}
				}

				freq = ((float) index) / length * 50;
			} else {
				outputData = input;
				freq = 0;
			}
			//FourierHelper.setOutputData(outputData);

		}
	};
	public static void setOutputData(float[] output){
		FourierHelper.outputData = output;
	}

	public static void setOnContextCountedListener(
			ContextCountedListener listener) {
		// Store the listener object
		if(!ccListeners.contains(listener)){
		FourierHelper.ccListeners.add(listener);
		}
	}
	
	public static void removeOnContextCountedListener(ContextCountedListener ccl){
		FourierHelper.ccListeners.remove(ccl);
	}

}
