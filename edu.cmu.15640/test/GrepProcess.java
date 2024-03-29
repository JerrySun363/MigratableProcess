package test;

/**
 * GrepProcess is one test case for the Migratable Process. 
 * It involves Input and Output of transactional file input/output.
 * 
 * @author Nicolas Yu
 * @author Chen Sun
 */
import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.HashMap;

import utility.TransactionalFileInputStream;
import utility.TransactionalFileOutputStream;
import nodes.SlaveNode;
import manager.MigratableProcess;

public class GrepProcess implements MigratableProcess {
	private static final long serialVersionUID = 8308005755247149213L;
	private TransactionalFileInputStream inFile;
	private TransactionalFileOutputStream outFile;
	private String query;
	private String[] args;
	private volatile boolean suspending;

	public GrepProcess(String args[]) throws Exception {
		if (args.length != 3) {
			System.out
					.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		this.args = args;
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2], false);
	}

	public void run() {
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			while (!suspending) {
				String line = in.readLine();
				System.out.println(line);
				if (line == null)
					break;

				if (line.contains(query)) {
					out.println(line);
				}

				// Make grep take longer so that we don't require extremely
				// large files for interesting results
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					out.close();
					in.close();
					return;

				}
			}
		} catch (EOFException e) {
			// End of File
			try {
				in.close();
				out.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			}

		} catch (IOException e) {
			System.out.println("GrepProcess: Error: " + e);
		}

		suspending = false;
		terminate();
	}

	/**
	 * Suspend the current process.
	 */
	public void suspend() {
		this.inFile.setMigrated(true);
		this.outFile.setMigrated(true);
		suspending = true;
		while (suspending)
			;
	}

	/**
	 * Provide information for current process.
	 */
	@Override
	public String toSring() {
		String name = this.getClass().getName();
		String parameter = "parameters:";
		for (String arg : this.args) {
			parameter += " " + arg;
		}
		String inputInfo = "Input Info: " + inFile.getFilename();
		String outputInfo = "Output Info: " + outFile.getFilename();
		return inputInfo + "\n" + outputInfo;
	}

	/**
	 * This methods tries to resume the program.
	 */
	@Override
	public void resume() {
		suspending = false;
	}

	/**
	 * 
	 * 
	 * @throws InterruptedException
	 * @see manager.MigratableProcess#terminate()
	 */
	@Override
	public void terminate() {
		SlaveNode instance = SlaveNode.getInstance();
		HashMap<Thread, Integer> referHashMap = SlaveNode.getInstance()
				.getThreadPIDMap();
		instance.getRunningPIDs().remove(
				referHashMap.get(Thread.currentThread()));

	}

}
