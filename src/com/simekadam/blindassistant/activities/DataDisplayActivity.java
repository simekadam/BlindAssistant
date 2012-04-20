package com.simekadam.blindassistant.activities;

import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.simekadam.blindassistant.R;
import com.simekadam.blindassistant.helpers.DatabaseAdapter;
import com.simekadam.blindassistant.helpers.FourierHelper;
import com.simekadam.blindassistant.helpers.MotionContextHelper;
import com.simekadam.blindassistant.interfaces.ContextCountedListener;
import com.simekadam.blindassistant.services.UpdaterService;

public class DataDisplayActivity extends Activity implements ContextCountedListener{

	
	private static final String TAG = DataDisplayActivity.class.getSimpleName();
	SimpleXYSeries series1;
	SimpleXYSeries series2;
	private static PendingIntent datadisplayintent;
	NotificationManager mNotificationManager;
	XYPlot mySimpleXYPlot;
	XYPlot mySimpleXYPlot2;
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Log.d(TAG, "backpressed");
		super.onBackPressed();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.d(TAG, "created");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.datadisplay);
		final ToggleButton startServiceBtn = (ToggleButton) findViewById(R.id.toggleUpdaterService);
		registerReceiver(broadcastReceiver, new IntentFilter("com.simekadam.blindassistant.UPDATE_CONTEXT_UI"));
		registerReceiver(broadcastReceiver, new IntentFilter("com.simekadam.blindassistant.UPDATE_GPS_UI"));
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		registerReceiver(broadcastReceiver, intentFilter);
		initPlot();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		FourierHelper.setOnContextCountedListener(this);
		TelephonyManager tMgr =(TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
  	  String mPhoneNumber = tMgr.getLine1Number();
  	  if(mPhoneNumber == null){
  		  Log.d(TAG, "pruser");
  	  }
  	  if(isMyServiceRunning()){
			//stopService(new Intent(getApplicationContext(), UpdaterService.class));
			startServiceBtn.setText(mPhoneNumber);
			startServiceBtn.setChecked(true);
		}else{
			//startService(new Intent(getApplicationContext(), UpdaterService.class));
			startServiceBtn.setText(mPhoneNumber);
			startServiceBtn.setChecked(false);
		}
		startServiceBtn.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				
		    	 
				// TODO Auto-generated method stub
				if(isMyServiceRunning()){
					stopService(new Intent(getApplicationContext(), UpdaterService.class));
					startServiceBtn.setText("Start updater service");
					//startServiceBtn.setActivated(false);
				}else{
					startService(new Intent(getApplicationContext(), UpdaterService.class));
					startServiceBtn.setText("Stop updater service");
					//startServiceBtn.setActivated(true);
				}
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		FourierHelper.removeOnContextCountedListener(this);

		super.onDestroy();
		
	}
	
	private void updateStateUI(Intent intent){
		TextView contextView = (TextView) findViewById(R.id.context);
		String contextString;
		int context = intent.getIntExtra("context", 0);
		switch (context) {
		case MotionContextHelper.CAR:
			contextString = getResources().getString(R.string.context_car);
			break;
		case MotionContextHelper.WALKING:
			contextString = getResources().getString(R.string.context_walking);
		default:
			contextString = getResources().getString(R.string.context_none);
			break;
		}
		contextView.setText(""+intent.getIntExtra("context", 0));
	}
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	Log.d("buuuu",service.service.getClassName());
	        if ("com.simekadam.blindassistant.services.UpdaterService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private void initPlot(){
		 mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot1);
		 mySimpleXYPlot2 = (XYPlot) findViewById(R.id.mySimpleXYPlot2);
		 
        // Create two arrays of y-values to plot:
        
        // Turn the above arrays into XYSeries:
        series1 = new SimpleXYSeries(new LinkedList<Number>()
                ,          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Vectors");                             // Set the display title of the series
 
        series2 = new SimpleXYSeries(new LinkedList<Number>()
                ,          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Vectors");   
 
        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(57, 146, 181),                   // line color
                null, null);              // fill color (optional)
        // Add series1 to the xyplot:
        series1Format.setVertexPaint(null);
        
        StepFormatter stepFormatter  = new StepFormatter(Color.rgb(0, 0,0), null);
        stepFormatter.getLinePaint().setStrokeWidth(1);
        mySimpleXYPlot.addSeries(series1, series1Format);
        mySimpleXYPlot.getGraphWidget().getBackgroundPaint().setAlpha(0);
        mySimpleXYPlot.getGraphWidget().getGridBackgroundPaint().setAlpha(0);
        mySimpleXYPlot.getBackgroundPaint().setAlpha(0);
        mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        mySimpleXYPlot.getLayoutManager().remove(mySimpleXYPlot.getLegendWidget());

        mySimpleXYPlot2.getGraphWidget().getBackgroundPaint().setAlpha(0);
        mySimpleXYPlot2.getGraphWidget().getGridBackgroundPaint().setAlpha(0);
        mySimpleXYPlot2.getBackgroundPaint().setAlpha(0);
        mySimpleXYPlot2.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        mySimpleXYPlot2.getLayoutManager().remove(mySimpleXYPlot2.getLegendWidget());

        mySimpleXYPlot2.addSeries(series2, series1Format);
        // Same as above, with series2:
        mySimpleXYPlot.setRangeUpperBoundary(10, BoundaryMode.FIXED);
        mySimpleXYPlot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
 
 
        // Reduce the number of range labels
        
        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL,1);
        // By default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        mySimpleXYPlot.disableAllMarkup();
        
        // By default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        mySimpleXYPlot2.disableAllMarkup();
	}
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	//Log.d("receivedbordel", intent.getAction());
        	if(intent.getAction().equals("com.simekadam.blindassistant.UPDATE_CONTEXT_UI")){
        		//try{
        		updateStateUI(intent);   
            	//updatePlot(intent);
        		//}
        		//catch(Exception ex){
        		//	Log.d(TAG, ex.toString());
        			
        		//}
        	}
        	else if(intent.getAction().equals("com.simekadam.blindassistant.UPDATE_GPS_UI")){
        		updateGPSUI(
        				intent.getFloatExtra("lat", 0),
        				intent.getFloatExtra("lon", 0),
        				intent.getFloatExtra("velocity", 0),
        				intent.getFloatExtra("acc", 0) );
        	}
        	
        	
        }

		
    };
    
    
    
    
    private void updateGPSUI(float lat, float lon, float velocity, float acc) {
		
	}
    
    public void updatePlot(float[] f, float[] h){
    	
    	LinkedList<Number> list = new LinkedList<Number>();
    	LinkedList<Number> list2 = new LinkedList<Number>();
    	for (float g : f) {
			list.add((Number)g);
		}
    	for (float g : h) {
			list2.add((Number)g);
		}
    	series1.setModel(list, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
    	series2.setModel(list2, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
    	mySimpleXYPlot.redraw();
    	mySimpleXYPlot2.redraw();


    }

	@Override
	public void contextCounted(ArrayList<Float> outputData, int context, int intent) {
		notify("Context's been counted ("+intent+")", context+"");
	}
	
	private void notify(String title, String text){
//		datadisplayintent = PendingIntent.getActivity(getApplicationContext(),
//				0, new Intent(getApplicationContext(),
//						DataDisplayActivity.class),
//				PendingIntent.FLAG_UPDATE_CURRENT);

//		RemoteViews contentView = new RemoteViews(getPackageName(),
//				R.layout.contextnotificationlayout);
//		contentView.setImageViewResource(R.id.notificationimage,
//				R.drawable.icon_simple);
//		contentView.setTextViewText(R.id.title, title);
//		contentView.setTextViewText(R.id.text, text);

//		Notification notification = new Notification(R.drawable.statusbar_icon,
//				"Context change", System.currentTimeMillis());
//		notification.contentView = contentView;
//		notification.contentIntent = datadisplayintent;
//		notification.flags |= Notification.FLAG_AUTO_CANCEL;
//		notification.defaults |= Notification.DEFAULT_SOUND;
//		notification.defaults |= Notification.DEFAULT_VIBRATE;
//		mNotificationManager.notify(42, notification);
	}

	@Override
	public void contextCounted(ArrayList<Float> outputData,
			ArrayList<Float> inputData, int context, int intent) {
		
		// TODO Auto-generated method stub
				float outputValues[] = new float[outputData.size()];
				for (int iter = 0; iter < outputData.size(); iter++) {
					outputValues[iter] = (float) outputData.get(iter);
				}
				float vectors[] = new float[inputData.size()];
				for (int iter = 0; iter < inputData.size(); iter++) {
					vectors[iter] = (float) inputData.get(iter);
				}
				updatePlot(vectors, outputValues);
		
	}

	@Override
	public void contextCounted(ArrayList<Float> outputData,
			ArrayList<Float> inputData, int context, int intent, float freq,
			float max) {
		contextCounted(outputData, inputData, context, intent);
		TextView freqLabel = (TextView) findViewById(R.id.frequency);
		freqLabel.setText(freq+"");
		TextView maxLabel = (TextView) findViewById(R.id.max);
		maxLabel.setText(max+"");
		TextView coeffiecient = (TextView) findViewById(R.id.coefficient);
		coeffiecient.setText((freq*max)+"");
	}
    
   

}
