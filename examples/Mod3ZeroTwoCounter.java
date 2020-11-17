package examples;

import main.DFA;

public class Mod3ZeroTwoCounter extends DFA{
	public Mod3ZeroTwoCounter() {
		super(3,3);
		this.name = "Mod 3 Zero Two Counter";
		for(int i=0; i <3; ++i){
			this.next[i][0] = (i+1)%3;
			this.next[i][2] = (i+1)%3;
			this.next[i][1] = i;
		}
	}
}
