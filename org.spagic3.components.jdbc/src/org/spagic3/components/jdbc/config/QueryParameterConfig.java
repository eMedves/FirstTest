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
package org.spagic3.components.jdbc.config;

import java.sql.Blob;

/**
 * Configuration used to treat parameters described in xml input messages
 * that have to be mapped into query statement.
 *
 */
public class QueryParameterConfig {
	
	/*
	 * Check if is possible to use types defined on java.sql.Types and
	 * obtain classes associated to each type.
	 */
	
	public static final String STRING_TYPE = String.class.getName();
	public static final String INT_TYPE = Integer.class.getName();
	public static final String LONG_TYPE = Long.class.getName();
	public static final String FLOAT_TYPE = Float.class.getName();
	public static final String BOOLEAN_TYPE = Boolean.class.getName();
	public static final String BINARY_TYPE = byte.class.getName();
	public static final String BINARY_BASE64_TYPE = "BINARY_BASE64_TYPE".intern();
	public static final String BLOB = Blob.class.getName();
	
	private String placeHolder;
	private String xpath;
	private String paramType;
	private boolean outputParam;

	public QueryParameterConfig(String placeHolder, String xpath, String paramType) {
		super();
		this.placeHolder = placeHolder;
		this.xpath = xpath;
		this.paramType = paramType;
	}
	
	public QueryParameterConfig() {}
	
	public String getParamType() {
		return paramType;
	}
	public void setParamType(String paramType) {
		this.paramType = paramType;
	}
	public String getPlaceHolder() {
		return placeHolder;
	}
	public void setPlaceHolder(String placeHolder) {
		this.placeHolder = placeHolder;
	}
	public String getXpath() {
		return xpath;
	}
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public boolean isOutputParam() {
		return outputParam;
	}

	public void setOutputParam(boolean outputParam) {
		this.outputParam = outputParam;
	}
	
	@Override
	public String toString() {
		return new StringBuffer("QueryParameterConfig [")
			.append("placeHolder=").append(placeHolder).append(",")
			.append("xpath=").append(xpath).append(",")
			.append("paramType=").append(paramType).append(",")
			.append("outputParam=").append(outputParam).append(",").toString();
	}
}
