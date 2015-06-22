package gov.loc.repository.bagit.filesystem.impl;

import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.utilities.ResourceHelper;

import java.io.File;

import org.junit.Before;

/**
 * <p>
 * Copied from ZipFileSystemTest and adapted to TAR.
 * </p>
 * 
 * @author Stefan E. Funk, SUB Göttingen (funk@sub.uni-goettingen.de)
 * @version 2015-06-22
 */

public class TarFileSystemTest extends AbstractFileSystemTest {

	File	rootFile;

	@Before
	public void setup() throws Exception {
		this.rootFile = ResourceHelper.getFile("file_systems/test.tar");
		assert (this.rootFile.exists());
		assert (this.rootFile.isFile());
	}

	@Override
	FileSystem getFileSystem() {
		return new TarFileSystem(this.rootFile);
	}

}
