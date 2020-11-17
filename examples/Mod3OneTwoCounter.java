package examples;

import main.DFA;

public class Mod3OneTwoCounter extends DFA{

	public Mod3OneTwoCounter() {
		super(3,3);
		this.name = "Mod 3 One Two Counter";
		for(int i=0; i <3; ++i){
			this.next[i][1] = (i+1)%3;
			this.next[i][2] = (i+1)%3;
			this.next[i][0] = i;
			//a.next[i][1] = i;
		}
	}
}
