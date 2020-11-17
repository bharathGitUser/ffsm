package examples;

import main.DFA;

public class PatternGeneratorComplex extends DFA{
	public PatternGeneratorComplex(){
		super(8,2);
		this.name = "Pattern Generator Complex";
		this.next[0][0]=4; this.next[0][1]=0;
		this.next[1][0]=0; this.next[1][1]=4;
		this.next[2][0]=1; this.next[2][1]=4;
		this.next[3][0]=5; this.next[3][1]=1;
		this.next[4][0]=6; this.next[4][1]=2;
		this.next[5][0]=2; this.next[5][1]=6;
		this.next[6][0]=3; this.next[6][1]=7;
		this.next[7][0]=7; this.next[7][1]=3;
	}
}
