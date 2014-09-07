import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This class provides the transactional file output stream method. To achieve
 * this, we wrap a FileOutputStream.
 * 
 * For output stream, the default cursor is always at the end of the file output
 * stream. Thus, we don't need to hold the positions.
 * 
 * However, we must flush every the write when the migrated flag is set, and
 * close the file properly.
 * 
 * 
 * @author Nicolas Yu
 * @author Jerry Sun
 */
public class TransactionalFileOutputStream extends OutputStream implements
		Serializable {

	/**
	 * Generated serailVersionUID
	 */
	private static final long serialVersionUID = -2759612490300531241L;

	/** Wrapper file input stream **/
	private transient FileOutputStream fileOutputStream;

	/** Migrated flag **/
	private boolean migrated = true;

	/** FileName and append option **/
	private String filename;
	private boolean append;

	/**
	 * 
	 * @param filename. The String representation of the file name.
	 * @param boolean value indicating whether to append or not.
	 * @throws FileNotFoundException
	 */
	public TransactionalFileOutputStream(String filename, boolean b)
			throws FileNotFoundException {
		this.filename = filename;
		this.append = b;
		openFileForWrite();
		this.migrated = true;
	}

	
	/**
	 * The following methods are overriden methods of wrapper methods for fileOutputStream.
	 * There is no need to track the position of current open file.
	 * 
	 * However, it does require to check whether it is migrated before each operation is performed.
	 * 
	 * Also, when it sets to be Migrated, we also need to check file status, 
	 * flush all the cached write operation and close the file to write.
	 * 
	 */
	@Override
	public void write(int b) throws IOException {
		if (this.isMigrated()) {
			this.openFileForWrite();
		}
		this.fileOutputStream.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		if (this.isMigrated()) {
			this.openFileForWrite();
		}
		this.fileOutputStream.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (this.isMigrated()) {
			this.openFileForWrite();
		}
		this.fileOutputStream.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		if (!this.isMigrated()) {
			this.fileOutputStream.close();
		}
	}

	@Override
	public void flush() throws IOException {
		// we can ignore this.
	}

	/**
	 * 
	 * @return whether it is migrated
	 */
	public boolean isMigrated() {
		return migrated;
	}
	
	/**
	 * SET Migrated, and also tries to close the file if it is true.
	 * 
	 * @param migrated
	 */
	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
		if (isMigrated() && this.fileOutputStream != null) {
			try {
				this.fileOutputStream.flush();
				this.fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
	/**
	 * This method tries to create an open file for write operation.
	 * When a new file is created, set the Migrated to be false.
	 * 
	 * @throws FileNotFoundException
	 */
	private void openFileForWrite() throws FileNotFoundException {
		this.fileOutputStream = new FileOutputStream(this.filename, this.append);
		this.setMigrated(false);
	}
}
