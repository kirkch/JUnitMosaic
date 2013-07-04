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
import static org.junit.Assert.fail;

@RunWith(Hammer.class)
public class CalculatorTests1 {
    private final Generator intGenerator = PrimitiveGenerators.integers(100, 2000, Distribution.POSITIV_NORMAL);


    private Calculator calc = new Calculator();

    @com.mosaic.hammer.junit.Test //(IntGenerator,IntGenerator)
    public void sumTwoIntegers() {
        sumTwoIntegersQC(10, 10);
    }

    @org.junit.Test //(IntGenerator,IntGenerator)
    public void junitTest() {
        sumTwoIntegersQC(10, 10);
    }

    @com.mosaic.hammer.junit.Test(generators={"intGenerator","intGenerator"})
    public void sumTwoIntegersQC( int a, int b) {
        int r = calc.sum(a,b);

        assertEquals( a + b, r );
        assertEquals( a, calc.subtract(r,b) );
        assertEquals(b, calc.subtract(r, a));
    }

    @com.mosaic.hammer.junit.Test(generators={"intGenerator","intGenerator"})
    public void verifyAssumeBehaviour( int a, int b) {
//        System.out.println( a+","+b + "->"+(a>b) );

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

//    @Benchmark(value=5, batchCount = 3)
//    public long readFile() throws IOException {
//        long sum = 0;
//
//        InputStream in = new FileInputStream("/Users/ck/t/wordlist.txt");
//        try {
//            int v = in.read();
//            while ( v >= 0 ) {
//                sum += v;
//                v = in.read();
//            }
//        } finally {
//            in.close();
//        }
//
//        return sum;
//    }
//
//    @Benchmark(value=5, batchCount = 3)
//    public long readFileBatched() throws IOException {
//        long sum = 0;
//        byte[] batch = new byte[4096];
//        InputStream in = new FileInputStream("/Users/ck/t/wordlist.txt");
//        try {
//            int numBytes = in.read(batch);
//            while ( numBytes >= 0 ) {
//                sum += numBytes;
//                numBytes = in.read(batch);
//            }
//        } finally {
//            in.close();
//        }
//
//        return sum;
//    }
//
//    @Benchmark(value=5, batchCount = 3)
//    public long readFileBatchedAndBuffered() throws IOException {
//        long sum = 0;
//        byte[] batch = new byte[4096];
//        InputStream in = new BufferedInputStream(new FileInputStream("/Users/ck/t/wordlist.txt"));
//        try {
//            int numBytes = in.read(batch);
//            while ( numBytes >= 0 ) {
//                sum += numBytes;
//                numBytes = in.read(batch);
//            }
//        } finally {
//            in.close();
//        }
//
//        return sum;
//    }
//
//    @Benchmark(value=5, batchCount = 3)
//    public long readFileBuffered() throws IOException {
//        long sum = 0;
//
//        InputStream in = new BufferedInputStream(new FileInputStream("/Users/ck/t/wordlist.txt"));
//        try {
//            int v = in.read();
//            while ( v >= 0 ) {
//                sum += v;
//                v = in.read();
//            }
//        } finally {
//            in.close();
//        }
//
//        return sum;
//    }

    @Benchmark
    public void nanoTimeBenchmark() {
        System.nanoTime();
    }

    @Benchmark
    public void currentTimeMillisBenchmark() {
        System.currentTimeMillis();
    }

}

