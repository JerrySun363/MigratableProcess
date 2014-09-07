import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * This class extends the InputStream class and implements Serializable.
 * 
 * This class is used to perform Transactional File Input. To achieve this, we
 * wrapper the File input stream with proper handling of serialized parameters.
 * 
 * @author Nicolas Yu
 * @author Jerry Sun
 * 
 */
public class TransactionalFileInputStream extends InputStream implements
		Serializable {
	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = -9067377820514406998L;

	/** Position of current read location **/
	private long position = 0;

	/** Wrapper file input stream **/
	private transient FileInputStream fileInputStream;

	/** Migrated flag **/
	private boolean migrated = true;

	/** FileName **/
	private String filename;
	

	public String getFilename() {
		return filename;
	}


	/**
	 * Override the read methods to take the change of position into
	 * consideration.
	 */
	@Override
	public int read() throws IOException {
		// if the file has been migrated, it should reopen the file and .
		if (this.migrated) {
			openFileForRead();
		}
		int result = this.fileInputStream.read();
		if (result != -1) {
			position++;
		}
		return result;
	}

	@Override
	public int read(byte[] b) throws IOException {
		// if the file has been migrated, it should reopen the file and .
		if (this.migrated) {
			openFileForRead();
		}

		int result = this.fileInputStream.read(b);
		if (result >= 0) {
			this.position += result;
		}
		return result;

	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		// if the file has been migrated, it should reopen the file and .
		if (this.migrated) {
			openFileForRead();
		}
		int result = this.fileInputStream.read(b, off, len);
		if (result >= 0) {
			this.position += result;
		}
		return result;
	}

	/**
	 * Try to skip certain number of bytes. Also need to increment the change of
	 * position.
	 * 
	 */
	@Override
	public long skip(long n) throws IOException {
		// if the file has been migrated, it should reopen the file and .
		if (this.migrated) {
			openFileForRead();
		}
		long number = this.fileInputStream.skip(n);
		if (number >= 0) {
			position += number;
		}
		return number;
	}

	/**
	 * The following methods should force the wrapped file input stream to
	 * achieve this.
	 */
	@Override
	public int available() throws IOException {
		// if the file has been migrated, it should reopen the file and .
		if (this.migrated) {
			openFileForRead();
		}
		return this.fileInputStream.available();
	}

	@Override
	public void close() throws IOException {
		// if the file has been migrated, it should reopen the file and .
		if (!this.migrated) {
			this.fileInputStream.close();
		}

	}

	@Override
	public boolean markSupported() {
		// if the file has been migrated, it should reopen the file and .
		if (this.migrated) {
			try {
				openFileForRead();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return this.fileInputStream.markSupported();
	}

	@Override
	public void reset() throws IOException {
		// if the file has been migrated, it should reopen the file and .
		if (this.migrated) {
			openFileForRead();
		}
		this.fileInputStream.reset();
	}

	@Override
	public void mark(int readlimit) {
		// if the file has been migrated, it should reopen the file and .
		if (this.migrated) {
			try {
				openFileForRead();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.fileInputStream.mark(readlimit);
	}

	/**
	 * This is the constructor for the TransactionalFileInputStream. It mainly
	 * handles the FileName as input and records the position of read/write
	 * location.
	 * 
	 * Also, to improve the performance of read, it also records the migrated
	 * state of the program. Once it is migrated, the file needs to be reopened
	 * and jumps to the location where it was left.
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public TransactionalFileInputStream(String filename) throws IOException {
		this.filename = filename;
		this.position = 0;
		openFileForRead();
		this.migrated = true;
	}

	/**
	 * 
	 * @return whether the file input stream is migrated.
	 */
	public boolean isMigrated() {
		return migrated;
	}

	/**
	 * Set migrated state. If this is set to be true, then it should close the
	 * 
	 * @param migrated
	 */
	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
		// when this is set to true, close the file handler.
		if (this.fileInputStream != null && this.isMigrated() == true) {
			try {
				this.fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Tries to open file given the filename and to skip certain positions
	 * 
	 * @throws IOException
	 */
	private void openFileForRead() throws IOException {
		this.fileInputStream = new FileInputStream(this.filename);
		this.fileInputStream.skip(position);
		this.migrated = false;
	}

}
