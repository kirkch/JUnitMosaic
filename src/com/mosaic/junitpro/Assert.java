package com.mosaic.junitpro;

import com.mosaic.junitpro.lang.Predicate;

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

}
