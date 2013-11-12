package com.mosaic.junitpro;

import com.mosaic.junitpro.lang.Predicate;
import com.mosaic.junitpro.tools.AssertionJob;
import com.mosaic.junitpro.tools.ConcurrentAsserter;

/**
 *
 */
public class Assert extends org.junit.Assert {


    public static void assertEventually( Predicate predicate ) {
        assertEventually( 3000, predicate );
    }

    public static void assertEventually( long timeoutMillis, Predicate predicate ) {
        long startMillis = System.currentTimeMillis();

        while ( !predicate.eval() ) {
            long durationMillis = System.currentTimeMillis() - startMillis;

            if ( durationMillis > timeoutMillis ) {
                throw new IllegalStateException( "Timeout - " +predicate.reasonForFailure() );
            }

            Thread.yield();
        }
    }

    public static void multiThreadedAssert( AssertionJob cloneableJob ) {
        multiThreadedAssert( Runtime.getRuntime().availableProcessors()*4, cloneableJob );
    }

    public static void multiThreadedAssert( int numThreads, AssertionJob cloneableJob ) {
        ConcurrentAsserter worker = new ConcurrentAsserter( numThreads, cloneableJob );

        worker.run();
    }
}
