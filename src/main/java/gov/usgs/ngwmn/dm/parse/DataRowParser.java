package gov.usgs.ngwmn.dm.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;


public class DataRowParser implements Parser {
	
	protected final XMLStreamReader     reader;
	protected final ParseState          state;
	protected final List<Element>       headers;
	protected final Set<String>         ignoredAttributes;
	protected final Map<String, String> contentDefinedElements;

	protected boolean eof;
	
	public DataRowParser(InputStream is) {
		state                  = new ParseState();
		ignoredAttributes      = new HashSet<String>();
		headers				   = new LinkedList<Element>();
		contentDefinedElements = new HashMap<String, String>();
//		reader 				   = new XMLStreamReader(is);
		try {
			reader = USGS_StAXUtils.getXMLInputFactory().createXMLStreamReader(is);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Set true to keep the information from elder elements when flattening
	 * @param isKeepElder
	 * @return 
	 */
	public void setKeepElderInfo(boolean keepElder) {
		state.isKeepElders = keepElder;
	}
	public void setRowElementName(String name) {
		state.maxRowDepthLevel = 1000; // set the ROW_DEPTH_LEVEL so deep that it never triggers
		state.rowElementIdentifier = name;
	}
	public void setCopyDown(boolean copyDown) {
		state.isDoCopyDown = copyDown;
	}
	
	public List<Element> headers() {
		if ( headers.isEmpty() ) {
			for (Element element : state.targetColumnList) {
				if ( ! element.hasChildren ) {
					headers.add( element );
				}
			}
		}
		return headers;
	}
	public Map<String, String> currentRow() {
		return state.targetColumnValues;
	}
	public Map<String, String> nextRow() throws IOException {
		boolean done = eof;
		
		try {
			while ( ! done && reader.hasNext() ) {
				int event = reader.next();
				
				switch (event) {
					case XMLStreamConstants.START_DOCUMENT:
						break; // no start document handling needed
					case XMLStreamConstants.START_ELEMENT:
						startElement(reader, state);
						break;
					case XMLStreamConstants.CHARACTERS:
						state.putChars(reader.getText().trim());
						break;
					case XMLStreamConstants.ATTRIBUTE:
						// TODO may need to handle this later
						break;
					case XMLStreamConstants.END_ELEMENT:
						// this is where writing to the stream happens
						// before this it was all setup
						done = endElement(); // the end elements for elders will be handled on nextRow
	//					endElement(in, out, state, checker);
						break;
					case XMLStreamConstants.END_DOCUMENT:
						eof = true;
						break;
					// TODO no default
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// if currentRow is last Row then returns empty set
		return  currentRow();
	}
	
	@SuppressWarnings("unchecked")
	private boolean endElement() {
		String localName = reader.getLocalName();
		boolean onTargetEnd = state.isOnTargetRowStartOrEnd(localName);
		state.finishEndElement(onTargetEnd);

		if (onTargetEnd) {
			if (state.isProcessingHeaders) {		
				if (state.isKeepElders) {
					updateQualifiedNames(state.elderColumnList, state.targetColumnList);
				} else {
					updateQualifiedNames(state.targetColumnList);
				}
			}
			// if on target end but has no data then we will want to continue to the next record
			onTargetEnd &= state.hasTargetContent();
		}
		
		return onTargetEnd;
	}
	
	protected void updateQualifiedNames(Set<Element> ... columnLists) {

		boolean hasDuplicates = true;
		while (hasDuplicates) { //for (int i = 0; hasDuplicates && i < 10; i++) { //Use this way to make sure we don't infinite loop?
			hasDuplicates = false;
			Set<String> allDisplayNames = new HashSet<String>();
			Set<String> duplicates = new HashSet<String>();
			// First create a collection of the duplicates. We only care about the
			// childless ones, however.
			for (Set<Element> columnList: columnLists) {
				for (Element element: columnList) {
					if ( ! element.hasChildren ) { // looking for leaf nodes
						boolean isUnique = allDisplayNames.add(element.displayName);
						if ( ! isUnique ) {
							duplicates.add(element.displayName);
							hasDuplicates = true;
						}
					}
				}
			}

			// now go through and update the qualified name
			for (Set<Element> columnList: columnLists) {
				for (Element element: columnList) {
					if ( ! element.hasChildren ) { // looking for leaf nodes
						if (duplicates.contains(element.displayName)) {
							element.addParentToDisplayName(); //displayName = URIUtils.parseQualifiedName(element.fullName, element.displayName);
						}
					}
				}
			}
		}
	}
	protected void startElement(XMLStreamReader in, ParseState state) {
		

		String  localName   = in.getLocalName();
		String  displayName = state.startElementBeginUpdate(in);

		if ( state.isTargetFound() && state.isInTarget ) {
			// PROCESS THE ELEMENT HEADERS
			// Read and record the column headers from the first row's elements.
			// Add columns for later rows, but they don't get headers because
			// we're streaming and can't go back to the column headers.
			state.addHeaderOrColumn(localName, displayName);
			processAttributeHeadersNamesValues(in, state.current(), 
					state.targetColumnList, state.targetColumnValues);
			
		} else if ( state.isKeepElders && ! state.isInTarget ) {
			state.addElderHeaderOrColumn(localName);
			processAttributeHeadersNamesValues(in, state.current(), 
					state.elderColumnList, state.elderColumnValues);
		}
	}
	
	protected void processAttributeHeadersNamesValues(XMLStreamReader in, String currentState, 
			Set<Element> elements, Map<String,String> values) {
		
		String  localName            = in.getLocalName();
		boolean isContentDefined     = isCurrentElementContentDefined(in);
		String  contentAttributeName = isContentDefined ? contentDefinedElements.get(localName) : null;
		
		// PROCESS/STORE ATTRIBUTE HEADERS AND NAME/VALUES
		for (int i=0; i<in.getAttributeCount(); i++) {
			String attLocalName = in.getAttributeLocalName(i);
			
			if ( ! ignoredAttributes.contains(attLocalName) 
			  && ! (isContentDefined && attLocalName.equals(contentAttributeName)) ) {
				String fullName = makeFullName(currentState, attLocalName);
				elements.add( new Element(fullName, attLocalName, null) );
				values.put(fullName, in.getAttributeValue(i).trim());
			}
		}
	}
	protected boolean isCurrentElementContentDefined(XMLStreamReader in) {
		String localName = in.getLocalName();
		String contentAttribute = contentDefinedElements.get(localName);
		if (contentAttribute != null) {
			// This is a content defined element only if it
			// matches the local name and has a corresponding attribute value;
			return in.getAttributeValue(null, contentAttribute) != null;
		}
		return false;
	}
	protected String makeFullName(String context, String name) {
		return (context.length() > 0)? context + Element.SEPARATOR + name: name;
	}
}
