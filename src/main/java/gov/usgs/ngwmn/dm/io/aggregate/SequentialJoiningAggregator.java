package gov.usgs.ngwmn.dm.io.aggregate;

import gov.usgs.ngwmn.WellDataType;
import gov.usgs.ngwmn.dm.io.EntryDescription;
import gov.usgs.ngwmn.dm.io.FilenameEntry;
import gov.usgs.ngwmn.dm.io.JoiningSupplier;
import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.io.transform.TransformSupplier;
import gov.usgs.ngwmn.dm.spec.Encoding;
import gov.usgs.ngwmn.dm.spec.SpecResolver;
import gov.usgs.ngwmn.dm.spec.Specification;
import gov.usgs.ngwmn.dm.spec.WellListResolver;

import java.io.OutputStream;


public class SequentialJoiningAggregator extends SequentialFlowAggregator {

    protected Specification spect;
	
    public SequentialJoiningAggregator(FlowFactory fac, Specification specification, Supplier<OutputStream> out) {
    	super(fac, null, out);
    	spect = specification;
    }
    
    @Override
    public Void call() throws Exception {
    	Flow exec = null;
    	boolean threw = true;
    	Encoding encode = spect.getEncode();
    	try {
        	output.begin();
        	        	
	        for (WellDataType type : spect.getDataTypes()) {
	        	logger.info("Getting wells data for {}", type);
	        	
	        	EntryDescription desc;
	        	// TODO Perhaps move this to WellDaatType enum?
	        	if (type == WellDataType.REGISTRY) {
	        		// overrule file name
	        		desc = new FilenameEntry( "SITE_INFO" );
	        	} else {
		        	desc = new FilenameEntry( type.toString() );
	        	}
	        	desc.extension( encode.extension() ); // TODO I would like to see this in the transform but the zip entry is made sooner at the moment and might not have to be.
	        	Supplier<OutputStream> substream  = output.makeEntry(desc);
	        	substream = new TransformSupplier(substream, type, encode);
	        	substream = new JoiningSupplier<OutputStream>(substream);
	        	
	        	// TODO Need a different substream for XSL processing chain
	        	// output is expected to be SupplyZipOutput which is OK
	        	
	        	// TODO this must be decoupled because we will want to send in lat-long and other resolvers
				SpecResolver resolver = new WellListResolver();
	        	Flow inner = new SequentialFlowAggregator(factory, resolver.specIterator(spect, type), substream);
	        	inner.call();
	        }
	        
        	threw = false;
        	
		} catch (Exception problem) {
			if ( ! handleErrors(exec, problem) ) {
				throw problem;
			}
		} finally {
			output.end(threw);
		}
        return null;
    }
    
}