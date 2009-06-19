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

import org.xml.sax.ErrorHandler;

public interface MessageAwareErrorHandler extends ErrorHandler {
	
    /**
     * Does the error handler contain any error messages?
     * 
     * @return
     */
	public boolean hasErrors();

    /**
     * Return the number of warnings.
     * 
     * @return
     */
	public int getWarningCount();

    /**
     * Return the number of errors.
     * 
     * @return
     */
	public int getErrorCount();

    /**
     * Return the number of fatal errors.
     * 
     * @return
     */
    public int getFatalErrorCount();

	
	
	
	public String getErrorMessageAsXML();
	
	
}
