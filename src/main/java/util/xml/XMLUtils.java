package util.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

/**
 * Utility-class with functions for XML.
 */
public class XMLUtils {
    /**
     * Validates XML document using XSD schema.
     *
     * @param document     document to validate
     * @param pathToSchema path to document with XSD schema
     * @return {@code true} if document is valid
     */
    public static boolean validate(Document document, String pathToSchema) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(pathToSchema));
            Validator validator = schema.newValidator();

            validator.validate(new DOMSource(document));
            return true;
        } catch (IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses document from specified location and returns it.
     *
     * @param path filename of XML document
     * @return parsed XML document
     */
    public static Document parseDocument(String path) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            return documentBuilderFactory.newDocumentBuilder().parse(new File(path));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
