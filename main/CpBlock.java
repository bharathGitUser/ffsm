package main;
/*
 * Represents the tuple set of cross product states such as <cp0,cp1,cp2>. Acts
 * as the blocks of the partition which forms the machine. All machines in the
 * closed partition lattice with the cross product as the top element will contain
 * this format of machine. 
 * 
 */
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class CpBlock {
	//The core data structure which holds the cross product states
	private Vector<CpState> cpCollection;
	private static boolean verbose = false;
	public CpBlock[] next;
	public Vector<CpBlockPointer> pointers;//Blocks pointing to this block..WARNING: This need not be in a consistent
	//state...just used for implementation ease.
	
	public CpBlock(){
		initialisation();
	}
	//Given a set of cross product states, generate a block out of them
	public CpBlock(Vector<CpState> componentStates){
		initialisation();
		for (Iterator iter = componentStates.iterator(); iter.hasNext();) {
			addCpState((CpState) iter.next());
		}
	}

	//Given a cross product state, generate a block out of it.
	public CpBlock(CpState componentState){
		initialisation();
		addCpState(componentState);
	}
	
	//Given two cpBlocks, combine and generate a new one out of them
	public CpBlock(CpBlock first, CpBlock second){
		initialisation();
		this.cpCollection.addAll(first.cpCollection);
		this.cpCollection.addAll(second.cpCollection);		
	}
	
	public CpBlock(CpBlock that){
		initialisation();
		this.cpCollection.addAll(that.cpCollection);
	}
	//general intialisation required by all constructors
	public void initialisation(){
		cpCollection = new Vector<CpState>();
		next = new CpBlock[Util.eventsIntop];
		pointers = new Vector<CpBlockPointer>();
	}
	
	public void addCpState(CpState cp){
		if(! this.contains(cp))
			cpCollection.add(cp);
	}

	public void mergeBlock(CpBlock that){
		for (Iterator<CpState> iterator = that.cpCollection.iterator(); iterator.hasNext();) {
			CpState cp = (CpState) iterator.next();
			if(!cpCollection.contains(cp))
				cpCollection.add(cp);
		}
	}

	//returns the cpBlock identical to this but with the next pointers all pointing to NULL
	public CpBlock freshCopy(){
		Vector<CpState> newCpCollection = new Vector<CpState>();
		for (Iterator iter = cpCollection.iterator(); iter.hasNext();) {
			CpState element = (CpState) iter.next();
			newCpCollection.add(element);
		}
		//if(LatticeFusionAlgo.verbose)System.out.println(new CpBlock(newCpCollection));
		return new CpBlock(newCpCollection);
	}
	
	
	public Vector<CpState> getCpCollection() {
		return cpCollection;
	}


	public int size(){
		return cpCollection.size();
	}
	
	public boolean equals(Object obj){
		CpBlock that = (CpBlock)obj;

		if(this.size()!= that.size())
			return false;

		Object[] this_array = this.cpCollection.toArray();
		Object[] that_array = that.cpCollection.toArray();

		for(int i =0 ; i < that.size(); ++i){
			CpState that_state = (CpState)that_array[i];
			boolean notIn = true;
			for(int j =0; j < this.size(); ++j){
				CpState this_state = (CpState)this_array[j];
				if(this_state.equals(that_state))
					notIn = false;
			}
			if(notIn)
				return false;
		}

		return true;
	}
	
	public boolean contains(CpState element){
		if(verbose)System.out.println("In cpcontainer,contains:"+element);
		boolean flag = false;
		for (Iterator iter = cpCollection.iterator(); iter.hasNext();){
			CpState cp = (CpState)iter.next();
			if(cp.equals(element)){
				flag = true;
				break;
			}
				
		}
		
		return flag;
	}
	

	/*Function returns the size of the intersection of two cpcontainers */
	public int sizeOfIntersection(CpBlock that){
		int sizeIntersecion = 0; 
		for (Iterator iter = that.cpCollection.iterator(); iter.hasNext();){
			CpState cp = (CpState)iter.next();
			if(verbose)System.out.println(cp+","+this.cpCollection.contains(cp));
			if(this.contains(cp))
				++sizeIntersecion;
		}

		return sizeIntersecion;
	}

	/*Function returns the intersection of two cpcontainers */
	public int maxCommon(){
		int maxCommon = 0; 
		for(int i=0; i < cpCollection.size();++i)
		for(int j= i + 1; j < cpCollection.size();++j){
			Vector<Integer> a = cpCollection.get(i).getComponentStates();
			Vector<Integer> b = cpCollection.get(j).getComponentStates();
			int common =0; 
			for (int k =0; k < a.size(); ++k){
				if(a.get(k)== b.get(k))
					++common;
			}
			if(common > maxCommon)
				maxCommon = common;
		}			
		return maxCommon;
	}
	
	/* Function returns a cross product container containining the cp states 
	 * which contain the state of the individual machine specified as a parameter.E.g [0]
	 * contains state 0 from machine 1.
	 * 
	 * 
	 * 
	
	public CpBlock subsetContainingState(int machineNumber,int stateNumber){
		CpBlock temp = new CpBlock();
		for (Iterator iter = this.cpCollection.iterator(); iter.hasNext();){
			CpState cp = (CpState)iter.next();
				if((cp).contains(machineNumber,stateNumber))
					temp.add(cp);
		}
		return temp;
	}
	 */
	/*
	 * Check if the container contains the initial state...used for generating the backup machines. 
	 * 
	 */
	public boolean containsInitialState(){
		boolean foundInitialState = false;
		for (Iterator iter = cpCollection.iterator(); iter.hasNext();){
			CpState cp = ((CpState)iter.next());
			if(0 == cp.getId()){
				foundInitialState = true;
			}
		}
		
		return foundInitialState;
	} 
	
	public boolean containsStateWithId(int id){
		boolean foundState = false;
		for (Iterator iter = cpCollection.iterator(); iter.hasNext();){
			CpState cp = ((CpState)iter.next());
			if(id == cp.getId()){
				foundState = true;
			}
		}
		
		return foundState;
		
	}
	/*
	 * Gives the next state by simply calculating , the transition function
	 * of the top machine applied over a set of cp states. WARNING: Used only 
	 * for the cross product machine. 
	 */
	public CpBlock nextState(int event){
		CpBlock temp = new CpBlock();
		
		Object[] collectionArray = cpCollection.toArray();
		
		for (int i=0 ; i < cpCollection.size(); ++i) {
			CpState cp = (CpState)collectionArray[i];
			temp.addCpState(cp.next(event));
		}
		
		return temp;
	}

	public boolean isSubsetOf(CpBlock that){
		for (Iterator iter = cpCollection.iterator(); iter.hasNext();){
			CpState cp = ((CpState)iter.next());
			if(false == that.contains(cp))
				return false;
		}
		return true;
	}
	
	public String toString(){//Print out the component cross product states...
		String out = new String();
		out = out + cpCollection.toString();
/*		for(int j=0; j < CpGenerator.nbEvents; ++j){
			out = out + "On " + j +" :" + next[j].toString()+"\n";
		}*/
/*		out = out + "Pointers pointing to me:\n";
		for(int i =0; i < pointers.size();++i){
			out = out + "Block "+ pointers.get(i).getBlock().getCpCollection()+" on event "+pointers.get(i).getEvent()+"\n";
		}
		if(pointers.size()==0) {out = out + "None\n";}*/
		for(int event =0; event < Util.eventsIntop;++event){
			if(next[event] != null && next[event]!= this)
				out = out + "("+event+" " + next[event].cpCollection+")";
		}
		
		return out;
	}
	

}
