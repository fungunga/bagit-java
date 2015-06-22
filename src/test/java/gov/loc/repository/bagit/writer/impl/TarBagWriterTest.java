package gov.loc.repository.bagit.writer.impl;

import gov.loc.repository.bagit.utilities.ResourceHelper;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.TarWriter;

import java.io.File;

import org.junit.Before;

/**
 * <p>
 * Copied from ZipFileSystem and adapted to TAR.
 * </p>
 * 
 * @author Stefan E. Funk, SUB Göttingen (funk@sub.uni-goettingen.de)
 * @version 2015-06-22
 */

public class TarBagWriterTest extends AbstractWriterTest {

	File	bagFile;

	@Before
	@Override
	public void setUp() throws Exception {
		bagFile = new File(ResourceHelper.getFile("bags"), "foo.tar");
	}

	@Override
	public File getBagFile() {
		return this.bagFile;
	}

	@Override
	public Writer getBagWriter() {
		return new TarWriter(bagFactory);

	}

}
