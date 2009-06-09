package org.spagic3.components.jdbc;

import org.apache.servicemix.nmr.api.Exchange;
import org.spagic3.core.AbstractSpagicService;


public class JDBCComponent extends AbstractSpagicService {

	@Override
	public void process(Exchange exchange) throws Exception {
		System.out.println("JDBC Component ["+getSpagicId()+"] process Exchange Method");	
	}
	

}
