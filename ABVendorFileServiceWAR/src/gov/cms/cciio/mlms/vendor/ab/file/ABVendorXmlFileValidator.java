/**
 * 
 */
package gov.cms.cciio.mlms.vendor.ab.file;

import java.io.File;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.util.logging.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author xnieibm
 * 
 *         Validate AB vendor xml file against the schema
 */
public class ABVendorXmlFileValidator {

	Logger logger = Logger.getLogger("ABVendorXmlFileValidator.class");

	Validator validator = null;

	public ABVendorXmlFileValidator() {

		// Validate xml file against schema:
		String schemaNS = XMLConstants.W3C_XML_SCHEMA_NS_URI;
		SchemaFactory schemaFactoy = SchemaFactory.newInstance(schemaNS);
		URL url = ClassLoader.getSystemResource("VendorTransferSchemaV2.xsd"); // return
																				// null
																				// here,
		// need get url from servlet by calling
		// servletContext.getResource("/WEB-INF/lib/myfile");
		logger.info("Schema URL: " + url);
		try {
			Schema schema = schemaFactoy.newSchema(new File(url.getFile()));
			validator = schema.newValidator();
		} catch (SAXException e) {
			logger.log(Level.SEVERE,"The AB Vendor XML Schema file can not be read! " + e.getMessage());
			e.printStackTrace();
		}

	}

	public boolean isValid(InputSource is) {

		boolean result = false;

		try {
			if (validator != null) {
				validator.validate((Source) is);
				result = true;
			}
		} catch (Exception e) {
			result = false;
			logger.log(Level.SEVERE,"Exception in Validation: " + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

}
