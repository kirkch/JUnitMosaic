package com.softwaremosaic.junit.examples.memoryleaks;

import com.softwaremosaic.junit.annotations.Test;
import com.softwaremosaic.junit.examples.BuggyStack;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.CombinedGenerators;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.Assume;

import static org.junit.Assert.assertEquals;

/**
 * Examples of asking the framework to track the GC of objects passed into the
 * test case.
 */
@SuppressWarnings("ALL")
public class StackMemoryLeakTests {

    private final Generator<Integer>  arraySizeGenerator = PrimitiveGenerators.integers(1, 100);
    private final Generator<String[]> stringsGenerator   = CombinedGenerators.arrays(PrimitiveGenerators.strings(), arraySizeGenerator, String.class);


    private BuggyStack stack = new BuggyStack();



    /*
     * This test will error if any of the objects passed in to the test method are not GC'able within a fixed time limit
     * after the test has completed.
     */
    @Test(memCheck=true, generators={"stringsGenerator"})
    public void detectMemoryLeaks( String[] values ) {
        Assume.assumeTrue(values.length < 10);

        pushAll(values);
        popAndAssertAllValues(values);  // NB comment this line to cause this test to fail as the values past in will not become GC'able
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
