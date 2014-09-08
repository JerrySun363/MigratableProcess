/**
 * 
 */
package manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

import nodes.MasterNode;

/**
 * ProcessManager serves as the running side to accept starting command and also
 * take the start command.
 * 
 * @author Nicolas Yu
 * @author Jerry Sun
 * 
 */
public class ProcessManager {
	private MasterNode masterNode;
	Scanner scanner;

	public ProcessManager() {
		MasterNode m = new MasterNode();
		Thread thread = new Thread(m);
		thread.start();
		this.setMasterNode(m);
		this.scanner = new Scanner(System.in);
	}

	public ProcessManager(int portNum) {
		MasterNode m = new MasterNode(portNum);
		Thread thread = new Thread(m);
		thread.start();
		this.setMasterNode(m);
		this.scanner = new Scanner(System.in);

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

		pm.acceptCommand();

	}

	public MasterNode getMasterNode() {
		return masterNode;
	}

	public void setMasterNode(MasterNode masterNode) {
		this.masterNode = masterNode;
	}

	public void acceptCommand() {
		String input = "";
		this.printUsageMessage();
		while (true) {
			input = scanner.nextLine();
			handleCommand(input);
		}

	}

	@SuppressWarnings("unchecked")
	private void handleCommand(String input) {
		if (input == null || input.trim().isEmpty()) {
			this.printUsageMessage();
		}

		int pid;
		String[] args = input.split("\\W");
		switch (args[0]) {
		case "exit":
			this.getMasterNode().disconnect();
			System.out.println("Byebye!");
			System.exit(0);
		case "launch":
			Class<MigratableProcess> migratableProcessClass;
			try {
				migratableProcessClass = (Class<MigratableProcess>) Class
						.forName(args[1]);
				Constructor<MigratableProcess> processConstructor = migratableProcessClass
						.getConstructor(String[].class);
				String[] parameters = new String[args.length - 2];
				for (int i = 0; i < parameters.length; i++) {
					parameters[i] = args[2 + i];
				}
				pid = this.getMasterNode().launchProcess(
						processConstructor.newInstance((Object) parameters));
				/*
				 * boolean isSuccess = false;
				 * 
				 * isSuccess = this.masterNode.checkLaunch(pid);
				 * 
				 * if (isSuccess) { System.out
				 * .println("Successful Running the Process!\n PID is " + pid);
				 * } else { System.out.println("Fail to create the process!"); }
				 */

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.out.println("Fail to create the process!");
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				System.out.println("Fail to create the process!");
			} catch (SecurityException e) {
				e.printStackTrace();
				System.out.println("Fail to create the process!");
			} catch (InstantiationException e) {
				e.printStackTrace();
				System.out.println("Fail to create the process!");
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				System.out.println("Fail to create the process!");
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				System.out.println("Fail to create the process!");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				System.out.println("Fail to create the process!");
			}
			return;
		case "migrate":
			if (args.length != 2) {
				System.out.println("Please input valid pid");
			}
			try {
				pid = Integer.parseInt(args[1]);
				this.masterNode.migrate(pid);
				/*
				 * boolean isSuccess = this.masterNode.checkMigrating(pid); if
				 * (isSuccess) { System.out
				 * .println("Successfully migrate the process with pid " + pid);
				 * } else {
				 * System.out.println("Fail to migrate the process with pid " +
				 * pid); }
				 */

				return;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("We have met an execption!");
				return;
			}

		case "remove":
			if (args.length != 2) {
				System.out.println("Please input valid pid");
			}
			try {
				pid = Integer.parseInt(args[1]);
				masterNode.remove(pid);
			} catch (Exception e) {
				System.out.println("We have met an unexpected exception!");
				return;
			}
		case "status":
			this.masterNode.pullInformation();
			this.masterNode.printStatusMessages();
			break;
		default:
			System.out.println("Unrecognized command!");
			this.printUsageMessage();
		}
	}

	private void printUsageMessage() {
		System.out.println("Usage:");
		System.out.println("launch <ProcessName>");
		System.out.println("migrate <pid>");
		System.out.println("remove <pid>");
		System.out.println("status");
		System.out.println("exit");
	}
}
