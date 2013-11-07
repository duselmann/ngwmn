package gov.usgs.ngwmn.dm.io;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IoUtil {

	private static final Logger logger = LoggerFactory.getLogger(IoUtil.class);

	public static final String LINE_SEPARATOR  = System.getProperty("line.separator");

	public static void quiteClose(Object ... open) {

		if (open == null) return;

		for (Object o : open) {
			if (o == null) continue;

			try {
				logger.debug("Closing resource. " + o.getClass().getName());
				if        (o instanceof Connection) {
					if ( !((Connection)o).isClosed() ) {
						((Connection)o).close();
					}
				} else if (o instanceof Statement) {
					if (  !( (Statement)o).isClosed() ) {
						( (Statement)o).close();
					}
				} else if (o instanceof ResultSet) {
					if (  !( (ResultSet)o).isClosed() ) {
						( (ResultSet)o).close();
					}
				} else if (o instanceof Closeable) {
					// cannot test for close so have to catch exception
					try { ((Closeable)o).close(); } catch (Exception e) {}
				} else {
					throw new UnsupportedOperationException("Cannot handle closing instances of " + o.getClass().getName());
				}

			} catch (UnsupportedOperationException e) {
				throw e;

			} catch (Exception e) {
				logger.warn("Failed to close resource. " + o.getClass().getName(), e);
			}
		}
	}
}
