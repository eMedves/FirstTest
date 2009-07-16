package org.spagic3.attachments;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.OID;
import org.neodatis.odb.impl.core.oid.OdbObjectOID;



public class AttachmentStore {

	private static final String ODB_ATTACHMENT_STORE = "/tmp/store/odb.attachments";
	
	public static Long store(String name, DataHandler value){
		ODB odb = null;
		
		try{
			odb = ODBFactory.open(ODB_ATTACHMENT_STORE);
			Attachment att = new Attachment(name, value);
			OID oid = odb.store(att);
			return oid.getObjectId();
		} finally {
            if (odb != null) {
                // Close the database
                odb.close();
            }
        }		
	}
	
	public static Attachment load(Long id){
		ODB odb = null;
		
		try{
			odb = ODBFactory.open(ODB_ATTACHMENT_STORE);
			OID oid = new OdbObjectOID(id);
			return (Attachment)odb.getObjectFromId(oid);
		} finally {
            if (odb != null) {
                // Close the database
                odb.close();
            }
        }	
	}
	
	public static void delete(Long id){
		ODB odb = null;
		
		try{
			odb = ODBFactory.open(ODB_ATTACHMENT_STORE);
			OID oid = new OdbObjectOID(id);
			odb.deleteObjectWithId(oid);
		} finally {
            if (odb != null) {
                // Close the database
                odb.close();
            }
        }	
	}
	

}
