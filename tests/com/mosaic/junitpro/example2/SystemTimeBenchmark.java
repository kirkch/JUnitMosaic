package com.mosaic.junitpro.example2;

import com.mosaic.junitpro.Benchmark;
import com.mosaic.junitpro.JUnitExt;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(JUnitExt.class)
public class SystemTimeBenchmark {

    @Benchmark( units="call to nanoTime()" )
    public void nanoTimeBenchmark() {
        System.nanoTime();
    }

    @Benchmark( units="call to currentTimeMillis()" )
    public void currentTimeMillisBenchmark() {
        System.currentTimeMillis();
    }

    /**
     * Optionally the iteration count may be passed in as a parameter, in which case
     * the framework will call the method once per batch run and let the method
     * handle its own iteration. This avoids the overhead incurred by using reflection.
     *
     * Also note that this example returns a value calculated from each run of the loop;
     * this helps to prevent the optimizer from spotting that this method does no work
     * and thus optimising the contents away.
     */
    @Benchmark
    public long nanoSecondBenchmark_avoidingReflectionOverhead( int numIterations) {
        long sum = 0;

        for ( int i=0; i<numIterations; i++ ) {
            sum += System.nanoTime();
        }

        return sum;
    }

}
