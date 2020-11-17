package examples;

import main.DFA;

public class Mod3ZeroCounter extends DFA{
	public Mod3ZeroCounter() {
		super(3,2);
		this.name = "Mod 3 Zero Counter";
		for(int i=0; i <3; ++i){
			this.next[i][0] = (i+1)%3;
		}
	}
}
