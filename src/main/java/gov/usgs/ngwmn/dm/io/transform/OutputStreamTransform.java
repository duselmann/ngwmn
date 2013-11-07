package gov.usgs.ngwmn.dm.io.transform;


import gov.usgs.ngwmn.dm.io.parse.Element;
import gov.usgs.ngwmn.dm.io.parse.Parser;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


public abstract class OutputStreamTransform
extends FilterOutputStream
implements HeaderWriter
{
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	private OutputStream    pout;
	protected boolean 		writtenHeaders;
	private Future<Long>    parserResult;
	private ExecutorService executor; // TODO this should be Spring-ed
	private final AtomicReference<List<Element>> headers;
	private final LinkedBlockingQueue<Map<String,String>> rows;
	public int id;

	private List<Element> overrideHeaders;
	protected final List<HeaderWrittenListener> headerListeners;

	public abstract String formatRow(List<Element> headers, Map<String, String> rowData);


	public OutputStreamTransform(OutputStream out) throws IOException {
		super(out);
		logger.trace("transform upstream: {}", out.toString());
		executor = Executors.newSingleThreadExecutor();
		headers  = new AtomicReference<List<Element>>();
		rows     = new LinkedBlockingQueue<Map<String,String>>();
		headerListeners = new LinkedList<HeaderWrittenListener>();
	}
	@Override
	protected void finalize() throws Throwable {
		// this shutdown should allow queued and running jobs to finish
		executor.shutdown();
	}


	public void skipHeaders(boolean skipHeaders) {
		writtenHeaders = skipHeaders;
	}

	public void setParser(Parser parser) {
		initParser(parser);
	}
	private void initParser(final Parser parser) {
		logger.trace("initParser rows from {}", System.identityHashCode(rows));
		logger.trace("initParser transformer {}",  System.identityHashCode(OutputStreamTransform.this));

		final PipedInputStream pin  = new PipedInputStream();
		try {
			pout = new PipedOutputStream(pin);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final Map<?,?> mdc = MDC.getCopyOfContextMap();

		Callable<Long> exec = new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				long count=0;
				try {
					MDC.setContextMap((mdc == null) ? Collections.emptyMap() : mdc);

					logger.trace("InputStream parser init started id-{} {}", id, OutputStreamTransform.this);
					parser.setInputStream(pin);
					logger.trace("InputStream parser init finished {}", this);

					logger.trace("parser started  {}", this);
					Map<String,String> row;
					while ( (row=parser.nextRow()) != null ) {
						// we have to update headers every time because new headers could be discovered
						// we also have another thread consuming rows as they are added
						// so that thread needs headers as soon as there are rows
						headers.set( new ArrayList<Element>( parser.headers() ) );

						// add the new row to the row cache
						Map<String,String> newRow = new HashMap<String,String>();
						newRow.putAll(row);
						rows.add(newRow);
						count++;
						logger.trace("parser row {}:{} ", count, newRow);
					}
					logger.trace("parser rows final {}",  rows);
					logger.trace("parser finished {} {}", count, this);
				} finally {
					MDC.clear();
				}
				return count;
			}
		};
		parserResult = executor.submit(exec);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		logger.trace("write bytes len {}", len );
		pout.write(b, off, len);
		processRow();
	}

	@Override
	public void write(int b) throws IOException {
		pout.write(b);
		processRow();
	}

	private void processRow() throws IOException {

		Map<String, String> row = rows.poll();
		if (row != null) {
			List<Element> headList = getHeaders();
			if ( ! writtenHeaders ) {
				writeRow(headList);
				signalHeaderListeners();
			}
			logger.trace("processing row");
			writeRow(headList, row);
		}
	}


	private void writeRow(List<Element> headers, Map<String, String> rowData) throws IOException {
		String rowText = formatRow(headers, rowData);
		logger.trace("writeRow data: {}", rowText);
		out.write( rowText.toString().getBytes() );
	}

	private void writeRow(List<Element> headers) throws IOException {
		String rowText = formatRow(headers, null); // TODO add method without the row data formatHeaders
		logger.trace("writeRow headers: {}", rowText.toString());
		out.write( rowText.toString().getBytes() );
	}


	private void finish() throws IOException {
		logger.trace("finish up processing rows");
		logger.trace("processRow rows from {}", System.identityHashCode(rows));
		logger.trace("processRow transformer id-{} {}", id,
				System.identityHashCode(OutputStreamTransform.this));

		while ( ! rows.isEmpty() || ! parserResult.isDone() ) {
			processRow();
		}
		try {
			long ct = parserResult.get(100, TimeUnit.MILLISECONDS);
			logger.debug("done with rows, ct={}", ct);
		} catch (Exception e) {
			logger.warn("Problem encountered in OutputStreamTransform.finish", e);
		}

		logger.trace("finished processing cached rows");
	}

	@Override
	public void close() throws IOException {
		try {
			logger.trace("closing transformer");
			pout.close(); // this must be done before flushing
			// so that the pin knows that it no longer has to wait for more bytes
			finish();
			//		out.close(); // TODO see if we can reactivate this line
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}



	// when joining two or more results we only want to use the headers from
	// the first source to preserve column ordinal.
	public void setHeaders(List<Element> headers) {
		overrideHeaders = headers;
	}

	public List<Element> getHeaders() {
		if (overrideHeaders == null) {
			return headers.get();
		}
		return overrideHeaders;
	}

	@Override
	public boolean addHeaderListener(HeaderWrittenListener listener) {
		if (listener != null) {
			return headerListeners.add(listener);
		}
		return false;
	}
	protected void signalHeaderListeners() {
		writtenHeaders=true;

		for (HeaderWrittenListener listener : headerListeners) {
			listener.headersWritten();
		}
	}
}

