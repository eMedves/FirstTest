package org.spagic3.connectors.jdbc;

public class PollingParameters {

	// The name of the key column. It should be returned from the root
	// statement, and
	// the value of which is used to update the value of stored key. For further
	// explanation
	// refer to next section.
	public String keyColumn;
	// The initial value for the stored keyColumn. This value is only used once to
	// initialize the keyColumn.
	public String initialKeyValue;
	// If this property exists then the output messages will have the column name
	// as an
	// attribute instead of as an element name.
	public boolean columnNameAsAttribute;
	// If this property exists and the name property is defined for a statement
	// element
	// then the output messages will have that name as an attribute instead of as
	// an element
	// name.
	public boolean rowNameAsAttribute;
	// If this property exists then the output messages will have the column
	// result value
	// as an attribute instead of as a CDATA section.
	public boolean valueAsAttribute;
	// The number of rows to be used to generate an outgoing message out of the
	// root statement result set. Typically, <rowsInMessage>1</rowsInMessage> 
	// is used to
	// generate an XML message per each row. In this case, the tranction of the
	// root SELECT
	// SQL statement is executed when all the rows previously returned are
	// processed.
	public int rowsInMessage;
	// A unique ID for this database communication point (unique from other
	// database
	// communication points). It is used to uniquely identify the key that is
	// stored inside database.
	public String commPointID;
	// The statement element contains one SQL element that will be executed and a
	// number of other statement elements that will each be executed for every row
	// returned
	// by the SQL statement.
	public StatementElement rootStatement;
	// Identifier of database: some statements may be database dependent
	public String databaseType;

	// JNDI datasource name
	public String datasourceName;
	// JNDI datasource name for keys table
	public String keyDatasourceName;
	public String keyCreationStatement;
	
	// The max number of messages processed for each polling unit 
	public int maxMessagesPerUnit;
}
