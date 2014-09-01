import java.io.Serializable;

/**
 * 
 */

/**
 * @author Nicolas_Yu
 *
 */
// it can be run via a java.lang.Thread object, it can be serialized and written to or read from a stream 
public interface MigratableProcess extends Runnable, Serializable{
	
	//This method affords an opportunity for the process to enter a known safe state.
	public void suspend();

	//print the class name of the process as well as the original set of arguments with which it was called.
	public String toSring();
	//TODO  override 
	
}


