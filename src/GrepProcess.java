
/**
 * GrepProcess is one test case for the Migratable Process. 
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

import manager.MigratableProcess;

public class GrepProcess implements MigratableProcess
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8308005755247149213L;
	private TransactionalFileInputStream  inFile;
	private TransactionalFileOutputStream outFile;
	private String query;

	private volatile boolean suspending;

	public GrepProcess(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		inFile = new TransactionalFileInputStream(args[1]);
		outFile = new TransactionalFileOutputStream(args[2], false);
	}

	public void run()
	{
		PrintStream out = new PrintStream(outFile);
		DataInputStream in = new DataInputStream(inFile);

		try {
			while (!suspending) {
				String line = in.readLine();
				System.out.println(line);
				if (line == null) break;
				
				if (line.contains(query)) {
					out.println(line);
				}
				
				// Make grep take longer so that we don't require extremely large files for interesting results
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		} catch (EOFException e) {
			//End of File
		} catch (IOException e) {
			System.out.println ("GrepProcess: Error: " + e);
		}


		suspending = false;
	}

	
	public void suspend()
	{
		this.inFile.setMigrated(true);
		this.outFile.setMigrated(true);
		suspending = true;
		while (suspending);
	}

	/**
	 * This methods tries to give information about the current running program.
	 */
	@Override
	public String toSring() {
		String inputInfo = "Input Info: "+ inFile.toString();
		String outputInfo = "Output Info: " + outFile.toString();
		return inputInfo+"\n"+ outputInfo;
	}
	
	/**
	 * This methods tries to resume the program.
	 */
	@Override
	public void resume() {
		this.inFile.setMigrated(false);
		this.outFile.setMigrated(false);
		suspending = false;
	}

}
