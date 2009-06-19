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
package org.spagic3.components.xsdvalidator;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * An implementation of {@link ErrorHandler} which aggregates all warnings and
 * error messages into a StringBuffer.
 *
 * @version $Revision: 359186 $
 */
public class MessageAggregatingErrorHandler implements MessageAwareErrorHandler {
    
    private static final String openCDATA = "<![CDATA[";
    private static final String closeCDATA = "]]>";
    private static final String openError = "<error>";
    private static final String closeError = "</error>";
    private static final String openFatalError = "<fatalError>";
    private static final String closeFatalError = "</fataError>";
    private static final String openWarning = "<warning>";
    private static final String closeWarning = "</warning>";
    
    private String openRootElement;
    private String closeRootElement;

    /**
     * Number of warnings.
     */
    private int warningCount;
    
    /**
     * Number of errors.
     */
    private int errorCount;
    
    /**
     * Number of fatal errors.
     */
    private int fatalErrorCount;
    
    /**
     * The root element name for the fault xml message
     */
    private String rootPath;
    
    /**
     * The namespace for the fault xml message
     */
    private String namespace;
    
    /**
     * Determines whether or not to include stacktraces in the fault xml message
     */
    private boolean includeStackTraces;
    
    /**
     * Variable to hold the warning/error messages from the validator
     */
    private StringBuffer messages = new StringBuffer();
    
  
    
    /**
     * Constructor.
     * 
     * @param rootElement
     *      The root element name of the fault xml message 
     * @param namespace
     *      The namespace for the fault xml message
     * @param includeStackTraces
     *      Include stracktraces in the final output
     */
    public MessageAggregatingErrorHandler(String rootPath, String namespace, boolean includeStackTraces) throws IllegalArgumentException {
        if (rootPath == null || rootPath.trim().length() == 0) {
            throw new IllegalArgumentException("rootPath must not be null or an empty string");
        }
        this.rootPath = rootPath;
        this.namespace = namespace;
        this.includeStackTraces = includeStackTraces;
        createRootElementTags();
    }

    /**
     * Creates the root element tags for later use down to n depth.
     * Note: the rootPath here is of the form:
     * 
     *      <code>rootElementName/elementName-1/../elementName-n</code>
     * 
     * The namespace will be appended to the root element if it is not
     * null or empty.
     */
    private void createRootElementTags() {
        /* 
         * since the rootPath is constrained to be not null or empty
         * then we have at least one path element.
         */
        String[] pathElements = rootPath.split("/");

        StringBuffer openRootElementSB = new StringBuffer().append("<").append(pathElements[0]);
        StringBuffer closeRootElementSB = new StringBuffer();
        
        if (namespace != null && namespace.trim().length() > 0) {
            openRootElementSB.append(" xmlns=\"").append(namespace).append("\">"); 
        } else {
            openRootElementSB.append(">");
        }
        
        if (pathElements.length > 0) {
            for (int i = 1, j = pathElements.length - 1; i < pathElements.length; i++, j--) {
                openRootElementSB.append("<").append(pathElements[i]).append(">");
                closeRootElementSB.append("</").append(pathElements[j]).append(">");
            }
        }
        
        // create the closing root element tag
        closeRootElementSB.append("</").append(pathElements[0]).append(">");
        
        openRootElement = openRootElementSB.toString();
        closeRootElement = closeRootElementSB.toString();
    }
    
    /*  (non-Javadoc)
     * @see org.apache.servicemix.components.validation.MessageAwareErrorHandler#hasErrors()
     */
    public boolean hasErrors() {
        return getErrorCount() > 0 || getFatalErrorCount() > 0;
    }

    /*  (non-Javadoc)
     * @see org.apache.servicemix.components.validation.MessageAwareErrorHandler#getWarningCount()
     */
    public int getWarningCount() {
        return warningCount;
    }

    /*  (non-Javadoc)
     * @see org.apache.servicemix.components.validation.MessageAwareErrorHandler#getErrorCount()
     */
    public int getErrorCount() {
        return errorCount;
    }

    /*  (non-Javadoc)
     * @see org.apache.servicemix.components.validation.MessageAwareErrorHandler#getFatalErrorCount()
     */
    public int getFatalErrorCount() {
        return fatalErrorCount;
    }

    /*  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException e) throws SAXException {
        ++warningCount;

        // open warning and CDATA tags
        messages.append(openWarning).append(openCDATA);
        
        // append the fatal error message
        appendErrorMessage(e);
        
        // close CDATA and warning tags
        messages.append(closeCDATA).append(closeWarning);
    }

    /*  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException e) throws SAXException {
        ++errorCount;

        // open fatal error and CDATA tags
        messages.append(openError).append(openCDATA);
        
        // append the error message
        appendErrorMessage(e);
        
        // close CDATA and error tags
        messages.append(closeCDATA).append(closeError);
    }

    /*  (non-Javadoc)
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException e) throws SAXException {
        ++fatalErrorCount;
        
        // open fatal error and CDATA tags
        messages.append(openFatalError).append(openCDATA);
        
        // append the fatal error message
        appendErrorMessage(e);
        
        // close CDATA and fatal error tags
        messages.append(closeCDATA).append(closeFatalError);
    }

    /**
     * Append the error message or stacktrace to the messages attribute.
     * 
     * @param e
     */
    private void appendErrorMessage(SAXParseException e) {
        if (includeStackTraces) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            messages.append(sw.toString());
        } else {
            messages.append(e.getLocalizedMessage());
        }
    }
    
    

    

    
    
    /**
     * Return the messages encapsulated with the root element.
     * 
     * @return
     */
    public String getErrorMessageAsXML() {
        return new StringBuffer().append(openRootElement).append(messages).append(closeRootElement).toString();
    }
    
    
    
    
   
    
}
