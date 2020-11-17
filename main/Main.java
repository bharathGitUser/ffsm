package main;

import java.util.Arrays;
import java.util.Vector;

import examples.EvenParity;
import examples.Mesi;
import examples.Mod3Divider;
import examples.Mod3OneCounter;
import examples.Mod3OneTwoCounter;
import examples.Mod3ZeroCounter;
import examples.Mod3ZeroTwoCounter;
import examples.NonCycA;
import examples.NonCycB;
import examples.PatternGeneratorComplex;
import examples.PatternGeneratorSimple;
import examples.ShiftRegister;
import examples.TCP;

public class Main {

	/**
	 * @param args
	 */
	static int subsetSize =3, eventReductionParameter=3	,faults=2;
	static boolean commonEvents = true; 
	private static Vector<DFA> DFACollection; 
	public static void main(String[] args) {
/*		DFACollection = new Vector<DFA>();
		//base line is non cyc A to mod 3 zero
		DFACollection.add(new NonCycA());
		DFACollection.add(new NonCycB());
		DFACollection.add(new EvenParity());
		DFACollection.add(new Mod3Divider());		
		DFACollection.add(new Mod3OneCounter());
		DFACollection.add(new Mod3ZeroCounter());
		DFACollection.add(new PatternGeneratorSimple());		
		DFACollection.add(new PatternGeneratorComplex());		
		DFACollection.add(new ShiftRegister());

		
		//test cases
		//event-based stuff...
		DFACollection.add(new Mod3OneTwoCounter());
		DFACollection.add(new Mod3ZeroTwoCounter());
		//DFACollection.add(new EventRedExample());
		DFACollection.add(new TCP());
		DFACollection.add(new Mesi());*/
		
		
	//using a subset routine because I am too dumb/bored to do it myself. 
		DFACollection = FileUtility.getBenchmarkFSMs();
	    int[] subset = new int[subsetSize];
		int[] set = new int[DFACollection.size()];
		for(int i =0; i < DFACollection.size();++i){
			set[i]=i;
		}
		Vector<int[]> subsets = new Vector<int[]>();
	    Util.processLargerSubsets(set, subset, 0, 0,subsets);//generates all subsets of size "subsetSize"
		for(int i=0; i < subsets.size(); ++i)
			genFFFusion(subsets.get(i));
	}
	
	static void genFFFusion(int[] subset) {
	    try {
			Vector<DFA> Input = new Vector<DFA>();
			int size=1;
			for(int i =0; i < subset.length; ++i){
				DFA machine =DFACollection.get(subset[i]);
				size = size* machine.nbStates;
				Input.add(machine);
			}
			if( (size > 300))
				return;
			
			for(int i =0; i < Input.size();++i)
				System.out.print(Input.get(i).name+" ");

			GraphFusionAlgo lf = new GraphFusionAlgo();
			//inc fusion
			long startTime = System.currentTimeMillis();
			Vector<CpPartition> incFusion = lf.incrementalFusion(Input,eventReductionParameter,faults,commonEvents);
			long endTime = System.currentTimeMillis();
			long incFusionTime = endTime - startTime;
			
			//normal fusion
			startTime = System.currentTimeMillis();
			Vector<CpPartition> normalFusion = lf.ffFusion(Input,eventReductionParameter,faults,commonEvents);
			endTime = System.currentTimeMillis();
			long normalFusionTime = endTime - startTime;

			//print metrics..
			int product = 1;
			for(int k =0; k < Input.size();++k){
				product = product*Input.get(k).nbStates;
			}
			int stateSpaceReplication = 1;
			for(int k =0; k < faults ; ++k){
				stateSpaceReplication = product*stateSpaceReplication;
			}
			System.out.println(lf.getTop().size()+" "+lf.getTop().actualNumberOfEvents()*faults+" "+stateSpaceReplication+" "+printMetrics(normalFusion,normalFusionTime)+" "+printMetrics(incFusion, incFusionTime));
		} catch (Exception e) {
			System.out.println("In Main: Too big");
		}
	}
	
	static String printMetrics(Vector<CpPartition> ffFusion,long timeTaken){
		int stateSpaceFusion = 1;
		int backupEvents=0;
		for(int k =0; k < ffFusion.size();++k){
			CpPartition fusion = ffFusion.get(k);
			backupEvents = backupEvents + fusion.actualNumberOfEvents();
			stateSpaceFusion = stateSpaceFusion*fusion.size();
		}
		return stateSpaceFusion+" "+backupEvents+" "+ timeTaken/100;
	}

}
