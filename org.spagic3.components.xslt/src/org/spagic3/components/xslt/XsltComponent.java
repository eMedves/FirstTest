/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spagic3.components.xslt;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.core.BaseSpagicService;
import org.spagic3.core.resources.IResource;


public class XsltComponent extends BaseSpagicService {
	
	protected Logger logger = LoggerFactory.getLogger(XsltComponent.class);
	private TransformerFactory transformerFactory;
    
    
    public IResource xslt = null;
	
	
	public void init(){
		try{
			this.xslt = propertyConfigurator.getResource("xslt");
		
			
			
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(" Cannot instantiate XsltComponent Component", e);
		}
	}
    
   
    // Properties
    // -------------------------------------------------------------------------
    public TransformerFactory getTransformerFactory() {
    	logger.debug("XSLT Transformer::getTranformerFactory::->start");
        if (transformerFactory == null) {
        	logger.debug("XSLT Transformer::TransformerFactory is null::create one");
            transformerFactory = TransformerFactory.newInstance();
            logger.debug("XSLT Transformer::TransformerFactory created");
        }
        logger.debug("XSLT Transformer::getTransformerFactory::transformerFactory " + ((transformerFactory != null) ? "transformerFactory is not null " : "transformerFactory IS NULL"));
        return transformerFactory;
    }

    public void setTransformerFactory(TransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }

    public Source getXsltSource() throws Exception {
    	Source xsltSource;
    	logger.debug("XSLT Transformer::getXsltSource::->start");
      
        	logger.debug("XSLT Transformer::getXsltSource::xsltSource is null create it ");
            // lets create a new one each time
            // as we can only read a stream once
            xsltSource = new StreamSource(xslt.openStream());
            logger.debug("XSLT Transformer::getXsltSource::xsltSource created ");
      
        logger.debug("XSLT Transformer::getXsltSource::xsltSource " + ((xsltSource != null) ? "XSLT Source is not null " : "xsltSource IS NULL"));
        return xsltSource;
    }

    

    

	
	public boolean run(Exchange exchange, Message in, Message out)
			throws Exception {
		try{
			Transformer transformer = getTransformerFactory().newTransformer(getXsltSource());
			org.dom4j.Document inDocument = DocumentHelper.parseText((String) in.getBody());
			DocumentSource source = new DocumentSource(inDocument);
			DocumentResult result = new DocumentResult();
			transformer.transform(source, result);

			// return the transformed document
			org.dom4j.Document transformedDoc = result.getDocument();
			out.setBody(transformedDoc.asXML());
			return true;
		}catch (Throwable e) {
			logger.error(e.getMessage(), e);
			e.fillInStackTrace();
			throw new Exception();
		}
	}

   
   
   

    

    
   
    
    

 

}

