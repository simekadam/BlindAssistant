package com.simekadam.blindassistant.receivers;

import com.simekadam.blindassistant.helpers.MotionDetectHelper;
import com.simekadam.blindassistant.services.UpdaterService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver{

	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(UpdaterService.START_SERVICE)){
			Log.d("receiver", "service has been started");
			context.startService(new Intent(context, UpdaterService.class));
		}
	}

}