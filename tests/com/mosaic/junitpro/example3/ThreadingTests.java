package com.mosaic.junitpro.example3;

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


    @Test
    public void demoAssertEventually_expectPassesAfter100ms() {
        final long targetMillis = System.currentTimeMillis() + 100;


        Assert.assertEventually( 200, new Predicate() {
            public boolean eval() {
                return System.currentTimeMillis() > targetMillis;
            }

            public String reasonForFailure() {
                return "targetMillis not reached within the time limit";
            }
        });
    }

    @Test
    public void demoAssertEventually_expectFailureAfter10ms() {
        final long targetMillis = System.currentTimeMillis() + 100;


        try {
            Assert.assertEventually( 10, new Predicate() {
                public boolean eval() {
                    return System.currentTimeMillis() > targetMillis;
                }

                public String reasonForFailure() {
                    return "targetMillis not reached within the time limit";
                }
            });

            Assert.fail("expected IllegalStateException");
        } catch ( IllegalStateException e ) {

        }
    }


    /*
     * Uncomment the following test annotation to see the test fail because
     * the thread that it starts will not terminate.
     */
//    @Test( threadCheck=true )
    public void threadCheck_expectFailureAsThreadWillNotFinish() {
        new Thread() {
            @Override
            public void run() {
                this.setName("threadCheck_expectFailureAsThreadWillNotFinish");

                while (true) {

                }
            }
        }.start();
    }

    @Test( threadCheck=true )
    public void threadCheck_expectPassAsThreadWillTerminateShortly() {
        final AtomicBoolean keepRunning = new AtomicBoolean(true);
        new Thread() {
            @Override
            public void run() {
                this.setName("threadCheck_expectPassAsThreadWillTerminateShortly");

                while (keepRunning.get()) {

                }
            }
        }.start();

        keepRunning.lazySet(false);
    }

}
