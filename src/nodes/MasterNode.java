/**
 * 
 */
package nodes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import manager.Message;
import manager.MigratableProcess;


/**
 * @author Nicolas_Yu
 *
 */
public class MasterNode implements Runnable {
	private int PID = 0;
	private static int DEFAULT_PORT = 15640;
	private ServerSocket serverSocket;
	private boolean isRun = false;
	private HashSet<Integer> slaveIds;
	private LinkedList<Socket> socketList;
	private HashMap<Integer, Socket> slaveSocketMap;
	private HashMap<Integer, Integer> PIDSlaveMap;
	private HashMap<Integer, Integer> slaveLoadMap;
	
	//record the intermediate status for different operations
	private volatile HashSet<Integer> launching; 
	private volatile HashSet<Integer> migrating;
	private volatile HashSet<Integer> removing;
	
	
	public MasterNode(){
		this(DEFAULT_PORT);
	}
	
	public MasterNode(int portNum) {
		try {
			this.serverSocket = new ServerSocket(portNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.isRun = true;
		this.slaveIds = new HashSet<Integer>();
		this.socketList = new LinkedList<Socket>();
		this.slaveSocketMap = new HashMap<Integer, Socket>();
		this.PIDSlaveMap = new HashMap<Integer, Integer>();
		this.slaveLoadMap = new HashMap<Integer, Integer>();
	}
	
	/*
	 * launch a new process for ProcessManager
	 */
	public int launchProcess(MigratableProcess process) {
		int slaveId = chooseBestSlave();
		Message launchMessage = new Message(PID, "launch", slaveId, process);
		sendMsgToSlave(launchMessage, slaveId);
		PID++;
		return PID-1;
	}
	
    /*
     *  migrate process from one slaveNode to another slaveNode
     */
    public void migrate(int PID) {
    	//suspend the process in the original slaveNode
    	int originalSlaveId = PIDSlaveMap.get(PID);
    	Message suspendMessage = new Message(PID, "suspend&migrate", originalSlaveId);
    	sendMsgToSlave(suspendMessage, originalSlaveId);
    }  
    
    
    /*
     *  remove one process using PID
     */
    public void remove(int PID) {
    	int slaveId = PIDSlaveMap.get(PID);
    	Message removeMessage = new Message(PID, "remove", slaveId);
    	sendMsgToSlave(removeMessage, slaveId);
    }
    
    
    /*
     *  send message to slaveNode to do launch/migrate/remove/suspend
     */
    public void sendMsgToSlave(Message message, int slaveId) {
    	Socket slaveSocket = slaveSocketMap.get(slaveId);
		try {
			ObjectOutputStream objectOut = new ObjectOutputStream(slaveSocket.getOutputStream());
			objectOut.writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void disconnect() {
		
		for (Socket slaveSocket : socketList) {
			try {
				slaveSocket.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while (isRun) {
			int slaveId = 0;
			try {
				Socket socket = serverSocket.accept();
				slaveIds.add(slaveId);
				new listener(socket, slaveId++).start();
				socketList.add(socket);
				slaveSocketMap.put(slaveId, socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// polling information from all the slave nodes
			//TODO
		}
	}
	
	
	public void excuteMasterJob(Message message) {
		switch (message.getType()) {

		case "launch":
			break;

		case "migrate":
			int migratePID = message.getPid();
			MigratableProcess migratedProcess = message.getProcess();
			//migratedProcess.suspend();
			int slaveId = chooseBestSlave();
	    	Message migrateMessage = new Message(migratePID, "migrate", slaveId, migratedProcess);
	    	sendMsgToSlave(migrateMessage, slaveId);

			break;

		case "suspend":
			break;

		case "remove":
			break;
		
		// Sunchen can print any success information if he want
		case "launchSuccess":
			
		    break;
		
		case "migrateSuccess":
			
			break;
	    
		case "removeSuccess":
			
			break;

		default:
			break;
		}
	}
	
	/*
	 * choose the slave node with least load
	 */
	private int chooseBestSlave() {
		int slaveId = 0;
		int load = slaveLoadMap.get(0);
		for (int id : slaveLoadMap.keySet()) {
			if (slaveLoadMap.get(id) < load) {
				slaveId = id;
			}
		}
		return slaveId;
	}
	
	private class listener extends Thread {
		private Socket socket;
		private int slaveId;
		
		public listener(Socket socket, int slaveId) {
			this.socket = socket;
			this.slaveId = slaveId;
			log("New connection with client# " + slaveId + " at " + socket);
		}
		
		public void run() {
			try {
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				
				Object object = null;
				Message message = null;
				while (true) {
					object = in.readObject();
					
					if (!(object instanceof Message)) {
						continue;
					}
					message = (Message)object;
					MasterNode.this.excuteMasterJob(message); // TODO 
				}
			} catch (Exception e) {
				log("Error handling client# " + slaveId + ": " + e);
			} finally {
				try {
					socket.close();
				} catch (Exception e2) {
					log("Couldn't close a socket, what's going on?");
				}
				log("Connection with client# " + slaveId + " closed");
			}
		}
		
		
		private void log(String info) {
			System.out.println(info);
		}
		
	}
	
}
