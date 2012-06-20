package gov.usgs.ngwmn.dm.io.parse;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultPostParser implements PostParser {

	@Override
	public List<Element> refineHeaderColumns(Collection<Element> headers) {
		// default no refinements
		return new LinkedList<Element>(headers);
	}

	@Override
	public void refineDataColumns(Map<String, String> data) {
		// default no refinements
	}

	@Override
	public void addConstColumn(String col, String string) {
		// default no additional columns accepted
	}

	@Override
	public Set<String> getRemoveColumns() {
		// default no removed columns
		return new HashSet<String>();
	}

}