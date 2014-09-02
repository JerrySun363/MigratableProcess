/**
 * 
 */
package nodes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import manager.MigratableProcess;

/**
 * @author Nicolas_Yu
 *
 */
public class MasterNode implements Runnable, MasterNodeInterface{

	private int PID = 0;
	private int portNum;
	private static int DEFAULT_PORT = 15640;
	private ServerSocket listener;
	private boolean isRun = false;
	
	public MasterNode() {
		this(DEFAULT_PORT);
	}
	
	
	
	public MasterNode(int portNum) {
		this.portNum = portNum;
		try {
			this.listener = new ServerSocket(portNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while (isRun) {
			try {
				Socket socket = listener.accept();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			// polling information from all the slave nodes
			//TODO
		}
	}

	public int launchProcess(MigratableProcess process) {
		return 1;
	}
	
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	public boolean migrate(int pid) {
		return true;
		// TODO Auto-generated method stub
		
	}

	public boolean remove(int pid) {
		// TODO Auto-generated method stub
		return true;
	}
	
}
