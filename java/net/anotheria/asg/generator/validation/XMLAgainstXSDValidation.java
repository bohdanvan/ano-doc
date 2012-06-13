package net.anotheria.asg.generator.validation;

import net.anotheria.asg.generator.util.IncludedDocuments;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;


public final class XMLAgainstXSDValidation {

    private static final String JAXP_SCHEMA_LANGUAGE =  "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    private static final String SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    public static void validateAgainstXSDSchema(String nameOfFile,String content,InputStream inputStream,IncludedDocuments includedDocuments){
        File tempXSDFile = null;
        try {
            System.out.println("----------VALIDATING "+nameOfFile+" STARTED");

            // create file xsd from input stream to validate xml against it
            tempXSDFile = createTempXSDFile(inputStream);

            DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            factory.setAttribute(JAXP_SCHEMA_LANGUAGE,W3C_XML_SCHEMA );
            factory.setAttribute(SCHEMA_SOURCE,tempXSDFile);

            DocumentBuilder builder = factory.newDocumentBuilder();
            // create and set error handler
            XMLAgainstXSDErrorHandler XMLAgainstXSDErrorHandler = new XMLAgainstXSDErrorHandler(includedDocuments);
            builder.setErrorHandler(XMLAgainstXSDErrorHandler);

            final InputStream contentOfFileAsInputStream = new ByteArrayInputStream(content.getBytes());

            builder.parse(new InputSource(contentOfFileAsInputStream));

            if (includedDocuments != null){
                includedDocuments.clearListOfIncludedDocuments();
            }
            // the program will terminate if xml file has errors
            if (XMLAgainstXSDErrorHandler.isHasErrors()){
                tempXSDFile.delete();
                System.exit(-1);
            }

            System.out.println("----------VALIDATING "+nameOfFile+" FINISHED SUCCESSFULLY");
        } catch (ParserConfigurationException e) {
            System.out.println("----------Error: ParserConfigurationException "+e.getMessage());
            throw new RuntimeException("ParserConfigurationException.",e);
        } catch (SAXException e) {
            System.out.println("----------Error: SAXException "+e.getMessage());
            throw new RuntimeException("SAXException.",e);
        } catch (IOException e) {
            System.out.println("----------Error: IOException"+e.getMessage());
            throw new RuntimeException("IOException.",e);
        } finally {
            if (tempXSDFile != null) {
                tempXSDFile.delete();
            }

        }
    }

    private static File createTempXSDFile(InputStream inputStream){
        try {
            File tempFile = File.createTempFile("temp-valid",".xsd");
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile,true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            inputStream.close();
            fileOutputStream.close();
            return tempFile;
        } catch (IOException e) {
            System.out.println("----------Error: IOException" + e.getMessage());
            throw new RuntimeException("IOException.",e);
        }
    }
}
