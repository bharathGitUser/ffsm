package examples;

import main.DFA;

public class Mod3OneCounter extends DFA{

	public Mod3OneCounter() {
		super(3,2);
		this.name = "Mod 3 One Counter";
		for(int i=0; i <3; ++i){
			this.next[i][1] = (i+1)%3;
		}
	}
}
