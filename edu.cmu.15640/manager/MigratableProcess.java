package manager;
import java.io.Serializable;

/**
 * This class provides the basic interface methods for any MigrataleProcess 
 * 
 * @author Nicolas Yu
 * @author Jerry Sun
 */
// it can be run via a java.lang.Thread object, it can be serialized and written to or read from a stream 
public interface MigratableProcess extends Runnable, Serializable{
	
	//This method offers an opportunity for the process to enter a known safe state.
	public void suspend();
	
	//This method offers an opportunity for the process to quit the previous safe state and be able to run again.
	public void resume();

	//This method offers an opportunity to terminate the process and notify masterNode to remove running PID
	public void terminate();
	
	//print the class name of the process as well as the original set of arguments with which it was called.
	public String toSring();
	//TODO  override 
	
}


