package main;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Vector;

import org.omg.CORBA.ORBPackage.InconsistentTypeCode;

public class GraphFusionAlgo {
	private TopHandler topManager;//Object which generates the cross product machine
	private CpPartition top;
	private int maxSizeCpMachine = 1;
	//Debug
	public static boolean verbose = false;
	
	public Vector<CpPartition> incrementalFusion(Vector<DFA> DFACollection,int eventReductionParameter,int f,boolean commonEvents) throws Exception{
		Vector<DFA> inputs = new Vector<DFA>();
		for(int i=0; i < 2; ++i){
			inputs.add(DFACollection.get(i));
		}
		Vector<CpPartition> backups = ffFusion(inputs, eventReductionParameter, f,commonEvents);
		for(int j=2; j < DFACollection.size();++j){
			Vector<DFA> nextInputSet = new Vector<DFA>();
			nextInputSet.add(DFACollection.get(j));
			DFA backupCrossProduct = backups.get(0).getEquivalentDFA();
			for(int m=1; m <backups.size();++m){
				DFA backupMachine = backups.get(m).getEquivalentDFA();
				backupCrossProduct = backupCrossProduct.crossproduct(backupMachine);
			}
			nextInputSet.add(backupCrossProduct);
			backups = ffFusion(nextInputSet, eventReductionParameter, f,commonEvents);
		}	
//		testffFusion(DFACollection, backups, f);
	    return backups;
	}

	public Vector<CpPartition> ffFusion(Vector<DFA> DFACollection,int eventReductionParameter,int f,boolean commonEvents) throws Exception{
/*		faults = f;
		this.DFACollection=DFACollection;  
		this.eventReductionParameter = eventReductionParameter;
*/		//make sure the machines have the correct event offset..
		if(commonEvents){
			Util.eventsIntop = 0;
			for(int i=0; i < DFACollection.size();++i){
				DFA machine =DFACollection.get(i); 
				if(Util.eventsIntop < machine.getEventSize())
						Util.eventsIntop = machine.getEventSize();
			}
		}else{
			int eventsUsed =0; 
			for(int i=0; i < DFACollection.size();++i){
				DFA machine =DFACollection.get(i); 
			    machine.setoffset(eventsUsed);
			    eventsUsed = eventsUsed + machine.getEventSize(); 
			}
			Util.eventsIntop = eventsUsed;
		}
		topManager = new TopHandler(DFACollection);
		
		for(int i =0; i < DFACollection.size();++i){
//			System.out.print(DFACollection.get(i).name+"|");
			int size = DFACollection.get(i).nbStates;
			maxSizeCpMachine = size*maxSizeCpMachine;
		}
		top = topManager.getTop();
		if(top.size()>150){
			throw new Exception("In graphFusion: Too  Big");
		}
		
		Vector<CpPartition> fusionMachines = new Vector<CpPartition>();
		Vector<Integer> faultVector = new Vector<Integer>();
		for(int i=0; i < topManager.getOriginalFaultVector().size();++i)
			faultVector.add(topManager.getOriginalFaultVector().get(i));
		while(true){
			Vector<Integer> weakestEdges = new Vector<Integer>();
			int weakestWeight = Integer.MAX_VALUE;
			for(int i =0; i < faultVector.size();++i){
				if(faultVector.get(i)< weakestWeight){
					weakestWeight = faultVector.get(i);
					weakestEdges.removeAllElements();
					weakestEdges.add(i);
				}else if(faultVector.get(i) == weakestWeight){
					weakestEdges.add(i);
				}
			}
			if(weakestWeight > f)
				break;
			
			//Event reduction
			HashSet<CpPartition> inputMachines = new HashSet<CpPartition>();
			inputMachines.add(top);
			for(int i =0; i < eventReductionParameter; ++i){
				HashSet<CpPartition> validMachines = new HashSet<CpPartition>();
				for (Iterator<CpPartition> iterator = inputMachines.iterator(); iterator
						.hasNext();) {
					CpPartition cpPartition = (CpPartition) iterator.next();
					validMachines.addAll(reducedEventSet(cpPartition,weakestEdges));
				}
				if(validMachines.size()==0)
					break;
				else{
					inputMachines = validMachines;
				}
			}
			//state reduction
			//just pick one of the machines among the even reduced machines...
			CpPartition machine = minimalMachine(inputMachines.iterator().next(),weakestEdges);
			Vector<Integer> fvNewMachine = topManager.faultVector(machine);
			for(int i =0; i < faultVector.size();++i){
				faultVector.set(i,faultVector.get(i)+ fvNewMachine.get(i));
			}
			fusionMachines.add(machine);
		}
		return fusionMachines;
	}

