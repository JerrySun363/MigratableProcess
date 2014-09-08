/**
 * 
 */
package nodes;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

	// record the pulling information of runningPID and corresponding slaveNode
	private Set<Integer> runningPID;
	private Map<Integer, Integer> runningPIDSlaveMap;

	private HashMap<Socket, ObjectOutputStream> socketObjectMap;

	public MasterNode() {
		
		this(DEFAULT_PORT);
	}

	public MasterNode(int portNum) {
		try {
			this.serverSocket = new ServerSocket(portNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.isRun = true;
		this.slaveIds = new HashSet<Integer>();
		this.socketList = new LinkedList<Socket>();
		this.slaveSocketMap = new HashMap<Integer, Socket>();
		this.PIDSlaveMap = new HashMap<Integer, Integer>();
		this.slaveLoadMap = new HashMap<Integer, Integer>();

		this.runningPID = new TreeSet<Integer>();
		this.runningPIDSlaveMap = new HashMap<Integer, Integer>();

		this.socketObjectMap = new HashMap<Socket, ObjectOutputStream>();
	}

	@Override
	public void run() {
		System.out.println("MasterNode begins to run");
		int slaveId = 0;
		while (isRun) {
			try {
				Socket socket = serverSocket.accept();
				this.slaveIds.add(slaveId);

				new ListenerForSlave(socket, slaveId, this).start();
				this.socketList.add(socket);
				this.slaveSocketMap.put(slaveId, socket);
				this.slaveLoadMap.put(slaveId, 0);
				slaveId++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// polling information from all the slave nodes
		}
	}

	/**
	 * launch a new process for ProcessManager
	 */
	public int launchProcess(MigratableProcess process) {
		System.out.println("MasterNode: launch Process "
				+ process.getClass().getName());
		int slaveId = chooseBestSlave();
		Message launchMessage = new Message(PID, "launch", slaveId, process);
		sendMsgToSlave(launchMessage, slaveId);
		PID++;
		return PID - 1;
	}

	/**
	 * migrate process from one slaveNode to another slaveNode
	 * 
	 * @param PID
	 *            the PID to be migrated
	 */
	public void migrate(int PID) {
		// suspend the process in the original slaveNode
		if(!PIDSlaveMap.containsKey(PID)){
			System.out.println("MasterNode: Migrate: PID "+ PID+" does not exist!");
			return;
		}
		int originalSlaveId = PIDSlaveMap.get(PID);
		System.out.println("MasterNode: migrate process with PID: " + PID
				+ " from " + originalSlaveId);
		Message suspendMessage = new Message(PID, "suspend&migrate",
				originalSlaveId);
		sendMsgToSlave(suspendMessage, originalSlaveId);
	}

	/**
	 * remove one process using PID
	 * 
	 * @param PID
	 *            the PID to be removed
	 */
	public void remove(int PID) {
		if(!PIDSlaveMap.containsKey(PID)){
			System.out.println("MasterNode: Remove: PID "+ PID+" does not exist!");
			return;
		}
		int slaveId = PIDSlaveMap.get(PID);
		System.out.println("MaterNode: remove process with PID: " + PID
				+ " running on slaveNode " + slaveId);
		Message removeMessage = new Message(PID, "remove", slaveId);
		sendMsgToSlave(removeMessage, slaveId);
	}

	/**
	 * send message to slaveNode to do launch/migrate/remove/suspend
	 * 
	 * @param message
	 *            the message to slave node
	 * @param slaveId
	 *            the slaveId to be sent to.
	 */
	public void sendMsgToSlave(Message message, int slaveId) {
		System.out.println("MasterNode: sendMsg To Slave " + slaveId
				+ " with messsage type of " + message.getType());
		Socket slaveSocket = slaveSocketMap.get(slaveId);
		try {
			if (socketObjectMap.containsKey(slaveSocket)) {
				ObjectOutputStream objectOut = socketObjectMap.get(slaveSocket);

				objectOut.writeObject(message);
			} else {
				ObjectOutputStream objectOut = new ObjectOutputStream(
						slaveSocket.getOutputStream());
				socketObjectMap.put(slaveSocket, objectOut);
				objectOut.writeObject(message);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * update the information of all the nodes.
	 */
	public void pullInformation() {
		System.out.println("MasterNode: pulling information from slaveNodes");
		// remove the outdated information
		this.runningPID.clear();
		this.runningPIDSlaveMap.clear();
		this.slaveLoadMap.clear();
		Message pullingMessage = new Message("pulling");
		for (int slaveId : this.slaveSocketMap.keySet()) {
			this.sendMsgToSlave(pullingMessage, slaveId);
		}

	}

	/**
	 * disconnect the sockets to all the slaveNodes
	 */
	public void disconnect() {
		for (Socket slaveSocket : socketList) {
			try {
				slaveSocket.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
 
	/**
	 * execute the master job
	 * 
	 * @param message
	 */
	public void excuteMasterJob(Message message, int fromSlaveId) {
		switch (message.getType()) {

		case "launch":
			break;

		case "migrate":
			int migratePID = message.getPid();
			MigratableProcess migratedProcess = message.getProcess();

			// update the slaveLoadMap when receive the "migrate" message
			if (!slaveLoadMap.containsKey(fromSlaveId)) {
				this.slaveLoadMap.put(fromSlaveId,
						slaveLoadMap.get(fromSlaveId) - 1);
			}
			int slaveId = chooseBestSlave();
			Message migrateMessage = new Message(migratePID, "migrate",
					slaveId, migratedProcess);
			sendMsgToSlave(migrateMessage, slaveId);
			break;

		case "suspend":
  
			break;

		case "remove":
			break;

		// Chen Sun can print any success information if he wants
		case "launchSuccess":
			int lPid = message.getPid();
			this.runningPID.add(lPid);
			this.PIDSlaveMap.put(lPid, fromSlaveId);
			if (!slaveLoadMap.containsKey(fromSlaveId)) {
				this.slaveLoadMap.put(fromSlaveId, 1);
			} else {
				this.slaveLoadMap.put(fromSlaveId,
						slaveLoadMap.get(fromSlaveId) + 1);
			}
			System.out.println("MasterNode: Launch process success with pid = "
					+ lPid);
			break;

		case "migrateSuccess":
			int mPid = message.getPid();
			this.PIDSlaveMap.put(mPid, fromSlaveId);
			if (!slaveLoadMap.containsKey(fromSlaveId)) {
				this.slaveLoadMap.put(fromSlaveId, 1);
			} else {
				this.slaveLoadMap.put(fromSlaveId,
						slaveLoadMap.get(fromSlaveId) + 1);
			}
			System.out.println("MasterNode: Migrate Process " + mPid
					+ " success!");
			break;

		case "removeSuccess":
			int rPid = message.getPid();
			runningPID.remove(rPid);
			runningPIDSlaveMap.remove(rPid);

			if (slaveLoadMap.containsKey(fromSlaveId)) {
				this.slaveLoadMap.put(fromSlaveId,
						slaveLoadMap.get(fromSlaveId) - 1);
			}
			System.out.println("MasterNode: Remove Process " + rPid
					+ " success!");
			break;

		case "pulling":
			LinkedList<Integer> runningPIDs = message.getRunningPIDs();
			for (Integer pid : runningPIDs) {
				runningPID.add(pid);
				PIDSlaveMap.put(pid, fromSlaveId);	
			}
			synchronized(this.slaveLoadMap) {
				slaveLoadMap.put(fromSlaveId, runningPIDs.size());
			}
			break;

		default:
			break;
		}
	}

	/**
	 * choose the slave node with least load
	 * 
	 * @return the slave Id to be used
	 */
	public int chooseBestSlave() {
		int slaveId = 0;
		int load = slaveLoadMap.get(slaveId);
		for (int id : slaveLoadMap.keySet()) {
			if (slaveLoadMap.get(id) < load) {
				load = slaveLoadMap.get(id);
				slaveId = id;
			}
		}
		return slaveId;
	}

	/**
	 * print the status message for all the slave nodes.
	 */
	public void printStatusMessages() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			//ignore this.
		}
		if (this.runningPID.size() == 0) {
			System.out.println("There are currently no running process!");
		} else {
			System.out.println("PId\tSlaveId");
			for (int pid : this.runningPID) {
				System.out.println(pid + "\t" + this.PIDSlaveMap.get(pid));
			}
		}

	}

	public HashMap<Integer, Socket> getSlaveSocketMap() {
		return slaveSocketMap;
	}

	public void setSlaveSocketMap(HashMap<Integer, Socket> slaveSocketMap) {
		this.slaveSocketMap = slaveSocketMap;
	}

	public HashSet<Integer> getSlaveIds() {
		return slaveIds;
	}

	public void setSlaveIds(HashSet<Integer> slaveIds) {
		this.slaveIds = slaveIds;
	}

	public HashMap<Integer, Integer> getSlaveLoadMap() {
		return slaveLoadMap;
	}

	public void setSlaveLoadMap(HashMap<Integer, Integer> slaveLoadMap) {
		this.slaveLoadMap = slaveLoadMap;
	}

}
