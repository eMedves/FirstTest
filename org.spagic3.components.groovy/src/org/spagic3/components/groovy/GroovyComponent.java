package org.spagic3.components.groovy;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.servicemix.nmr.api.Exchange;
import org.apache.servicemix.nmr.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.core.BaseSpagicService;
import org.spagic3.core.SpagicUtils;


public class GroovyComponent extends BaseSpagicService {
	
	private ScriptEngine engine;
	private CompiledScript compiledScript;
	protected Logger logger = LoggerFactory.getLogger(GroovyComponent.class);
	
	public URL script = null;
	
	
	public void init(){
		try{
			String scriptProperty = propertyConfigurator.getString("script");
			this.script = SpagicUtils.getURL(scriptProperty);
			this.engine = createScriptEngine();
			
			if (engine instanceof Compilable) {
	                Compilable compilable = (Compilable) engine;
	                compileScript(compilable);
	        }
			
		}catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(" Cannot instantiate Groovy Component", e);
		}
	}
	
	protected void compileScript(Compilable compilable) throws Exception {
        try{
        	this.compiledScript = compilable.compile(new InputStreamReader(script.openStream()));
        }catch (ScriptException e) {
            throw new Exception("Failed to parse compiledScript. Reason:  " + e, e);
        } catch (IOException e) {
            throw new Exception("Failed to parse compiledScript. Reason:  " + e, e);
        }
    }

    protected ScriptEngine createScriptEngine() {
        ScriptEngineManager manager = new ScriptEngineManager();
        return manager.getEngineByName("groovy");
    }
	
    protected void populateBindings(Bindings bindings, Exchange exchange, Message in, Message out) throws Exception {
        bindings.put("exchange", exchange);
        bindings.put("inMessage", in);
        bindings.put("log", logger);
        bindings.put("outMessage", out);
       
    }
    
	@Override
	public boolean run(Exchange exchange, Message in, Message out)
			throws Exception {
		
		//synchronized (this) {
 		
		logger.info(" GroovyComponent Component -> Run ["+getSpagicId()+"]"  );
		Bindings bindings = null;
     	try{
     		logger.info(" GroovyComponent Component -> Before Create Bindings ["+getSpagicId()+"]"  );
     		bindings = engine.createBindings();
     		logger.info(" GroovyComponent Component -> Populating Bindings  ["+getSpagicId()+"]"  );
     		populateBindings(bindings, exchange, in, out);
     	}catch (Throwable e) {
     		logger.error(" Script Component Error", e);
		}
     	try {
     		long startAt = System.currentTimeMillis();
         	runScript(bindings);
         	long endAt = System.currentTimeMillis();
         	logger.info(" Script["+getSpagicId()+" Script ] |"+ (endAt -startAt) );
         
         	logger.info(" Script Component -> End ["+getSpagicId()+"]");
         	return true;
         	
     	}catch (ScriptException e) {
     		logger.error(" Script Component Error", e);
         	throw new Exception("Failed to run compiledScript. Reason: " + e, e);
     	}
     	
	}
	
	 protected void runScript(Bindings bindings) throws ScriptException {
	        if (compiledScript != null) {
	            compiledScript.eval(bindings);
	        }
	 }
}
