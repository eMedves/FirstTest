package org.spagic3.core;

public class MandatoryPropertyNotFoundException extends RuntimeException {

	

	public MandatoryPropertyNotFoundException(String propertyName, Throwable arg1) {
		super("Property ["+propertyName+"] is Mandatory", arg1);
		
	}

	public MandatoryPropertyNotFoundException(String propertyName) {
		super("Property ["+propertyName+"] is Mandatory");
	}

	
	
}
