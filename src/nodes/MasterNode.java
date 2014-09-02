/**
 * 
 */
package nodes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import manager.ProcessManager;

/**
 * @author Nicolas_Yu
 *
 */
public class MasterNode implements Runnable{

	private int portNum = 15640;
	private ServerSocket listener;
	private boolean isRun = false;
	
	public MasterNode() {
		try {
			this.listener = new ServerSocket(portNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MasterNode(int portNum2) {
		// TODO Auto-generated constructor stub
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
			
		}
	}
	
}
