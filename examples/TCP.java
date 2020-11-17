package examples;
import main.DFA;

public class TCP extends DFA{

	//states
	static int closed =0,listen=1,syn_rcvd = 2,syn_sent = 3,
		established = 4,fin_wait_1 = 5,fin_wait_2 = 6, close_wait = 7,last_ack = 8,closing = 9,time_wait = 10;
	
	//events
	static int pass_open = 0,active_open = 1,syn = 2,send = 3,ack = 4,syn_and_ack = 5,
		close = 6,fin = 7,time_out = 8,ack_and_fin = 9;
	
	public TCP() {
		super(11,10);
		this.name = "TCP";
		this.next[closed][pass_open] = listen; this.next[closed][active_open] = syn_sent;
		this.next[listen][syn] = syn_rcvd; this.next[listen][send] = syn_sent;
		
		this.next[syn_rcvd][ack] = established; this.next[syn_rcvd][close] = fin_wait_1;
		this.next[syn_sent][syn] = syn_rcvd; this.next[syn_sent][syn_and_ack] = established;
		
		this.next[established][close] = fin_wait_1;this.next[established][fin] = close_wait;
		
		this.next[fin_wait_1][ack] = fin_wait_2; this.next[fin_wait_1][fin] = closing;this.next[fin_wait_1][ack_and_fin] = time_wait;
		this.next[close_wait][close] = last_ack;
		this.next[fin_wait_2][fin] = time_wait ;
		this.next[last_ack][ack] = closed;
		this.next[closing][ack] = time_wait;
		
		this.next[time_wait][time_out] = closed;
	}
}
