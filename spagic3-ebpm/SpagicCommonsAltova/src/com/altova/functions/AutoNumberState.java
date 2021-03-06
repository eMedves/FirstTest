/**
 * AutoNumberState.java
 *
 * This file was generated by MapForce 2007sp1.
 *
 * YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
 * OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
 *
 * Refer to the MapForce Documentation for further details.
 * http://www.altova.com/mapforce
 */
 
package com.altova.functions;
 
import com.altova.types.SchemaInt;

public class AutoNumberState
{
    boolean mInit = false;
    int mValue;
    int mStep;

    public SchemaInt getCurrent()
    {
    	if (!mInit)
            throw new com.altova.AltovaException("AutoNumberState: Current: not initialized");
			
        SchemaInt v = new SchemaInt(mValue);
        mValue = mValue+mStep;
        return v;
    }

    public boolean isInitialized()
    {
        return mInit;
    }

    public void init(int startValue, int step)
    {
        mValue = startValue;
        mStep = step;
        mInit = true;
    }
}
