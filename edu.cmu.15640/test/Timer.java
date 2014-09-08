package test;
import java.util.HashMap;

import nodes.SlaveNode;
import manager.MigratableProcess;

/**
 * Timer is an implementation of Migratable Process. It does not inovle any I/O
 * operation.
 * 
 * The program simply runs starting with 0, and prints the second since start
 * every second.
 * 
 * This is meant for the simplest version of input.
 * 
 * @author Jerry Sun
 * 
 */
public class Timer implements MigratableProcess {

	/**
	 * Below is the generated serialVersionUID.
	 */
	private static final long serialVersionUID = 7942280544949069827L;

	private long count = 0;
	final private long SLEEP = 1000;
	private boolean suspending = false;

	public Timer(String args[]) {

	}

	public Timer() {

	}

	@Override
	public void run() {
		while (!this.suspending) {
			System.out.println(count);
			count++;
			try {
				Thread.currentThread().sleep(SLEEP);
			} catch (InterruptedException e) {
				return;
			}
		}
		terminate();

	}

	@Override
	public void suspend() {
		this.suspending = true;
	}

	@Override
	public void resume() {
		this.suspending = false;

	}

	
	@Override
	public String toSring() {
		return this.getClass().getName() + "\nNo Arguments\nCurrent Number is "
				+ this.count + ". And is it suspended? " + this.suspending;
	}

	/* (non-Javadoc)
	 * @see manager.MigratableProcess#terminate()
	 */
	@Override
	public void terminate() {
		HashMap<Thread, Integer> referHashMap = SlaveNode.getInstance().getThreadPIDMap();
		SlaveNode.getInstance().getRunningPIDs().remove(referHashMap.get(Thread.currentThread()));

		
	}

}
