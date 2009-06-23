package org.spagic3.connectors.filesystem.reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.connectors.filesystem.adapters.DefaultFileAdapter;
import org.spagic3.connectors.filesystem.adapters.IFileAdapter;
import org.spagic3.core.AbstractSpagicConnector;
import org.spagic3.dirwatcher.DirWatcher;

public class FileSystemPollerConnector extends AbstractSpagicConnector{

		protected Logger logger = LoggerFactory.getLogger(FileSystemPollerConnector.class);
		
		private Timer pollingScheduler = null;
		private TimerTask pollingTask = null;
		private File directory;
	    private String extensionFilter;
	    private boolean deleteFile = true;
	   
	 
	    private IFileAdapter adapter = new DefaultFileAdapter();
	    private ConcurrentHashMap<String, InputStream> openedStream = new ConcurrentHashMap<String, InputStream>();
	    private ConcurrentHashMap<String, File> openedFiles = new ConcurrentHashMap<String, File>();
	    public void init() {
	    	String directoryString = propertyConfigurator.getString("directory");
	    	this.directory = new File(directoryString);
	    	this.extensionFilter = propertyConfigurator.getString("filter", ".xml");
	    	
		}
	   
		public void start() throws Exception {
			this.pollingTask  = new DirWatcher(directory, extensionFilter) {
				protected void onChange(File file, String action) {
					try{
						if (action.equalsIgnoreCase("add")){
							String uri = file.toURI().relativize(file.toURI()).toString();
							processFile(file);
						}
					}catch(Exception e){
						logger.error(e.getMessage(), e);
					}		
				}
			};

			pollingScheduler = new Timer();
			pollingScheduler.schedule(pollingTask, new Date(), 5000);
			
		}
		
		public void stop() throws Exception {
			pollingScheduler.cancel();
			
		}
		
		public void process(Exchange exchange) throws Exception {
			String exchangeId = exchange.getId();
			
			InputStream is = openedStream.remove(exchangeId);
			if (is == null){
				throw new Exception("Cannot Get The Opened File For Stream for Exchange" + exchangeId);
			}
			is.close();
			
			File f = openedFiles.remove(exchangeId);
			
			if (f == null)
				throw new Exception("Cannot Get The Opened file Stream for Exchange" + exchangeId);
			
			if (deleteFile){
				if (f.delete()){
					  throw new IOException("Could not delete file " + f);
				}
			}
			
			
			
		}
		
		public void processFile(File f) {
			try{
				InputStream stream = new BufferedInputStream(new FileInputStream(f));
				Exchange exchange = createInOnlyExchange();
				Message inMessage = exchange.getIn(true);
				
				adapter.readFile(exchange, inMessage, stream, f.getCanonicalPath());
				
				this.openedStream.put(exchange.getId(), stream);
				this.openedFiles.put(exchange.getId(), f);
	        
				send(exchange);
				stream.close();
			}catch (Exception e) {
				logger.error("Error in Process File " + e.getMessage(), e);
			}
		}
		
}
