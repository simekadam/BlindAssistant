package com.simekadam.blindassistant.helpers;

import java.util.Iterator;
import java.util.LinkedList;

import com.simekadam.blindassistant.interfaces.LocationHelperEventsListener;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationHelper implements LocationListener{

	private static LocationHelper instance;
	private LocationManager locationManager;
	
	public final String LOCATION_NOT_AVAILABLE = "lna";
	private LinkedList<LocationHelperEventsListener> locationHelperEventListeners;
	
	private Location currentLocation;
	
	private LocationHelper(Context context){
		this.currentLocation = new Location("lna");
		locationHelperEventListeners = new LinkedList<LocationHelperEventsListener>();
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

	}
	
	
	public static LocationHelper getLocationHelper(Context context){
		if(instance == null){
			instance = new LocationHelper(context);
		}
		return instance;
	}
	
	
	public void registerLocationUpdates(int interval){
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0, this);

	}
	
	
	public void unregisterLocationUpdates(){
		locationManager.removeUpdates(this);
	}

	
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		currentLocation = location;
		
		Iterator<LocationHelperEventsListener> locationHelperIterator = locationHelperEventListeners.iterator();
		while(locationHelperIterator.hasNext()){
			locationHelperIterator.next().onLocationChanged();
		}
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
	
	
	public float getCurrentVelocity(){
		return this.currentLocation.getSpeed();
	}
	
	public double getCurrentLatitude(){
		return this.currentLocation.getLatitude();
	}
	
	public double getCurrentLongitude(){
		
		return this.currentLocation.getLongitude();
	}
	
	public long getCurrentLocationTime(){
		return this.currentLocation.getTime();
	}
	
	public String getLocationProvider(){
		return this.currentLocation.getProvider();
	}
	
	public float getLocationAccuracy(){
		return this.currentLocation.getAccuracy();
	}
	
	
	public void addLocationHelperEventListener(LocationHelperEventsListener listener){
		if(!locationHelperEventListeners.contains(listener)){
			this.locationHelperEventListeners.add(listener);
		}
	}
	
	public void removeLocationHelperEventListener(LocationHelperEventsListener listener){
		this.locationHelperEventListeners.remove(listener);
	}
	

}
