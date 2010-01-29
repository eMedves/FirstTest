package org.spagic3.exchanges;

import org.apache.servicemix.nmr.api.Exchange;
import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.OID;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.query.IQuery;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;


public class DroolsExchangeStore {

	private static final String EXCHANGE_ID = "exchangeId";
	private static final String ODB_ATTACHMENT_STORE = "/tmp/store/drools.exchanges";
		
	public static void store(String exchangeId, Exchange ex){
		ODB odb = null;
		
		try{
			odb = ODBFactory.open(ODB_ATTACHMENT_STORE);
			DroolsODBExchange odbEx = new DroolsODBExchange(exchangeId, ex);
			OID oid = odb.store(odbEx);
			odb.commit();
		} finally {
            if (odb != null) {
                // Close the database
                odb.close();
            }
        }		
	}
	
	public static Exchange load(String exchangeId){
		ODB odb = null;
		
		try{
			odb = ODBFactory.open(ODB_ATTACHMENT_STORE);
            IQuery query = new CriteriaQuery(DroolsODBExchange.class, Where.equal(EXCHANGE_ID, exchangeId));
            Objects odbExchanges = odb.getObjects(query);
            
            if (odbExchanges.size() == 0) {
            	return null;
            } else if (odbExchanges.size() == 1) {
            	return ((DroolsODBExchange)odbExchanges.next()).getExchange();
            } else {
            	throw new RuntimeException("ExchangeStore: found more than one attachment with exchangeId: " + exchangeId);
            }
		} finally {
            if (odb != null) {
                // Close the database
                odb.close();
            }
        }	
	}
	
	public static Exchange delete(String exchangeId){
		ODB odb = null;
		
		try{
			odb = ODBFactory.open(ODB_ATTACHMENT_STORE);
			IQuery query = new CriteriaQuery(DroolsODBExchange.class, Where.equal(EXCHANGE_ID, exchangeId));
			Objects odbExchanges = odb.getObjects(query);
			
            if (odbExchanges.size() == 0) {
            	return null;
            } else if (odbExchanges.size() == 1) {
            	DroolsODBExchange odbEx = (DroolsODBExchange)odbExchanges.next();
            	Exchange result = odbEx.getExchange();
            	odb.delete(odbEx);
            	return result;
            } else {
                while (odbExchanges.hasNext()) {
                	odb.delete(odbExchanges.next());
                }
            	throw new RuntimeException("ExchangeStore: found more than one attachment with exchangeId: " + exchangeId);
            }            
            
		} finally {
            if (odb != null) {
                // Close the database
                odb.close();
            }
        }	
	}
	

}
