package com.softwaremosaic.junit.tools;


/**
 * Performs a single step in a concurrent test.  This job needs to be immutable.<p/>
 *
 * This class is usually used from 'JUnitMosaic.runFromMultipleThreads()'.
 */
public interface AssertJob<T> {

    /**
     * Perform a single step in the concurrent testing.  Called from multiple
     * threads to create a concurrent/stochastic test of a class.<p/>
     *
     * The state past in to this method is always null on the first call, and
     * from then on will be the value returned from the last call to 'step()' from that thread.<p/>
     *
     * @param expectedStateSoFar initially null, then the result of the last call to this method from the same thread
     *
     * @return the state to be carried on to the next call
     */
    public T step( T expectedStateSoFar );

}
