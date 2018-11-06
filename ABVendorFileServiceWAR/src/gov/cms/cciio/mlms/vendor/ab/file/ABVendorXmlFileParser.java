/**
 * 
 */
package gov.cms.cciio.mlms.vendor.ab.file;

import gov.cms.cciio.mlms.vendor.ab.model.VendorTransferFileInfo;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author xnieibm
 * 
 */
public class ABVendorXmlFileParser extends DefaultHandler {

	/**
	 * Logger for class.
	 */
	private Logger logger = Logger
			.getLogger("ABVendorXmlFileParser");

	private String elementStrValue = "";

	private final String VENDOR_NAME = "VendorName";
	private final String FILE_ID = "FileIdentifer";
	private final String FILE_DATE = "FileDate";
	private final String FILE_TIME = "FileTime";
	private final String FILE_RECORD_COUNT = "FileRecordCount";
	private final String RECORD = "Record";

	VendorTransferFileInfo fileInfo = null;

	SAXParser parser = null;

	public ABVendorXmlFileParser() {
		try {
			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			saxFactory.setFeature(
					"http://xml.org/sax/features/namespace-prefixes", true);
			saxFactory.setFeature("http://xml.org/sax/features/namespaces",
					true);
			parser = saxFactory.newSAXParser();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"SaxParser initialization failed.", e);

		}
	}

	/**
	 * @param is
	 *            InputSource
	 * @return VendorTrasferFileInfo
	 */

	public VendorTransferFileInfo parseDocument(final InputSource is) {

		logger.info("Start to parse xml file... ");

		if (fileInfo != null) {
			fileInfo = null; // clean the object before the parsing
		}

		try {
			if (parser != null) {
				parser.parse(is, this);
			} else {
				logger.log(Level.SEVERE,"SaxParser is not initialized");
				// TODO throw an exception
			}
		} catch (Exception e) {

			logger.log(Level.SEVERE,"SaxParsing failed.", e);

		}

		return fileInfo;
	}

	@Override
	public void startDocument() throws SAXException {
		fileInfo = new VendorTransferFileInfo();
	}

	@Override
	public void characters(char[] chars, int start, int length)
			throws SAXException {
		elementStrValue = new String(chars, start, length);

	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equalsIgnoreCase(VENDOR_NAME)) {
			fileInfo.setVendorName(elementStrValue);
			logger.info("Vendor name: " + elementStrValue);
		} else if (localName.equalsIgnoreCase(RECORD)) {
			fileInfo.getRecords().add(elementStrValue);
		} else if (localName.equalsIgnoreCase(FILE_ID)) {
			fileInfo.setFileID(elementStrValue);
		} else if (localName.equalsIgnoreCase(FILE_RECORD_COUNT)) {
			fileInfo.setFileRecordCount(elementStrValue);
		} else if (localName.equalsIgnoreCase(FILE_DATE)) {
			fileInfo.setFileDate(elementStrValue);
		} else if (localName.equalsIgnoreCase(FILE_TIME)) {
			fileInfo.setFileTime(elementStrValue);
		}
	}

	// @Override
	// public void endDocument() throws SAXException {
	//
	// }

}
