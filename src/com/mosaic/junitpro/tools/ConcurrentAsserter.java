package com.mosaic.junitpro.tools;

import com.mosaic.junitpro.Assert;
import net.java.quickcheck.Generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Spin up n threads that invoke the step() method of the supplied instance of AssertJob.  This
 * method will not return until all of the threads have completed calling 'step()' a random number
 * of times.<p/>
 *
 * Each thread will invoke the 'step()' method by passing in the 'state' of the thread on each call.
 * The state is always null on the first call, and from then on will be the value returned from
 * the last call to 'step()' from that thread.<p/>
 *
 * The AssertJob itself must be immutable.<p/>
 *
 * This class is usually used from 'Assert.multiThreadedAssert()'.
 */
@SuppressWarnings("unchecked")
public class ConcurrentAsserter<T> {

    private int             numThreads;
    private AssertJob<T>    concurrentJob;

    private List<T>         allResults;
    private List<Throwable> failedJobs = new Vector();

    private Generator<Integer> numStepsPerThreadGenerator;


    public ConcurrentAsserter( int numThreads, AssertJob concurrentJob, Generator<Integer> numStepsPerThreadGenerator ) {
        this.numThreads                 = numThreads;
        this.concurrentJob              = concurrentJob;
        this.numStepsPerThreadGenerator = numStepsPerThreadGenerator;

        allResults = new ArrayList<T>( numThreads );

        for ( int i=0; i<numThreads; i++ ) {
            allResults.add(null);
        }
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

    private void startWorkerThreads( final CountDownLatch latch ) {
        for ( int i=0; i<numThreads; i++ ) {
            startWorkerThread( i, latch );
        }
    }


    private void startWorkerThread( final int workerNumber, final CountDownLatch latch ) {
        final int numSteps = numStepsPerThreadGenerator.next();

        new Thread() {
            public void run() {
                try {
                    T stateSoFar = null;

                    for ( int i=0; i<numSteps; i++ ) {
//                        Thread.yield();

                        stateSoFar = concurrentJob.step(stateSoFar);
                    }

                    allResults.set( workerNumber, stateSoFar );
                } catch ( Throwable e ) {
                    failedJobs.add(e);
                } finally {
                    latch.countDown();
                }
            }
        }.start();
    }

    private void waitForWorkerThreads( CountDownLatch latch ) {
        try {
            boolean wasSuccessfulFlag = latch.await( 10, TimeUnit.SECONDS );

            if ( !wasSuccessfulFlag ) {
                Assert.fail("Concurrent test failed, some of the worker threads did not complete");
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
