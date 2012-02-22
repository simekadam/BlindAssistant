package com.simekadam.blindassistant;

import java.util.LinkedList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

public class DataDisplayActivity extends Activity {

	private static final String TAG = DataDisplayActivity.class.getSimpleName();
	SimpleXYSeries series1;
	SimpleXYSeries series2;
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
		initPlot();
		if(isMyServiceRunning()){
			//stopService(new Intent(getApplicationContext(), UpdaterService.class));
			startServiceBtn.setText("Stop updater service");
			startServiceBtn.setChecked(true);
		}else{
			//startService(new Intent(getApplicationContext(), UpdaterService.class));
			startServiceBtn.setText("Start updater service");
			startServiceBtn.setChecked(false);
		}
		startServiceBtn.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(isMyServiceRunning()){
					stopService(new Intent(getApplicationContext(), UpdaterService.class));
					startServiceBtn.setText("Start updater service");
					startServiceBtn.setActivated(false);
				}else{
					startService(new Intent(getApplicationContext(), UpdaterService.class));
					startServiceBtn.setText("Stop updater service");
					startServiceBtn.setActivated(true);
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
		super.onDestroy();
	}
	
	private void updateStateUI(Intent intent){
		TextView contextView = (TextView) findViewById(R.id.context);
		contextView.setText(""+intent.getStringExtra("context"));
	}
	
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	Log.d("buuuu",service.service.getClassName());
	        if ("com.simekadam.blindassistant.UpdaterService".equals(service.service.getClassName())) {
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
        	Log.d(TAG, intent.getAction());
        	if(intent.getAction().equals("com.simekadam.blindassistant.UPDATE_CONTEXT_UI")){
        		updateStateUI(intent);   
            	updatePlot(intent);
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
    
    public void updatePlot(Intent intent){
    	LinkedList<Number> list = new LinkedList<Number>();
    	LinkedList<Number> list2 = new LinkedList<Number>();
    	float[] f = intent.getFloatArrayExtra("vectors");
    	for (float g : f) {
			list.add((Number)g);
		}
    	float[] h = intent.getFloatArrayExtra("counted");
    	for (float g : h) {
			list2.add((Number)g);
		}
    	series1.setModel(list, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
    	series2.setModel(list2, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
    	mySimpleXYPlot.redraw();
    	mySimpleXYPlot2.redraw();


    }
    
   

}
