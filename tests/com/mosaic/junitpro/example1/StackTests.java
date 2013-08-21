package com.mosaic.junitpro.example1;


import com.mosaic.junitpro.Benchmark;
import com.mosaic.junitpro.JUnitExt;
import com.mosaic.junitpro.Test;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.CombinedGenerators;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.Assume;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


@RunWith(JUnitExt.class)
@SuppressWarnings("ALL")
public class StackTests {

    private final Generator arraySizeGenerator = PrimitiveGenerators.integers(1,100);
    private final Generator stringsGenerator   = CombinedGenerators.arrays(PrimitiveGenerators.strings(), arraySizeGenerator, String.class);


    private Stack stack = new Stack();


    @Test
    public void pushPop() {
        assertEquals( 0, stack.size() );

        stack.push("foo");
        assertEquals( 1, stack.size() );

        assertEquals( "foo", stack.pop() );
        assertEquals( 0, stack.size() );
    }

    @Test
    public void pushPushPopPop() {
        stack.push("foo");
        stack.push("bar");

        assertEquals( 2, stack.size() );

        assertEquals( "bar", stack.pop() );
        assertEquals( "foo", stack.pop() );

        assertEquals( 0, stack.size() );
    }

    /*
     * Uncomment this test to demonstrate that the stack does not resize.
     */
//    @com.mosaic.junitpro.Test(generators={"stringsGenerator"})
    public void sumTwoIntegersFails( String[] values ) {
        pushAll(values);

        popAndAssertAllValues(values);
    }

    @Test(generators={"stringsGenerator"})
    public void verifyAssumeBehaviour( String[] values ) {
        Assume.assumeTrue( values.length < 10 );

        pushAll(values);

        popAndAssertAllValues(values);
    }

    /*
     * Uncomment this annotation to have JUnit detect that the stack holds on
     * to popped items.
     */
//    @Test(memCheck=true, generators={"stringsGenerator"})
    public void detectMemoryLeaks( String[] values ) {
        Assume.assumeTrue( values.length < 10 );

        pushAll(values);
    }

    @Benchmark( durationResultMultiplier=1.0/3 )
    public int benchmark( int limit  ) {
        for ( int i=0; i<limit; i++ ) {
            stack.push("a");
            stack.push("b");
            stack.push("c");

            stack.pop();
            stack.pop();
            stack.pop();
        }

        return stack.size();
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

