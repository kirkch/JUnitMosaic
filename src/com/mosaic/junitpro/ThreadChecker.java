package com.mosaic.junitpro;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ThreadChecker {

    private static Set<String> initialThreadNames = new HashSet<String>();

    public static void testAboutToStart( Test testAnnotation ) {
        if ( !testAnnotation.threadCheck() ) {
            return;
        }

        assert initialThreadNames.isEmpty();

        initialThreadNames = getActiveThreadNames();
    }

    public static void testHasFinished( Test testAnnotation ) {
        if ( !testAnnotation.threadCheck() ) {
            return;
        }

        try {
            Assert.assertEventually( 5000, new Predicate() {
                private Set<String> remainingThreadNames;

                public boolean eval() {
                    this.remainingThreadNames = getActiveThreadNames();

                    remainingThreadNames.removeAll( initialThreadNames );

                    return remainingThreadNames.isEmpty();
                }

                public String reasonForFailure() {
                    return "The following threads were not running when the test started, and have not shutdown in a timely manner after the test finished: " + remainingThreadNames;
                }
            });
        } finally {
            initialThreadNames.clear();
        }
    }


    private static Set<String> getActiveThreadNames() {
        Set<String> threadNames = new HashSet<String>();

        for ( Thread t : Thread.getAllStackTraces().keySet() ) {
            threadNames.add(t.getName());
        }

        return threadNames;
    }
}
