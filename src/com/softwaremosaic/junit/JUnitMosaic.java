package com.softwaremosaic.junit;

import com.softwaremosaic.junit.lang.Predicate;
import com.softwaremosaic.junit.tools.AssertJob;
import com.softwaremosaic.junit.tools.ConcurrentAsserter;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

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
     * Spin up n threads that invoke the step() method of the supplied instance of AssertJob.  This
     * method will not return until all of the threads have completed calling 'step()' a random number
     * of times.<p/>
     *
     * Each thread will invoke the 'step()' method by passing in the 'state' of the thread on each call.
     * The state is always null on the first call, and from then on will be the value returned from
     * the last call to 'step()' from that thread.<p/>
     *
     * The AssertJob itself must be immutable.
     *
     *
     * @param concurrentJob the concurrent work to be carried out from multiple threads
     *
     * @return a list containing the result of each threads final call to 'step()'
     */
    public static <T> List<T> runFromMultipleThreads(AssertJob<T> concurrentJob) {
        int                numThreads                 = Runtime.getRuntime().availableProcessors() * 4;
        Generator<Integer> numStepsPerThreadGenerator = PrimitiveGenerators.integers(0, 100);

        return multiThreadedAssert( numThreads, concurrentJob, numStepsPerThreadGenerator );
    }

    /**
     * Spin up n threads that invoke the step() method of the supplied instance of AssertJob.  This
     * method will not return until all of the threads have completed calling 'step()' a random number
     * of times.<p/>
     *
     * Each thread will invoke the 'step()' method by passing in the 'state' of the thread on each call.
     * The state is always null on the first call, and from then on will be the value returned from
     * the last call to 'step()' from that thread.<p/>
     *
     * The AssertJob itself must be immutable.
     *
     *
     * @param concurrentJob the concurrent work to be carried out from multiple threads
     *
     * @return a list containing the result of each threads final call to 'step()'
     */
    public static <T> List<T> multiThreadedAssert( int numThreads, AssertJob<T> concurrentJob, Generator<Integer> numStepsPerThreadGenerator ) {
        ConcurrentAsserter worker = new ConcurrentAsserter( numThreads, concurrentJob, numStepsPerThreadGenerator );

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
    public static void spinUntilTrue( long timeoutMillis, Callable<Boolean> predicate ) {
        long startMillis = System.currentTimeMillis();

        while ( !callPredicate(predicate) ) {
            long durationMillis = System.currentTimeMillis() - startMillis;

            if ( durationMillis > timeoutMillis ) {
                throw new IllegalStateException( "Timeout" );
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
        Runtime.getRuntime().gc();

        spinUntilTrue( new Callable<Boolean>() {
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
