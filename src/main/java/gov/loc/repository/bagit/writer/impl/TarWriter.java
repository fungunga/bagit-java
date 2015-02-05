package gov.loc.repository.bagit.writer.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.Bag.Format;
import gov.loc.repository.bagit.BagFactory.LoadOption;
import gov.loc.repository.bagit.filesystem.FileSystem;
import gov.loc.repository.bagit.filesystem.impl.TarFileSystem;
import gov.loc.repository.bagit.writer.impl.AbstractWriter;

/**
 * <p>
 * Copied from ZipWriter and adapted to TAR.
 * </p>
 * 
 * @author Stefan E. Funk, SUB Göttingen (funk@sub.goettingen.de)
 * @version 2015-02-05
 */

public class TarWriter extends AbstractWriter {

	private static final Log		log			= LogFactory
														.getLog(TarWriter.class);

	private static final int		BUFFERSIZE	= 65536;

	private TarArchiveOutputStream	tarOut		= null;
	private String					bagDir		= null;
	private Bag						newBag		= null;
	private File					newBagFile	= null;
	private List<String>			filepaths	= new ArrayList<String>();
	private int						fileTotal	= 0;
	private int						fileCount	= 0;
	private File					tempFile;

	/**
	 * @param bagFactory
	 */
	public TarWriter(BagFactory bagFactory) {
		super(bagFactory);
	}

	/**
	 * @param bagDir
	 */
	public void setBagDir(String bagDir) {
		this.bagDir = bagDir;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.writer.impl.AbstractWriter#getFormat()
	 */
	@Override
	protected Format getFormat() {
		return Format.TAR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.loc.repository.bagit.impl.AbstractBagVisitor#startBag(gov.loc.repository
	 * .bagit.Bag)
	 */
	@Override
	public void startBag(Bag bag) {
		try {
			this.tarOut = new TarArchiveOutputStream(new FileOutputStream(
					this.tempFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.newBag = this.bagFactory.createBag(this.newBagFile, bag
				.getBagConstants().getVersion(), LoadOption.NO_LOAD);
		this.fileCount = 0;
		this.fileTotal = bag.getTags().size() + bag.getPayload().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.loc.repository.bagit.impl.AbstractBagVisitor#endBag()
	 */
	@Override
	public void endBag() {
		try {
			if (this.tarOut != null) {
				this.tarOut.close();
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		this.switchTemp(this.newBagFile);
		FileSystem fileSystem = new TarFileSystem(this.newBagFile);
		// TODO TAR has no index, so we do not need this?
		// for (String filepath : this.filepaths) {
		// this.newBag.putBagFile(new FileSystemBagFile(filepath, fileSystem
		// .resolve(this.bagDir + "/" + filepath)));
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.loc.repository.bagit.impl.AbstractBagVisitor#visitPayload(gov.loc
	 * .repository.bagit.BagFile)
	 */
	@Override
	public void visitPayload(BagFile bagFile) {
		log.debug(MessageFormat.format("Writing payload file {0}.",
				bagFile.getFilepath()));
		this.write(bagFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.loc.repository.bagit.impl.AbstractBagVisitor#visitTag(gov.loc.repository
	 * .bagit.BagFile)
	 */
	@Override
	public void visitTag(BagFile bagFile) {
		log.debug(MessageFormat.format("Writing tag file {0}.",
				bagFile.getFilepath()));
		this.write(bagFile);
	}

	/**
	 * @param bagFile
	 */
	private void write(BagFile bagFile) {

		this.fileCount++;
		this.progress("writing", bagFile.getFilepath(), this.fileCount,
				this.fileTotal);

		// Add TAR entry
		try {
			TarArchiveEntry entry = new TarArchiveEntry(this.bagDir + "/"
					+ bagFile.getFilepath());
			entry.setSize(bagFile.getSize());
			this.tarOut.putArchiveEntry(entry);

			InputStream in = bagFile.newInputStream();
			try {
				byte[] dataBytes = new byte[BUFFERSIZE];
				int nread = in.read(dataBytes);
				while (nread > 0) {
					this.tarOut.write(dataBytes, 0, nread);
					nread = in.read(dataBytes);
				}
			} finally {
				IOUtils.closeQuietly(in);
			}
			this.tarOut.closeArchiveEntry();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.filepaths.add(bagFile.getFilepath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gov.loc.repository.bagit.writer.Writer#write(gov.loc.repository.bagit
	 * .Bag, java.io.File)
	 */
	@Override
	public Bag write(Bag bag, File file) {
		log.info("Writing bag");

		this.newBagFile = file;
		if (this.bagDir == null) {
			this.bagDir = file.getName().replaceFirst("\\..*$", "");
		}

		try {
			File parentDir = file.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				FileUtils.forceMkdir(parentDir);
			}
			this.tempFile = this.getTempFile(file);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		bag.accept(this);

		if (this.isCancelled())
			return null;

		return this.newBag;
	}

}
