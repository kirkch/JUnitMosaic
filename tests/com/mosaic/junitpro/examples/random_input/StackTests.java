package com.mosaic.junitpro.examples.random_input;


import com.mosaic.junitpro.JUnitExt;
import com.mosaic.junitpro.annotations.Test;
import com.mosaic.junitpro.examples.BuggyStack;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.CombinedGenerators;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.Assume;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


/**
 * An example that is unit testing a buggy implementation of a stack.
 */
@RunWith(JUnitExt.class)
@SuppressWarnings("ALL")
public class StackTests {

    private final Generator arraySizeGenerator = PrimitiveGenerators.integers(1,100);
    private final Generator stringsGenerator   = CombinedGenerators.arrays(PrimitiveGenerators.strings(), arraySizeGenerator, String.class);


    private BuggyStack stack = new BuggyStack();


    /**
     * Example that @Test works as the standard JUnit annotation does.
     */
    @Test
    public void pushPop() {
        assertEquals( 0, stack.size() );

        stack.push("foo");
        assertEquals( 1, stack.size() );

        assertEquals( "foo", stack.pop() );
        assertEquals( 0, stack.size() );
    }


    @Test(generators={"stringsGenerator"})
    public void verifyAssumeBehaviour( String[] values ) {
        Assume.assumeTrue( values.length < 10 );   // NB comment out this line to discover that BuggyStack does not resize itself internally

        pushAll(values);

        popAndAssertAllValues(values);
    }



    private void pushAll(String[] values) {
        for ( String v : values ) {
            stack.push(v);
        }
    }

    private void popAndAssertAllValues(String[] values) {
        assertEquals( values.length, stack.size() );

        for ( int i=values.length-1; i>=0; i-- ) {
            assertEquals( values[i], stack.pop() );
        }

        assertEquals( 0, stack.size() );
    }

}

