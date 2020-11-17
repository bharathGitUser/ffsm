package examples;

import main.DFA;

// This assumes a write back cache.
public class Mesi extends DFA {
	
	//states
	static int shared = 0,invalid =1,modified = 2,exclusive =3;
	
	//events
	static int read_hit =0, write_hit = 1,snoop_hit_read = 2,snoop_hit_write = 3,invd = 4,flush = 5,read_miss=6;
	//flush indicates flush/wbinvd/invd
	
	public Mesi(){
		super(4,7);
		this.name = "MESI Protocol";
		this.next[shared][read_hit]= shared; this.next[shared][write_hit]= exclusive;
		this.next[shared][snoop_hit_read]= shared;this.next[shared][snoop_hit_write]= invalid;
		
		//exclusive
		this.next[exclusive][read_hit]= exclusive; this.next[exclusive][write_hit]= modified;
		this.next[exclusive][snoop_hit_read]= shared;this.next[exclusive][snoop_hit_write]= invalid;
		this.next[exclusive][flush]= invalid;
		
		//modified
		this.next[modified][read_hit]= modified; this.next[modified][write_hit]= modified;
		this.next[modified][snoop_hit_read]= shared;this.next[modified][snoop_hit_write]= invalid;
		this.next[modified][flush]= invalid;

		//invalid
		this.next[invalid][read_miss] = exclusive;

	}
}
