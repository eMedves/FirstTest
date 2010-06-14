package org.spagic3.components.hl7;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.eclipse.ebpm.core.BaseSpagicService;
import org.eclipse.ebpm.messaging.api.Exchange;
import org.eclipse.ebpm.messaging.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eng.IMessageTransformer;


/**
 * 
 * 
 * This class is the JBI LightWeight Component That Wrap HL7 Transformation in a Lightweight 
 * Container
 * 
 */
public class HL7Component extends BaseSpagicService {
	 
		private static final Logger log = LoggerFactory.getLogger(HL7Component.class);


	    private String  transformationClass;
	    private String 	transformationType;
	    private String  hl7containerElement = "PLAIN-HL7";
	   
	    private IMessageTransformer hl7Transformer = null;
	    
	  
	    
	    private void loadParameters() throws Exception {
			transformationClass = propertyConfigurator.getString("TransformationClass");
			transformationType = propertyConfigurator.getString("TransformationType");
	    }
	   
	    public void initTransformer() throws Exception  {
	    	try{
	    		log.debug("HL7Component::initTransformer ->  Transformer Class Istantiation ");
	    		hl7Transformer =(IMessageTransformer)Class.forName(transformationClass).newInstance();
	    		log.debug("HL7Component::initTransformer ->  Transformer Class OK ");
	    	}catch (Throwable e) {
	    		log.error("HL7Component::initTransformer ->  Transformer Class Istantiation Failed",e);
	    		throw new Exception("Failed to run compiledComponent. Reason: " + e, e);

			}
	    }
	    
	    
	    
	    @PostConstruct
	    public void init()   {
	    	try{
	    		loadParameters();
	    		initTransformer();}
	    	catch(Throwable e){
	    		logger.error(e.getMessage(), e);
				throw new RuntimeException(" Cannot instantiate HL7 Component", e);
	    	}   
	    }
	   
	    public boolean run (Exchange exchange, Message in, Message out) throws Exception {
	    
	    
	    		//SourceTransformer sourceTransformer = new SourceTransformer();
	    		try {
	    			log.debug("HL7 Getting Input Payload");
	    			String payloadInput = in.getBodyText();
	          
	            
	    			String transfomerInput = null;
	    			String transfomerOutput = null;
	            
	    			if (transformationType.equalsIgnoreCase("plain2xml")){
	    				//Node domNode = sourceTransformer.toDOMNode(new StringSource(payloadInput));
	    				transfomerInput = getPlainHL7(payloadInput);  
	    			}
	    
	    			if (transformationType.equalsIgnoreCase("xml2plain")){
	    				transfomerInput = payloadInput; 
	    			}
	    			log.debug("Performing Transformation......");
	    			log.debug("Input....\n "+transfomerInput);
	    			transfomerOutput = hl7Transformer.runWithString(transfomerInput);
	    			log.debug("Transformer Output....\n "+transfomerOutput);
	            
	    			String outMsg = null;
	    			if (transformationType.equalsIgnoreCase("plain2xml")){
	    				outMsg = transfomerOutput;
	    			}else if (transformationType.equalsIgnoreCase("xml2plain")){
	    				outMsg = wrapInPlainHL7Element(transfomerOutput);
	    			}
	    
	    			if (outMsg != null) {
	    				log.debug("Response: " + out.getBody());
	    				out.setBody(outMsg);
	    				return true;
	    			} else {
	    				Message fault = exchange.getFault(true);
	    				//String source???
	    				fault.setBody("<ERROR>Tranformation produced a null message  </ERROR>");
	    				//fault.setHeader("", payloadInput);
	    				throw new Exception("HL7Tranformation Failed ");
	    
	    			}
	    		}catch(Exception e){
	    			e.printStackTrace();
	    			log.error("Exception in HL7 Component",e);
	    			Message fault = exchange.getFault(true);
	    			fault.setBody("<ERROR>Tranformation produced a null message  </ERROR>");
	    
	    
	    			throw new Exception("HL7Tranformation Failed ");
	    		}
	   
	    }
	    
	   
	    
	   
	   
	   
	   
	    protected String wrapInPlainHL7Element(String output) throws Exception {
	    		return "<"+hl7containerElement+" base64encoded=\"true\"><![CDATA[\n"+new String(Base64.encodeBase64(output.getBytes("UTF-8")))+"\n]]></"+hl7containerElement+">";
	    
	    }
	    
	    public String getPlainHL7(String payloadInput) throws Exception {
	    	log.debug("getPlainHL7 from input message....");
	    	log.debug("getPlainHL7 from input message payload input is " + payloadInput);
	    
	    	Document payloadDocument = DocumentHelper.parseText(payloadInput);
	    
	    	log.debug("getPlainHL7 -> getting HL7 Node....");
	    	Node plainHL7Node = payloadDocument.selectSingleNode(hl7containerElement);
	    	String base64encoded = plainHL7Node.valueOf("@base64encoded");
	    
	    	log.debug("getPlainHL7 -> evaluating base64 encoded attribute value is ["+base64encoded+"]");
	    	boolean mustDecode = false;
	    	if (base64encoded != null && base64encoded.trim().length() > 0){
	    		mustDecode = Boolean.valueOf(base64encoded);
	    	}
	    	log.debug("getPlainHL7 -> must decode is  ["+mustDecode+"]");
	    	String plainHL7Text = plainHL7Node.getText();
	    	if (mustDecode){
	    		log.debug("getPlainHL7 -> return decoded base 64");
	    		return new String(Base64.decodeBase64(plainHL7Text.getBytes("UTF-8")));
	    	}else{
	    		log.debug("getPlainHL7 -> we don't decode ");
	    		return plainHL7Text;
	    	}
	    }
	    


		public String getTransformationType() {
			return transformationType;
		}
		public void setTransformationType(String transformationType) {
			this.transformationType = transformationType;
		}

		public String getTransformationClass() {
			return transformationClass;
		}

		public void setTransformationClass(String transformationClassName) {
			this.transformationClass = transformationClassName;
		}

		public String getHl7containerElement() {
			return hl7containerElement;
		}

		public void setHl7containerElement(String hl7containerElement) {
			this.hl7containerElement = hl7containerElement;
		}



}
