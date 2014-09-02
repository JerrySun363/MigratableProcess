/**
 * 
 */
package nodes;

import manager.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
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
	
	private volatile LinkedList<Integer> runningPIDs;
	private HashMap<Integer, MigratableProcess> PIDProcessMap;
	private HashMap<Integer, Thread> PIDThreadMap;
	
	
	public SlaveNode(String mHost, int mPort) {
		this.masterHost = mHost;
		this.masterPort = mPort;	
	}
	
	
	public void sendMsgToMaster(Message message) {
		try {
			synchronized (objectOut) {
				objectOut.writeObject(message);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
					message = (Message) object;
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
	 * lunch/migrate/suspend/resume/terminate the specified process
	 */
	private void excuteJob(Message message) {
		switch (message.getType()) {
		case "launch":
				MigratableProcess newProcess = message.getProcess();
				Thread newThread = new Thread();
				newThread.start();
				newProcess.resume();
				break;
		case "migrate":
				MigratableProcess migratedProcess = message.getProcess();
				migratedProcess.suspend();
				Thread migrateThread = new Thread(); 
				migrateThread.start();
				migratedProcess.resume();
				break;
		case "remove":
			    int pid = message.getPid();
			    Thread removedThread = PIDThreadMap.get(pid);
				removedThread.interrupt();
				break;

		default:
			break;
		}
	}
	
}
