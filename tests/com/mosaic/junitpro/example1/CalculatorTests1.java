package com.mosaic.junitpro.example1;


import com.mosaic.junitpro.Benchmark;
import com.mosaic.junitpro.JUnitExt;
import com.mosaic.junitpro.Test;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.Assume;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnitExt.class)
public class CalculatorTests1 {
    private final Generator intGenerator = PrimitiveGenerators.integers();


    private Calculator calc = new Calculator();

    /**
     * Uncomment this test to demonstrate what happens when there is an error.
     */
//    @com.mosaic.junitpro.Test(generators={"intGenerator","intGenerator"})
    public void sumTwoIntegersFails( int a, int b) {
        int r = calc.sum(a,b);

        assertEquals( a + b, r );
        assertEquals( a, calc.subtract(r,b) );
        assertEquals( b, calc.subtract(r,a) );
        assertTrue(r > a);
        assertTrue( r > b );
    }

    @Test
    public void sumTwoIntegers() {
        sumTwoIntegersQC(10, 10);
    }

    @org.junit.Test
    public void junitTest() {
        sumTwoIntegersQC(10, 10);
    }

    @Test(generators={"intGenerator","intGenerator"})
    public void sumTwoIntegersQC( int a, int b) {
        int r = calc.sum(a,b);

        assertEquals( a + b, r );
        assertEquals( a, calc.subtract(r,b) );
        assertEquals(b, calc.subtract(r, a));
    }

    @Test(generators={"intGenerator","intGenerator"})
    public void verifyAssumeBehaviour( int a, int b) {
        Assume.assumeTrue(a > b);

        if ( a < b ) {
            System.out.println( "  blah" );
            fail("blah");
        }
    }

    @Benchmark
    public void benchmark() {
        calc.sum(100,(int) System.nanoTime());
    }

}

