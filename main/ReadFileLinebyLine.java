package main;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;


public class ReadFileLinebyLine {

/*  public static void main(String... aArgs) throws FileNotFoundException {
    ReadWithScanner parser = new ReadWithScanner();
    parser.processLineByLine(File fFile);
    log("Done.");
  }
*/  
  /**
   Constructor.
   @param aFileName full name of an existing, readable file.
  */
  
  /** Template method that calls {@link #processLine(String)}.  */
  int numberOfEventBits; 
/*  public final void processLineByLine(Vector<DFA> dfaCollection, File fFile) throws FileNotFoundException {
    //Note that FileReader is used, not File, since File is not Closeable
    Scanner scanner = new Scanner(new FileReader(fFile));
    DFA machine = new DFA();
    machine.setName(fFile.getName());
    try {
      //first use a Scanner to get each line
      while ( scanner.hasNextLine() ){
    	genFSM(machine, scanner.nextLine());
        //processLine( scanner.nextLine() );
      }
      dfaCollection.add(machine);
    }
    finally {
      //ensure the underlying stream is always closed
      //this only has any effect if the item passed to the Scanner
      //constructor implements Closeable (which it does in this case).
      scanner.close();
    }
  }
*/  public final void parse(Vector<DFA> dfaCollection, File fFile){
	    DFA machine = new DFA();
	    machine.setName(fFile.getName());
	  try{
		    // Open the file that is the first 
		    // command line parameter
		    FileInputStream fstream = new FileInputStream(fFile);
		    // Get the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		        BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    //Read File Line By Line
		    while ((strLine = br.readLine()) != null)   {
		      // Print the content on the console
		    	genFSM(machine, strLine);
		    }
		    //Close the input stream
		      dfaCollection.add(machine);
		    in.close();
		    }catch (Exception e){//Catch exception if any
		      System.err.println("Error: " + e.getMessage());
		    }
  }
  protected void genFSM(DFA machine, String aLine){
	if(aLine.isEmpty())
		return; 
	StringTokenizer st = new StringTokenizer(aLine);
	String firstString = st.nextToken();
//	System.out.println("first String:"+firstString);
	if(firstString.equals(".s")){
		machine.nbStates = Integer.parseInt(st.nextToken());
		if((machine.nbStates > 0) && (machine.nbLetters >0)){
			machine.next = new int[machine.nbStates][machine.nbLetters];
			for(int i=0; i < machine.nbStates; ++i)
				for(int event = 0; event < machine.nbLetters; ++event)
					machine.next[i][event] = i;
		}
	}else 
	if(firstString.equals(".i")){
		numberOfEventBits = Integer.parseInt(st.nextToken());
		machine.nbLetters = (int)Math.pow(2,numberOfEventBits);
		if((machine.nbStates > 0) && (machine.nbLetters >0)){
			machine.next = new int[machine.nbStates][machine.nbLetters];
			for(int i=0; i < machine.nbStates; ++i)
				for(int event = 0; event < machine.nbLetters; ++event)
					machine.next[i][event] = i;
		}
	}else
	if((firstString.charAt(0) == '0')||(firstString.charAt(0) == '1')||(firstString.charAt(0) == '-')){
		int currentState = Integer.parseInt(st.nextToken());
		int nextState = Integer.parseInt(st.nextToken());
		for(int event=0; event < machine.nbLetters;++event){
			String strNum = specialString(numberOfEventBits,event);
			boolean match = true; 
			for(int j=0; j < strNum.length();++j){
				if((firstString.charAt(j)!= '-') && (firstString.charAt(j)!=strNum.charAt(j))){
					match = false;
					break; 
				}
			}
			if(match){
				machine.next[currentState][event] = nextState;  
			}
		}
	}
  }
  
  //converts the string with leading zeroes added...
  private String specialString(int outputLength,int num){
	  String out;
	  String format= "%0"+outputLength+"d";
	  out = String.format(format, Integer.parseInt(Integer.toBinaryString(num))); 
	  return out; 
  }
} 
