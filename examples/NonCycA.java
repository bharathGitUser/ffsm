package examples;

import main.DFA;

public class NonCycA extends DFA{
	public NonCycA(){
		super(3,2);
		this.name = "Non Cyclic Machine A";
		this.next[0][0]=1; this.next[0][1]=0;
		this.next[1][0]=2; this.next[1][1]=0;
		this.next[2][0]=1; this.next[2][1]=0; 
		
	}
}
