package com.mosaic.junitpro;

import com.mosaic.junitpro.annotations.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(JUnitExt.class)
public class AssertTest {

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

}
