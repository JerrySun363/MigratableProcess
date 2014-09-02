/**
 * 
 */
package nodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.LinkedList;

import manager.Message;

/**
 * @author Nicolas_Yu
 *
 */
public class SlaveNode {
	
	private Socket socket;
	private ObjectInputStream objectIn;
	private ObjectOutputStream objectOut;
	
	private boolean isRun = false;
	
	private String masterHost;
	private int masterPort;
	
	private volatile LinkedList<Integer> test; 
	
	
	public SlaveNode(String mHost, int mPort) {
		this.masterHost = mHost;
		this.masterPort = mPort;
		
		
	}
	
	// launchSlaveNode masterHost masterPort
	public static void main(String[] argv) {
		if (argv.length != 3) {
			System.out.println("Usage: launchSlaveNode masterHost masterPort");
		} else {
			String masterHost = argv[1];
			int masterPort = Integer.valueOf(argv[2]);
			
			SlaveNode slaveNode = new SlaveNode(masterHost, masterPort);
			slaveNode.isRun = true;
			
			try {
				slaveNode.socket = new Socket(masterHost, masterPort);
				
				slaveNode.objectIn = new ObjectInputStream(slaveNode.socket.getInputStream());
				
				//BufferedReader output = new BufferedReader(new Reader(slaveNode.socket.getOutputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
			while (slaveNode.isRun) {
				
				Message message = null;

					Object object;
					try {
						object = slaveNode.objectIn.readObject();
						if (!(object instanceof Message)) {
							continue;
						}
						message = (Message)object;
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				
				
			}
			
			
		}
		

	}
	
	/*
	 * run/migrate/suspend/resume/terminate the specified process
	 */
	private void excuteJob(Message message) {
		switch (message.getType()) {
		case "run":
			
			break;

		default:
			break;
		}
	}
	
}
