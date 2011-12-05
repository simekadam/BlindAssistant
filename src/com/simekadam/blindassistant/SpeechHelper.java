package com.simekadam.blindassistant;


import java.util.HashMap;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;

public class SpeechHelper implements OnInitListener, OnUtteranceCompletedListener{

	private static TextToSpeech mTts;
	private String text; //data to speech
	private static SpeechHelper helper;
	private OnSpeechEndedListener onSpeechEndedListener;
	public static final String STANDARD_MSG = "standardMsg";
	public static final String UI_RESPONSE = "uiResponse";
	public static final String UI_RESPONSE_WITH_CALLBACK = "uiCallback";
	
	private SpeechHelper(){
		
	}
	
	
	public static SpeechHelper getInstance(){
		if(helper == null){
			helper = new SpeechHelper();
		}
		return helper;
	}
	
	
	
	
	
	public void say(String text, Context context, String utteranceID){
		
		if(mTts == null){
			this.text = text;
			mTts = new TextToSpeech(context, (OnInitListener) helper);
					
		}
		else{
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceID);
			mTts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
		}
	}
	
	
	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		if (status == TextToSpeech.SUCCESS) {
			mTts.setOnUtteranceCompletedListener(SpeechHelper.getInstance());
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UI_RESPONSE_WITH_CALLBACK);
			mTts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
		}
	}
	
	
	
	public void stopTTS(){
		if(mTts != null){
			mTts.shutdown();
			mTts.stop();
			mTts = null;
		}
	}
	
	

        @Override
        public void onUtteranceCompleted(String utteranceId) {
        	Log.v("utteranceId", utteranceId);
        	if(utteranceId.equals(UI_RESPONSE_WITH_CALLBACK)){
        		Log.v("speech", "SpeechEndedListener called");
        	
        	onSpeechEndedListener.speechEnded();
        	}
        }
    	
	

	public interface OnSpeechEndedListener{
		public abstract void speechEnded();
	}
	

	public OnSpeechEndedListener getOnSpeechEndedListener() {
		return onSpeechEndedListener;
	}


	public void setOnSpeechEndedListener(OnSpeechEndedListener onSpeechEndedListener) {
		this.onSpeechEndedListener = onSpeechEndedListener;
	}
	
}
