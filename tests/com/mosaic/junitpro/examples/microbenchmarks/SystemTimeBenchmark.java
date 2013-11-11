package com.mosaic.junitpro.examples.microbenchmarks;

import com.mosaic.junitpro.Benchmark;
import com.mosaic.junitpro.JUnitExt;
import org.junit.runner.RunWith;

/**
 * Provides multiple examples of how to write a benchmark with this tool.
 */
@RunWith(JUnitExt.class)
public class SystemTimeBenchmark {

    /**
     * This method will be invoked 6,000,000 times.  Reporting the mean elapsed time per method call six times, once
     * per million calls.
     */
    @Benchmark( units="call to nanoTime()" )
    public void nanoTimeBenchmark() {
        System.nanoTime();
    }

    /**
     * Optionally the iteration count may be passed in as a parameter, in which case
     * the framework will call the method once per batch run and let the method
     * handle its own iteration. This avoids the overhead incurred by using reflection.
     */
    @Benchmark
    public void nanoSecondBenchmark_avoidingReflectionOverhead( int numIterations) {
        for ( int i=0; i<numIterations; i++ ) {
            System.nanoTime();
        }
    }

    /**
     * The Java Runtime is very good at optimising methods.  If it can prove that a method has not effect, then it
     * will eliminate it.  To help avoid the removal of the function call that we are trying to make, have the method
     * perform some work and return it.
     */
    @Benchmark
    public long nanoSecondBenchmark_avoidingDeadCodeRemoval( int numIterations) {
        long sum = 0;

        for ( int i=0; i<numIterations; i++ ) {
            sum += System.nanoTime();
        }

        return sum;
    }

}
