package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;


public class FileUtility {
	public static Vector<DFA> getBenchmarkFSMs() {
	    File folder = new File("testCases");
	    File[] listOfFiles = folder.listFiles();
	    Vector<DFA> dfaCollection = new Vector<DFA>(); 
	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	    ReadFileLinebyLine parser = new ReadFileLinebyLine();
					parser.parse(dfaCollection,listOfFiles[i]);
	      } else if (listOfFiles[i].isDirectory()) {
//	        System.out.println("Directory " + listOfFiles[i].getName());
	      }
	    }
	    return dfaCollection; 
	  }
}
