package main;

public class EventBasedStuff {

	public static CpPartition inconPartitionNotContainingEvent(CpPartition inputMachine,int event){
			CpPartition inconsistentMachine = new CpPartition();
			for(int blockNo =0; blockNo < inputMachine.size();++blockNo){
				CpBlock block = inputMachine.get(blockNo);
				boolean alreadyAdded = false; 
				for(int i=0; i < inconsistentMachine.size();++i){
					if(block.sizeOfIntersection(inconsistentMachine.get(i))>0){
						alreadyAdded = true;
						break; 
					}
				}
				if(!alreadyAdded){
					CpBlock newBlock = new CpBlock(block.freshCopy());
					eventBasedDFS(newBlock,block, event,inconsistentMachine);
				}
			}
			return inconsistentMachine;
	}
	
	public static void eventBasedDFS(CpBlock newBlock, CpBlock oldBlock, int event, CpPartition inconsistentMachine){
		CpBlock next = oldBlock.next[event];
		if(next.sizeOfIntersection(newBlock)>0){
			inconsistentMachine.add(newBlock);
			return; 
		}else{
			newBlock.mergeBlock(next);
			eventBasedDFS(newBlock,next,event,inconsistentMachine);
		}
	}

}
