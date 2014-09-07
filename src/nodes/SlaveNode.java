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
import java.util.HashSet;
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
	
	private HashSet<Integer> runningPIDs;
	private HashMap<Integer, MigratableProcess> PIDProcessMap;
	private HashMap<Integer, Thread> PIDThreadMap;
	
	public SlaveNode(String mHost, int mPort) {
		this.masterHost = mHost;
		this.masterPort = mPort;	
		
		this.isRun = true;
		
		this.runningPIDs = new HashSet<Integer>();
		this.PIDProcessMap = new HashMap<Integer, MigratableProcess>();
		this.PIDThreadMap = new HashMap<Integer, Thread>();
		
		try {
			this.socket = new Socket(masterHost, masterPort);
			this.objectIn = new ObjectInputStream(this.socket.getInputStream());
			this.objectOut = new ObjectOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * send message back to MaterNode if needed
	 */
	
	public void sendMsgToMaster(Message message) {
		try {
			System.out.println("SlaveNode SendMsgToMater: " + message.getType() + " ");
			synchronized (objectOut) {
				objectOut.writeObject(message);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 *  launchSlaveNode masterHost masterPort
	 */
	
	public static void main(String[] argv) {
		if (argv.length != 3) {
			System.out.println("Usage: launchSlaveNode masterHost masterPort");
		} else {
			String masterHost = argv[1];
			int masterPort = Integer.valueOf(argv[2]);
			
			SlaveNode slaveNode = new SlaveNode(masterHost, masterPort);
			
			System.out.println("SlaveNode begins to run");
			
			while (slaveNode.isRun) {
				
				Message message = null;

				Object object;
				try {
					object = slaveNode.objectIn.readObject();
					if (!(object instanceof Message)) {
						continue;
					}
					message = (Message) object;
					slaveNode.excuteJob(message);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					// When socket disconnect, we assume that master exits, then we shut down the slaveNode
					System.exit(0);
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
				System.out.println("SlaveNode receive message from masterNode: lauch process with PID: " +  message.getPid());
				int launchPID = message.getPid();
				MigratableProcess newProcess = message.getProcess();
				Thread newThread = new Thread(newProcess);
				newThread.start();
				
				runningPIDs.add(launchPID);
				PIDProcessMap.put(launchPID, newProcess);
				PIDThreadMap.put(launchPID, newThread);
				
				Message launchSuccessMessage = new Message(launchPID, "launchSuccess", -1);
				sendMsgToMaster(launchSuccessMessage);
				
				break;
		
		case "suspend&migrate":
				System.out.println("SlaveNode receive message from masterNode: suspend and migrate process with PID: " +  message.getPid());
			    int suspendPID = message.getPid();
			    MigratableProcess suspendProcess = PIDProcessMap.get(suspendPID);
			    suspendProcess.suspend();
			    runningPIDs.remove(suspendPID);
			    Message migrateMessage = new Message(suspendPID, "migrate", 0, suspendProcess);
			    sendMsgToMaster(migrateMessage);
			    
			    break;
				
		case "migrate":
				System.out.println("SlaveNode receive message from masterNode: migrate process with PID: " +  message.getPid());
				int migratePID = message.getPid();
				MigratableProcess migratedProcess = message.getProcess();
				migratedProcess.resume();
				Thread migrateThread = new Thread(migratedProcess); 
				migrateThread.start();
				runningPIDs.add(migratePID);
				PIDProcessMap.put(migratePID, migratedProcess);
				PIDThreadMap.put(migratePID, migrateThread);
				
				Message migrateSuccessMessage = new Message(migratePID, "migrateSuccess", -1);
				sendMsgToMaster(migrateSuccessMessage);

				break;
				
		case "suspend":

				break;
				
		case "remove":
				System.out.println("SlaveNode receive message from masterNode: remove process with PID: " +  message.getPid());
			    int pid = message.getPid();
			    Thread removedThread = PIDThreadMap.get(pid);
				removedThread.interrupt();
				runningPIDs.remove(pid);
				PIDProcessMap.remove(pid);
				PIDThreadMap.remove(pid);
				
				Message removeMessage = new Message(pid, "removeSuccess", -1);
				sendMsgToMaster(removeMessage);
				break;
		
		case "pulling":
			
				System.out.println("SlaveNode receive message from masterNode: pulling information");
				LinkedList<Integer> runningPIDLists = new LinkedList<Integer>();
				for (Integer a : this.runningPIDs) {
					runningPIDLists.add(a);
				}
				Message pullingMessage = new Message("pulling", runningPIDLists);
				sendMsgToMaster(pullingMessage);
				
				break;

		default:
			break;
		}
	}
	
}
