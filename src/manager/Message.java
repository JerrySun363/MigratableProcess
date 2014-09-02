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
	
	
	public Message (int PID, String type, int slaveId) {
		this.pid = PID;
		this.type = type;
		this.slaveId = slaveId;
	}


	public int getPid() {
		return pid;
	}


	public void setPid(int pid) {
		this.pid = pid;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public int getSlaveId() {
		return slaveId;
	}


	public void setSlaveId(int slaveId) {
		this.slaveId = slaveId;
	}
}
