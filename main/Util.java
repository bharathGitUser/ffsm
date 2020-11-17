package main;

import java.util.Vector;

public class Util{
	//static int eventReductionParameter;
	static void processLargerSubsets(int[] set, int[] subset, int subsetSize, int nextIndex,Vector<int[]> subsets) {
	    if (subsetSize == subset.length) {
	    	int[] finalSubset = new int[subset.length];
	    	for(int i=0; i < subset.length;++i)
	    		finalSubset[i]=subset[i];
	    	subsets.add(finalSubset);
	        //process(subset);
	    } else {
	        for (int j = nextIndex; j < set.length; j++) {
	            subset[subsetSize] = set[j];
	            processLargerSubsets(set, subset, subsetSize + 1, j + 1,subsets);
	        }
	    }
	}
	static int eventsIntop =0;
}
