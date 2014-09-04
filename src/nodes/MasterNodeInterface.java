package nodes;

import manager.MigratableProcess;
/**
 * This is the interface for the master node.
 * 
 * Any master should implement this master so that these commands required should be able to run
 * without knowing the implementation.
 * 
 * @author Jerry Sun
 *
 */
public interface MasterNodeInterface {
	/**
	 * Starting command for the master node to run after an instance is initialized.
	 * 
	 */
	public void run();
	
	/**
	 * Launch the process at some slave node.
	 * 
	 * @param process
	 * @return pid the process allocated by master node.
	 * 
	 */
	public int launchProcess(MigratableProcess process);
	
	/**
	 * Migrate the process with specific pid. 
	 * It involves the change of running node or computer.
	 * 
	 * @param pid
	 * @return boolean value indicating whether the migration is successful.
	 */
	public boolean migrate(int pid);
	
	/**
	 * Remove the process with specific pid from the running nodes.
	 * 
	 * @param pid
	 * @return boolean value indicating whether the removal is successful.
	 */
	public boolean remove(int pid);
}
