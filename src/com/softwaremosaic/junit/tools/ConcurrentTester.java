package com.softwaremosaic.junit.tools;

import com.softwaremosaic.junit.JUnitMosaic;
import com.softwaremosaic.junit.lang.TakesIntFunction;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Spin up n threads that each invoke the same supplied job.  The return value of each job is
 * then collected and returned together.<p/>
 *
 * This class is usually used from 'JUnitMosaic.multiThreaded()'.
 */
@SuppressWarnings("unchecked")
public class ConcurrentTester<T> {

    private String              name;
    private int                 numThreads = Runtime.getRuntime().availableProcessors() * 4;
    private TakesIntFunction<T> concurrentJob;

    private T[]                 allResults;
    private List<Throwable>     failedJobs = new Vector();

    private Generator<Integer>  numIterationsPerThreadGenerator = PrimitiveGenerators.integers( 0, 1000 );


    public ConcurrentTester( String name, TakesIntFunction concurrentJob ) {
        this.name                       = name;
        this.concurrentJob              = concurrentJob;

        allResults = (T[]) new Object[numThreads];
    }

    public ConcurrentTester<T> withIterationCountGenerator( Generator<Integer> generator ) {
        this.numIterationsPerThreadGenerator = generator;

        return this;
    }

    public ConcurrentTester<T> withNumThreads( int numThreads ) {
        this.numThreads = numThreads;

        return this;
    }


    public List<T> runToCompletion() {
        CountDownLatch latch = new CountDownLatch(numThreads);


        startWorkerThreads( latch );
        waitForWorkerThreads( latch );

        throwIfAnyThreadErrored();

        return collectResults();
    }

    private void throwIfAnyThreadErrored() {
        if ( !failedJobs.isEmpty() ) {
            throw new RuntimeException( "Concurrent task threw an exception in "+failedJobs.size()+" threads.  Here is the stack trace of the first recorded exception.", failedJobs.get(0) );
        }
    }

    private void startWorkerThreads( final CountDownLatch completionLatch ) {
        // Starting the threads can take a significant amount of time.. to maximise the concurrent
        // overlap of the threads we use a latch to get all of the threads to the same starting
        // line and then release them all together.
        CountDownLatch startupLatch = new CountDownLatch( numThreads );

        for ( int i=0; i<numThreads; i++ ) {
            startWorkerThread( i, startupLatch, completionLatch );
        }
    }


    private void startWorkerThread( final int workerNumber, final CountDownLatch startupLatch, final CountDownLatch completionLatch ) {
        final int numIterationsRequested = numIterationsPerThreadGenerator.next();

        new Thread(name+workerNumber) {
            public void run() {
                startupLatch.countDown();

                try {
                    T result = concurrentJob.invoke(numIterationsRequested);

                    allResults[workerNumber] = result;
                } catch ( Throwable e ) {
                    failedJobs.add(e);
                } finally {
                    completionLatch.countDown();
                }
            }
        }.start();
    }

    private void waitForWorkerThreads( CountDownLatch latch ) {
        try {
            boolean wasSuccessfulFlag = latch.await( 60, TimeUnit.SECONDS );

            if ( !wasSuccessfulFlag ) {
                JUnitMosaic.fail("Concurrent test failed, some of the worker threads did not complete");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<T> collectResults() {
        List<T> allResults = new ArrayList(numThreads);

        for ( T singleThreadsResults : this.allResults) {
            allResults.add( singleThreadsResults );
        }

        return allResults;
    }

}
