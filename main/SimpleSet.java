/*
 * 
 * This implements a simple form of the set. Gives O(n) addition and also defines the suboperator to 
 * simulate A|B
 * 
 */

package main;

import java.util.Iterator;
import java.util.Vector;

public class SimpleSet<T> {

	public Vector<T> core;
	private static boolean verbose = false;
	
	public SimpleSet(){
		core = new Vector<T>();
	}

	public void add(T element){
		if(!core.contains(element))
			core.add(element);
	}
	

	public int indexOf(T element){
		return core.indexOf(element);
	}
	
	public T get(int index){
		return core.get(index);
	}
	
	public boolean contains(T element){
		return core.contains(element);
	}
	
	public int size(){
		return core.size();
	}
	
	public void removeElementsIn(SimpleSet<T> that){
		for(int i=0 ; i < core.size(); ++i){
			if(verbose)System.out.println("In simple set:"+core.get(i));
			if(verbose)System.out.println("In simple set:" + that.contains(core.get(i)));
			
			if(that.contains(core.get(i))){
				if(verbose)System.out.println(core.get(i));
				core.remove(i);
				--i;
			}
		}
	}
	
	public void remove(T obj){
		core.remove(obj);
	}
	
	public String toString(){
		return core.toString();
	}
	
	public Iterator<T> iterator(){
		return core.iterator();
	}
}
