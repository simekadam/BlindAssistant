package com.simekadam.blindassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseAdapter {

	
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TIME = "time";
	public static final String KEY_VECTOR = "vector";
	public static final String KEY_X = "x";
	public static final String KEY_Y = "y";
	public static final String KEY_Z = "z";
	public static final String KEY_FOURIER_MAX = "fourierMax";
	public static final String KEY_FOURIER_FREQ = "fourierFreq";
	
	private static final String ACCELEROMETER_TABLE = "accelerometer";
	private static final String FOURIER_TABLE = "fourier";
	private Context context;
	private SQLiteDatabase database;
	private SqlLiteHelper dbHelper;

	public DatabaseAdapter(Context context) {
		this.context = context;
	}

	public DatabaseAdapter open() throws SQLException {
		dbHelper = new SqlLiteHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public long addAccelerometerValue(float vector, float x, float y, float z, int timestamp){
		ContentValues values = new ContentValues();
		values.put(KEY_TIME, timestamp);
		values.put(KEY_VECTOR, vector);
		values.put(KEY_X, x);
		values.put(KEY_Y, y);
		values.put(KEY_Z, z);
		return database.insert(ACCELEROMETER_TABLE, null, values);
	}

	public long addFourierData(float fourierMax, float fourierFreq, int time){
		ContentValues values = new ContentValues();
		values.put(KEY_FOURIER_MAX, fourierMax);
		values.put(KEY_FOURIER_FREQ, fourierFreq);
		values.put(KEY_TIME, time);
		return database.insert(FOURIER_TABLE, null, values);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private class SqlLiteHelper extends SQLiteOpenHelper{
		
		private static final String DATABASE_NAME = "sensordata";
		private static final int DATABASE_VERSION = 1;
		private static final String ACCELEROMETER_TABLE_CREATE = "create table accelerometer " +
			"(_id integer primary key autoincrement, "
			+ "time long, vector float, x float, y float, z float);";
		private static final String FOURIER_TABLE_CREATE =	"create table fourier (_id integer primary key autoincrement,fourierMax float, fourierFreq float, context text, time integer)";

		public SqlLiteHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(ACCELEROMETER_TABLE_CREATE);
			db.execSQL(FOURIER_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
