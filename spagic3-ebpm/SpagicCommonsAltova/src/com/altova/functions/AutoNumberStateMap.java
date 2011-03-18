/**
 * AutoNumberStateMap.java
 *
 * This file was generated by MapForce 2007sp1.
 *
 * YOU SHOULD NOT MODIFY THIS FILE, BECAUSE IT WILL BE
 * OVERWRITTEN WHEN YOU RE-RUN CODE GENERATION.
 *
 * Refer to the MapForce Documentation for further details.
 * http://www.altova.com/mapforce
 */
 
package  com.altova.functions;

import java.util.Hashtable;

public class AutoNumberStateMap
{
    private Hashtable mStates = new Hashtable();

    public AutoNumberState getInstance(long ID)
    {
    	Long id = new Long (ID);
        if (mStates.containsKey(id))
            return (AutoNumberState) mStates.get(id);

        AutoNumberState state = new AutoNumberState();
        mStates.put(id, state);
        return state;
    }
}
