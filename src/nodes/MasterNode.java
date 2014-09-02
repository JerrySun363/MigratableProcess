/**
 * 
 */
package nodes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import manager.MigratableProcess;
import manager.ProcessManager;

/**
 * @author Nicolas_Yu
 *
 */
public class MasterNode implements Runnable{

	private int PID = 0;
	private int portNum = 15640;
	private ServerSocket listener;
	private boolean isRun = false;
	ProcessManager pm;
	
	public MasterNode(ProcessManager pm) {
		this.pm = pm;
		try {
			this.listener = new ServerSocket(portNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int launchProcess(MigratableProcess process) {
		return 0;
	}
	
    public void disconnect() {
		
		List<Socket> socketList = null;
		for (Socket slaveSocket : socketList) {
			try {
				slaveSocket.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
    
    public void migrate() {
    	
    }  
    
    public void remove() {
    	
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
	
}
