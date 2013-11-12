package com.mosaic.junitpro.tools;

import com.mosaic.junitpro.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ConcurrentAsserter {

    private int          numThreads;
    private AssertionJob cloneableJob;

    private List<AssertionJob> allJobs = new ArrayList<AssertionJob>();


    public ConcurrentAsserter( int numThreads, AssertionJob cloneableJob ) {
        this.numThreads   = numThreads;
        this.cloneableJob = cloneableJob;
    }


    public void run() {
        CountDownLatch latch = new CountDownLatch(numThreads);


        startWorkerThreads( latch );
        waitForWorkerThreads( latch );

        assertResults();
    }

    private void startWorkerThreads( final CountDownLatch latch ) {
        for ( int i=0; i<numThreads; i++ ) {
            startWorkerThread(latch);
        }
    }


    private void startWorkerThread(final CountDownLatch latch) {
        final AssertionJob job = cloneableJob.clone();

        allJobs.add( job );


        new Thread() {
            public void run() {
                try {
                    while ( !job.isComplete() ) {
                        Thread.yield();

                        job.invoke();
                    }
                } catch ( Throwable e ) {
                    job.failed( e );
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

    private void assertResults() {
        AssertionJob mergedJob = mergeAllJobs();

        if ( mergedJob.didFail() ) {
            throw new RuntimeException( mergedJob.getException() );
        }

        mergedJob.performAsserts();
    }

    private AssertionJob mergeAllJobs() {
        AssertionJob mergedJob = cloneableJob.clone();

        for ( AssertionJob j : allJobs ) {
            mergedJob = mergedJob.merge(j);
        }

        return mergedJob;
    }

}
