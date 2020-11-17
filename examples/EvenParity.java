package examples;

import main.DFA;

public class EvenParity extends DFA{
	static int nbStates =2; static int nbEvents=2;
	public EvenParity(){
		super(nbStates,nbEvents);
		this.name = "EvenParity";
		this.next[0][0]=1; this.next[0][1]=0;
		this.next[1][0]=0; this.next[1][1]=1;
	}
}   
