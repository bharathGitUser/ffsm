/*
 * Class which generates the cross product set of states.
 * 
 */
package main;

import java.util.Hashtable;
import java.util.Vector;

public class TopHandler {
	
	static int nbOriginalMachines;
	/*These are the containers which hold the states corresponding to the cross product machine.
	 *The unpruned version consists of unreachable states which is later refined 
	 */
	Vector<CpState> cpCollection;
	CpPartition top; 
	private Hashtable<String,Integer> faultVectorIndices;
	private Vector<Integer> originalFaultVector;
	
	//The input collection of all dfas.
	Vector<DFA> DFACollection;
	
	public static boolean verbose = false;
	
	public Vector<CpState> getCpCollection() {
		return cpCollection;
	}


	public void setCpCollection(Vector<CpState> cpCollection) {
		this.cpCollection = cpCollection;
	}


	public TopHandler(Vector<DFA> DFACollection){
		
		this.DFACollection = DFACollection;
		nbOriginalMachines = DFACollection.size();
		cpCollection = new Vector<CpState>();
		
		constructTop();
		
		constructInitialFaultVector();
	}	

	public void constructTop(){
		top = new CpPartition();
		
		Vector<Integer> compVector = new Vector<Integer>();
		CpState initialState = new CpState(DFACollection);
		for(int i =0; i < DFACollection.size(); ++i){
			compVector.add(0);
		}
		initialState.setComponentStates(compVector);//initial state..
		CpBlock iniBlock = new CpBlock(initialState);
		dfsTop(iniBlock);
	}

	public void dfsTop(CpBlock block){
		top.add(block);
		for(int i =0 ; i < Util.eventsIntop ; ++i){
			CpState cp = block.getCpCollection().get(0).next(i);//we know there is only one cpstate in this block
			CpBlock nextBlock = new CpBlock(cp);
			block.next[i] = nextBlock;
			if(verbose)System.out.println(cp);
			if(top.contains(nextBlock) == false){//toDo...optimise this!!
				dfsTop(nextBlock);
			}
		}
	}

	public CpPartition getTop(){
		return top;
	}

	
	public void constructInitialFaultVector(){
		faultVectorIndices = new Hashtable<String, Integer>();
		int indexCount =0;
		for(int i = 0; i < top.size();++i)
		for(int j = i+1; j < top.size();++j){
			CpState a = top.get(i).getCpCollection().firstElement();
			CpState b = top.get(j).getCpCollection().firstElement();;
			
			if(a.getId()< b.getId()){
				faultVectorIndices.put(a.getId()+" "+ b.getId(),indexCount);
			}
			else{
				faultVectorIndices.put(b.getId()+" "+ a.getId(),indexCount);
			}
			++indexCount;
		}
		
		//generate fault vector for original machines
		originalFaultVector = new Vector<Integer>();
		for(int i =0; i < faultVectorIndices.size();++i)
			originalFaultVector.add(DFACollection.size());
		
		if(verbose)System.out.println(originalFaultVector);
		for(int i =0; i < DFACollection.size() ;++i){
			CpPartition machine = getEquivalentLatticeMachine(DFACollection.get(i));
			for(int blockNo =0; blockNo < machine.size();++blockNo){
				Vector<CpState> cpCollection = machine.get(blockNo).getCpCollection();
				for(int cpStateNo = 0; cpStateNo < cpCollection.size();++cpStateNo){
					for(int nextCpStateNo = cpStateNo+1; nextCpStateNo < cpCollection.size();++nextCpStateNo){
						int index = getFaultvectorIndex(cpCollection.get(cpStateNo), cpCollection.get(nextCpStateNo));
						int value = originalFaultVector.get(index);
						originalFaultVector.set(index,value-1);
					}
				}
			}
		}

	}	
	
	//given two cp states, get the corresponding fault vector index. 
	public int getFaultvectorIndex(CpState a, CpState b){
		if(a.getId()< b.getId()){
			return faultVectorIndices.get(a.getId()+" "+ b.getId());
		}
		else{
			return faultVectorIndices.get(b.getId()+" "+ a.getId());
		}
	}

	/*
	 * Given a DFA, which we know is lesser than the top, get the coresponding 
	 * partition of the DFA.
	 */
	public CpPartition getEquivalentLatticeMachine(DFA machine){
		CpPartition equiLatMachine = new CpPartition();
		Vector<CpState> visited = new Vector<CpState>();
		for (int i =0; i < machine.nbStates;++i) {
			CpBlock block = new CpBlock();
			equiLatMachine.add(block);
		}

		for(int i =0; i < top.size(); ++i){
			CpState element = top.get(i).getCpCollection().firstElement();
			if(0 == element.getId()){
				equiLatMachine.get(0).addCpState(element);
				homomorphicDfs(visited, equiLatMachine,machine, element,0);
				break;
			}
		}
		return equiLatMachine;
	}
	
	//dfs just meant for the function written above..
	private void homomorphicDfs(Vector<CpState> visited,CpPartition equiLatMachine,DFA machine,CpState cpMachineState,  int dfaState ){
		if(visited.contains(cpMachineState))
			return;
		else
			visited.add(cpMachineState);
		
		for(int event =0; event < Util.eventsIntop; ++event){
			int nextDFAState =0;
			try{
				nextDFAState = machine.offsettedNext(dfaState, event);
			}catch(ArrayIndexOutOfBoundsException e){
				nextDFAState = dfaState;
			}
			
			CpState nextCpState = cpMachineState.next(event);
			equiLatMachine.get(nextDFAState).addCpState(nextCpState);
			homomorphicDfs(visited, equiLatMachine,machine, nextCpState,nextDFAState);
		}
	}

	public String toString(){
		String out = new String();
		out = out + cpCollection;
		return out;
	}


	public Vector<Integer> getOriginalFaultVector() {
		return originalFaultVector;
	}
	
	public Vector<Integer> faultVector(CpPartition machine){
		Vector<Integer> faultVector = new Vector<Integer>();
		for(int i =0; i < faultVectorIndices.size();++i)
			faultVector.add(1);
		
		if(verbose)System.out.println(faultVector);
		for(int blockNo =0; blockNo < machine.size();++blockNo){
			Vector<CpState> cpCollection = machine.get(blockNo).getCpCollection();
			for(int cpStateNo = 0; cpStateNo < cpCollection.size();++cpStateNo){
				for(int nextCpStateNo = cpStateNo+1; nextCpStateNo < cpCollection.size();++nextCpStateNo){
					int index = getFaultvectorIndex(cpCollection.get(cpStateNo), cpCollection.get(nextCpStateNo));
					faultVector.set(index,0);
				}
			}
		}

		return faultVector;
		
	}
	
	public int dmin(Vector<Integer> faultVector){
		int dmin = Integer.MAX_VALUE;
		for(int i =0; i < faultVector.size();++i){
			if(faultVector.get(i)<dmin)
				dmin = faultVector.get(i); 
		}
		return dmin;
	}
}
