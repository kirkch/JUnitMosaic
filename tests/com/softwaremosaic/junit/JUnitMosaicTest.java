package com.softwaremosaic.junit;

import com.softwaremosaic.junit.annotations.Test;
import com.softwaremosaic.junit.lang.Predicate;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(JUnitMosaicRunner.class)
public class JUnitMosaicTest {

    @Test
    public void demoAssertEventually_expectPassesAfter100ms() {
        final long targetMillis = System.currentTimeMillis() + 100;


        JUnitMosaic.assertEventually(200, new Predicate() {
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
            JUnitMosaic.assertEventually(10, new Predicate() {
                public boolean eval() {
                    return System.currentTimeMillis() > targetMillis;
                }

                public String reasonForFailure() {
                    return "targetMillis not reached within the time limit";
                }
            });

            JUnitMosaic.fail("expected IllegalStateException");
        } catch ( IllegalStateException e ) {

        }
    }

}
