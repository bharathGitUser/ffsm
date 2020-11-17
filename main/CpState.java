package main;
/*
 * This is the main representation of the cross product states.
 * 
 */
import java.util.Vector;

public class CpState {
	/*Each cross product state needs a pointer to the DFA Colection to enable 
	 * recovery and also assign ids to the state
	 */
	private Vector<DFA> DFACollection;
	
	/*Set of component states of the cross product state. For example:
	 * cp1 = <0,1> where 0 belongs to the 1st machine and 1 belongs to the second machine. Hence and 0 and 1 are 
	 * the component states which are maintained in that order.
	 */
	private Vector<Integer> componentStates;
	
	public Vector<Integer> getComponentStates() {
		return componentStates;
	}

	public void setComponentStates(Vector<Integer> componentStates) {
		for(int i =0; i < componentStates.size();++i)
			this.componentStates.add(componentStates.get(i));
	}

	public CpState(Vector<DFA> DFACollection){
		this.DFACollection = DFACollection;
		componentStates = new Vector<Integer>();
	}
	
	/*
	 * Given an event determines the state to which you need to go.
	 */
	public CpState next(int event){
		CpState cp = new CpState(DFACollection);
		for(int i= 0 ; i < DFACollection.size();++i){
			DFA machine = DFACollection.get(i);
			int state = componentStates.get(i);
			int nextState =0;
			try {
				nextState = machine.offsettedNext(state, event);
			} catch (ArrayIndexOutOfBoundsException e) {
				nextState = state;//if the even is not defined for the machine, just make it loop.. 
			}
			cp.getComponentStates().add(nextState);
		}
		return cp;
	}

	/*
	 * Get an easy id representation of the value: j + k*statesA + i*statesA*statesB and so on... 
	 */
	public int getId(){
		int id = (Integer)componentStates.lastElement();
		//System.out.println(id);
		int productFactor = DFACollection.lastElement().nbStates;
		for(int i = DFACollection.size()-2  ; i >= 0 ;--i){
			id = id + (Integer)componentStates.get(i) * productFactor;
			productFactor = productFactor * DFACollection.get(i).nbStates;
		}
		return id;
		
	}
	public String toString(){
		String out = new String();
		out = getId()+"";
/*		for(int event =0; event < DFACollection.lastElement().nbLetters;++event)
			out = out + "On "+ event +" -> "+next(event).getId()+"\n";
*/		return out;
	}
	
	public boolean contains(int machineNumber,int stateNumber){
		if((Integer)this.componentStates.get(machineNumber)== stateNumber)
			return true;
		else
			return false;
		
	}
	public boolean equals(Object obj){//==Operator....java sucks!!!!.
		CpState that = (CpState)obj;
		
		for(int i =0; i < componentStates.size();++i){
			if(that.getComponentStates().get(i)!= this.getComponentStates().get(i))
				return false;
		}
		
		return true;
	}
}


