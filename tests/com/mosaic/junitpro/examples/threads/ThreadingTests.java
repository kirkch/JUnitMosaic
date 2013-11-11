package com.mosaic.junitpro.examples.threads;

import com.mosaic.junitpro.Assert;
import com.mosaic.junitpro.JUnitExt;
import com.mosaic.junitpro.Predicate;
import com.mosaic.junitpro.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
@RunWith(JUnitExt.class)
public class ThreadingTests {


    /*
     * JUnitExt can make a list of threads that were running before the test started, and after.  Reporting errors if
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

}
