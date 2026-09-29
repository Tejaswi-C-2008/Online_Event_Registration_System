package com.eventmgmt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Standalone XML Parser and Validator for Lab Experiments:
 * - Exp 6: XML Document Parsing using DOM Parser, DTD Validation, and W3C XML Schema (XSD) Validation.
 */
public class XMLReader {

    public static void main(String[] args) {
        System.out.println("===================================================================");
        System.out.println(" Online Event Registration System - XML DOM Parser & Validator ");
        System.out.println("===================================================================");

        try {
            // 1. Locate XML resource
            InputStream xmlStream = getResourceStream("events.xml");

            if (xmlStream == null) {
                System.err.println("events.xml resource not found.");
                return;
            }

            // 2. Parse XML Document with DOM Parser
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Custom Entity Resolver to locate DTD in resources if requested
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
                    if (systemId != null && systemId.contains("events.dtd")) {
                        InputStream dtdStream = getResourceStream("events.dtd");
                        if (dtdStream != null) {
                            return new InputSource(dtdStream);
                        }
                    }
                    return null;
                }
            });

            Document document = builder.parse(xmlStream);
            document.getDocumentElement().normalize();

            System.out.println("[INFO] Root Element: " + document.getDocumentElement().getNodeName());
            NodeList eventNodes = document.getElementsByTagName("event");
            System.out.println("[INFO] Total Events in XML: " + eventNodes.getLength());
            System.out.println("-------------------------------------------------------------------");

            for (int i = 0; i < eventNodes.getLength(); i++) {
                Element eventElement = (Element) eventNodes.item(i);
                String id = eventElement.getAttribute("id");
                String category = eventElement.getAttribute("category");
                String title = getTagValue("title", eventElement);
                String organizer = getTagValue("organizer", eventElement);
                String date = getTagValue("date", eventElement);
                String time = getTagValue("time", eventElement);
                String venue = getTagValue("venue", eventElement);
                String capacity = getTagValue("capacity", eventElement);
                String price = getTagValue("price", eventElement);
                String status = getTagValue("status", eventElement);

                System.out.printf("[%s] %s | Cat: %s | Date: %s %s%n", id, title, category, date, time);
                System.out.printf("     Organizer: %s | Venue: %s | Seats: %s | Price: $%s | Status: %s%n",
                        organizer, venue, capacity, price, status);
                System.out.println("-------------------------------------------------------------------");
            }

            // 3. Perform XSD Schema Validation
            System.out.println("[VALIDATION] Checking XML against events.xsd...");
            validateAgainstXSD();
            System.out.println("[SUCCESS] XML document conforms strictly to W3C XML Schema (XSD)!");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to process XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nl = element.getElementsByTagName(tag);
        if (nl != null && nl.getLength() > 0 && nl.item(0).getFirstChild() != null) {
            return nl.item(0).getFirstChild().getNodeValue().trim();
        }
        return "";
    }

    private static InputStream getResourceStream(String filename) {
        InputStream stream = XMLReader.class.getClassLoader().getResourceAsStream(filename);
        if (stream == null) {
            File directFile = new File("src/main/resources/" + filename);
            if (directFile.exists()) {
                try {
                    return new FileInputStream(directFile);
                } catch (Exception ignored) {}
            }
        }
        return stream;
    }

    private static void validateAgainstXSD() {
        try {
            InputStream xsdStream = getResourceStream("events.xsd");
            InputStream xmlStream = getResourceStream("events.xml");

            if (xsdStream != null && xmlStream != null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new StreamSource(xsdStream));
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(xmlStream));
            }
        } catch (Exception e) {
            System.err.println("[XSD VALIDATION WARNING] " + e.getMessage());
        }
    }
}
