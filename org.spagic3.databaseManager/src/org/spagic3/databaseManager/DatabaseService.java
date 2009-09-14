package org.spagic3.databaseManager;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.spagic.metadb.base.ProcessExecutionStateConstants;
import org.spagic.metadb.base.ServiceInstanceStateConstants;
import org.spagic.metadb.base.TypeServiceConstants;
import org.spagic.metadb.dbutils.HibernateUtil;
import org.spagic.metadb.model.Component;
import org.spagic.metadb.model.Property;
import org.spagic.metadb.model.Service;
import org.spagic.metadb.model.ServiceInstance;
import org.spagic.metadb.model.TransitionInstance;
import org.spagic3.constants.SpagicConstants;

public class DatabaseService implements IDatabaseManager {
	
	public final static String COMPONENT_ID = "idComponent".intern();
	public final static String COMPONENT_NAME = "name".intern();
	
	public final static String SERVICE_ID = "idService".intern();
	public final static String SERVICE_VERSION = "serviceVersion".intern();
	
	public final static String SERVICE_INSTANCE_ID = "idServiceInstance".intern();
	public final static String SERVICE_INSTANCE_SERVICE_ID = "service.idService".intern();
	public final static String SERVICE_INSTANCE_MESSAGE_ID = "messageId".intern();
	public final static String SERVICE_INSTANCE_CORRELATION_ID = "correlationId".intern();
	
	
	public void bindMetaDB(javax.sql.DataSource ds){
		System.out.println("DatabaseService: Metadb Datasource has been bound");
	}
	
	public void unbindMetaDB(javax.sql.DataSource ds){
		System.out.println("DatabaseService: Metadb Datasource has been unbound");
	}

	private Component getComponentByName(Session aSession, String componentName) {
		Criteria aCriteria = aSession.createCriteria(Component.class);
		aCriteria.add(Expression.eq(COMPONENT_NAME, componentName));
		return (Component) aCriteria.uniqueResult();
	}

	@Override
	public Component getComponentByName(String componentName) {
		Session aSession = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			return getComponentByName(aSession, componentName);
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
	}

	@Override
	public Service getServiceById(String serviceId) {
		Session aSession = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			return getServiceById(aSession, serviceId);
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
	}
	
	protected Service getServiceById(Session aSession, String serviceId) {
		Criteria aCriteria = aSession.createCriteria(Service.class);
		aCriteria.add(Expression.eq(SERVICE_ID, serviceId));
		return (Service) aCriteria.uniqueResult();
	}

	@Override
	public Service getServiceByIdAndVersion(String serviceId, String version) {
		Session aSession = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			Criteria aCriteria = aSession.createCriteria(TransitionInstance.class);
			aCriteria.add(Expression.eq(SERVICE_ID, serviceId));
			aCriteria.add(Expression.eq(SERVICE_VERSION, version));
			return (Service) aCriteria.uniqueResult();
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
	}
	
	@Override
	public ServiceInstance getServiceInstance(Long serviceInstanceId) {
		Session aSession = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			Criteria aCriteria = aSession.createCriteria(ServiceInstance.class);
			aCriteria.add(Expression.eq(SERVICE_INSTANCE_ID, serviceInstanceId));
			return (ServiceInstance) aCriteria.uniqueResult();
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
	}

	@Override
	public ServiceInstance getServiceInstanceByMessageId(String serviceId,
			String exchangeID) {
		Session aSession = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			Criteria aCriteria = aSession.createCriteria(ServiceInstance.class);
			aCriteria.add(Expression.eq(SERVICE_INSTANCE_SERVICE_ID, serviceId));
			aCriteria.add(Expression.eq(SERVICE_INSTANCE_MESSAGE_ID, exchangeID));
			return (ServiceInstance) aCriteria.uniqueResult();
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
	}
	
	@Override
	public ServiceInstance getServiceInstanceByCorrelationId(String serviceId,
			String correlationID) {
		Session aSession = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			Criteria aCriteria = aSession.createCriteria(ServiceInstance.class);
			aCriteria.add(Expression.eq(SERVICE_INSTANCE_SERVICE_ID, serviceId));
			aCriteria.add(Expression.eq(SERVICE_INSTANCE_CORRELATION_ID, correlationID));
			return (ServiceInstance) aCriteria.uniqueResult();
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
	}

