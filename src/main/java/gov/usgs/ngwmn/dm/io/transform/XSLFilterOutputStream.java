package gov.usgs.ngwmn.dm.io.transform;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * On the model of {@link gov.usgs.ngwmn.dm.io.transform.OutputStreamTransform}, 
 * start a Callable that filters the input to the output,
 * passing the input (assumed XML) through the specified XSL transform.
 * 
 * @author rhayes
 *
 */
public class XSLFilterOutputStream extends FilterOutputStream {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	public XSLFilterOutputStream(OutputStream out) {
		super(out);
	}
	
	private OutputStream    pout;  // This is sent as input to the transform

	protected Future<Void> xformOutcome;
	private ExecutorService executor;

	protected String xformResourceName;
	protected Templates templates; // TODO Optimize, make this a shared resource
		
	public void setExecutor(ExecutorService e) {
		executor = e;
	}

	public void setTransform(String xformName) throws Exception {
		xformResourceName = xformName;
		templates = loadXSLT(xformName);

		// TODO defer initialization until first write
		// init();
	}
   
	public synchronized void ensureInitialized() {
		if (pout == null) {
			init();
		}
	}
	
	private synchronized void init() {
		final PipedInputStream pin  = new PipedInputStream();
		synchronized (this) {
			try {
				pout = new PipedOutputStream(pin);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		final Map<?,?> mdc = MDC.getCopyOfContextMap();

		Callable<Void> exec = new Callable<Void>() {
			public Void call() throws Exception {
				
				MDC.setContextMap((mdc == null) ? Collections.emptyMap() : mdc);

				Transformer t = templates.newTransformer();
				setupTransform(t);
				StreamResult result = new StreamResult(out);	// this goes to output channel
				
				StreamSource source = new StreamSource(pin);	// this is piped from write methods
				
				t.transform(source, result);
								
				return null;
			}
		};
		
		xformOutcome = executor.submit(exec);

	}
	
	/**
	 * Do whatever you need to do to initialize the transform (like set parameters).
	 * @param t
	 */
	protected void setupTransform(Transformer t) {
		// This space available
	}

	private void finish() throws IOException {
    	try {
    		xformOutcome.get(100, TimeUnit.MILLISECONDS);
    		logger.debug("done with XSL");
    	} catch (Exception e) {
    		logger.warn("Problem encountered in finish", e);
    	}
		
		logger.trace("finished XSL processing");
	}

    private Templates loadXSLT(String xsltFile) throws TransformerConfigurationException, IOException {
    	InputStream xin = getClass().getResourceAsStream(xsltFile);
    	try {
	    	Source xslSource = new StreamSource(xin);
	
	    	TransformerFactory transFact = TransformerFactory.newInstance();
	    	// Can get more details by implementing ErrorListener transFact.setErrorListener(listener);
    		logger.debug("Transformer factory class is {}", transFact.getClass());
	    	Templates templates = transFact.newTemplates(xslSource);
	
	    	return templates;
    	} catch (TransformerConfigurationException tce) {
    		logger.error("Problem loading templates from " + xsltFile, tce);
    		logger.error("java.home is {}", System.getProperty("java.home", "unknown"));
    		throw tce;
    	} finally {
    		xin.close();
    	}
    }

	@Override
    public void close() throws IOException {
		try {
			logger.trace("closing transformer");
			pout.close(); // this triggers XSL completion
			finish();
			out.close();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
    }

	@Override
	public void write(int b) throws IOException {
		ensureInitialized();
		pout.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		ensureInitialized();
		pout.write(b, off, len);
	}

    
}
