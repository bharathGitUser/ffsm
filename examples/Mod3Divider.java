package examples;

import main.DFA;

public class Mod3Divider extends DFA {
	public Mod3Divider(){
		super(3,2);
		this.name = "Mod 3 Divider";
		this.next[0][0]=0; this.next[0][1]=1;
		this.next[1][0]=2; this.next[1][1]=0;
		this.next[2][0]=1; this.next[2][1]=2;
	}
}
