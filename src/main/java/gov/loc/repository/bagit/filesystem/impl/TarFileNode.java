package gov.loc.repository.bagit.filesystem.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

import gov.loc.repository.bagit.filesystem.FileNode;

/**
 * <p>
 * Copied from ZipFileNode and adapted to TAR.
 * </p>
 * 
 * @author Stefan E. Funk, SUB Göttingen (funk@sub.goettingen.de)
 * @version 2015-02-05
 */

public class TarFileNode extends AbstractTarNode implements FileNode {

	private TarArchiveEntry	entry;

	/**
	 * @param entry
	 * @param filepath
	 * @param fileSystem
	 */
	protected TarFileNode(TarArchiveEntry entry, String filepath,
			TarFileSystem fileSystem) {
		super(filepath, fileSystem);
		this.entry = entry;
	}

	/**
	 * @return
	 */
	public TarArchiveEntry getEntry() {
		return this.entry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileNode#getSize()
	 */
	@Override
	public long getSize() {
		return this.entry.getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileNode#newInputStream()
	 */
	@Override
	public InputStream newInputStream() {
		if (this.entry == null) {
			throw new RuntimeException("Does not exist");
		}

		try {
			return new FileInputStream(this.fileSystem.getTarfile().getFile());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileNode#exists()
	 */
	@Override
	public boolean exists() {
		return this.entry != null;
	}

}
