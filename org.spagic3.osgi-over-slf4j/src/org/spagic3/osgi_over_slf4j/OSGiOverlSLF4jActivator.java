package org.spagic3.osgi_over_slf4j;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.util.StatusPrinter;

public class OSGiOverlSLF4jActivator implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger("org.spagic3.osgi_over_slf4j");
	private ServiceTracker logTracker = null;
	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		System.out.println("**********************************************************************");
		System.out.println("***************  OSGi OVER SLF4J ACTIVATED ***************************");
		
		
		Context lc = (Context) LoggerFactory.getILoggerFactory();
	    // print logback's internal status
	    StatusPrinter.print(lc);
	    System.out.println("**********************************************************************");
		Filter filter = context.createFilter("(objectClass=org.osgi.service.log.LogReaderService)");
		
		logTracker = new LogServiceTracker(context, filter );
		logTracker.open(true);

	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (logTracker != null)
			logTracker.close();
	}

	private class LogServiceTracker extends ServiceTracker {

		public LogServiceTracker(BundleContext ctx, Filter filter){
			super(ctx, filter, null);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			Object service = context.getService(reference);
			
			((LogReaderService)service).addLogListener(new TOSLF4JLogListener());
			return super.addingService(reference);
		
		}
		
	}
	
	private class TOSLF4JLogListener implements LogListener {

		@Override
		public void logged(LogEntry entry) {
			logLogEntry(logger, 
					entry.getLevel(), 
					MarkerFactory.getMarker(entry.getBundle().getSymbolicName()), 
					entry.getMessage(), 
					entry.getException());
			
		}
		
		private void logLogEntry(Logger logger, int level, Marker marker, String message, Throwable t){
			//
			// Level here is the LogService Level
			//
			switch(level){
			case LogService.LOG_DEBUG:
				logger.debug(marker, message, t);
				break;
			case LogService.LOG_ERROR:
				logger.error(marker, message, t);
				break;
			case LogService.LOG_WARNING:
				logger.warn(marker, message, t);
				break;	
			default:
				logger.info(marker, message, t);
				break;	
			}
		}

	}
}


