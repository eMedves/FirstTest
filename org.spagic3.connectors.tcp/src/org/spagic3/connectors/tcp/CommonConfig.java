/**

    Copyright 2007, 2008 Engineering Ingegneria Informatica S.p.A.

    This file is part of Spagic.

    Spagic is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    any later version.

    Spagic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
**/
package org.spagic3.connectors.tcp;

import java.net.URI;

public class CommonConfig {
	/* COMMON CONFIG */
	public static final String OPERATION_MODE_BIDIRECTIONAL = "OPERATION_MODE_BIDIRECTIONAL".intern();
	public static final String OPERATION_MODE_OUT_IN = "OPERATION_MODE_OUT_IN".intern();
	public static final String OPERATION_MODE_IN_OUT = "OPERATION_MODE_IN_OUT".intern();
	public static final String OPERATION_MODE_IN = "OPERATION_MODE_IN".intern();
	public static final String OPERATION_MODE_OUT = "OPERATION_MODE_OUT".intern();
	
	public static final String POINT_TYPE_CLIENT = "POINT_TYPE_CLIENT";
	public static final String POINT_TYPE_SERVER = "POINT_TYPE_SERVER";
	
	public static final String FAIL_ACTION_MSG_TO_QUEUE = "FAIL_ACTION_MSG_TO_QUEUE".intern();
	public static final String FAIL_ACTION_CLOSE_CONN = "FAIL_ACTION_CLOSE_CONN".intern();
	public static final String FAIL_ACTION_MSG_TO_QUEUE_CLOSE_CONNECTION = "FAIL_ACTION_MSG_TO_QUEUE_CLOSE_CONNECTION".intern();
	
	public static final String RETRY_TYPE_NO_RETRY = "RETRY_TYPE_NO_RETRY";
	public static final String RETRY_TYPE_IMMEDIATE = "RETRY_TYPE_IMMEDIATE";
	public static final String RETRY_TYPE_LINEAR = "RETRY_TYPE_LINEAR";
	public static final String RETRY_TYPE_EXPONENTIAL = "RETRY_TYPE_EXPONENTIAL";
	
	/**default in-out mep mode url.*/
	public static final String DEFINOUTMEP =
		"http://www.w3.org/2004/08/wsdl/in-out";
	/**default in-only mep mode url.*/
	public static final String DEFINMEP =
		"http://www.w3.org/2004/08/wsdl/in-only";
	
	public static final String DEFOUTMEP = 
		"http://www.w3.org/2004/08/wsdl/out-only";

	private String pointName;
	private String pointType;
	private String pointMode;
	
	/* low implementation priority */
	private int inputThrottling = -1;
	private boolean inputThrottlingIndividualConnection = false;
	private int outThrottling = -1;
	private boolean outThrottlingIndividualConnection = false;

	/* Response communication settings */
	private long responseTimeout = 10*1000;
	private int retryCount = 1;
	private String failAction;

	private int connNumber = -1;
	
	/* Connection initialization settings */
	private int retryNumber = 3;
	private String retryType;
	private long retryDelay;
	
	/* JBI Marshalling/Unmarshalling */
	private String outNmEnvelope;
	private String inNmEnvelope;
	private URI defaultMep;
	private boolean base64encode = true;
	private boolean base64decode = true;
	
	public long getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(long retryDelay) {
		this.retryDelay = retryDelay;
	}

	public int getRetryNumber() {
		return retryNumber;
	}

	public void setRetryNumber(int retryNumber) {
		this.retryNumber = retryNumber;
	}

	public int getConnNumber() {
		return connNumber;
	}

	public void setConnNumber(int connNumb) {
		this.connNumber = connNumb;
	}

	public String getFailAction() {
		return failAction;
	}

	public void setFailAction(String failAction) {
		this.failAction = failAction;
	}

	public int getInputThrottling() {
		return inputThrottling;
	}

	public void setInputThrottling(int inputThrottling) {
		this.inputThrottling = inputThrottling;
	}

	public boolean isInputThrottlingIndividualConnection() {
		return inputThrottlingIndividualConnection;
	}

	public void setInputThrottlingIndividualConnection(
			boolean inputThrottlingIndividualConnection) {
		this.inputThrottlingIndividualConnection = inputThrottlingIndividualConnection;
	}

	public int getOutThrottling() {
		return outThrottling;
	}

	public void setOutThrottling(int outThrottling) {
		this.outThrottling = outThrottling;
	}

	public boolean isOutThrottlingIndividualConnection() {
		return outThrottlingIndividualConnection;
	}

	public void setOutThrottlingIndividualConnection(
			boolean outThrottlingIndividualConnection) {
		this.outThrottlingIndividualConnection = outThrottlingIndividualConnection;
	}

	public String getPointMode() {
		return pointMode;
	}

	public void setPointMode(String pointMode) {
		this.pointMode = pointMode;
	}

	public String getPointName() {
		return pointName;
	}

	public void setPointName(String pointName) {
		this.pointName = pointName;
	}

	public String getPointType() {
		return pointType;
	}

	public void setPointType(String pointType) {
		this.pointType = pointType;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public String getRetryType() {
		return retryType;
	}

	public void setRetryType(String retryType) {
		this.retryType = retryType;
	}

	public long getResponseTimeout() {
		return responseTimeout;
	}

	public void setResponseTimeout(long timeout) {
		this.responseTimeout = timeout;
	}

	public String getOutNmEnvelope() {
		return outNmEnvelope;
	}

	public void setOutNmEnvelope(String nmEnvelope) {
		this.outNmEnvelope = nmEnvelope;
	}

	public URI getDefaultMep() {
		return defaultMep;
	}

	public void setDefaultMep(URI defaultMep) {
		this.defaultMep = defaultMep;
	}

	public String getInNmEnvelope() {
		return inNmEnvelope;
	}

	public void setInNmEnvelope(String inNmEnvelope) {
		this.inNmEnvelope = inNmEnvelope;
	}

	public boolean isBase64encode() {
		return base64encode;
	}

	public void setBase64encode(boolean base64encode) {
		this.base64encode = base64encode;
	}
	
	public boolean isUseOutNMEnvelope() {
		return outNmEnvelope != null && !outNmEnvelope.trim().equals("");
	}
	
	public boolean isUseInNMEnvelope() {
		return inNmEnvelope != null && !inNmEnvelope.trim().equals("");
	}

	public boolean isBase64decode() {
		return base64decode;
	}

	public void setBase64decode(boolean base64decode) {
		this.base64decode = base64decode;
	}
}
