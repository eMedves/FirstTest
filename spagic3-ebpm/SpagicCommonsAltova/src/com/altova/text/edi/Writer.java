////////////////////////////////////////////////////////////////////////
//
// Writer.java
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

import java.io.OutputStreamWriter;
import java.io.IOException;

public class Writer
{
	OutputStreamWriter mOutput;
	StringBuffer mPendingSeparators = new StringBuffer ();
	boolean mNewlineAfterSegments = true;
	ServiceChars mServiceChars;

	public ServiceChars getServiceChars() {
		return mServiceChars;
	}

	public boolean getNewlineAfterSegments() {
		return mNewlineAfterSegments; 
	}
	
	void setNewlineAfterSegments(boolean newline) { 
		mNewlineAfterSegments = newline; 
	}

	public Writer (OutputStreamWriter output, ServiceChars serviceChars) {
		mOutput = output;
		mServiceChars = serviceChars;
	}

	public void write (String s) throws IOException{
		if (s.length() == 0)
			return;
		
		mOutput.write (mPendingSeparators.toString());
		mOutput.write (s);
		mPendingSeparators.setLength(0);
	}

	public void addSeparator (byte serviceChar) {
		if ( serviceChar == ServiceChars.None)
			return;
		mPendingSeparators.append (mServiceChars.getSeparator(serviceChar));
	}

	public void clearPendingSeparators (byte separator)	{
		while (mPendingSeparators.length() > 0 && mPendingSeparators.charAt(mPendingSeparators.length()-1) == mServiceChars.getSeparator(separator))
			mPendingSeparators.deleteCharAt(mPendingSeparators.length()-1); // gasp!
	}
	
	public void clearPendingSeparators ()	{
		mPendingSeparators.setLength(0);
	}
}