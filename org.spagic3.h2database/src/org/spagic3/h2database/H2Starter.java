package org.spagic3.h2database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.Console;
import org.h2.util.JdbcUtils;

public class H2Starter implements Runnable{
	public H2Starter(){
		
	}
	/*
	public static void main(String[] args) {
	try{
		System.out.println(" Starting H2 ");
		new Console().run(new String[]{});

		System.out.println(" Starting H2 IS UP");
		/*
		String spagicDbUrl = "jdbc:h2:tcp://localhost/~/spagic";
		String jbpmDbUrl = "jdbc:h2:tcp://localhost/~/jbpm";
	
		System.out.println(" Creating Spagic User ");
		executeSQL(spagicDbUrl, "sa", "", "CREATE USER IF NOT EXISTS  spagic PASSWORD 'spagic'");
		executeSQL(spagicDbUrl, "sa", "", "ALTER USER spagic ADMIN TRUE");
		System.out.println(" User Spagic Created ");
	
	
		executeSQL(jbpmDbUrl, "sa", "", "CREATE USER IF NOT EXISTS  jbpm PASSWORD 'jbpm'");
		executeSQL(jbpmDbUrl, "sa", "", "ALTER USER jbpm ADMIN TRUE");
		
	}catch (Exception e) {
		e.printStackTrace();
	}
	}*/


private static void executeSQL(String url, String user, String password, String sql) throws SQLException {
 	Connection conn = null;
    java.sql.Statement stat = null;
    try {
        conn = DriverManager.getConnection(url, user, password);
        stat = conn.createStatement();
       
        stat.execute(sql);
    }catch (Throwable t) {
    	t.printStackTrace();
		
    } finally {
        JdbcUtils.closeSilently(stat);
        JdbcUtils.closeSilently(conn);
    }
}


@Override
public void run() {
	try{
		new Console().run(new String[]{});
	}catch (Exception e) {
		e.printStackTrace();
	}
}
}
