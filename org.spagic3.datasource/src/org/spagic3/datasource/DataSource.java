package org.spagic3.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.osgi.service.component.ComponentContext;
import org.spagic3.core.PropertyConfigurator;



public class DataSource implements javax.sql.DataSource{
		
	private javax.sql.DataSource internalDS = null;
	private BasicDataSourceFactory bdf = new BasicDataSourceFactory();
	
	protected void activate(ComponentContext componentContext){
		try{
			PropertyConfigurator propertyConfigurator = new PropertyConfigurator(componentContext.getProperties());	
			this.internalDS = bdf.createDataSource(propertyConfigurator.asProperties());
		}catch (Exception e) {
			throw new IllegalStateException("Cannot istantiate DataSource", e);
		}
	}
	
	
	
	@Override
	public Connection getConnection() throws SQLException {
		
		return internalDS.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		
		return internalDS.getConnection(username, password);
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		
		return internalDS.getLogWriter();
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return internalDS.getLoginTimeout();
	}

	@Override
	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		internalDS.setLogWriter(logWriter);
		
	}

	@Override
	public void setLoginTimeout(int loginTimeout) throws SQLException {
		internalDS.setLoginTimeout(loginTimeout);
		
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		
		throw new SQLException("Not Implemented");
	}

	
	
	
}
