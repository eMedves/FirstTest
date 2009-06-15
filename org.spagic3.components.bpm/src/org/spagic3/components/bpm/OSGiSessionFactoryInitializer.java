package org.spagic3.components.bpm;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.spagic.metadb.dbutils.ISessionFactoryInitializer;
import org.spagic3.components.bpm.activator.BPMComponentActivator;

public class OSGiSessionFactoryInitializer implements
		ISessionFactoryInitializer {

	@Override
	public SessionFactory buildSessionFactory() {
		SessionFactory sf = null;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			
			Thread.currentThread().setContextClassLoader(BPMComponentActivator.class.getClassLoader());
			sf = new Configuration().configure()
				.buildSessionFactory();
			
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			
			throw new ExceptionInInitializerError(ex);
		}finally{
			Thread.currentThread().setContextClassLoader(cl);
		}
		return sf;
	}

}
