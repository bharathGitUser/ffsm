package examples;

import main.DFA;

public class PatternGeneratorSimple extends DFA{
	public PatternGeneratorSimple(){
		super(4,2);
		this.name = "Pattern Generator Simple";
		this.next[0][0]=0; this.next[0][1]=1;
		this.next[1][0]=0; this.next[1][1]=2;
		this.next[2][0]=3; this.next[2][1]=2;
		this.next[3][0]=0; this.next[3][1]=1;
	}
}
