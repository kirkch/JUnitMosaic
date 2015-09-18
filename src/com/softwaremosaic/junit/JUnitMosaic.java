package com.softwaremosaic.junit;

import com.softwaremosaic.junit.lang.Function0;
import com.softwaremosaic.junit.lang.Predicate;
import com.softwaremosaic.junit.lang.TakesIntFunction;
import com.softwaremosaic.junit.tools.ConcurrentTester;
import com.softwaremosaic.junit.tools.SystemStallDetector;
import net.java.quickcheck.Generator;
import org.junit.ComparisonFailure;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.internal.ExactComparisonCriteria;
import org.junit.internal.runners.model.MultipleFailureException;
import testmosaics.audit.Backdoor;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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



    public static void assertException( Throwable expected, Function0 op ) {
        try {
            op.invoke();
            fail( "No exception thrown.  Expected " + expected );
        } catch ( Throwable ex ) {
            assertEquals( toString(expected), toString(ex) );
        }
    }

    private static String toString( Throwable ex ) {
        return ex.getClass().getCanonicalName() + ": " + ex.getMessage();
    }

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

    /**
     * This method will not return until the number of active threads with the specified prefix
     * equals 'targetCount' or n seconds of active CPU time has past.
     */
    public static void spinUntilThreadCountsReaches( final String targetThreadNamePrefix, final int targetCount ) {
        spinUntilTrue(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return countThreads(targetThreadNamePrefix) == targetCount;
            }
        });
    }

    /**
     * Block the current thread until there are no threads whose name begins with
     * the specified prefix running or n seconds of active CPU time has past.
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
     * Block the current thread until the specified condition is true or n seconds of active CPU
     * time has past.
     */
    public static void spinUntilTrue( Callable<Boolean> predicate ) {
        spinUntilTrue( 3000, predicate );
    }

    /**
     * Block the current thread until the specified condition is true or n seconds has past.
     */
    public static void spinUntilTrue( String errorMessage, Callable<Boolean> predicate ) {
        spinUntilTrue( errorMessage, 3000, predicate );
    }

    /**
     * Block the current thread until the two arguments become equal or n seconds has past.
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
     * Block the current thread until the specified condition is true or at least timeoutMillis of
     * active CPU time has past.  System stalls, such as stop the world GCs or swap usage will not
     * count towards timeoutMillis.
     */
    public static void spinUntilTrue( long timeoutMillis, Callable<Boolean> predicate ) {
        spinUntilTrue( "Timedout after " + timeoutMillis + "ms", timeoutMillis, predicate );
    }

    /**
     * Block the current thread until the specified condition is true or at least timeoutMillis of
     * active CPU time has past.  System stalls, such as stop the world GCs or swap usage will not
     * count towards timeoutMillis.
     */
    public static void spinUntilTrue( String errorMessage, long timeoutMillis, Callable<Boolean> predicate ) {
        long startMillis  = System.currentTimeMillis();
        long systemDelay0 = SystemStallDetector.getTotalDelaySoFarMillis();

        while ( !callPredicate(predicate) ) {
            long durationMillis = System.currentTimeMillis() - startMillis;

            if ( durationMillis > timeoutMillis ) {
                // we have exceeded the max duration via the wall clock time;  however has there
                // been any major stop the world GCs or OS stalls due to IO etc?  If so, give this
                // spin more time.
                long systemDelay1     = SystemStallDetector.getTotalDelaySoFarMillis();
                long totalDelayMillis = systemDelay1 - systemDelay0;

                if ( durationMillis-totalDelayMillis > timeoutMillis ) {
                    throw new IllegalStateException( errorMessage );
                }
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

    /**
     * Invokes n jobs concurrently and waits for them to all complete.  Very similar in principle to
     * JUnitMosaic.multiThreaded() however where function takes a single job and invokes it n times
     * concurrently this function takes n jobs and runs each of them once concurrently.<p/>
     *
     * Will wait up to 10 seconds for all of the jobs to complete before aborting the run.
     */
    public static void runConcurrentlyAndWaitFor( Runnable...jobs ) throws MultipleFailureException {
        runConcurrentlyAndWaitFor( inferCallerRefFromCallStack(), jobs );
    }

    private static String inferCallerRefFromCallStack() {
        try {
            StackTraceElement caller    = new RuntimeException().getStackTrace()[2];
            String            callerFQN = caller.getClassName();
            String            callerRef = Class.forName( callerFQN ).getSimpleName() + "." + caller.getMethodName();

            return callerRef;
        } catch ( ClassNotFoundException ex ) {
            // very very unlikely to ever occur, as we read the name of the class off of the callers stack trace
            throw new RuntimeException( ex );
        }
    }

    /**
     * Invokes n jobs concurrently and waits for them to all complete.  Very similar in principle to
     * JUnitMosaic.multiThreaded() however where function takes a single job and invokes it n times
     * concurrently this function takes n jobs and runs each of them once concurrently.<p/>
     *
     * Will wait up to 10 seconds for all of the jobs to complete before aborting the run.
     */
    public static void runConcurrentlyAndWaitFor( String name, Runnable...jobs ) throws MultipleFailureException {
        runConcurrentlyAndWaitFor( name, 10, jobs );
    }

    /**
     * Invokes n jobs concurrently and waits for them to all complete.  Very similar in principle to
     * JUnitMosaic.multiThreaded() however where function takes a single job and invokes it n times
     * concurrently this function takes n jobs and runs each of them once concurrently.
     *
     * @param maxDurationSeconds will wait up to this many seconds before aborting the run
     */
    public static void runConcurrentlyAndWaitFor( String name, int maxDurationSeconds, Runnable...jobs ) throws MultipleFailureException {
        final CountDownLatch  startingLineLatch  = new CountDownLatch( jobs.length );
        final CountDownLatch  finishingLineLatch = new CountDownLatch( jobs.length );
        final List<Throwable> errors             = new Vector<>();


        for ( int i=0; i<jobs.length; i++ ) {
            final Runnable job = jobs[i];
            new Thread(name + i) {
                public void run() {
                    startingLineLatch.countDown();  // wait for all job threads to reach the starting line

                    try {
                        job.run();
                    } catch ( Throwable e ) {
                        errors.add( e );
                    } finally {
                        finishingLineLatch.countDown();
                    }
                }
            }.start();
        }

        try {
            finishingLineLatch.await( maxDurationSeconds, TimeUnit.SECONDS );

            if ( !errors.isEmpty() ) {
                // TODO suggest to JUnit that MultipleFailureException becomes an unchecked exception
                // TODO suggest to JUnit that MultipleFailureException.assertEmpty be changed to not throw Throwable
                // TODO check the latest version of JUnit ;)

                // NB we must throw this exception without wrapping to a typed exception because
                // the JUnit infrastructure performs instanceof checks that will detect this exception
                // and pull it apart for reporting purposes.. so for now, all clients must suffer
                // the typed exception by passing them on without catching them.
                throw new MultipleFailureException(errors);
            }
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Assert that the result of invoking fetcher is null.  Fetcher may block, so it is invoked
     * from a separate thread and given a few seconds to generate a result.  Failure to return in
     * time will cause the assertion to fail.
     */
    public static <T> void assertPotentiallyBlockingCallNull( Function0<T> fetcher ) {
        assertPotentiallyBlockingCallEquals( null, fetcher );
    }

    /**
     * Assert that the result of invoking fetcher is null.  Fetcher may block, so it is invoked
     * from a separate thread and given a few seconds to generate a result.  Failure to return in
     * time will cause the assertion to fail.
     */
    public static <T> void assertPotentiallyBlockingCallEquals( String expected, final Function0<T> fetcher ) {
        final AtomicReference<T> value        = new AtomicReference<>();
        final AtomicBoolean      hasThreadRun = new AtomicBoolean( false );

        Thread t = new Thread() {
            public void run() {
                T v = fetcher.invoke();

                value.set(v);
                hasThreadRun.set(true);
            }
        };
        t.setDaemon( true );
        t.start();

        JUnitMosaic.spinUntilTrue( new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return hasThreadRun.get();
            }
        } );

        assertEquals( expected, value.get() );
    }

    /**
     * Asserts that invoking the supplied method will block the calling thread.  Performs the test
     * from a new thread so as to not stall the test.
     */
    public static <T> void assertFunctionBlocksTheCallingThread( final Function0<T> blockingFunction ) {
        final AtomicBoolean hasThreadStarted = new AtomicBoolean( false );
        final AtomicBoolean isThreadRunning = new AtomicBoolean( false );

        Thread t = new Thread() {
            public void run() {
                isThreadRunning.set( true );
                hasThreadStarted.set( true );

                try {
                    blockingFunction.invoke();
                } finally {
                    isThreadRunning.set( false );
                }
            }
        };

        t.setDaemon( true );
        t.start();

        try {
            JUnitMosaic.spinUntilTrue( new Callable<Boolean>() {
                public Boolean call() throws Exception {
                    return isThreadRunning.get();
                }
            } );

            Backdoor.sleep( 100 );

            assertTrue( isThreadRunning.get() );
        } finally {
            t.interrupt();  // try to wake the thread up so that it can be shutdown
        }
    }



    private static String joinParagraph( List<String> paragraph ) {
        StringBuilder buf = new StringBuilder();

        boolean separatorFlag = false;
        for ( String s : paragraph ) {
            if ( separatorFlag ) {
                buf.append("\n");
            } else {
                separatorFlag = true;
            }

            buf.append(s);
        }

        return buf.toString();
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
