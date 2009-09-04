package org.spagic3.databaseManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.spagic.metadb.base.ProcessExecutionStateConstants;
import org.spagic.metadb.dbutils.HibernateUtil;
import org.spagic.metadb.model.Component;
import org.spagic.metadb.model.Property;
import org.spagic.metadb.model.Service;
import org.spagic.metadb.model.ServiceInstance;
import org.spagic.metadb.model.TransitionInstance;

public class DatabaseService implements IDatabaseManager {
	
	public final static String COMPONENT_ID = "idService".intern();
	
	public final static String SERVICE_ID = "idService".intern();
	public final static String SERVICE_VERSION = "serviceVersion".intern();
	
	public final static String SERVICE_INSTANCE_ID = "idServiceInstance".intern();
	public final static String SERVICE_INSTANCE_SERVICE_ID = "service.idService".intern();
	public final static String SERVICE_INSTANCE_MESSAGE_ID = "messageId".intern();
	
	@Override
	public Component getComponentByName(String componentName) {
		Session aSession = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			Criteria aCriteria = aSession.createCriteria(Component.class);
			aCriteria.add(Expression.eq(COMPONENT_ID, componentName));
			return (Component) aCriteria.uniqueResult();
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
	public ServiceInstance getServiceInstance(String serviceId,
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
	public ServiceInstance createServiceInstance(String serviceId,
			String exchangeID, String request, String response) {
		Session aSession = null;
		ServiceInstance serviceInstance = null;
		Transaction tx = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			tx = aSession.beginTransaction();
			
			serviceInstance = new ServiceInstance();
			serviceInstance.setMessageId(exchangeID);
			serviceInstance.setRequest(request);
			serviceInstance.setResponse(response);
			serviceInstance.setService(getServiceById(aSession, serviceId));
			
			aSession.save(serviceInstance);
			tx.commit();
		} catch (Exception ex) {
			tx.rollback();
		}
		finally {
			if (aSession != null) {
				aSession.close();
			}
		}
		return serviceInstance;
	}

	@Override
	public void updateServiceInstance(ServiceInstance serviceInstance,
			String response) {
		Session aSession = null;
		try {
			aSession = HibernateUtil.getSessionFactory().openSession();
			// TODO Auto-generated method stub
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
				// TODO do something
				
			}
			Component component = getComponentByName(componentName);
			
			Set<Property> propertiesSet = new HashSet<Property>(properties.size());
			for (String propKey : properties.keySet()) {
				
				String propValue = properties.get(propKey);
//			---
				Property metaDBProperty = new Property();
	
				metaDBProperty.setCode(propKey);
				metaDBProperty.setValue(propValue);
				aSession.save(metaDBProperty);
				
				propertiesSet.add(metaDBProperty);
	
				// Associate Property and Process
//				PropertyProcessId propertyProcessId = new PropertyProcessId(metaDBProperty.getIdProperty(), metaDBProcess
//						.getIdProcess());
//				PropertyProcess metaDBPropertyProcess = new PropertyProcess(propertyProcessId, metaDBProcess, metaDBProperty);
//				aSession.save(metaDBPropertyProcess);
//			---
			
			}
			
			
			service = new Service(serviceId, component, (int)ProcessExecutionStateConstants.NORMAL_EXECUTION, (Set)propertiesSet, (Set)null);
//			service.setService(getServiceById(aSession, serviceId));
			
			aSession.save(service);
			tx.commit();
			return service;
		} catch (Exception ex) {
			tx.rollback();
		} finally {
			if (aSession != null) {
				aSession.close();
			}
		}
		return service;
	}

}
