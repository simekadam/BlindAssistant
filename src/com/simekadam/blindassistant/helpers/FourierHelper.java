package com.simekadam.blindassistant.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.simekadam.blindassistant.DatabaseAdapter;
import com.simekadam.blindassistant.UpdaterService;
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
	public static final int WALKING = 1;
	public static final int STEADY = 0;
	public static final int CAR = 2;
	public static final int COUNT_CONTEXT = 100;

	private static ArrayList<Float> list;
	private static Float[] vectors;
	private static float[] input;
	private static float[] outputData;
	private static float freq;
	private static float max;

	private static ArrayList<ContextCountedListener> ccListeners = new ArrayList<ContextCountedListener>();

	public static void processFourierData(Message msg) {
		// TODO Auto-generated method stub
		// super.handleMessage(msg);
		Log.d(TAG, "handleCalled");
		if (msg.arg1 == COUNT_CONTEXT) {
			int context = getContext(msg.obj);
			Message contextComputedMessage = new Message();
			contextComputedMessage.arg1 = context;
			/* Sending the message */
			ArrayList<Float> output = new ArrayList<Float>();
			for (float f : outputData) {
				output.add(f);
			}
			for (ContextCountedListener ccl : ccListeners) {
				ccl.contextCounted(output, context);
			}
		}

	}

	private static int getContext(Object o) {
		FourierHelper.list = (ArrayList<Float>) o;
		countFourierTransform(list);
		double coefficient = freq * max;
		int context;
		if (coefficient < 600 && coefficient > 100) {
			context = WALKING;
		} else {
			context = CAR;
		}

		return context;
	}

	private static void countFourierTransform(ArrayList<Float> list) {
		vectors = new Float[list.size()];
		list.toArray(vectors);
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

			Object[] values = list.toArray();

			// vectors = new float[values.length];

			max = getMax(input);

			int length = vectors.length;

			if (max > 2) {
				FloatFFT_1D fftlib = new FloatFFT_1D(length);
				Log.d("delky", length + " " + input.length);
				fftlib.realForward(input);
				outputData = new float[(input.length + 1) / 2];

				// WTF zjistit co je to za sracku

				if (true) {
					for (int i = 0; i < length; i++) {

						outputData[i] = (float) Math.sqrt((Math.pow(
								input[2 * i], 2))
								+ (Math.pow(input[2 * (i) + 1], 2)));
					}
				} else {
					for (int i = 0; i < length / 2 + 1; i++) {

						outputData[i] = (float) Math.sqrt((Math.pow(
								input[2 * i], 2))
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

		}
	};

	public static void setOnNewsUpdateListener(ContextCountedListener listener) {
		// Store the listener object
		FourierHelper.ccListeners.add(listener);
	}

}
