package main;

import java.util.Vector;

import examples.EventExample;

public class EventTest {
	public static void main(String[] args) {
		try {
			Vector<DFA> DFACollection = FileUtility.getBenchmarkFSMs();
			for(int i=0; i < DFACollection.size();++i){
				DFA machine = DFACollection.get(i);
				System.out.print(machine.name+":");
				Vector<CpPartition> eDecomp = EventHandler.eventDecompose(machine, 2);
				System.out.println(eDecomp);
				System.out.println("---------------------------------");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
