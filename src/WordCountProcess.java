import java.io.IOException;
import java.util.Scanner;

import manager.MigratableProcess;

/**
 * The WordCountProcess is another test case for Migratable Process including
 * Transactional Input.
 * 
 * @author Jerry Sun
 * 
 */
public class WordCountProcess implements MigratableProcess {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 1901953592955832517L;
	private int lineCount = 0;
	private int wordCount = 0;
	private boolean suspend = false;
	private TransactionalFileInputStream input;
	private String[] args;

	public WordCountProcess(String args[]) throws IOException {
		if (args == null || args.length != 1) {
			System.out.println("Usage: java WordCount <inputFile>");
			System.exit(0);
		}
		this.input = new TransactionalFileInputStream(args[0]);
		this.args = args;
	}

	@Override
	public void run() {
		Scanner scanner = new Scanner(input);
		while (!this.suspend) {
			if (scanner.hasNextLine()) {
				this.lineCount++;
				String line = scanner.nextLine();
				String[] subs = line.split("\\W");
				this.wordCount += subs.length;
			} else {
				scanner.close();
				System.out.println("Finish running.");
				System.out.println(String.format(
						"Line Count: %d, Word Count: %d", this.lineCount,
						this.wordCount));
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				scanner.close();
				return;
			}
		}

	}

	@Override
	public void suspend() {
		this.input.setMigrated(true);
		this.suspend = true;
	}

	@Override
	public void resume() {
		//this.input.setMigrated(false);
		this.suspend = false;
	}

	@Override
	public String toSring() {
		String name = this.getClass().getName();
		String arguments = "Arguments:";
		for (String arg : args) {
			arguments += " " + arg;
		}
		String stat = String.format("Already read %d lines, got %d words",
				this.getLineCount(), this.getWordCount());
		return name + "\n" + arguments + "\n" + stat;
	}

	public int getLineCount() {
		return lineCount;
	}

	public int getWordCount() {
		return wordCount;
	}

}
