package main;

import java.util.Vector;

///////////////////////////////////
//          XOR Program // Vijay K. Garg
//          2006-08-20
/////////////////////////////////
/**
   This class implements deterministic complete finite automata.
   The transition function is represented by a double array 
   <code>next[][]</code>. The set of terminal states is given
   by a partition of the set of states 
   (class {@link Partition Partition}).
   A state <code>q</code> is terminal if 
   <code>terminal.blockName[p]=1</code> 
   (the default value is <code>0</code>). 
*/
public class DFA implements Comparable<DFA>{
    private int initial =0;          // the initial state
    private Alphabet alphabet;     // the alphabet
    public int nbStates;
    public int nbLetters;
    private boolean [] reachable;
    private boolean [] marked;
    static boolean verbose = false;
    public void setName(String name) {
		this.name = name;
	}

	protected String name;
    public int[][] next;          // the nextstate function
    private int offset=0;
    /**
       creates a DFA with <code>n</code> states 
       and <code>k</code> letters.
    */
    
    public DFA(){}
    public   DFA(int n, int k) {
    this(n, new Alphabet(k));
    }
    
    public void setoffset(int newValue){
    	this.offset = newValue; 
    }
    /**
       creates a DFA with n states and alphabet a.
    */
    public   DFA(int n, Alphabet a) {
	nbStates = n; 
	alphabet = a;
	nbLetters = alphabet.size();
	next = new int[nbStates][nbLetters];
	
	for(int i=0; i < nbStates; ++i)
		for(int event = 0; event < nbLetters; ++event)
			next[i][event] = i;

	if(verbose)System.out.println("trying to create partiotions");
	//info = new int[n];
	marked = new boolean[nbStates];
	reachable = new boolean[nbStates];
    }

    /**
       returns the REACHABLE crossproduct of this DFA with DFA T 
	@param t second dfa
        @return REACHABLE crossproduct of dfa
    */
    public int offsettedNext(int state, int event){
    	if((event < offset) ||(event >= offset+nbLetters)){
    		return state; 
    	}
    	return next[state][event-offset];
    }

    public int getEventSize(){
    	return nbLetters;
    }
    public DFA crossproduct(DFA t) {
	int tStates = t.nbStates;
	if(verbose)System.out.println(tStates);
	DFA q = new DFA(nbStates * tStates, alphabet);
	for (int i=0; i < nbStates; i++)
	   for (int j=0; j < tStates; j++)
	       for (int k=0; k < nbLetters; k++)
		 q.next[i*tStates + j][k] = next[i][k] * tStates + t.next[j][k];
	q.initial = 0;
        q.computeReachable(q.initial);
        
    //We now create a machine which has only the reachable number of states
    int[] mapping = new int[q.nbStates];
    
    for(int i =0; i < q.nbStates ; ++i)
    	mapping[i] =-1;
    
    int reachableStates = 0;
    for(int i=0; i < q.nbStates ; ++i){
    	if(q.reachable[i]){
    		mapping[i] = reachableStates;
    		++reachableStates;
    	}
    }
    DFA reachableCrossProduct = new DFA(reachableStates,q.nbLetters);
    
    for(int state =0; state < q.nbStates; ++state)
    for(int event =0; event < q.nbLetters ; ++event){
    	if(q.reachable[state])
    		reachableCrossProduct.next[mapping[state]][event] = mapping[q.next[state][event]];
    }
    
    	return reachableCrossProduct;    
    }

    /**
	calculates the reachable set of states from the given state
          @param x starting state 
    */
/*	public DFA getCrossProduct(Vector<DFA> dfaCollection){
		int events;
		int states;
		for(int i =0; i < DFACollection.size();++i){
			if (Util.nbEvents < DFACollection.get(i).getEventSize())
				Util.nbEvents = DFACollection.get(i).getEventSize();
		}

		DFA cpDFA = new DFA(top.size(), Util.nbEvents);
		cpDFA = dfaCollection.get(0);
		for(int j =1; j < dfaCollection.size(); ++j)
			cpDFA = cpDFA.crossproduct(dfaCollection.get(j));
		
		return cpDFA;
	}
*/
     public void computeReachable(int x) {
        for (int i=0; i<nbStates;i++) {
	 marked[i] = false;
	 reachable[i] = false;
        }
	reachable[x] = true;
        dfs(x);
     }

