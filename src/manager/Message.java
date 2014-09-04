/**
 * 
 */
package manager;

/**
 * @author Nicolas_Yu
 *
 */
public class Message {
	
	
	private int pid;
	private int slaveId;
	private String type;
	private MigratableProcess process;
	private boolean isSuccess;
	
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

}
