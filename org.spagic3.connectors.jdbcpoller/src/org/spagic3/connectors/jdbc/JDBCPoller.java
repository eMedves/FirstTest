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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.core.AbstractSpagicConnector;
import org.spagic3.core.PropertyConfigurator;
import org.spagic3.datasource.IDataSourceManager;

public class JDBCPoller extends AbstractSpagicConnector {
	
	public static final String PROPERTY_MAINQUERYNAME = "mainQueryName";
	public static final String PROPERTY_MAINQUERYSQL = "mainQuerySQL";
	public static final String PROPERTY_KEYCOLUMN = "keyColumn";
	public static final String PROPERTY_INITIALKEYVALUE = "initialKeyValue";
	public static final String PROPERTY_COLUMNNAMEASATTRIBUTE = "columnNameAsAttribute";
	public static final String PROPERTY_ROWNAMEASATTRIBUTE = "rowNameAsAttribute";
	public static final String PROPERTY_VALUEASATTRIBUTE = "valueAsAttribute";
	public static final String PROPERTY_COMMPOINTID = "commPointID";
	public static final String PROPERTY_DATABASETYPE = "databaseType";
	public static final String PROPERTY_DATASOURCENAME = "datasourceName";
	public static final String PROPERTY_PERIOD = "period";
//	public static final String PROPERTY_PERIOD = "NumberOfSubQueries";
	
	public static final String PROPERTY_ROWSFOREACHMESSAGE = "rowsForEachMessage";
	public static final String PROPERTY_MAXMESSAGESPERUNIT = "maxMessagesPerUnit";
	
	// Supported databases (the key table creation statement may be database
	// dependent)

	// Constants Used in Spagic Studio
	private static String MS_SQL_SERVER = "MS_SQL";
	private static String MySQL_SERVER = "MySQL";
	private static String Oracle_SERVER = "Oracle";
	private static String Sybase_SERVER = "Sysbase";
	private static String DB2_SERVER = "DB2";
	private static String Postgre_SQL_SERVER = "Postgres";
	private static String H2_SERVER = "H2";
	private static String JDBC_SERVER = "JDBC_Generic";
	private static String[] supportedDatabases = { MS_SQL_SERVER, MySQL_SERVER, Oracle_SERVER, Sybase_SERVER, DB2_SERVER,
			Postgre_SQL_SERVER, H2_SERVER, JDBC_SERVER };

	protected Logger log = LoggerFactory.getLogger(JDBCPoller.class);
	
	// Container for polling parameters
	private PollingParameters pollingParams;
		
	// Status of the component: necessary because the method poll is called
	private boolean active;
    private long period;
	private Timer timerService;
	
	protected final AtomicReference<IDataSourceManager> datasourceManager = new AtomicReference<IDataSourceManager>(null); 

	// Bundle for database dependent statements
	private ResourceBundle statementsBundle;

	static Map<Integer, String> jdbcTypesNames = null;

	// This method returns the name of a JDBC type.
	// Returns null if jdbcType is not recognized.
	public static String getJdbcTypeName(int jdbcType) {
		// Return the JDBC type name
		return (String) jdbcTypesNames.get(new Integer(jdbcType));
	}

	@Override
	public void init() {
		pollingParams = new PollingParameters();
		
		StatementElement rootStatement = new StatementElement(propertyConfigurator.getString(PROPERTY_MAINQUERYNAME),
				 propertyConfigurator.getString(PROPERTY_MAINQUERYSQL));
		setRootStatement(rootStatement);
		
		setKeyColumn(propertyConfigurator.getString(PROPERTY_KEYCOLUMN));
		setInitialKeyValue(propertyConfigurator.getString(PROPERTY_INITIALKEYVALUE));
		setColumnNameAsAttribute(propertyConfigurator.getBoolean(PROPERTY_COLUMNNAMEASATTRIBUTE, Boolean.FALSE));
		setRowNameAsAttribute(propertyConfigurator.getBoolean(PROPERTY_ROWNAMEASATTRIBUTE, Boolean.TRUE));
		setValueAsAttribute(propertyConfigurator.getBoolean(PROPERTY_VALUEASATTRIBUTE, Boolean.FALSE));
		setRowsInMessage(propertyConfigurator.getInteger(PROPERTY_ROWSFOREACHMESSAGE, 0));
		setCommPointID(propertyConfigurator.getString(PROPERTY_COMMPOINTID));
		setDatabaseType(propertyConfigurator.getString(PROPERTY_DATABASETYPE));
		setDatasourceName(propertyConfigurator.getString(PROPERTY_DATASOURCENAME));

		period = propertyConfigurator.getInteger(PROPERTY_PERIOD, 5000);
		setMaxMessagesPerUnit(propertyConfigurator.getInteger(PROPERTY_MAXMESSAGESPERUNIT, 0));

		// Load database dependent statements
		statementsBundle = ResourceBundle.getBundle("statements");		
		setKeyCreationStatement(statementsBundle.getString(getDatabaseType() + "_KEY_CREATE"));
		
		
		Map<String, Properties> subQueriesMap = (Map<String, Properties>)propertyConfigurator.getXMapProperty("subQueries");
		StatementElement[] subQueries = new StatementElement[subQueriesMap.size()];
		int subQueryCounter = 0;
		for (String key : subQueriesMap.keySet()) {
			Properties value = subQueriesMap.get(key);
			
			subQueries[subQueryCounter] = new StatementElement(key, new PropertyConfigurator(value).getString("Sql"));
//			subQueries[subQueryCounter].setName();
//			subQueries[subQueryCounter].setSql();
			subQueryCounter++;			
		}
		rootStatement.setSubQueries(subQueries);

		// Use reflection to populate a map of int values to names
		if (jdbcTypesNames == null) {
			jdbcTypesNames = new HashMap<Integer, String>();

			// Get all field in java.sql.Types
			Field[] fields = java.sql.Types.class.getFields();
			for (int i = 0; i < fields.length; i++) {
				try {
					// Get field name
					String name = fields[i].getName();

					// Get field value
					Integer value = (Integer) fields[i].get(null);

					// Add to map
					jdbcTypesNames.put(value, name);
				} catch (IllegalAccessException e) {
				}
			}
		}

	}

