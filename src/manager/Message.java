/**
 * 
 */
package manager;

import java.io.Serializable;
import java.util.LinkedList;

/**
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
	private boolean isSuccess;
	private LinkedList<Integer> runningPIDs;
	
	public Message (int PID, String type, int slaveId) {
		this.pid = PID;
		this.type = type;
		this.slaveId = slaveId;
	}
	
	public Message (int PID, String type, int slaveId, MigratableProcess process) {
		this.pid = PID;
		this.type = type;
		this.slaveId = slaveId;
		this.process = process;
	}
	 
	/*
	 * pulling message constructor
	 */
	public Message (String type, LinkedList<Integer> runnningPIDs) {
		this.type = "pulling";
		this.runningPIDs = runnningPIDs;
	}

	public Message (String type) {
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


	/**
	 * @return the isSuccess
	 */
	public boolean isSuccess() {
		return isSuccess;
	}


	/**
	 * @param isSuccess the isSuccess to set
	 */
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public LinkedList<Integer> getRunningPIDs() {
		return runningPIDs;
	}

	public void setRunningPIDs(LinkedList<Integer> runningPIDs) {
		this.runningPIDs = runningPIDs;
	}

}
