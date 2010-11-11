package org.spagic3.extension.servicedescriptor;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ebpm.core.servicedefinitions.extensionprovider.AbstractServiceDefinition;
import org.eclipse.ebpm.core.servicedefinitions.extensionprovider.IServiceDefinition;
import org.spagic3.servicedefinition.hl7.Spagic3Hl7SvcActivator;
import org.xml.sax.SAXException;

public class HL7Descriptor extends AbstractServiceDefinition implements IServiceDefinition {

	public HL7Descriptor() throws IOException, ParserConfigurationException, SAXException {
		URL scrappyDefFile = Spagic3Hl7SvcActivator.getDefault().getFileURL("/conf/hl7-def.xml");
		
        // Parse the XML as a W3C document.
		DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();
		xmlFact.setNamespaceAware(true);
        DocumentBuilder builder = xmlFact.newDocumentBuilder();
        
		setServiceDefDocument(builder.parse(scrappyDefFile.openStream()));

	}
	
}
