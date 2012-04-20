package com.simekadam.blindassistant.interfaces;

import java.util.ArrayList;

public interface ContextCountedListener {
	
		
	
		 void contextCounted(ArrayList<Float> outputData, int context, int intent);
		 
		 void contextCounted(ArrayList<Float> outputData,ArrayList<Float> inputData, int context, int intent);
		 
		 void contextCounted(ArrayList<Float> outputData,ArrayList<Float> inputData, int context, int intent, float freq, float max);

		 
}
