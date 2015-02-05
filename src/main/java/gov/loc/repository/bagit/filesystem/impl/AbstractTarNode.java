package gov.loc.repository.bagit.filesystem.impl;

import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.utilities.FilenameHelper;

/**
 * <p>
 * Copied from AbstractZipDirNode and adapted to TAR.
 * </p>
 * 
 * @author Stefan E. Funk, SUB Göttingen (funk@sub.goettingen.de)
 * @version 2015-02-05
 */

public class AbstractTarNode implements FileSystemNode {

	protected String		filepath;
	protected String		name	= null;
	protected TarFileSystem	fileSystem;

	/**
	 * @param filepath
	 * @param fileSystem
	 */
	protected AbstractTarNode(String filepath, TarFileSystem fileSystem) {
		this.filepath = filepath;
		// Root
		if (!filepath.equals("")) {
			this.name = FilenameHelper.getName(filepath);
		}
		this.fileSystem = fileSystem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileSystemNode#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileSystemNode#getFilepath()
	 */
	@Override
	public String getFilepath() {
		return this.filepath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileSystemNode#getFileSystem()
	 */
	@Override
	public FileSystem getFileSystem() {
		return this.fileSystem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AbstractTarNode))
			return false;
		final AbstractTarNode that = (AbstractTarNode) obj;
		return this.filepath.equals(that.getFilepath());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 23 + this.filepath.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileSystemNode#isSymlink()
	 */
	@Override
	public boolean isSymlink() {
		return false;
	}

}
