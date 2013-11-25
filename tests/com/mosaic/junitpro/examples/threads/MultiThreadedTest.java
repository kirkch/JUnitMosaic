package com.mosaic.junitpro.examples.threads;

import com.mosaic.junitpro.Assert;
import com.mosaic.junitpro.JUnitExt;
import com.mosaic.junitpro.annotations.Test;
import com.mosaic.junitpro.tools.AssertJob;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 *
 */
@RunWith(JUnitExt.class)
@SuppressWarnings("ALL")
public class MultiThreadedTest {

    private static final Generator<String > RND_STRING = PrimitiveGenerators.strings();


    @Test
    public void concurrentPushPopTest() {
        final List<String> stack = Collections.synchronizedList(new ArrayList<String>());

        List<List<String>> perThreadResults = Assert.multiThreadedAssert(new AssertJob<List<String>>() {
            public List<String> step( List<String> expectedStateSoFar ) {
                if ( expectedStateSoFar == null ) {
                    expectedStateSoFar = new ArrayList<String>();
                }

                String v = RND_STRING.next();

                stack.add(v);
                expectedStateSoFar.add(v);

                return expectedStateSoFar;
            }
        });


        verifyStack( stack, perThreadResults );
    }

    @Test
    public void concurrentPushPopTest_expectToDetectThatStackIsNotThreadSafe() {
        final List<String> stack = new ArrayList<String>();

        try {
            List<List<String>> perThreadResults = Assert.multiThreadedAssert(new AssertJob<List<String>>() {
                public List<String> step( List<String> expectedStateSoFar ) {
                    if ( expectedStateSoFar == null ) {
                        expectedStateSoFar = new ArrayList<String>();
                    }

                    String v = RND_STRING.next();

                    stack.add(v);
                    expectedStateSoFar.add(v);

                    return expectedStateSoFar;
                }
            });



            verifyStack( stack, perThreadResults );

            fail( "expected there to be a problem" );
        } catch ( Throwable e ) {

        }
    }




    private void verifyStack( List<String> stack, List<List<String>> perThreadResults ) {
        List<String> allPushedItems = flatten( perThreadResults );

        while ( !stack.isEmpty() ) {
            String head = stack.remove(0);

            boolean wasRemoved = allPushedItems.remove(head);
            assertTrue( "stack is not thread safe; stack contained a value that was not reported to have been pushed: '" +head+"'", wasRemoved );
        }

        assertEquals( "stack is not thread safe; the stack has lost the following items: " + allPushedItems, 0, allPushedItems.size() );
    }

    private List<String> flatten(List<List<String>> perThreadResults) {
        List<String> all = new ArrayList<String>();

        for ( List<String> resultsFromOneThread : perThreadResults ) {
            if ( resultsFromOneThread != null ) {
                all.addAll( resultsFromOneThread );
            }
        }

        return all;
    }

}
