package gov.usgs.ngwmn.dm.io.aggregate;


import gov.usgs.ngwmn.dm.io.Supplier;
import gov.usgs.ngwmn.dm.spec.Specifier;

import java.io.IOException;
import java.io.OutputStream;

public interface FlowFactory {
	Flow makeFlow(Specifier spec, Supplier<OutputStream> out) throws IOException;
}
