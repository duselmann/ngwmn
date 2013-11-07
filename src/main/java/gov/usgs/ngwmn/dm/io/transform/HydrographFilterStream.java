package gov.usgs.ngwmn.dm.io.transform;

import java.io.IOException;
import java.io.OutputStream;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HydrographFilterStream 
	extends XSLFilterOutputStream
	implements ErrorListener
{

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	protected Double elevation;

	public HydrographFilterStream(OutputStream out) throws IOException {
		super(out);
		setTransform("/gov/usgs/ngwmn/hydrograph.xsl");
		logger.debug("created");
	}
	
	@Override
	protected void setupTransform(Transformer t) {
		if (getElevation() != null) {
			t.setParameter("elevation", String.valueOf(getElevation()));
		}
		logger.debug("Set transform parameters to elevation={}", getElevation());
		
		t.setErrorListener(this);
		super.setupTransform(t);
		
		logger.debug("setup Transform {}", t);

	}

	public Double getElevation() {
		return elevation;
	}

	public void setElevation(Double elevation) {
		this.elevation = elevation;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HydrographFilterStream [xform=");
		builder.append(xslHelper.getXformResourceName());
		builder.append(", out=");
		builder.append(out);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public void warning(TransformerException exception)
			throws TransformerException {
		logger.warn("transformer warning {}", exception.getMessageAndLocation());
	}

	@Override
	public void error(TransformerException exception)
			throws TransformerException {
		logger.error("transformer error {}", exception.getMessageAndLocation());
	}

	@Override
	public void fatalError(TransformerException exception)
			throws TransformerException {
		logger.error("transformer fatal {}", exception.getMessageAndLocation());		
	}
	

}
