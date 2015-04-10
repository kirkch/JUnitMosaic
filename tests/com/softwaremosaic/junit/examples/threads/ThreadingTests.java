package com.softwaremosaic.junit.examples.threads;

import com.softwaremosaic.junit.JUnitMosaic;
import com.softwaremosaic.junit.JUnitMosaicRunner;
import com.softwaremosaic.junit.annotations.Test;
import org.junit.Assert;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Example of asking the framework to detect whether any threads started after the
 * test completes within a small time period after the test has completed.
 */
@RunWith(JUnitMosaicRunner.class)
public class ThreadingTests {


    /*
     * JUnitMosaic can make a list of threads that were running before the test started, and after.  Reporting errors if
     * any threads that were started during the test run do not finish within a timely manner.
     */
    @Test( threadCheck=true )
    public void threadCheck_expectPassAsThreadWillTerminateShortly() {
        final AtomicBoolean keepRunning = new AtomicBoolean(true);

        new Thread() {
            @Override
            public void run() {
                this.setName("threadCheck_expectPassAsThreadWillTerminateShortly");

                while (keepRunning.get()) {}
            }
        }.start();

        keepRunning.lazySet(false);   // NB comment this line out to prevent the thread from finishing, and to see the test fail
    }


    /**
     * Run n jobs together.
     */
    @Test( threadCheck=true )
    public void runConcurrentlyAndWaitFor() throws MultipleFailureException {
        final List<String> jobThreadNames = new Vector<>();

        Runnable job1 = new Runnable() {
            public void run() {
                jobThreadNames.add( Thread.currentThread().getName() );
            }
        };

        Runnable job2 = new Runnable() {
            public void run() {
                jobThreadNames.add( Thread.currentThread().getName() );
            }
        };

        JUnitMosaic.runConcurrentlyAndWaitFor( job1, job2 );

        Collections.sort( jobThreadNames );

        List<String> expected = Arrays.asList( "ThreadingTests.runConcurrentlyAndWaitFor0", "ThreadingTests.runConcurrentlyAndWaitFor1" );
        Assert.assertEquals( expected, jobThreadNames );
    }

}
