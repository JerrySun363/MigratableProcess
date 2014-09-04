import java.io.IOException;

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
	
	public WordCountProcess(String args[]) throws IOException {
		if (args == null || args.length != 1) {
			System.out.println("Usage: java WordCount <inputFile>");
			System.exit(0);
		}
		this.input = new TransactionalFileInputStream(args[0]);
	}

	@Override
	public void run() {
		
	}

	@Override
	public void suspend() {
		this.suspend = true;
		while(suspend){};
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public String toSring() {
		String stat = String.format("Already read %d lines, got %d words",
				this.getLineCount(), this.getWordCount());
		return stat;
	}

	public int getLineCount() {
		return lineCount;
	}

	public int getWordCount() {
		return wordCount;
	}

}
