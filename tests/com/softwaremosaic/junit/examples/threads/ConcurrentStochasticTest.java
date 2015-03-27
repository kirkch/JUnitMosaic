package com.softwaremosaic.junit.examples.threads;

import com.softwaremosaic.junit.JUnitMosaic;
import com.softwaremosaic.junit.JUnitMosaicRunner;
import com.softwaremosaic.junit.annotations.Test;
import com.softwaremosaic.junit.lang.TakesIntFunction;
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
 * An example testing a concurrent data structure by pushing and popping
 * random values from multiple threads.
 */
@RunWith(JUnitMosaicRunner.class)
@SuppressWarnings("ALL")
public class ConcurrentStochasticTest {

    private static final Generator<String > RND_STRING = PrimitiveGenerators.strings();


    @Test
    public void concurrentPushPopTest() {
        final List<String> stack = Collections.synchronizedList(new ArrayList<String>());

        List<List<String>> itemsPushedByEachThread = JUnitMosaic.multiThreaded( new TakesIntFunction<List<String>>() {
            public List<String> invoke( int numIterations ) {
                List<String> allGeneratedValues = new ArrayList<String>();

                for ( int i=0; i<numIterations; i++ ) {
                    String v = RND_STRING.next();

                    stack.add( v );
                    allGeneratedValues.add( v );
                }

                return allGeneratedValues;
            }
        } );


        verifyStack( stack, itemsPushedByEachThread );
    }

    @Test
    public void concurrentPushPopTest_expectToDetectThatStackIsNotThreadSafe() {
        final List<String> stack = new ArrayList<String>();

        try {
            List<List<String>> itemsPushedByEachThread = JUnitMosaic.multiThreaded( new TakesIntFunction<List<String>>() {
                public List<String> invoke( int numIterations ) {
                    List<String> allGeneratedValues = new ArrayList<String>();

                    for ( int i = 0; i < numIterations; i++ ) {
                        String v = RND_STRING.next();

                        stack.add( v );
                        allGeneratedValues.add( v );
                    }

                    return allGeneratedValues;
                }
            } );



            verifyStack( stack, itemsPushedByEachThread );

            fail( "expected there to be a problem" );
        } catch ( Throwable e ) {
            // expected
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


    private <T> List<T> flatten( List<List<T>> perThreadResults ) {
        List<T> all = new ArrayList<T>();

        for ( List<T> resultsFromOneThread : perThreadResults ) {
            if ( resultsFromOneThread != null ) {
                all.addAll( resultsFromOneThread );
            }
        }

        return all;
    }

}
