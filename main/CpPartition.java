package main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

/*
 * CpPartition is the finite state machine which we try to model. Hence a CpPartion consists
 * of closed partitions where the individual parts are the blocks.
 */
public class CpPartition extends Vector<CpBlock> implements Comparable<CpPartition>{
		public String toString(){
			String out = new String();
			out = out + "Events:"+actualNumberOfEvents()+"\n";
			for(int i =0; i < this.size();++i){
				out = out + this.get(i)+"\n";
			}
			return out;
		}
		
		public CpPartition freshCopy(){
			CpPartition copy = new CpPartition();
			for(int i =0; i < this.size(); ++i){
				//if(LatticeFusionAlgo.verbose)System.out.println(this.get(i).freshCopy());
				copy.add(this.get(i).freshCopy());
			}
			return copy;
		}
		
		public Iterator<CpBlock> iterator(){
			return super.iterator();
		}
		
		public DFA getEquivalentDFA(){
			DFA equiDFA = new DFA(this.size(), Util.eventsIntop);
			
			/*
			 * First we make sure that the element in the partition containing
			 * the initial state is in position 0 of the vector containg these 
			 * blocks
			 * 
			 */ 
			
			for(int i =0; i < this.size();++i){
				if(this.get(i).containsInitialState()){
					this.insertElementAt(this.remove(i), 0);
				}
			}
			
			for(int state = 0; state < this.size(); ++state)
			for(int event = 0; event < Util.eventsIntop; ++event){
//				System.out.println("state:"+ state+" event:"+event);
				int nextState = this.indexOf(this.get(state).next[event]);
				equiDFA.next[state][event] = nextState;
			}
			return equiDFA;
		}
		
		public int compareTo(CpPartition that){
			if(this.size() == that.size())
				return 0;
			else if(this.size() < that.size())
				return -1;
			else if(this.size() > that.size())
				return 1;
			
			return 0;
		}
		
		public boolean equals(CpPartition that){
			return this.getEquivalentDFA().equals(that.getEquivalentDFA()); 
		}
		
		public boolean isEvent(int event){
            for(int i=0; i < size(); ++i){
                if(this.get(i).next[event]!= this.get(i))
                    return true;
            }
            return false;
           
        }
		public int actualNumberOfEvents(){
			HashSet<Integer> eventSet = new HashSet<Integer>();
			for(int i=0; i < size(); ++i){
				for(int event =0; event < Util.eventsIntop;++event){
					if(this.get(i).next[event]!= this.get(i))
						eventSet.add(event);
				}
			}
			return eventSet.size();
		}
}
