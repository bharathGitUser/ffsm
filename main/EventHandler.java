package main;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

import org.omg.CORBA.ORBPackage.InconsistentTypeCode;

public class EventHandler {
	private static TopHandler topManager;//Object which generates the cross product machine
	private static CpPartition top;
	//private static int totalEvents;
	private static int maxSizeCpMachine = 1;
	//Debug
	public static boolean verbose = false;
	

	public static Vector<CpPartition> eventDecompose(DFA inputMachine,int e) throws Exception{
		Util.eventsIntop  = inputMachine.nbLetters;
		Vector<DFA> DFACollection = new Vector<DFA>();
		DFACollection.add(inputMachine);
		topManager = new TopHandler(DFACollection);
		
		for(int i =0; i < DFACollection.size();++i){
//			System.out.print(DFACollection.get(i).name+"|");
			int size = DFACollection.get(i).nbStates;
			maxSizeCpMachine = size*maxSizeCpMachine;
		}
		top = topManager.getTop();
		if(top.size()>150){
			throw new Exception("Too Big");
		}
		Vector<CpPartition> eventDecomposition = new Vector<CpPartition>();		
		HashSet<CpPartition> inputMachines = new HashSet<CpPartition>();
		inputMachines.add(top);
		for(int i =0; i < e; ++i){
			HashSet<CpPartition> eventReducedMachines = new HashSet<CpPartition>();
			for (Iterator<CpPartition> iterator = inputMachines.iterator(); iterator
					.hasNext();) {
				CpPartition machine = (CpPartition) iterator.next();
				eventReducedMachines.addAll(reducedEvent(machine));
				//System.out.println("iteration 1:"+ eventReducedMachines);
			}
			if(eventReducedMachines.size()==0)
				return eventDecomposition;
			else{
				inputMachines = eventReducedMachines;
			}
		}
		
		//System.out.println(inputMachines);
		//System.out.println("--------------------------- Pick Valid Machines --------------");
		Vector<Integer> fvTop = topManager.faultVector(top);

		Vector<Integer> faultVector = new Vector<Integer>();
		for(int i=0; i < fvTop.size();++i)
			faultVector.add(0);
		int dmin =1;
		for (Iterator<CpPartition> iterator = inputMachines.iterator(); iterator
		.hasNext();) {
			CpPartition machine = (CpPartition) iterator.next();
			if(machine.size()<=1)
				continue;
			eventDecomposition.add(machine);
			//System.out.println("Machine:"+ machine);
			Vector<Integer> fvMachine = topManager.faultVector(machine);
		//	System.out.println("old fv:"+ faultVector+ " mach fv:"+fvMachine);
			for(int i=0; i < fvMachine.size();++i){
				if(fvMachine.get(i)>0){
	//				System.out.println("set");
					faultVector.set(i, 1);
				}
			}
			if(topManager.dmin(faultVector) >0)
				break; 
		}
//		System.out.println(faultVector);
		if(topManager.dmin(faultVector) ==0)
			return null;
		else
			return eventDecomposition;
	}

