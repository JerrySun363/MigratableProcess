/**
 * 
 */
package manager;

import java.net.Socket;
import java.util.List;

/**
 * @author Nicolas_Yu
 *
 */
public class ProcessManager {
	
	
	
	private MigratableProcess launchProcess() {
		return null;
	}
	
	
	public void disconnect() {
		
		List<Socket> socketList = null;
		for (Socket slaveSocket : socketList) {
			try {
				slaveSocket.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
}
