package gov.loc.repository.bagit.filesystem.impl;

import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.filesystem.FileSystemNode;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Copied from ZipFileSystem and adapted to TAR.
 * </p>
 * 
 * @author Stefan E. Funk, SUB Göttingen (funk@sub.goettingen.de)
 * @version 2015-02-05
 */

public class TarFileSystem implements FileSystem {

	private static final Log	log	= LogFactory.getLog(TarFileSystem.class);

	private File				file;
	private TarArchiveEntry		tarFile;
	private TarDirNode			root;

	/**
	 * @param file
	 */
	public TarFileSystem(File file) {
		assert file != null;
		if (!file.isFile())
			throw new RuntimeException("Not a file");
		this.file = file;
		this.tarFile = new TarArchiveEntry(file);

		// Read the directory
		Map<String, TarDirNode> dirNodeMap = new HashMap<String, TarDirNode>();
		this.root = new TarDirNode("", this);
		dirNodeMap.put("", this.root);

		TarArchiveEntry entryEnum[] = this.tarFile.getDirectoryEntries();
		for (TarArchiveEntry entry : entryEnum) {
			String entryFilepath = entry.getName();
			if (entryFilepath.endsWith("/"))
				entryFilepath = entryFilepath.substring(0,
						entryFilepath.length() - 1);

			// Create the node for the entry
			File parentFile = new File(entryFilepath).getParentFile();
			List<String> parentPaths = new ArrayList<String>();
			while (parentFile != null) {
				parentPaths.add((parentFile.getPath()));
				parentFile = parentFile.getParentFile();
			}
			// Can skip if first one already in map
			TarDirNode parentOfParentDirNode = this.root;
			if (!parentPaths.isEmpty()
					&& dirNodeMap.containsKey(parentPaths.get(0))) {
				parentOfParentDirNode = dirNodeMap.get(parentPaths.get(0));
			} else {
				Collections.reverse(parentPaths);
				// Create the parents
				for (String parentPath : parentPaths) {
					TarDirNode parentDirNode = dirNodeMap.get(parentPath);
					if (parentDirNode == null) {
						parentDirNode = new TarDirNode(parentPath, this);
						dirNodeMap.put(parentPath, parentDirNode);
					}
					parentOfParentDirNode.addChild(parentDirNode);
					parentOfParentDirNode = parentDirNode;
				}
			}
			// Add
			FileSystemNode entryNode = null;
			if (entry.isDirectory()) {
				entryNode = new TarDirNode(entryFilepath, this);
			} else {
				entryNode = new TarFileNode(entry, entry.getName(), this);
			}
			parentOfParentDirNode.addChild(entryNode);
		}
	}

	/**
	 * @return
	 */
	public TarArchiveEntry getTarfile() {
		return this.tarFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() {
		// TODO Must TAR not be closed?
		// TarFile.closeQuietly(this.tarFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileSystem#closeQuietly()
	 */
	@Override
	public void closeQuietly() {
		// TODO Must TAR not be closed?
		// TarFile.closeQuietly(this.tarFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileSystem#getRoot()
	 */
	@Override
	public DirNode getRoot() {
		return this.root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.FileSystem#getFile()
	 */
	@Override
	public File getFile() {
		return this.file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.loc.repository.bagit.filesystem.FileSystem#resolve(java.lang.String)
	 */
	@Override
	public FileNode resolve(String filepath) {
		log.trace(MessageFormat.format("Resolving {0}", filepath));
		return new TarFileNode(this.tarFile, filepath, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		this.close();
	}

}
