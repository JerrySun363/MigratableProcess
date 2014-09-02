/**
 * 
 */
package manager;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import nodes.MasterNode;

/**
 * ProcessManager serves as the running side to accept starting command and also
 * take the start command.
 * 
 * It also maintains the PID for each process it tries to run.
 * 
 * @author Nicolas Yu
 * @author Jerry Sun
 * 
 */
public class ProcessManager {
	private MasterNode masterNode;

	public ProcessManager() {
		this.setMasterNode(new MasterNode());
		this.getMasterNode().run();
	}

	public ProcessManager(int portNum) {
		this.setMasterNode(new MasterNode(portNum));
		this.getMasterNode().run();
	}

	public static void main(String args[]) {
		if (args.length > 1) {
			System.out
					.println("Usage: java ProcessManager or java ProcessManager <portNumer>");
			System.exit(0);
		}
		ProcessManager pm;
		if (args.length == 1) {
			int portNum = Integer.parseInt(args[0]);
			pm = new ProcessManager(portNum);
		} else {
			pm = new ProcessManager();
		}

	}

	public MasterNode getMasterNode() {
		return masterNode;
	}

	public void setMasterNode(MasterNode masterNode) {
		this.masterNode = masterNode;
	}

	public void acceptCommand() {
		Scanner scanner = new Scanner(System.in);
		String input = "";
		this.printUsageMessage();
		while (true) {
			input = scanner.nextLine();
			handleCommand(input);
		}
		
		
	}
	
	private void handleCommand(String input){
		if(input == null || input.trim().isEmpty()){
			this.printUsageMessage();
		}
	}
	
	private void printUsageMessage(){
		System.out.println("Usage:");
		System.out.println("-launch <ProcessName>");
		System.out.println("-migrate <pid>");
		System.out.println("-remove <pid>");
		System.out.println("exit");
	}
}
