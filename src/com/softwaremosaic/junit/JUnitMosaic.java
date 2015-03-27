package com.softwaremosaic.junit;

import com.softwaremosaic.junit.lang.Predicate;
import com.softwaremosaic.junit.lang.TakesIntFunction;
import com.softwaremosaic.junit.tools.ConcurrentTester;
import net.java.quickcheck.Generator;
import org.junit.ComparisonFailure;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.internal.ExactComparisonCriteria;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.unmodifiableSet;

/**
 * A collection of tools that enhance writing of unit tests.
 */
@SuppressWarnings("unchecked")
public class JUnitMosaic extends org.junit.Assert {

    private static final Set        EMPTY           = unmodifiableSet( new HashSet() );
    private static final AtomicLong nextLongCounter = new AtomicLong(0);



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


    /**
     * Spin up n threads that each invoke the concurrentJob.  This method will not return until all of
     * the threads have completed.
     *
     * @param concurrentJob the concurrent work to be carried out from multiple threads, its argument
     *                      is the number of times that the job has been requested to repeat its work.
     *
     * @return a list containing the result of each threads call to concurrentJob
     */
    public static <T> List<T> multiThreaded( TakesIntFunction<T> concurrentJob ) {
        String           name   = new Exception().getStackTrace()[1].getMethodName();
        ConcurrentTester worker = new ConcurrentTester( name, concurrentJob );

        return worker.runToCompletion();
    }

    /**
     * Spin up n threads that each invoke the concurrentJob.  This method will not return until all of
     * the threads have completed.
     *
     * @param numThreads    the number of threads to use in the test
     * @param concurrentJob the concurrent work to be carried out from multiple threads, its argument
     *                      is the number of times that the job has been requested to repeat its work.
     * @param numStepsPerThreadGenerator how many times should each thread request concurrentJob to repeat
     *                                   its own work?  This value becomes the argument past to concurrentJob.
     *
     * @return a list containing the result of each threads call to concurrentJob
     */
    public static <T> List<T> multiThreaded( int numThreads, TakesIntFunction<T> concurrentJob, Generator<Integer> numStepsPerThreadGenerator ) {
        String           name   = new Exception().getStackTrace()[1].getMethodName();
        ConcurrentTester worker = new ConcurrentTester( name, concurrentJob )
                                      .withNumThreads( numThreads )
                                      .withIterationCountGenerator( numStepsPerThreadGenerator );

        return worker.runToCompletion();
    }






    /**
     * Counts the number of active threads whose names start with the specified prefix.
     */
    public static int countThreads( String targetThreadNamePrefix ) {
        int count = 0;

        for ( Thread t : Thread.getAllStackTraces().keySet() ) {
            if ( t.getName().startsWith(targetThreadNamePrefix) ) {
                count += 1;
            }
        }

        return count;
    }

