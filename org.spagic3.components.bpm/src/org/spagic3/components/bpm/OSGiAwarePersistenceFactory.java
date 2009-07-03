package org.spagic3.components.bpm;

import javax.sql.DataSource;

import org.jbpm.persistence.db.DbPersistenceServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.components.bpm.activator.BPMComponentActivator;
import org.spagic3.datasource.IDataSourceManager;

public class OSGiAwarePersistenceFactory extends DbPersistenceServiceFactory {
	
	private static Logger logger = LoggerFactory.getLogger(OSGiAwarePersistenceFactory.class);
	@Override
	public DataSource getDataSource() {
		if (getDataSourceJndiName() != null && getDataSourceJndiName().startsWith(IDataSourceManager.OSGI_PREFIX)){
			String realDatasourceId = getDataSourceJndiName().substring(IDataSourceManager.OSGI_PREFIX.length()+1);
			try{
				return BPMComponentActivator.getDataSourceManager().getDataSource(realDatasourceId);
			}catch (Exception e) {
				logger.error("Cannot get Datasource from Spagic OSGi DatasourceManager", e);
				return null;
			}
		}else{
			return super.getDataSource();	
		}

	}

}