	public HashSet<CpPartition> reducedEventSet(CpPartition inputMachine,Vector<Integer> edges){
//		System.out.println("Reduced Event Set for\n"+inputMachine);
//		System.out.println("All reduced Machines");
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
				//System.out.println("machine after merge:"+inconsistentMachine);
			}
			//System.out.println("final incon machine:"+inconsistentMachine);
			//System.out.println("--------------------------------------");
			CpPartition lowerMachine = largestConsistentPartition(inconsistentMachine);
			//if the machine distinguishes between all pairs in edges, return it. 
			//System.out.println(lowerMachine);
			Vector<Integer> faultVector = topManager.faultVector(lowerMachine);
			boolean edgesSeperated = true;
			for(int edgeNo =0;   edgeNo < edges.size();++edgeNo){
				int index = edges.get(edgeNo);
				if(0 == faultVector.get(index)){
					edgesSeperated = false;
					break;
				}
			}
			if(edgesSeperated)
				validMachines.add(lowerMachine);
		}
//		System.out.println("Valid reduced Machines");
//		System.out.println(validMachines);
//		System.out.println("---------------------------------");
		return validMachines; 
	}
	/*
	 * Given a machine and a set of edges, this function returns the minimal machine 
	 * seperating those edges by digging deeper and deeper in the lower covers of the machines
	 */
	private CpPartition minimalMachine(CpPartition inputMachine, Vector<Integer> edges){
		while(true){
			CpPartition suitableMachine = lowerSuitableMachine(inputMachine,edges);
			/*
			 * If no machine is generated, then the input machine is the minimal fusion.
			 */
			if(suitableMachine == null)
				return inputMachine;
			else {
				inputMachine = suitableMachine;
			}
		}

	}
	/*
	 * Given a machine and a set of eges, generate a machine in the lower cover 
	 * of the innput machine, seperating the given edges.
	 */
	
	public CpPartition lowerSuitableMachine(CpPartition inputMachine, Vector<Integer> edges){
		/* Take pairs of the blocks of the partition and 
		 * generate the largest closed partition containing this pair */
		for(int i =0; i < inputMachine.size(); ++i)
			for(int j =i+1; j < inputMachine.size(); ++j){

				CpPartition inconsistentMachine = new CpPartition();
				CpBlock combinedBlock = new CpBlock(inputMachine.get(i).freshCopy(), inputMachine.get(j).freshCopy());
				inconsistentMachine.add(combinedBlock);
				/*
				 * Apart from the states that we are combining, add all other states of the 
				 * original machine to the machine.
				 */
				for(int blockNo =0; blockNo < inputMachine.size();++blockNo){
					if(!inputMachine.get(blockNo).isSubsetOf(combinedBlock)){
						CpBlock block = inputMachine.get(blockNo).freshCopy();
						inconsistentMachine.add(block);
					}
				}
				//dump the blocks into a vector and obtain the larges consistent machine for them.. 
				CpPartition lowerMachine = largestConsistentPartition(inconsistentMachine);
				
				//if the machine distinguishes between all pairs in edges, return it. 
				Vector<Integer> faultVector = topManager.faultVector(lowerMachine);
				boolean edgesSeperated = true;
				for(int edgeNo =0;   edgeNo < edges.size();++edgeNo){
					int index = edges.get(edgeNo);
					if(0 == faultVector.get(index)){
						edgesSeperated = false;
						break;
					}
				}
				
				if(edgesSeperated)
					return lowerMachine;
			}
			return null;
		
	}

	public Vector<CpPartition> genBasis(){
		return genLowerCover(top);
	}
	
	public Vector<CpPartition> genLowerCover(CpPartition inputMachine){
		
		Vector<CpPartition> lowerCover = new Vector<CpPartition>();
		
		/* Take pairs of the blocks of the partition and 
		 * generate the largest closed partition containing this pair */
		for(int i =0; i < inputMachine.size(); ++i)
			for(int j =i+1; j < inputMachine.size(); ++j){
				CpPartition inconsistentMachine = new CpPartition();
				CpBlock combinedBlock = new CpBlock(inputMachine.get(i).freshCopy(), inputMachine.get(j).freshCopy());
				inconsistentMachine.add(combinedBlock);
				/*
				 * Apart from the states that we are combining, add all other states of the 
				 * original machine to the machine.
				 */
				for(int blockNo =0; blockNo < inputMachine.size();++blockNo){
					if(!inputMachine.get(blockNo).isSubsetOf(combinedBlock)){
						CpBlock block = inputMachine.get(blockNo).freshCopy();
						inconsistentMachine.add(block);
					}
				}
				//dump the blocks into a vector and obtain the larges consistent machine for them.. 
				CpPartition lowerMachine = largestConsistentPartition(inconsistentMachine);
				//System.out.println(lowerMachine);
				lowerCover.add(lowerMachine);
			}
			for(int i =0; i < lowerCover.size() ; ++i)
			for(int j =0; j < lowerCover.size(); ++j){
				/* Compare the ith machine with all other machines. If it is lesser than any of them, remove it. 
				 * 
				 */
				if(i != j){
					//System.out.println(lowerCover.get(i));
					DFA machineI = lowerCover.get(i).getEquivalentDFA();
					DFA machineJ = lowerCover.get(j).getEquivalentDFA();
					if(machineJ.isGreaterThan(machineI)){
						lowerCover.remove(i);
						--i;
						break;
					}
				}
					
			}
			return lowerCover;
	}
	
	public CpPartition largestConsistentPartition(CpPartition lcpMachine){
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
	
	
	//give the current state of all the machines, returns the id of the cpstate
	//which features the max number of times among all these states.
	public int recover(Vector<CpBlock> currentStates){
		int[] count = new int[maxSizeCpMachine]; 
		for(int i =0; i < currentStates.size();++i){
			CpBlock block = currentStates.get(i);
			Vector<CpState> cpCollection = block.getCpCollection();	
			for(int j =0; j < cpCollection.size();++j){
				int state = cpCollection.get(j).getId();
				++count[state];
			}
			
		}
		int maxIndex = 0;
		int max =0;
		for(int i =0; i < maxSizeCpMachine;++i){
			if(count[i] > max)
				max = count[i];
				maxIndex = i;
		}
		
		return maxIndex;
	}
	
/*	//test function for the recover function
	public void testRecover(){
		CpPartition one = topManager.getEquivalentLatticeMachine(DFACollection.get(0));
		CpPartition two = topManager.getEquivalentLatticeMachine(DFACollection.get(1));
		
		for (Iterator iter = one.iterator(); iter.hasNext();) {
			CpBlock element = (CpBlock) iter.next();
			System.out.println(element);
			break;
		}
		
	}
*/
	public void testffFusion(Vector<DFA> dfaCollection, Vector<CpPartition> ffFusion,int f){
		Vector<DFA> allMachines = new Vector<DFA>();
		DFA topDFA = dfaCollection.get(0);
		for(int i=0; i < dfaCollection.size();++i){
			topDFA = topDFA.crossproduct(dfaCollection.get(i));
			allMachines.add(dfaCollection.get(i));
		}
		
		for(int j=0; j < ffFusion.size();++j)
			allMachines.add(ffFusion.get(j).getEquivalentDFA());
		/* make sure that the rcp of all subsets of size n among the machines and the fusions
		 * in allMachines yields the cross product of the machines in dfaCollection 
		 */
		int[] subset = new int[dfaCollection.size()];
	    
		int[] set = new int[dfaCollection.size()+f];
		for(int i =0; i < dfaCollection.size()+f;++i)
			set[i]=i;
		Vector<int[]> allSubsets = new Vector<int[]>();
	    Util.processLargerSubsets(set, subset, 0, 0,allSubsets);
	    
	    for(int i=0; i < allSubsets.size(); ++i){
	    	int[] subsetOfMachines = allSubsets.get(i);
	    	DFA crossProd = allMachines.get(subsetOfMachines[0]);
	    	System.out.println(crossProd);
	    	for(int j=1; j < subsetOfMachines.length;++j){
	    		crossProd = allMachines.get(subsetOfMachines[j]).crossproduct(crossProd); 
	    	}
	    	try {
				if(!crossProd.equals(topDFA))
					throw new Exception("Not a valid Fusion");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	public void printFaultVector(){
		
		int index =0;
		for(int i = 0; i < top.size();++i)
			for(int j = i+1; j < top.size();++j){
				CpState a = top.get(i).getCpCollection().firstElement();
				CpState b = top.get(j).getCpCollection().firstElement();
				
				System.out.println(a.getId()+"-"+b.getId()+" :"+topManager.getOriginalFaultVector().get(index));
				++index;
			}
			
	}

	public CpPartition getCpMachine() {
		return top;
	}

	public CpPartition getTop() {
		return top;
	}
}	
                                         