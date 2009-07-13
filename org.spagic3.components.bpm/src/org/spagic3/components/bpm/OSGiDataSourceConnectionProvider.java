package org.spagic3.components.bpm;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.components.bpm.activator.BPMComponentActivator;
import org.spagic3.datasource.IDataSourceManager;


public class OSGiDataSourceConnectionProvider implements ConnectionProvider {
	
	private DataSource ds;
	private String user;
	private String pass;

	private static final Logger log = LoggerFactory.getLogger(OSGiDataSourceConnectionProvider.class);

	protected DataSource getDataSource() {
		return ds;
	}

	protected void setDataSource(DataSource ds) {
		this.ds = ds;
	}

	public void configure(Properties props) throws HibernateException {

		String jndiName = props.getProperty(Environment.DATASOURCE);
		if (jndiName==null) {
			String msg = "datasource JNDI name was not specified by property " + Environment.DATASOURCE;
			log.error(msg);
			throw new HibernateException(msg);
		}

		user = props.getProperty(Environment.USER);
		pass = props.getProperty(Environment.PASS);

		try {
			String realDatasourceId = jndiName.substring(IDataSourceManager.OSGI_PREFIX.length());
			ds = (DataSource) BPMComponentActivator.getDataSourceManager().getDataSource(realDatasourceId);
		}
		catch (Exception e) {
			log.error("Could not find datasource: " + jndiName, e );
			throw new HibernateException( "Could not find datasource", e );
		}
		if (ds==null) {
			throw new HibernateException( "Could not find datasource: " + jndiName );
		}
		log.info( "Using datasource: " + jndiName );
	}

	public Connection getConnection() throws SQLException {
		if (user != null || pass != null) {
			return ds.getConnection(user, pass);
		}
		else {
			return ds.getConnection();
		}
	}

	public void closeConnection(Connection conn) throws SQLException {
		conn.close();
	}

	public void close() {}

	/**
	 * @see ConnectionProvider#supportsAggressiveRelease()
	 */
	public boolean supportsAggressiveRelease() {
		return true;
	}


}
