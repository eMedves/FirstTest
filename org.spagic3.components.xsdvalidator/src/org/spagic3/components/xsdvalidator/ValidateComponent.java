package org.spagic3.components.xsdvalidator;

import java.io.File;
import java.io.StringReader;
import java.net.URL;

import javax.script.Compilable;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.apache.servicemix.nmr.api.Status;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.SAXReader;
import org.spagic3.core.BaseSpagicService;
import org.spagic3.core.ExchangeUtils;
import org.spagic3.core.SpagicUtils;
import org.xml.sax.InputSource;


public class ValidateComponent extends BaseSpagicService {

	public static final String FAULT_FLOW = "FAULT_FLOW";
	public static final String FAULT_WITH_EXCEPTION = "FAULT_WITH_EXCEPTION";
	private Schema schema;
	private String schemaLanguage = "http://www.w3.org/2001/XMLSchema";
	private Source schemaSource;
	private boolean includeStackTraceInError = false;
	private String schemaURLString = null;
	private String faultHandlingMethod = null;
	private  URL schemaURL = null;
	
	
	public void init(){
		try{
			schemaURLString = propertyConfigurator.getString("schema");
			includeStackTraceInError = propertyConfigurator.getBoolean("includeStackTraceInError");
			this.schemaURL = SpagicUtils.getURL(schemaURLString);
            SchemaFactory factory = SchemaFactory.newInstance(schemaLanguage);
            schemaSource = new StreamSource(schemaURL.openStream());
            schema = factory.newSchema(schemaSource);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(" Cannot instantiate Validate Component", e);
		}
	}	

	@Override
	public boolean run(Exchange exchange, Message in, Message out)
			throws Exception {
		
		Validator validator = schema.newValidator();

        MessageAwareErrorHandler errorHandler = new MessageAggregatingErrorHandler("ERROR", "urn:eng:spagic3", includeStackTraceInError);
        validator.setErrorHandler(errorHandler);
        DOMResult result = new DOMResult();
        

       
            // Only DOMSource and SAXSource are allowed for validating
            // See http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/validation/Validator.html#validate(javax.xml.transform.Source,%20javax.xml.transform.Result)
            // As we expect a DOMResult as output, we must ensure that the input is a 
            // DOMSource
        	
        	Document dom4jDocument = DocumentHelper.parseText((String)in.getBody());
        
            DOMSource src = new DOMSource(transformtoDOM(dom4jDocument));
            validator.validate(src, result);
            if (errorHandler.hasErrors()) {
             
                if (!faultHandlingMethod.equalsIgnoreCase(FAULT_FLOW)){
                	throw new Exception("Failed to validate against schema: " + schema );
                }else{
                	out.setBody(errorHandler.getErrorMessageAsXML());
                	return true;
                }
            } else {
                out.setBody(in.getBody());
                return true;
            }

    }
		
	 public org.w3c.dom.Document transformtoDOM(Document doc) throws DocumentException {
		    DOMWriter writer = new DOMWriter();
		    return writer.write(doc);
	}

	
	
    
	

}
