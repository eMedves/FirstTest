/**
 * SchemaTypeFactory.java
 *
 * This file was generated by MapForce 2007sp1.
 *
 * YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
 * OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
 *
 * Refer to the MapForce Documentation for further details.
 * http://www.altova.com/mapforce
 */


package com.altova.types;

import java.math.BigInteger;
import java.math.BigDecimal;

public class SchemaTypeFactory {

  public static SchemaType createInstanceByString(String value) {
    if( value == null ) {
      SchemaString result = new SchemaString();
      result.setNull();
      return result;
    }
    if( value.length() == 0)
      return new SchemaString();
    // is it a boolean?
    if( value.compareToIgnoreCase("false")==0 )
      return new SchemaBoolean( false );
    if( value.compareToIgnoreCase("true")==0 )
      return new SchemaBoolean( true );
    // is it a kind of dateTime value?
    try {
      SchemaDateTime result = new SchemaDateTime( value );
      return result;
    }
    catch( StringParseException e ) {}
    try {
      SchemaDuration result = new SchemaDuration( value );
      return result;
    }
    catch( StringParseException e ) {}
    try {
      SchemaDate result = new SchemaDate( value );
      return result;
    }
    catch( StringParseException e ) {}
    try {
      SchemaTime result = new SchemaTime( value );
      return result;
    }
    catch( StringParseException e ) {}
    // is it a numeric value
    try {
      BigDecimal tmp = new BigDecimal(value);
      if( tmp.scale() <= 0 ) {
        if( tmp.compareTo( new BigDecimal( Integer.MAX_VALUE ) ) <= 0 &&
            tmp.compareTo( new BigDecimal( Integer.MIN_VALUE ) ) >= 0 )
          return new SchemaInt( tmp.intValue() );
        return new SchemaInteger( tmp.toBigInteger() );
      } else {
        return new SchemaDecimal( tmp );
      }
    }
    catch (NumberFormatException e) {
      // non of all - use string
      return new SchemaString( value );
    }
  }

  public static SchemaType createInstanceByObject(Object value) {
    if( value instanceof Boolean )
		return new SchemaBoolean( ((Boolean)value).booleanValue() );
    else if( value instanceof Byte )
      return new SchemaByte( ((Byte)value).byteValue() );
    // !!!!! to be completed
    else
      return new SchemaString( value.toString() );
  }

}
