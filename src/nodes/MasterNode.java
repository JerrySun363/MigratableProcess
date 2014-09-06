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

	// record the intermediate status for different operations
	private volatile HashSet<Integer> launching;
	private volatile HashSet<Integer> migrating;
	private volatile HashSet<Integer> removing;

	// record the pulling information of runningPID and corresponding slaveNode
	private Set<Integer> runningPID;
	private Map<Integer, Integer> runningPIDSlaveMap;
	
	private static int RETRY = 5;
	private static int SLEEP = 1000;
	
	private int pullingNum = 0;

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
		
		this.launching = new HashSet<Integer>();
		this.migrating = new HashSet<Integer>();
		this.removing = new HashSet<Integer>();
		
	}

	/**
	 * launch a new process for ProcessManager
	 */
	public int launchProcess(MigratableProcess process) {
		int slaveId = chooseBestSlave();
		Message launchMessage = new Message(PID, "launch", slaveId, process);
		sendMsgToSlave(launchMessage, slaveId);
		this.launching.add(PID);
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
		int originalSlaveId = PIDSlaveMap.get(PID);
		Message suspendMessage = new Message(PID, "suspend&migrate",
				originalSlaveId);
		sendMsgToSlave(suspendMessage, originalSlaveId);
		this.migrating.add(PID);
	}

	/**
	 * remove one process using PID
	 * 
	 * @param PID
	 *            the PID to be removed
	 */
	public void remove(int PID) {
		int slaveId = PIDSlaveMap.get(PID);
		Message removeMessage = new Message(PID, "remove", slaveId);
		sendMsgToSlave(removeMessage, slaveId);
		this.removing.add(PID);
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
		Socket slaveSocket = slaveSocketMap.get(slaveId);
		try {
			ObjectOutputStream objectOut = new ObjectOutputStream(
					slaveSocket.getOutputStream());
			objectOut.writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	@Override
	public void run() {
		int slaveId = 0;
		while (isRun) {
			try {
				Socket socket = serverSocket.accept();
				this.slaveIds.add(slaveId);

				new ListenerForSlave(socket, slaveId++, this).start();

				this.socketList.add(socket);
				this.slaveSocketMap.put(slaveId, socket);
				this.slaveLoadMap.put(slaveId, 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// polling information from all the slave nodes
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
			this.PIDSlaveMap.put(lPid, fromSlaveId);
			this.launching.remove(message.getPid());
			break;

		case "migrateSuccess":
			int mPid = message.getPid();
			this.migrating.remove(message.getPid());
			this.PIDSlaveMap.put(mPid, fromSlaveId);
			break;

		case "removeSuccess":
			int rPid = message.getPid();
			runningPID.remove(rPid);
			runningPIDSlaveMap.remove(rPid);
			this.removing.remove(message.getPid());
			break;
			
		case "pulling":
			LinkedList<Integer> runningPIDs = message.getRunningPIDs();
			for (Integer pid : runningPIDs) {
				runningPID.add(pid);
				PIDSlaveMap.put(pid, fromSlaveId);
			}
			pullingNum++;
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
		int slaveId = 1;
		int load = slaveLoadMap.get(slaveId);
		for (int id : slaveLoadMap.keySet()) {
			if (slaveLoadMap.get(id) < load) {
				slaveId = id;
			}
		}
		return slaveId;
	}

	// The functions follow check the availability
	/**
	 * Check whether the process with certain PID has been migrated
	 * successfully.
	 * 
	 * @param pid
	 *            the PID to check
	 * @return whether the migration is successful.
	 * @throws InterruptedException
	 */
	public boolean checkMigrating(int pid) throws InterruptedException {
		for (int i = 0; i < RETRY; i++) {
			if (this.migrating.contains(pid)) {
				Thread.sleep(SLEEP);
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the process with the certain PID has been launched
	 * successfully.
	 * 
	 * @param pid
	 *            the PID to check
	 * @return whether the launch is successful
	 * @throws InterruptedException
	 */
	public boolean checkLaunch(int pid) throws InterruptedException {
		for (int i = 0; i < RETRY; i++) {
			if (this.launching.contains(pid)) {
				Thread.sleep(SLEEP);
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether the process with the certain PID has been removed
	 * successfully.
	 * 
	 * @param pid
	 *            the PID to check
	 * @return whether the removing is successful
	 * @throws InterruptedException
	 */
	public boolean checkRemoving(int pid) throws InterruptedException {
		for (int i = 0; i < RETRY; i++) {
			if (this.launching.contains(pid)) {
				Thread.sleep(SLEEP);
			} else {
				return true;
			}
		}
		return false;
	}
	 
	/**
	 * update the information of all the nodes.
	 */
	public void pullInformation() {
		Message pullingMessage = new Message("pulling");
		for (int slaveId : this.slaveSocketMap.keySet()) {
			this.sendMsgToSlave(pullingMessage, slaveId);
		}
		
	}
	
	/**
	 * print the status message for all the slave nodes.
	 */
	public void printStatusMessages(){
		while (pullingNum < slaveIds.size()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		pullingNum = 0;
		System.out.println("PId\tSlaveId");
		for(int pid : this.runningPID){
			System.out.println(pid+"\t"+this.PIDSlaveMap.get(pid));
		}
	}
	

}
