/**
 * 
 */
package manager;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * This is the Message used to pass information between slave and master nodes.
 * A message contains pid(ProcessId), slaveId(the id of slave node), type (the
 * message type), process(Migrtable Process), a list of runningPIDs (used for
 * pulling information);
 * 
 * 
 * @author Nicolas_Yu
 * 
 */
public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -288312225466865055L;
	private int pid;
	private int slaveId;
	private String type;
	private MigratableProcess process;
	private LinkedList<Integer> runningPIDs;

	public Message(int PID, String type, int slaveId) {
		this.pid = PID;
		this.type = type;
		this.slaveId = slaveId;
	}

	public Message(int PID, String type, int slaveId, MigratableProcess process) {
		this.pid = PID;
		this.type = type;
		this.slaveId = slaveId;
		this.process = process;
	}

	/*
	 * pulling message constructor
	 */
	public Message(String type, LinkedList<Integer> runnningPIDs) {
		this.type = "pulling";
		this.runningPIDs = runnningPIDs;
	}

	public Message(String type) {
		this.type = "pulling";
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getSlaveId() {
		return slaveId;
	}

	public void setSlaveId(int slaveId) {
		this.slaveId = slaveId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public MigratableProcess getProcess() {
		return process;
	}

	public void setProcess(MigratableProcess process) {
		this.process = process;
	}

	public LinkedList<Integer> getRunningPIDs() {
		return runningPIDs;
	}

	public void setRunningPIDs(LinkedList<Integer> runningPIDs) {
		this.runningPIDs = runningPIDs;
	}

}
