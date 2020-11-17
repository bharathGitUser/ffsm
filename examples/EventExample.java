package examples;

import main.DFA;

public class EventExample extends DFA{
	public EventExample(){
		super(4,4);
		this.name = "Event Reduction Example";
		this.next[0][0]=3; this.next[0][1]=1;this.next[0][2]=2;
		this.next[1][1]=3; this.next[1][2]=2;
		this.next[2][3]=3;  
		this.next[3][1]=1;this.next[3][2]=2;
	}
}
