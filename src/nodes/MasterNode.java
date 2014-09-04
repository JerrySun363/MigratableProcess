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

	private static int RETRY = 5;
	private static int SLEEP = 1000;

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
		while (isRun) {
			int slaveId = 0;
			try {
				Socket socket = serverSocket.accept();
				slaveIds.add(slaveId);

				new ListenerForSlave(socket, slaveId++, this).start();

				socketList.add(socket);
				slaveSocketMap.put(slaveId, socket);
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
	public void excuteMasterJob(Message message) {
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
			this.launching.remove(message.getPid());
			break;

		case "migrateSuccess":
			this.migrating.remove(message.getPid());
			break;

		case "removeSuccess":
			this.removing.remove(message.getPid());
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

}
