package gov.loc.repository.bagit;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.writer.impl.TarWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * <p>
 * Test Bagit TAR creation.
 * </p>
 * 
 * @author Stefan E. Funk, SUB Göttingen (funk@sub.goettingen.de)
 * @version 2015-02-05
 */

public class BagitExample {

	private static final String	THE_BAG_LOCATION	= "./THE_BAG.tar";

	public static void main(String[] args) throws IOException {

		// Create example files for testing THE BAG!
		String e1 = "example1";
		File f1 = File.createTempFile(e1 + ".", ".txt", new File("."));
		BufferedWriter b = new BufferedWriter(new FileWriter(f1));
		b.write(e1);
		b.close();

		String e2 = "example1";
		File f2 = File.createTempFile(e2 + ".", ".txt", new File("."));
		BufferedWriter b2 = new BufferedWriter(new FileWriter(f2));
		b2.write(e2);
		b2.close();

		// Start creation of THE BAG!
		BagFactory bagFac = new BagFactory();
		Bag theBag = bagFac.createBag();

		// Add example files to THE BAG!
		theBag.addFileToPayload(f1);
		theBag.addFileToPayload(f2);

		// Complete THE BAG!
		DefaultCompleter completor = new DefaultCompleter(bagFac);
		theBag = completor.complete(theBag);

		// Create file for THE BAG and write using the TAR writer.
		File theBagFile = new File(THE_BAG_LOCATION);
		TarWriter zw = new TarWriter(bagFac);
		theBag.write(zw, theBagFile);

		// Verify THE BAG!
		System.out.println(theBag.verifyComplete());

		// Close THE BAG after verifying!
		theBag.close();

		// Delete example files.
		f1.deleteOnExit();
		f2.deleteOnExit();
	}

}
