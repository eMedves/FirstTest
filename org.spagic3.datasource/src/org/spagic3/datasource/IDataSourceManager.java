package org.spagic3.datasource;

import javax.sql.DataSource;

public interface IDataSourceManager {
	
	public static final String OSGI_PREFIX = "osgi/";
	
	public DataSource getDataSource(String datasourceIdentifier) throws Exception;
}