    public static void spinUntilThreadCountsReaches( final String targetThreadNamePrefix, final int targetCount ) {
        spinUntilTrue(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return countThreads(targetThreadNamePrefix) == targetCount;
            }
        });
    }

    /**
     * Block the current thread until there are no threads whose name begins with
     * the specified prefix running.
     */
    public static void spinUntilAllThreadsComplete( String targetThreadNamePrefix ) {
        spinUntilThreadCountsReaches( targetThreadNamePrefix, 0 );
    }




    public static long nextLong() {
        return nextLongCounter.incrementAndGet();
    }

    /**
     * Returns a unique name derived from an incrementing threadsafe counter and the name of the calling class.
     */
    public static String nextName() {
        try {
            String v = Class.forName( new RuntimeException().getStackTrace()[1].getClassName() ).getSimpleName();

            return v + nextLong();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException( e );
        }
    }

    /**
     * Block the current thread until the specified condition is true.
     */
    public static void spinUntilTrue( Callable<Boolean> predicate ) {
        spinUntilTrue( 3000, predicate );
    }

    /**
     * Block the current thread until the specified condition is true.
     */
    public static void spinUntilTrue( String errorMessage, Callable<Boolean> predicate ) {
        spinUntilTrue( errorMessage, 3000, predicate );
    }

    /**
     * Block the current thread until the two arguments become equal.
     */
    public static <T> void spinUntilEquals( final T expected, final Callable<T> fetcher ) {
        final AtomicReference<T> lastResult = new AtomicReference<>();

        try {
            spinUntilTrue( 9000, new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    try {
                        T b = fetcher.call();
                        lastResult.set( b );

                        return Objects.deepEquals( expected, b );
                    } catch ( ConcurrentModificationException ex ) {
                        // ignore concurrent modification exceptions and try again
                        return false;
                    }
                }
            } );
        } catch ( IllegalStateException e ) {
            throw new ComparisonFailure("a != b after 3s", Objects.toString(expected), Objects.toString(lastResult.get()));
        }
    }

    /**
     * Block the current thread until the specified condition is true.
     */
    public static void spinUntilTrue( long timeoutMillis, Callable<Boolean> predicate ) {
        spinUntilTrue( "Timedout after " + timeoutMillis + "ms", timeoutMillis, predicate );
    }

    /**
     * Block the current thread until the specified condition is true.
     */
    public static void spinUntilTrue( String errorMessage, long timeoutMillis, Callable<Boolean> predicate ) {
        long startMillis = System.currentTimeMillis();

        while ( !callPredicate(predicate) ) {
            long durationMillis = System.currentTimeMillis() - startMillis;

            if ( durationMillis > timeoutMillis ) {
                throw new IllegalStateException( errorMessage );
            }

            Thread.yield();
        }
    }


    /**
     * Tool for detecting memory links. When an object should be GC'd, wrap it with a WeakReference and then trigger
     * any code necessary to break any strong references. Once done call this method to wait for the GC to release
     * the object. If it does not release then the weak reference will hold on to the underlying object and this
     * method will error.
     */
    public static void spinUntilReleased( final Reference ref ) {
        spinUntilReleased( "value was not garbage collected", ref );
    }

    /**
     * Tool for detecting memory links. When an object should be GC'd, wrap it with a WeakReference and then trigger
     * any code necessary to break any strong references. Once done call this method to wait for the GC to release
     * the object. If it does not release then the weak reference will hold on to the underlying object and this
     * method will error.
     */
    public static void spinUntilReleased( final String msg, final Reference ref ) {
        Runtime.getRuntime().gc();

        spinUntilTrue( msg,  new Callable<Boolean>() {
            public Boolean call() {
                return ref.get() == null;
            }
        });
    }

    /**
     * Factory method for wrapping multiple objects with a WeakReference.
     */
    public static List<Reference> createWeakReferences( Object...objects ) {
        List<Reference> list = new ArrayList(objects.length);

        for ( Object o : objects ) {
            list.add( new WeakReference(o) );
        }

        return list;
    }

    /**
     * Block the current thread until each reference supplied has been garbage
     * collected.
     */
    public static void spinUntilReleased( List<Reference> refs ) {
        for ( Reference ref : refs ) {
            spinUntilReleased( ref );
        }
    }




    public static void assertSetEquals( Set expectedSet, Set actualSet ) {
        SetComparison r = compare( expectedSet, actualSet );

        assertEquals( "The following elements were not expected: "+r.onlyInSetB, 0, r.onlyInSetB.size() );
        assertEquals( "The following elements were expected, but did not occur: " + r.onlyInSetA, 0, r.onlyInSetA.size() );
    }




    private static <T> Set<T> copySet( Set<T> set ) {
        Set<T> copy = new HashSet<T>(set.size());

        copy.addAll( set );

        return copy;
    }

    static <T> SetComparison<T> compare( Set<T> setA, Set<T> setB ) {
        setA = convertNullSetToEmptySet(setA);
        setB = convertNullSetToEmptySet(setB);

        int maxSize = Math.max( setA.size(), setB.size() );

        Set<T> workingSetB = copySet(setB);

        Set<T> onlyInA = new HashSet(maxSize);
        Set<T> inBoth  = new HashSet(maxSize);

        for ( T v : setA ) {
            boolean wasRemoved = workingSetB.remove( v );

            if ( wasRemoved ) {
                inBoth.add( v );
            } else {
                onlyInA.add( v );
            }
        }

        return new SetComparison<T>(
                onlyInA,
                workingSetB,
                inBoth
        );
    }

    /**
     * Assert that two lists of strings are equal.  Written to take advantage of IntelliJ's
     * text comparison display.
     */
    public static void assertParagraphs( List<String> expected, List<String> actual ) {
        String a = joinParagraph(expected);
        String b = joinParagraph(actual);

        assertEquals( a, b );
    }


    /**
     * Uses reflection to support primitive arrays.
     */
    public static void assertArrayEquals(Object expecteds, Object actuals) throws ArrayComparisonFailure {
        new ExactComparisonCriteria().arrayEquals( "",  expecteds, actuals );
    }



    private static String joinParagraph( List<String> paragraph ) {
        StringJoiner joiner = new StringJoiner("\n");

        for ( String s : paragraph ) {
            joiner.add( s );
        }

        return joiner.toString();
    }


    static class SetComparison<T> {
        public final Set<T> onlyInSetA;
        public final Set<T> onlyInSetB;
        public final Set<T> inBothSets;

        private SetComparison( Set<T> onlyInSetA, Set<T> onlyInSetB, Set<T> inBothSets ) {
            this.onlyInSetA = unmodifiableSet( onlyInSetA );
            this.onlyInSetB = unmodifiableSet( onlyInSetB );
            this.inBothSets = unmodifiableSet( inBothSets );
        }
    }

    private static <T> Set<T> convertNullSetToEmptySet( Set<T> set ) {
        if ( set == null ) {
            return EMPTY;
        }

        return set;
    }

    private static boolean callPredicate( Callable<Boolean> predicate ) {
        try {
            return predicate.call();
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }


}
