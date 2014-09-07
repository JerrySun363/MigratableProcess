/**
 * 
 */
package nodes;

import java.io.ObjectInputStream;
import java.net.Socket;

import manager.Message;

/**
 * @author Nicolas_Yu
 *
 */
public class ListenerForSlave extends Thread {
	private Socket socket;
	private int slaveId;
	private MasterNode masterNode;
	
	public ListenerForSlave(Socket socket, int slaveId, MasterNode masterNode) {
		this.socket = socket;
		this.slaveId = slaveId;
		this.masterNode = masterNode;
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
				masterNode.excuteMasterJob(message, slaveId); 
			}
		} catch (Exception e) {
			log("Error handling client# " + slaveId + ": " + e);
			// update socket map when socket is closed 
			masterNode.getSlaveSocketMap().remove(slaveId);
			masterNode.getSlaveIds().remove(slaveId);
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
