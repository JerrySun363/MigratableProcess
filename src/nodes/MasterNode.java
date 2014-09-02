/**
 * 
 */
package nodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

//import listenServer.listener;
import manager.MigratableProcess;

/**
 * @author Nicolas_Yu
 *
 */
public class MasterNode implements Runnable{

	private int PID = 0;
	private int portNum = 15640;
	private ServerSocket serverSocket;
	private boolean isRun = false;
	private HashSet<Integer> slaveIds;
	private HashMap<Integer, Socket> slaveSocketMap;
	private HashMap<Integer, Integer> PIDSlaveMap;
	private HashMap<Integer, Integer> slaveLoadMap;
	
	
	public MasterNode() {
		try {
			this.serverSocket = new ServerSocket(portNum);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int launchProcess(MigratableProcess process) {
		
		PID++;
		return 0;
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
    
    public void migrate() {
    	
    }  
    
    public void remove() {
    	
    }
	
	
	public MasterNode(int portNum2) {
		// TODO Auto-generated constructor stub
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
				new listener(socket, slaveId++).start();
				slaveSocketMap.put(slaveId, socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// polling information from all the slave nodes
			//TODO
		}
	}
	
	private static class listener extends Thread {
		private Socket socket;
		private int slaveId;
		
		public listener(Socket socket, int slaveId) {
			this.socket = socket;
			this.slaveId = slaveId;
			log("New connection with client# " + slaveId + " at " + socket);
		}
		
		public void run() {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				while (true) {
					String input = in.readLine();
					
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
