package com.mosaic.hammer.example1;


import com.mosaic.hammer.junit.Benchmark;
import com.mosaic.hammer.junit.Hammer;
import com.mosaic.hammer.junit.Test;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.Generators;
import net.java.quickcheck.generator.PrimitiveGenerators;
import net.java.quickcheck.generator.distribution.Distribution;
import org.junit.Assume;
import org.junit.runner.RunWith;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Hammer.class)
public class CalculatorTests1 {
    private final Generator intGenerator = PrimitiveGenerators.integers();


    private Calculator calc = new Calculator();

    /**
     * Uncomment this test to demonstrate what happens when there is an error.
     */
//    @com.mosaic.hammer.junit.Test(generators={"intGenerator","intGenerator"})
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