     public void dfs(int x) {
    	//System.out.println("Entry:"+x); 
		marked[x] = true;
		for (int k=0; k < nbLetters; k++) {
		    int y = next[x][k];

		    if (y < nbStates) {
			//   System.out.println(y);	
		       reachable[y] = true;
		       if (!marked[y]) dfs(y);
		    }
		}
	}
      

    /**
       returns the state reached from state <code>p</code> after 
       reading the word <code>w</code>.
       @param p starting state
       @param w input word (w is not <code>null</code>
       @return state reached
    */
    public int nextState(int p, String w){ 
	return nextState(p, alphabet.toShort(w));
    }

    int nextState(int p, short[] w){
	//transition by a word   w in a DFA
	for(int i=0;i<w.length;i++){
	    p=next[p][w[i]];
	    if(p==-1) break;
	}
	return p;
    }
    public DFA copy() {
	DFA q = new DFA(nbStates, alphabet);
	for (int i=0; i < nbStates; i++)
	       for (int k=0; k < nbLetters; k++)
		 q.next[i][k] = next[i][k];
	q.initial = initial;
        q.computeReachable(q.initial);
	return q;
    }
    
    public int compareTo(DFA that) {
    	if(this.nbStates < that.nbStates)
    		return -1;
    	
    	if(this.nbStates > that.nbStates)
    		return 1;
    	
    	return 0;

    }
    
/*    public boolean equals(DFA that){
    	if(this.nbStates != that.nbStates)
    		return false;
    	
    	if(this.nbLetters != that.nbLetters)
    		return false;
    	System.out.println("got here");
    	for(int state =0; state < nbStates ; ++state)
    	for(int event =0; event < nbLetters; ++ event)
    		if(this.next[state][event] != that.next[state][event])
    			return false;
    	
    	return true;
    }
*/  
    public boolean equals(DFA that){
    	if(this.isGreaterThan(that) && (this.nbStates == that.nbStates))
    		return true;
    	else
    		return false;
    }
    
    /*
     * This is the function to test homomorpism between two machines...the basic idea
     * is to build a mapping between the states of this to tha
	public String toString() {
		System.out.println("EvenParity");
	}
t and then see
     * if this mapping is homomorphic
     */
    public boolean isGreaterThan(DFA that){
    	int[] mapping = new int[this.nbStates];
    	boolean[] visited = new boolean[this.nbStates];
    	
    	if(this.nbStates < that.nbStates)
    		return false;
    	
    	for(int i =0; i < nbStates; ++i){
    		mapping[i] = -1;
    		visited[i] = false;
    	}
    	
    	mapping[this.initial] = that.initial;
    	try{
    		comparisonDFS(this.initial, mapping[this.initial], that, mapping, visited);
    	}
    	catch(NotHomomorphicException e){
    		return false;
    	}
    	
    	//Make sure that no state of 'that' is leftuntouched
    	boolean[] touched = new boolean[that.nbStates];
    	for(int i =0; i < that.nbStates;++i)
    		touched[i] = false;
    		
    	for(int i =0; i < this.nbStates ;++i){
    		touched[mapping[i]] = true;
    	}
    		
    	for(int i =0; i < that.nbStates ;++i){
    		if(!touched[i])
    			return false;
    	}
    	
    	return true;
    }
    
    /*
     * this is a DFS which is exclusively to compare two machines.
     */
    public void comparisonDFS(int stateThis, int stateThat, DFA that, int[] mapping, boolean[] visited) throws NotHomomorphicException{
    	
    	if(visited[stateThis])
    		return;
    	else
    		visited[stateThis] = true;
    	
    	for(int event =0; event < nbLetters; ++event){
    		int nextStateThis = this.next[stateThis][event];
    		int nextStateThat = that.next[stateThat][event];
    		if(mapping[nextStateThis] == -1)
    			mapping[nextStateThis]= nextStateThat;
    		else if(mapping[nextStateThis] != nextStateThat)
    			throw new NotHomomorphicException();
    		comparisonDFS(nextStateThis, mapping[nextStateThis], that, mapping, visited);
    	}
    }

    
    public String toString(){
    	String out = new String();
    	for(int state =0; state < nbStates ; ++state){
    		out = out + "[" + state +"]";
    		for(int event =0; event < nbLetters;++event){
   				out = out + "|"+event+" [" + next[state][event]+"]";
    		}
    		out =out +"\n";
    	}
    	return out;	
    }
    
    public String getName(){
    	return name;
    }
}
