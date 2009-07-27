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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbutils.ResultSetHandler;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ResultSetHandler implementation that put the query result in a dom4j Element.
 * 
 * @author buso
 * 
 */
/*
 * TODO Propose this code to DBUtils maintainers.
 */
public class XmlResultSetHandler implements ResultSetHandler {
	private String rowsXmlEnvelopName = "rows";
	private String rowXmlEnvelopName = "row";
	private Boolean columnNameAsAttribute;
	private Boolean valueAsAttribute;
	private static final Logger logger = LoggerFactory.getLogger(XmlResultSetHandler.class);

	public XmlResultSetHandler(String rowsXmlEnvelopName, String rowXmlEnvelopName, Boolean columnNameAsAttribute,
			Boolean valueAsAttribute) {
		super();
		if (rowsXmlEnvelopName != null && !rowsXmlEnvelopName.trim().equals("")) {
			this.rowsXmlEnvelopName = rowsXmlEnvelopName;
		}
		if (rowXmlEnvelopName != null && !rowXmlEnvelopName.trim().equals("")) {
			this.rowXmlEnvelopName = rowXmlEnvelopName;
		}
		this.columnNameAsAttribute = columnNameAsAttribute;
		this.valueAsAttribute = valueAsAttribute;
	}

	public XmlResultSetHandler(String rowsXmlEnvelopName, String rowXmlEnvelopName) {
		new XmlResultSetHandler(rowsXmlEnvelopName, rowXmlEnvelopName, false, false);
	}

	protected String handleBlob(Blob aBlob) {
		try {
			logger.debug("Handling BLOB");

			byte[] returnBytes;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			logger.debug("Calling Blog get Binary Stream");
			InputStream inputStream = aBlob.getBinaryStream();

			int inByte;
			logger.debug("Writing BLOB TO Temporary Array");
			while ((inByte = inputStream.read()) != -1) {
				byteArrayOutputStream.write(inByte);
			}

			byteArrayOutputStream.flush();
			returnBytes = byteArrayOutputStream.toByteArray();
			logger.debug("BLOB Readed in bytearray of length[" + returnBytes.length + "]");
			byteArrayOutputStream.close();

			if (returnBytes != null && returnBytes.length > 0) {
				return new String(Base64.encodeBase64(returnBytes));
			} else {
				logger.debug("BinaryValue is Null returning empty string");
				return "";
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "null";
		}
	}

	public Object handle(ResultSet rs) throws SQLException {

		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();

		Element rows = DocumentHelper.createElement(rowsXmlEnvelopName);
		Element row = null;
		while (rs.next()) {
			row = DocumentHelper.createElement(rowXmlEnvelopName);
			Element column = null;
			for (int i = 1; i <= cols; i++) {
				Object value = rs.getObject(i);
				if (columnNameAsAttribute) {
					column = DocumentHelper.createElement("column");
					column.addAttribute("name", rsmd.getColumnName(i));
				} else {
					column = DocumentHelper.createElement(rsmd.getColumnName(i));
				}

				String text = null;
				int columnType = rsmd.getColumnType(i);
				if (columnType == Types.BLOB) {
					text = (value != null) ? handleBlob((Blob) value) : "";
				} else if (columnType == Types.BINARY) {
					text = (value != null) ? new String(Base64.encodeBase64((byte[]) value)) : "";
				} else {
					text = (value != null) ? value.toString() : "";
				}
				
				if (valueAsAttribute) {
					column.addAttribute("value", text);
				} else {
					column.setText(text);
				}
				
				row.add(column);
			}
			rows.add(row);
		}
		return rows;
	}

	public String getRowsXmlEnvelopName() {
		return rowsXmlEnvelopName;
	}

	public void setRowsXmlEnvelopName(String rowsXmlEnvelopName) {
		this.rowsXmlEnvelopName = rowsXmlEnvelopName;
	}

	public String getRowXmlEnvelopName() {
		return rowXmlEnvelopName;
	}

	public void setRowXmlEnvelopName(String rowXmlEnvelopName) {
		this.rowXmlEnvelopName = rowXmlEnvelopName;
	}

}