	public static HashSet<CpPartition> reducedEvent(CpPartition inputMachine){
		HashSet<CpPartition> validMachines = new HashSet<CpPartition>();
		for(int event=0; event < Util.eventsIntop; ++event){
			if(!inputMachine.isEvent(event))
                continue;
            CpPartition inconsistentMachine = new CpPartition();
			for(int blockNo =0; blockNo < inputMachine.size();++blockNo){
				CpBlock block = inputMachine.get(blockNo);
				CpBlock next = block.next[event];
				CpBlock combinedBlock;
				if(!block.equals(next))
					combinedBlock = new CpBlock(block.freshCopy(), next.freshCopy());
				else
					combinedBlock = new CpBlock(block.freshCopy());
				boolean added = false;
				//System.out.print("combined block:"+ combinedBlock+ " existing blocks:"+ inconsistentMachine);
				for(int i=0; i < inconsistentMachine.size();++i){
					CpBlock existingBlock = inconsistentMachine.get(i);
					if(existingBlock.sizeOfIntersection(combinedBlock) > 0){
						//System.out.print("existing Block:"+existingBlock+" combined Block:"+combinedBlock);
						existingBlock.mergeBlock(combinedBlock);
						//System.out.println(" merged Block:"+existingBlock);
						added = true;
						combinedBlock = existingBlock; 
					}
				}
				if(!added)
					inconsistentMachine.add(combinedBlock);
			}
			CpPartition lowerMachine = largestConsistentPartition(inconsistentMachine);
//			System.out.println(lowerMachine);
			validMachines.add(lowerMachine);
		}
		return validMachines; 
	}
	/*
	 * Given a machine and a set of edges, this function returns the minimal machine 
	 * seperating those edges by digging deeper and deeper in the lower covers of the machines
	 */

	
	public static CpPartition largestConsistentPartition(CpPartition lcpMachine){
		//System.out.println("inconsistent machine:"+lcpMachine);
		CpBlock[] mapping = new CpBlock[maxSizeCpMachine];//for efficient merge..
		for(int i=0; i < lcpMachine.size();++i){
			CpBlock block = lcpMachine.get(i);
			for (Iterator<CpState> iter = block.getCpCollection().iterator(); iter.hasNext();) {
				CpState element = (CpState)iter.next();
				mapping[element.getId()] = block;
			}
		}
		for(int m =0; m < lcpMachine.size();++m){
			CpBlock block = (CpBlock)lcpMachine.get(m);
			//we need to iterate over the cross product states of the block
			for(int event =0; event < Util.eventsIntop; ++ event){
				//The first element of the block determines the next state 
				CpState firstElement = block.getCpCollection().firstElement();
				
				CpBlock nextBlock = mapping[firstElement.next(event).getId()];

				/* The current block's next block for this event, is the block 
				 * corresponding to the next(firstElement,event)*/
				block.next[event] = nextBlock;
				
				/* Update the fact that 'block' points to 'nextBlock' on 
				 * application of 'event'*/
				nextBlock.pointers.add(new CpBlockPointer(block,event));
				
				/* For the remaining elements of the block, check the block 
				 * corresponding to otherelement.next[event].*/
				for (int cpStateNo = 1; cpStateNo < block.getCpCollection().size();++cpStateNo) {
					CpState otherElement = block.getCpCollection().get(cpStateNo);
					CpBlock nextBlockOtherElement = mapping[otherElement.next(event).getId()] ;
					/*
					 * If the nextBlock of the other element is not the same as this block,
					 * remove both of them and make a new combined block.
					 */
					if(nextBlockOtherElement!= nextBlock){
						CpBlock newCombinedBlock = new CpBlock(nextBlock, nextBlockOtherElement);
						for (Iterator it = newCombinedBlock.getCpCollection().iterator(); it.hasNext();) {
							CpState element = (CpState)it.next();
							mapping[element.getId()] = newCombinedBlock;
						}
						
						/*
						 * Update the next state function of the states whose next state were 
						 * pointing to both the old blocks to point to this new combined block.
						 */
						for(int g =0; g < nextBlock.pointers.size();++g){
							CpBlock blockPointer = nextBlock.pointers.get(g).getBlock();
							int blockEvent = nextBlock.pointers.get(g).getEvent();
							blockPointer.next[blockEvent] = newCombinedBlock;
							newCombinedBlock.pointers.add(new CpBlockPointer(blockPointer,blockEvent));
						}
						
						for(int g =0; g < nextBlockOtherElement.pointers.size();++g){
							CpBlock blockPointer = nextBlockOtherElement.pointers.get(g).getBlock();
							int blockEvent = nextBlockOtherElement.pointers.get(g).getEvent();
							blockPointer.next[blockEvent] = newCombinedBlock;
							newCombinedBlock.pointers.add(new CpBlockPointer(blockPointer,blockEvent));
						}
						
						/*
						 * Just to make sure we iterate correctly, since removing and adding 
						 * blocks changes the index.
						 */
						int temp = m;
						if(lcpMachine.indexOf(nextBlock) <= temp)
							--m;

						if(lcpMachine.indexOf(nextBlockOtherElement) <= temp)
							--m;

						lcpMachine.remove(nextBlock);
						lcpMachine.remove(nextBlockOtherElement);
						
						lcpMachine.add(newCombinedBlock);
						if((block == nextBlock)||(block == nextBlockOtherElement))
							block = newCombinedBlock;
						
						nextBlock = newCombinedBlock;
						nextBlockOtherElement = newCombinedBlock;
					}
				}
			}
			
		}
		return lcpMachine; 
	}
	public CpPartition getTop() {
		return top;
	}
}	
                                         