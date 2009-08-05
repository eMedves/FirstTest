package org.spagic3.connectors.filesystem.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.connectors.filesystem.adapters.DefaultFileAdapter;
import org.spagic3.connectors.filesystem.adapters.IFileAdapter;
import org.spagic3.core.AbstractSpagicConnector;


public class FileSystemWriterConnector extends AbstractSpagicConnector {

	protected Logger logger = LoggerFactory.getLogger(FileSystemWriterConnector.class);
	private File directory;
	
    private IFileAdapter adapter = new DefaultFileAdapter();
    
    private String filePrefix = "spagic-";
    private String fileSuffix = ".xml";
    private boolean autoCreateDirectory = true;
    
    
	public void init() {
		  String directoryString = propertyConfigurator.getString("directory");
		  this.directory = new File(directoryString);
	}

	public void start() throws Exception {
		 if (directory == null) {
	            throw new RuntimeException("You must specify the directory");
	        }
	        if (autoCreateDirectory) {
	            directory.mkdirs();
	        }
	        if (!directory.isDirectory()) {
	            throw new RuntimeException("The directory property must be a directory but was: " + directory);
	        }
		
	}
    
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}
    
    public File getDirectory() {
        return directory;
    }

  
    public void setDirectory(File directory) {
        this.directory = directory;
    }	

	@Override
	public void process(Exchange exchange) {
			BufferedWriter bw = null;
	        File newFile = null;
	        boolean success = false;
	        try {
	        	Message in =  exchange.getIn(false);
	            String name = adapter.getOutputFileName(exchange, in);
	            if (name == null) {
	                newFile = File.createTempFile(filePrefix, fileSuffix, directory);
	            } else {
	                newFile = new File(directory, name);
	            }
	            if (!newFile.getParentFile().exists() && autoCreateDirectory) {
	                newFile.getParentFile().mkdirs();
	            }
	            if (logger.isDebugEnabled()) {
	                logger.debug("Writing to file: " + newFile.getCanonicalPath());
	            }
	            bw = new BufferedWriter(new FileWriter(newFile, false));
	            adapter.writeMessage(exchange, in, bw);
	            success = true;
	            done(exchange);
	        }catch (IOException e) {
				throw new IllegalStateException(e.getMessage(), e);
	        }catch (Exception genericException) {
	        	throw new IllegalStateException(genericException.getMessage(), genericException);

			} finally {
	            if (bw != null) {
	                try {
	                	bw.close();
	                } catch (IOException e) {
	                    logger.error("Caught exception while closing stream on error: " + e, e);
	                }
	            }
	            //cleaning up incomplete files after things went wrong
	            if (!success) {
	            	try {
	            		logger.debug("An error occurred while writing file " + newFile.getCanonicalPath() + ", deleting the invalid file");
	            		if (!newFile.delete())
	            			logger.warn("Unable to delete the file " + newFile.getCanonicalPath() + " after an error had occurred");
	            	} catch (IOException e) {
		                    logger.error("Caught exception while closing stream on error: " + e, e);
		            }
	            }
	    }
		
	}

}
