package main;
/*
 * Every block needs to maintain a list of the pointers pointing to it so that 
 * they can be updated whenever the partition gets integrated with some other partition
 */
public class CpBlockPointer{
	private CpBlock block;
	private int event;
	public CpBlockPointer(CpBlock block, int event) {
		this.block = block;
		this.event = event;
	}
	public CpBlock getBlock() {
		return block;
	}
	public void setBlock(CpBlock block) {
		this.block = block;
	}
	public int getEvent() {
		return event;
	}
	public void setEvent(int event) {
		this.event = event;
	}
}