	@Override
	public ServiceInstance createServiceInstance(String serviceId,
			String exchangeID, String correlationId, 
			ServiceInstance targetServiceInstance, String request, String response) {
		Session aSession = null;
		ServiceInstance serviceInstance = null;
		Transaction tx = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			tx = aSession.beginTransaction();
			
			serviceInstance = new ServiceInstance();
			serviceInstance.setMessageId(exchangeID);
			serviceInstance.setCorrelationId(correlationId);	
			serviceInstance.setRequest(request);
			serviceInstance.setResponse(response);
			serviceInstance.setService(getServiceById(aSession, serviceId));
			serviceInstance.setTargetServiceInstance(targetServiceInstance);
			serviceInstance.setState(ServiceInstanceStateConstants.SERVICE_ACTIVE);
			serviceInstance.setStartdate(new Date());
			
			aSession.save(serviceInstance);
			tx.commit();
		} catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
		}
		finally {
			if (aSession != null) {
				aSession.close();
			}
		}
		return serviceInstance;
	}

	@Override
	public void updateServiceInstance(ServiceInstance serviceInstance) {
		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			tx = aSession.beginTransaction();
			
			aSession.saveOrUpdate(serviceInstance);
			tx.commit();
		} catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
	}

	@Override
	public Service registerService(String serviceId, String componentName,
			Map<String, String> properties) {
		Session aSession = null;
		Service service = null;
		Transaction tx = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			tx = aSession.beginTransaction();
			
			// Check if the service is already registered
			service = getServiceById(aSession, serviceId);
			if (service != null) {
				// Service already registered: check if we must update it
				// TODO
				return service;				
			}
			Component component = getComponentByName(componentName);
			
			Set<Property> propertiesSet = new HashSet<Property>(properties.size());
			for (String propKey : properties.keySet()) {
				
				String propValue = properties.get(propKey);
				Property metaDBProperty = new Property();
	
				metaDBProperty.setCode(propKey);
				metaDBProperty.setValue(propValue);
				aSession.save(metaDBProperty);
				
				propertiesSet.add(metaDBProperty);
			}
			
			service = new Service(serviceId, component, (int)ProcessExecutionStateConstants.NORMAL_EXECUTION, true, new Date(), null, (Set)propertiesSet, (Set)null);		
			aSession.save(service);
			tx.commit();
			return service;
		} catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
		return service;
	}

	@Override
	public Component registerComponent(String componentName,
			String componentType,
			Map<String, String> properties) {
		Session aSession = null;
		Component component = null;
		Transaction tx = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			tx = aSession.beginTransaction();
			
			// Check if the service is already registered
			component = getComponentByName(aSession, componentName);
			if (component != null) {
				// Component already registered: check if we must update it
				// TODO
				return component;				
			}
			
			Set<Property> propertiesSet = new HashSet<Property>(properties.size());
			for (String propKey : properties.keySet()) {
				
				String propValue = properties.get(propKey);
				Property metaDBProperty = new Property();
	
				metaDBProperty.setCode(propKey);
				metaDBProperty.setValue(propValue);
				aSession.save(metaDBProperty);
				
				propertiesSet.add(metaDBProperty);
			}
			
			long idComponentType = 0;
			if (componentType.equals(SpagicConstants.SERVICE_TYPE_CONNECTOR)) {
				idComponentType = TypeServiceConstants.OSGI_CONNECTOR;
			} else if (componentType.equals(SpagicConstants.SERVICE_TYPE_SERVICE)) {
				idComponentType = TypeServiceConstants.OSGI_SERVICE;
			}
			
			component = new Component(idComponentType, componentName, (int)ProcessExecutionStateConstants.NORMAL_EXECUTION, (Set<Service>)null, propertiesSet);
			aSession.save(component);
			tx.commit();
			return component;
		} catch (Exception ex) {
			if (tx != null) {
				tx.rollback();
			}
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
		return component;
	}

}