	@Override
	public void start() throws Exception {
		
		// Validate some configuration parameters
		validate();
		
		// Activate polling thread
		timerService = new Timer();
		timerService.schedule(new PollingTask(pollingParams, this), new Date(), period);		

		// Component activated
		active = true;
	}

	@Override
	public void stop() throws Exception {
		active = false;
		timerService.cancel();
		timerService = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.common.endpoints.ConsumerEndpoint#validate()
	 * Method invoked before starting the component
	 */
	public void validate() {

		boolean databaseSupported = false;
		for (int i = 0; i < supportedDatabases.length; i++) {
			if (supportedDatabases[i].equals(pollingParams.databaseType)) {
				databaseSupported = true;
				break;
			}
		}
		if (!databaseSupported) {
			throw new RuntimeException("Database not supported");
		}

		if (pollingParams.keyDatasourceName == null) {
			// If a datasourceName was provided for the polling, use it
			log.debug("Datasource not provided, using: " + pollingParams.datasourceName);
			pollingParams.keyDatasourceName = pollingParams.datasourceName;			
		}
	}

	public void setColumnNameAsAttribute(boolean columnNameAsAttribute) {
		pollingParams.columnNameAsAttribute = columnNameAsAttribute;
	}

	public void setDatabaseType(String databaseType) {
		pollingParams.databaseType = databaseType;
	}
	
	public String getDatabaseType() {
		return pollingParams.databaseType;
	}

	public void setInitialKeyValue(String initialKeyValue) {
		pollingParams.initialKeyValue = initialKeyValue;
	}

	public void setKeyColumn(String keyColumn) {
		pollingParams.keyColumn = keyColumn;
	}

	public void setRowNameAsAttribute(boolean rowNameAsAttribute) {
		pollingParams.rowNameAsAttribute = rowNameAsAttribute;
	}

	public void setRowsInMessage(int rowsInMessage) {
		pollingParams.rowsInMessage = rowsInMessage;
	}

	public void setValueAsAttribute(boolean valueAsAttribute) {
		pollingParams.valueAsAttribute = valueAsAttribute;
	}

	public void setCommPointID(String commPointID) {
		pollingParams.commPointID = commPointID;
	}

	public void setRootStatement(StatementElement rootStatement) {
		pollingParams.rootStatement = rootStatement;
	}

	public String getDatasourceName() {
		return pollingParams.datasourceName;
	}

	public void setDatasourceName(String datasourceName) {
		pollingParams.datasourceName = datasourceName;
	}
	public void setMaxMessagesPerUnit(int maxMessagesPerUnit) {
		pollingParams.maxMessagesPerUnit = maxMessagesPerUnit;
	}
	public void setKeyCreationStatement(String keyCreationStatement) {
		pollingParams.keyCreationStatement = keyCreationStatement;
	}
	

	public IDataSourceManager getDataSourceManager() {
		return this.datasourceManager.get();
	}
	public void setDataSourceManager(IDataSourceManager dsm) {
		this.datasourceManager.set(dsm);
	}
	
	public void unsetDataSourceManager(IDataSourceManager dsManager) {
		this.datasourceManager.compareAndSet(dsManager, null);
	}

	/**
	 * Send a message to the NMR
	 * 
	 * @param messageStringBuffer
	 * @throws Exception
	 */
	protected void sendMessage(StringBuffer messageStringBuffer) throws Exception {
		log.debug("Sending message: " + messageStringBuffer);

		// Envelope the string in a "message" tag: if rowsInMessage is specified
		// this is
		// necessary to ensure a proper XML message
		messageStringBuffer.insert(0, "<message>");
		messageStringBuffer.append("</message>");

		Exchange exchange = createInOnlyExchange();
//		exchange.setProperty(JbiConstants.SENDER_ENDPOINT, service.toString());
//		exchange.setProperty(JbiConstants.CORRELATION_ID, exchange.getExchangeId());
		
		Message inMessage = exchange.getIn(true);
		inMessage.setBody(messageStringBuffer.toString());
		
		send(exchange);
	}

	@Override
	public void process(Exchange exchange) {
		String exchangeId = exchange.getId();
		log.debug("Received acknowledge for exchange: " + exchangeId);
	}

}
