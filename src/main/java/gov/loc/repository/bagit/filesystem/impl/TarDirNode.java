package gov.loc.repository.bagit.filesystem.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gov.loc.repository.bagit.filesystem.DirNode;
import gov.loc.repository.bagit.filesystem.FileNode;
import gov.loc.repository.bagit.filesystem.FileSystemNode;
import gov.loc.repository.bagit.filesystem.FileSystemNodeFilter;
import gov.loc.repository.bagit.filesystem.impl.NodeHelper;

/**
 * <p>
 * Copied from ZipDirNode and adapted to TAR.
 * </p>
 * 
 * @author Stefan E. Funk, SUB Göttingen (funk@sub.goettingen.de)
 * @version 2015-02-05
 */

public class TarDirNode extends AbstractTarNode implements DirNode {

	private Map<String, FileSystemNode>	childrenMap	= new ConcurrentHashMap<String, FileSystemNode>();

	/**
	 * @param filepath
	 * @param fileSystem
	 */
	protected TarDirNode(String filepath, TarFileSystem fileSystem) {
		super(filepath, fileSystem);
	}

	/**
	 * @param child
	 */
	public void addChild(FileSystemNode child) {
		this.childrenMap.put(child.getName(), child);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.DirNode#listChildren()
	 */
	@Override
	public Collection<FileSystemNode> listChildren() {
		return Collections.unmodifiableCollection(this.childrenMap.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.loc.repository.bagit.filesystem.DirNode#childFile(java.lang.String)
	 */
	@Override
	public FileNode childFile(String name) {
		FileSystemNode child = this.childrenMap.get(name);
		if (child != null && child instanceof FileNode)
			return (FileNode) child;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.loc.repository.bagit.filesystem.DirNode#childDir(java.lang.String)
	 */
	@Override
	public DirNode childDir(String name) {
		FileSystemNode child = this.childrenMap.get(name);
		if (child != null && child instanceof DirNode)
			return (DirNode) child;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.loc.repository.bagit.filesystem.DirNode#listChildren(gov.loc.repository
	 * .bagit.filesystem.FileSystemNodeFilter)
	 */
	@Override
	public Collection<FileSystemNode> listChildren(FileSystemNodeFilter filter) {
		return NodeHelper.listChildren(this, filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.DirNode#listDescendants()
	 */
	@Override
	public Collection<FileSystemNode> listDescendants() {
		return NodeHelper.listDescendants(this, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.filesystem.DirNode#listDescendants(gov.loc.
	 * repository.bagit.filesystem.FileSystemNodeFilter,
	 * gov.loc.repository.bagit.filesystem.FileSystemNodeFilter)
	 */
	@Override
	public Collection<FileSystemNode> listDescendants(
			FileSystemNodeFilter filter, FileSystemNodeFilter descentFilter) {
		return NodeHelper.listDescendants(this, filter, descentFilter);
	}

}
