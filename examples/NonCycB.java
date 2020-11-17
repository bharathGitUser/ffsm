package examples;

import main.DFA;

public class NonCycB extends DFA{
	public NonCycB(){
		super(3,2);
		this.name = "Non Cyclic Machine B";
		this.next[0][0]=1; this.next[0][1]=2;
		this.next[1][0]=2; this.next[1][1]=2;
		this.next[2][0]=1; this.next[2][1]=2; 
	}
}
