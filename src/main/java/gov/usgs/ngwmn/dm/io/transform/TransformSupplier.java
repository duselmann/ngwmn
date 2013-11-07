package gov.usgs.ngwmn.dm.io.transform;

import gov.usgs.ngwmn.NotImplementedException;
import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.io.EntryDescription;
import gov.usgs.ngwmn.dm.io.SimpleSupplier;
import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.io.parse.Element;
import gov.usgs.ngwmn.dm.io.parse.HeaderChangeListener;
import gov.usgs.ngwmn.dm.spec.Encoding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TransformSupplier extends Supplier<OutputStream>
implements HeaderChangeListener, HeaderWrittenListener {
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected Supplier<OutputStream> upstream;
	protected EntryDescription entryDesc;
	protected Encoding encoding;
	protected OutputStream oStream;
	protected boolean skipHeaders;
	protected WellDataType dataType;
	protected ExecutorService pipelineExecutor = Executors.newSingleThreadExecutor();


	// this supplier will be a listener for the headers on the first entry
	// it will supply subsequent entries with these headers to preserve column ordinal
	protected List<Element> masterHeaders; // previous headers

	public TransformSupplier(Supplier<OutputStream> output, WellDataType type, Encoding encode) {
		upstream = output;
		dataType = type;
		encoding = (encode==null) ? Encoding.NONE : encode;
	}
	@Override
	protected void finalize() throws Throwable {
		// this shutdown should allow queued and running jobs to finish
		pipelineExecutor.shutdown();
	}


	@Override
	public void headersChanged(List<Element> headers) {
		masterHeaders = headers;
	}

	@Override
	public Supplier<OutputStream> makeEntry(EntryDescription entryDesc) {
		logger.trace("making entry for {}",entryDesc);
		entryDesc.extension( encoding.extension() );

		if (oStream == null) {
			throw new RuntimeException("Cannot makeEntry on a uninitialized supplier.");
		}
		if (oStream instanceof HeaderWriter) {
			HeaderWriter hw = (HeaderWriter)oStream;
			hw.addHeaderListener(this);
		}

		Supplier<OutputStream> value;
		switch (dataType) {
		case WATERLEVEL:
			value = new DirectWaterlevelCSVOutputStreamSupplier(oStream, pipelineExecutor, skipHeaders, entryDesc);
			skipHeaders = true;
			break;

		case QUALITY:
			value = new DirectQualityCSVOutputStreamSupplier(oStream, pipelineExecutor, skipHeaders, entryDesc);
			skipHeaders = true;
			break;

		default:
			OutputStreamTransform ost = (OutputStreamTransform)oStream;
			Supplier<OutputStreamTransform> sos = new SimpleSupplier<OutputStreamTransform>(ost);
			TransformEntrySupplier transformer;
			if (skipHeaders) {
				// we must have master headers by now because we are skipping them so we pass-on what we have
				transformer  = new TransformEntrySupplier(sos, entryDesc, dataType, skipHeaders, masterHeaders);
			} else {
				// we need master headers so listen for them
				transformer  = new TransformEntrySupplier(sos, entryDesc, dataType, skipHeaders, this);
			}
			value = transformer;
		}

		return value;
	}



	@Override
	public OutputStream initialize() throws IOException {
		OutputStream os = upstream.begin();

		switch (encoding) {
		case NONE:
			return os;

		case TSV:
			switch (dataType) {
			case WATERLEVEL:
			case QUALITY:
				oStream = os;
				break;
			default:
				oStream = new TsvOutputStream(os);
			}

			break;

		case XLSX: // TODO need to impl
			throw new NotImplementedException();

		default:   // Default to CSV
		case CSV:
			switch (dataType) {
			case WATERLEVEL:
			case QUALITY:
				oStream = os;
				break;
			default:
				oStream = new CsvOutputStream(os);
				break;
			}
		}
		return oStream;
	}

	@Override
	public void headersWritten() {
		skipHeaders = true;
	}
}
