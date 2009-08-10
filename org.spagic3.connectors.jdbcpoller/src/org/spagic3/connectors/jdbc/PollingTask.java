/**

    Copyright 2007, 2009 Engineering Ingegneria Informatica S.p.A.

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
package org.spagic3.connectors.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.TimerTask;

import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollingTask extends TimerTask {
    
	protected Logger log = LoggerFactory.getLogger(JDBCPoller.class);
	
	private static final String KEY_QUERY = "SELECT VALUE FROM KEY_VALUES WHERE ID=?";
	private static final String KEY_INSERT = "INSERT INTO KEY_VALUES (ID,VALUE) VALUES (?,?)";
	private static final String KEY_UPDATE = "UPDATE KEY_VALUES SET VALUE=? WHERE ID=?";

	private PollingParameters pollingParams;
	// JNDI datasource
	private DataSource datasource;
	
	// Current record management
	// JNDI datasource for keys table
	private DataSource keyDatasource;
	private JDBCPoller poller;
	
	// Indicates if the task can run properly (e.g. because all datasources has been retrieved)
	private boolean initialized = false;

	public DataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(DataSource datasource) {
		this.datasource = datasource;
	}
	
	public String getKeyCreationStatement() {
		return pollingParams.keyCreationStatement;
	}

	public class ConditionKeyNotFoundException extends Exception {
		private static final long serialVersionUID = 1L;

		public ConditionKeyNotFoundException(String message) {
			super(message);
		}
	}

	public PollingTask(PollingParameters pollingParams, JDBCPoller poller) throws Exception {
		this.pollingParams = pollingParams;
		this.poller = poller;
	}
	
	public void initializeKey() {
		if (initialized)
			return;

		// Try to update the table for keys: if it already exists, the statement
		// will have no effect
		Connection keyConnection = null;
		try {
			keyConnection = openKeyConnection();
			if (keyConnection == null)
				// Connection still not available
				return;
			
			// Verify if the key table already exists
			updateConditionKey(keyConnection, null, null);
			log.debug("Table for keys already existing");

			// Verify if the row containing the condition key exists
			try {
				// Retrieve condition key from our database
				getConditionKey(keyConnection, pollingParams.commPointID);
				log.debug("Row for condition key already existing");
				initialized = true;
			} catch (ConditionKeyNotFoundException ex) {
				// Insert the row containing the initial key value
				insertConditionKey(keyConnection, pollingParams.initialKeyValue, pollingParams.commPointID);
				log.debug("Row for condition key created");
				initialized = true;
			}
		} catch (Exception ex) {
			log.debug("Trying to create table for keys");
			try {
				createConditionKeyTable(keyConnection, pollingParams.initialKeyValue, pollingParams.commPointID);
				initialized = true;
			} catch (Exception e) {
				log.debug("Unable to create table for keys");
			}
		} finally {
			if (keyConnection != null) {
				try {
					keyConnection.close();
				} catch (SQLException e) {
					log.error("Unable to close key connection");
				}
			}
		}
		
	}
	
    
	/**
	 * Opens the connection used for polling
	 * 
	 * @return Database connection
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected Connection openConnection() throws ClassNotFoundException, SQLException {
		// Open database connection or get it from the datasource
		Connection conn = null;
		if (datasource == null) {
			log.debug("Lookup of datasource: " + pollingParams.datasourceName);
			
			try {
				try {
					datasource = poller.getDataSourceManager().getDataSource(pollingParams.datasourceName);
				} catch (Exception e) {
					log.warn("Datasource " + pollingParams.datasourceName + " not available. Polling not executed.");
					return null;
				}
			} catch (Exception e) {
				log.error("Error during lookup of datasource", e);
				try {
					log.error("Deactivating polling task for JDBC Poller");
					// Cancels this timer task: it will never run again
					cancel();
					poller = null;
				} catch (Exception e1) {
					log.error("Error deactivating component JDBC Poller");
				}
			}
		}
		conn = datasource.getConnection();
		return conn;
	}

	/**
	 * Method that performs the database polling
	 */
	public void run() {
		initializeKey();
		
		if (!initialized)
			return;
		
		log.debug("Entering pollDatabase");
		Connection conn = null;

		try {
			conn = openConnection();
			// Check if the connection is available. It may be null if the datasource
			// hasn't been deployed yet
			if (conn != null) {
				executeStatement(conn, pollingParams.rootStatement);
			}
		} catch (Exception ex) {
			log.error("Error executing statement", ex);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception ex) {
				log.error("Error closing connection", ex);
			}
		}
		log.debug("Exiting pollDatabase");
	}    

	protected String handleBinary(Object value) {

		if (value != null) {
			return new String(Base64.encodeBase64((byte[]) value));

		} else {
			log.debug("BinaryValue is Null returning empty string");
			return "";
		}

	}

	protected String handleBlob(Blob aBlob) {
		try {
			log.debug("Handling BLOB");

			if (aBlob == null) {
				log.info("Handling BLOB->Blob is null return an empty string");
				return "null";
			}

			byte[] returnBytes;
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			log.debug("Calling Blog get Binary Stream");
			InputStream inputStream = aBlob.getBinaryStream();

			int inByte;
			log.debug("Writing BLOB TO Temporary Array");
			while ((inByte = inputStream.read()) != -1) {
				byteArrayOutputStream.write(inByte);
			}

			byteArrayOutputStream.flush();
			returnBytes = byteArrayOutputStream.toByteArray();
			log.debug("BLOB Readed in bytearray of length[" + returnBytes.length + "]");
			byteArrayOutputStream.close();
			return handleBinary(returnBytes);

		} catch (Exception e) {
			log.error("Error managing a BLOB", e);
			return "null";
		}

	}

	/**
	 * Execute the root statement
	 * 
	 * @param conn
	 *          Database connection
	 * @param currentStatement
	 *          Root statement
	 * @throws Exception
	 */
	protected void executeStatement(Connection conn, StatementElement currentStatement) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String conditionKey = null;
		String keyValue = null;

		log.debug("Executing statement " + currentStatement.getSql());
		try {
			stmt = conn.prepareStatement(currentStatement.getSql());
			// Retrieve condition keyColumn from our database
			conditionKey = getConditionKey(conn, pollingParams.commPointID);
			// Use it for data retrieval
			stmt.setString(1, conditionKey);

			rs = stmt.executeQuery();

			// Retrieve metadata to get the column names
			ResultSetMetaData metadata = rs.getMetaData();

			// Retrieve result column names
			int columnCount = metadata.getColumnCount();
			String[] columnNames = new String[columnCount];
			String[] resultValues = new String[columnCount];
			for (int i = 0; i < columnCount; i++) {
				columnNames[i] = metadata.getColumnName(i + 1);
			}

			// Fetch all rows
			int messageCount = 0;
			StringBuffer collectionMessageString = new StringBuffer();
			int numMex = 0;
			while (rs.next()) {
				keyValue = rs.getString(pollingParams.keyColumn);

				for (int i = 0; i < columnCount; i++) {

					log.debug("Analyze type of column(" + (i + 1) + ")");
					log.debug(" Column [" + (i + 1) + "] has type [" + poller.getJdbcTypeName(metadata.getColumnType(i + 1)) + "]");

					if (metadata.getColumnType(i + 1) == Types.BINARY) {

						log.debug("Treat column(" + (i + 1) + ") as BINARY");
						resultValues[i] = handleBinary(rs.getObject(i + 1));

					} else if (metadata.getColumnType(i + 1) == Types.BLOB) {

						log.debug("Treat column(" + (i + 1) + ") as BLOB");
						resultValues[i] = handleBlob(rs.getBlob(i + 1));

					} else {

						log.debug("Treat column(" + (i + 1) + ") as normal get Column using rs.getString");
						resultValues[i] = rs.getString(i + 1);

					}
				}

				// Begin XML
				buildStartMessageString(currentStatement.getName(), columnNames, resultValues, collectionMessageString);
				// Produce XML body
				buildBodyMessageString(currentStatement.getName(), columnNames, resultValues, collectionMessageString);

				// Execute the subqueries
				StatementElement[] subQueries = currentStatement.getSubQueries();
				if (subQueries != null) {
					for (int i = 0; i < subQueries.length; i++) {
						StringBuffer subStatementResult = executeSubStatement(conn, subQueries[i], columnNames, resultValues);
						if (subStatementResult != null) {
							collectionMessageString.append(subStatementResult);
						}
					}
				}

				// Close XML
				buildEndMessageString(currentStatement.getName(), columnNames, resultValues, collectionMessageString);
				messageCount++;

				// Check if we collect enough messages to send
				if (pollingParams.rowsInMessage == messageCount) {
					// Send message to NMR
					poller.sendMessage(collectionMessageString);
					// Restart collecting messages
					messageCount = 0;
					collectionMessageString = new StringBuffer();
				}
				numMex++;
				if (pollingParams.maxMessagesPerUnit > 0 && numMex >= pollingParams.maxMessagesPerUnit)
					break;
			}

			// Send message to NMR
			if (collectionMessageString.length() != 0) {
				poller.sendMessage(collectionMessageString);
			}
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
				} finally {
					// Update the condition key for the next iteration
					if (keyValue != null) {
						updateConditionKey(conn, pollingParams.commPointID, keyValue);
					}
				}
			}
		}
	}

	/**
	 * Executes a substatement
	 * 
	 * @param conn
	 *          Database connection
	 * @param currentStatement
	 *          Substatement
	 * @param parentColumnNames
	 *          Parent statement column names
	 * @param parentResultValues
	 *          Parent statement result values
	 * @return StringBuffer containing the XML message of the subquery
	 * @throws Exception
	 */
	protected StringBuffer executeSubStatement(Connection conn, StatementElement currentStatement,
			String[] parentColumnNames, String[] parentResultValues) throws Exception {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		log.debug("Preparing statement " + currentStatement.getSql());
		try {
			// The statement can contain references to the values of the parent query
			String originalStatement = currentStatement.getSql();
			String modifiedStatement = originalStatement;

			while (modifiedStatement.indexOf('@') != -1) {
				int atIndex = modifiedStatement.indexOf('@');
				int endIndex = modifiedStatement.indexOf(' ', atIndex);
				if (endIndex == -1) {
					endIndex = modifiedStatement.length();
				}

				// Retrieve the parameter name without the @
				String parameterName = modifiedStatement.substring(atIndex + 1, endIndex);
				String parameterValue = null;
				// Evaluate parameter value
				for (int i = 0; i < parentColumnNames.length; i++) {
					if (parameterName.equalsIgnoreCase(parentColumnNames[i])) {
						parameterValue = parentResultValues[i];
						break;
					}
				}

				if (parameterValue != null) {
					modifiedStatement = modifiedStatement.replaceFirst("@" + parameterName, "'" + parameterValue + "'");
				} else {
					log.error("Unable to retrieve parameter value for parameter " + parameterName);
					return null;
				}
			}

			log.debug("Executing statement " + modifiedStatement);
			stmt = conn.prepareStatement(modifiedStatement);
			rs = stmt.executeQuery();

			ResultSetMetaData metadata = rs.getMetaData();

			// Retrieve result column names
			int columnCount = metadata.getColumnCount();
			String[] columnNames = new String[columnCount];
			String[] resultValues = new String[columnCount];
			for (int i = 0; i < columnCount; i++) {
				columnNames[i] = metadata.getColumnName(i + 1);
			}

			// Fetch all rows
			StringBuffer childRowsMessageString = new StringBuffer();
			while (rs.next()) {
				for (int i = 0; i < columnCount; i++) {
					resultValues[i] = rs.getString(i + 1);
				}

				// Begin XML
				buildStartMessageString(currentStatement.getName(), columnNames, resultValues, childRowsMessageString);
				// Produce XML body
				buildBodyMessageString(currentStatement.getName(), columnNames, resultValues, childRowsMessageString);

				// Execute the subqueries
				StatementElement[] subQueries = currentStatement.getSubQueries();
				if (subQueries != null) {
					for (int i = 0; i < subQueries.length; i++) {
						StringBuffer subMessage = executeSubStatement(conn, subQueries[i], columnNames, resultValues);
						if (subMessage != null) {
							childRowsMessageString.append(subMessage);
						}
					}
				}

				// Close XML
				buildEndMessageString(currentStatement.getName(), columnNames, resultValues, childRowsMessageString);
			}
			return childRowsMessageString;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}
		}
	}
	
	/**
	 * Insert the first condition key to filter the data to select
	 * 
	 * @param initialKeyValue
	 *          Parameter defined in component configuration
	 * @param commPointID
	 *          Unique ID for keyColumn retrieval
	 * @throws Exception
	 */
	protected void insertConditionKey(Connection conn, String initialKeyValue, String commPointID) throws Exception {
		PreparedStatement insertStmt = null;

		try {
			// The row doesnt exists: insert it
			insertStmt = conn.prepareStatement(KEY_INSERT);
			insertStmt.setString(1, commPointID);
			insertStmt.setString(2, initialKeyValue);
			insertStmt.executeUpdate();
		} finally {
			if (insertStmt != null) {
				insertStmt.close();
			}
		}
	}

	/**
	 * Create the condition key table, if it doesn't exist, and insert the first
	 * condition row with the initial key values.
	 * 
	 * @param initialKeyValue
	 *          Parameter defined in component configuration
	 * @param commPointID
	 *          Unique ID for key retrieval
	 * @throws Exception
	 */
	protected void createConditionKeyTable(Connection conn, String initialKeyValue, String commPointID) throws Exception {
		PreparedStatement stmt = null;
		try {
			String keyCreationStatement = getKeyCreationStatement();
			log.debug("Key Creation statement[" + keyCreationStatement + "]");
			stmt = conn.prepareStatement(keyCreationStatement);

			try {
				stmt.executeUpdate();
				log.debug("Table for keys created");

				// Insert the row containing the initial keyColumn value
				insertConditionKey(conn, initialKeyValue, commPointID);
				log.debug("Row for condition keyColumn created");
			} catch (Exception ex1) {
				log.error("Unable to create table for keys", ex1);
				// Throw again the exception to avoid starting correctly the
				// component
				throw ex1;
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * Opens the connection used for key management
	 * 
	 * @return Database connection
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	protected Connection openKeyConnection() throws SQLException {
		// Open database connection or get it from the datasource
		Connection conn = null;
		if (keyDatasource == null) {
			log.debug("Lookup of datasource: " + pollingParams.keyDatasourceName);
			try {
				try {
					keyDatasource = poller.getDataSourceManager().getDataSource(pollingParams.keyDatasourceName);
				} catch (Exception e) {
					log.warn("Datasource " + pollingParams.datasourceName + " not available. Polling not executed.");
					return null;
				}
			} catch (Exception e) {
				log.error("Error during lookup of datasource", e);
				try {
					log.error("Deactivating component JDBC Poller");
					// Cancels this timer task: it will never run again
					cancel();
					poller = null;
				} catch (Exception e1) {
					log.error("Error deactivating component JDBC Poller");
				}
			}
		}
		conn = keyDatasource.getConnection();
		return conn;
	}

	/**
	 * Retrieves the condition keyColumn to filter the data to select
	 * 
	 * @param commPointID
	 *          Unique ID for keyColumn retrieval
	 * @return The condition keyColumn
	 * @throws Exception
	 */
	protected String getConditionKey(Connection conn, String commPointID) throws Exception {
		String result = null;

		PreparedStatement selectStmt = null;
		ResultSet rs = null;

		try {
			selectStmt = conn.prepareStatement(KEY_QUERY);
			selectStmt.setString(1, commPointID);
			rs = selectStmt.executeQuery();
			if (rs.next()) {
				// The row already exists: retrieve current value
				result = rs.getString(1);
			} else {
				// The row doesn't exists: this should not happen NEVER
				throw new ConditionKeyNotFoundException("Condition key not found");
			}
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} finally {
				if (selectStmt != null) {
					selectStmt.close();
				}
			}
		}

		return result;
	}

	/**
	 * Updates the current condition key according to the previous selected data
	 * 
	 * @param commPointID
	 *          Unique ID for key retrieval
	 * @param currentKeyValue
	 *          Last key used
	 * @throws Exception
	 */
	protected void updateConditionKey(Connection conn, String commPointID, String currentKeyValue) throws Exception {
		PreparedStatement stmt = null;

		try {
			stmt = conn.prepareStatement(KEY_UPDATE);
			stmt.setString(1, currentKeyValue);
			stmt.setString(2, commPointID);
			stmt.executeUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * Build the message root: for example <main> or <row name="main">
	 * 
	 * @param statementName
	 *          Name of the statement
	 * @param columnNames
	 *          Names for the result columns
	 * @param columnValues
	 *          Values of the result columns
	 * @param result
	 *          StringBuffer of the resulting XML
	 * @return StringBuffer of the resulting XML
	 */
	protected StringBuffer buildStartMessageString(final String statementName, final String[] columnNames,
			final String[] columnValues, StringBuffer result) {
		if (pollingParams.rowNameAsAttribute) {
			result.append("<row name=\"");
			result.append(StringEscapeUtils.escapeXml(statementName));
			result.append("\">");
		} else {
			result.append("<");
			result.append(StringEscapeUtils.escapeXml(statementName));
			result.append(">");
		}

		return result;
	}

	/**
	 * Build the message body: for example <id>20234</id> <date>2002-1-2
	 * 10:32:25</date> or <column name="id" value="97320"/> <column name="date"
	 * value="2002-1-1 12:12:34"/>
	 * 
	 * @param statementName
	 *          Name of the statement
	 * @param columnNames
	 *          Names for the result columns
	 * @param columnValues
	 *          Values of the result columns
	 * @param result
	 *          StringBuffer of the resulting XML
	 * @return StringBuffer of the resulting XML
	 */
	protected StringBuffer buildBodyMessageString(final String statementName, final String[] columnNames,
			final String[] columnValues, StringBuffer result) {
		// Following there are some samples of XML according to different attributes
		// values
		// columnNameAsAttribute = null
		// valueAsAttribute = null
		// <PIPPO>123</PIPPO><PLUTO>456</PLUTO>
		//
		// columnNameAsAttribute != null
		// valueAsAttribute = null
		// <column name="PIPPO"/>123<column/><column name="PLUTO"/>456<column/>
		//    	
		// columnNameAsAttribute = null
		// valueAsAttribute != null
		// <PIPPO value="123"/><PLUTO value="456"/>
		//    	
		// columnNameAsAttribute != null
		// valueAsAttribute != null
		// <column name="PIPPO" value="123"/><column name="PLUTO" value="456"/>

		if (pollingParams.columnNameAsAttribute) {
			for (int count = columnNames.length, i = 0; i < count; i++) {
				result.append("<column name=\"");
				result.append(StringEscapeUtils.escapeXml(columnNames[i]));
				result.append("\"");

				if (pollingParams.valueAsAttribute) {
					result.append(" value=\"");
					result.append(StringEscapeUtils.escapeXml(columnValues[i]));
					result.append("\"/>");
				} else {
					result.append(">");
					result.append(StringEscapeUtils.escapeXml(columnValues[i]));
					result.append("</column>");
				}
			}
		} else {
			for (int count = columnNames.length, i = 0; i < count; i++) {
				result.append("<");
				result.append(StringEscapeUtils.escapeXml(columnNames[i]));

				if (pollingParams.valueAsAttribute) {
					result.append(" value=\"");
					result.append(StringEscapeUtils.escapeXml(columnValues[i]));
					result.append("\"/>");
				} else {
					result.append(">");
					result.append(StringEscapeUtils.escapeXml(columnValues[i]));
					result.append("</");
					result.append(StringEscapeUtils.escapeXml(columnNames[i]));
					result.append(">");
				}
			}
		}

		return result;
	}

	/**
	 * Build the message tail: for example </main> or </row>
	 * 
	 * @param statementName
	 *          Name of the statement
	 * @param columnNames
	 *          Names for the result columns
	 * @param columnValues
	 *          Values of the result columns
	 * @param result
	 *          StringBuffer of the resulting XML
	 * @return StringBuffer of the resulting XML
	 */
	protected StringBuffer buildEndMessageString(final String statementName, final String[] columnNames,
			final String[] columnValues, StringBuffer result) {
		if (pollingParams.rowNameAsAttribute) {
			result.append("</row>");
		} else {
			result.append("</");
			result.append(StringEscapeUtils.escapeXml(statementName));
			result.append(">");
		}

		return result;
	}

	@Override
	public boolean cancel() {
		log.info("Deactivating polling task for JDBC Poller");
		poller = null;
		
		return super.cancel();
	}	
}
