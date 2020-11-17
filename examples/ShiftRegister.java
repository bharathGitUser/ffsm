package examples;

import main.DFA;
/* 3 bit Shift Register: Saves the last 3 bits of the data stream*/
public class ShiftRegister extends DFA{

	public ShiftRegister() {
		super(8,2);
		this.name = "3 bit Shift Register";
		this.next[0][0]=0; this.next[0][1]=4;
		this.next[1][0]=0; this.next[1][1]=4;
		this.next[2][0]=1; this.next[2][1]=5;
		this.next[3][0]=1; this.next[3][1]=5;
		this.next[4][0]=2; this.next[4][1]=6;
		this.next[5][0]=2; this.next[5][1]=6;
		this.next[6][0]=3; this.next[6][1]=7;
		this.next[7][0]=3; this.next[7][1]=7;
	}
}
