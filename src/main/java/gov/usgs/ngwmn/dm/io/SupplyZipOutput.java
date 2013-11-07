package gov.usgs.ngwmn.dm.io;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;


public class SupplyZipOutput extends Supplier<OutputStream> {
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	private Supplier<OutputStream> os;
	private ZipOutputStream oz;
	
	public SupplyZipOutput(Supplier<OutputStream> os) {
		this.os = os;
	}

	private synchronized ZipOutputStream init() throws IOException {
		if (oz == null) {
			oz = new ZipOutputStream( os.begin() );
		}
		return oz;
	}
	
	@Override
	public OutputStream initialize() throws IOException {
		logger.debug("initialize : zip output stream");
		return oz = init();
	}

	/**
	 *  if there is an entry open, close the entry, otherwise we close the stream
	 */
	@Override
	public void end(boolean threw) throws IOException {
		logger.debug("end : closing zip stream");
		super.end(threw);
	}
	
	
	public Supplier<OutputStream> makeEntry(EntryDescription entryDesc) {
		return new SupplyZipEntry(this, entryDesc);
	}
	
	protected ZipOutputStream getZip() {
		try {
			return init();
		} catch (IOException ioe) {
			logger.warn("Failed getting zip", ioe);
			throw new RuntimeException(ioe);
		}
	}


	public void addStream(String name, String mimeType, InputStream dd) throws IOException {
		logger.debug("Adding stream for {}", name);
		ZipOutputStream zip = getZip();
		
		ZipEntryOutputStream zos = new ZipEntryOutputStream(zip, name);
		try {
			ByteStreams.copy(dd, zos);
		} finally {
			zos.close();
		}
	}
}
