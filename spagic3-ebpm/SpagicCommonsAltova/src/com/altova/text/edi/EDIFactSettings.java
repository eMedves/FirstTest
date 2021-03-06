////////////////////////////////////////////////////////////////////////
//
// EDIFactSettings.java
//
// This file was generated by MapForce 2007sp1.
//
// YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
// OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
//
// Refer to the MapForce Documentation for further details.
// http://www.altova.com/mapforce
//
////////////////////////////////////////////////////////////////////////

package com.altova.text.edi;

public class EDIFactSettings extends EDISettings {
	private long mSyntaxVersionNumber = 2;

	private char mSyntaxLevel = 'A';

	private String mControllingAgency = "UNO";

	private boolean mWriteUNA = true;

	public long getSyntaxVersionNumber() {
		return mSyntaxVersionNumber;
	}

	public char getSyntaxLevel() {
		return mSyntaxLevel;
	}

	public String getControllingAgency() {
		return mControllingAgency;
	}

	public boolean getWriteUNA() {
		return mWriteUNA;
	}

	public void setSyntaxVersionNumber(long rhs) {
		mSyntaxVersionNumber = rhs;
	}

	public void setSyntaxLevel(char rhs) {
		mSyntaxLevel = rhs;
	}

	public void setControllingAgency(String rhs) {
		mControllingAgency = rhs;
	}

	public void setWriteUNA(boolean rhs) {
		mWriteUNA = rhs;
	}
}
