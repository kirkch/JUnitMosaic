package com.mosaic.junitpro;

import com.mosaic.junitpro.lang.Predicate;
import com.mosaic.junitpro.tools.AssertJob;
import com.mosaic.junitpro.tools.ConcurrentAsserter;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;

import java.util.List;

/**
 *
 */
public class Assert extends org.junit.Assert {


    public static void assertEventually( Predicate predicate ) {
        assertEventually( 3000, predicate );
    }

    public static void assertEventually( long timeoutMillis, Predicate predicate ) {
        long startMillis = System.currentTimeMillis();

        while ( !predicate.eval() ) {
            long durationMillis = System.currentTimeMillis() - startMillis;

            if ( durationMillis > timeoutMillis ) {
                throw new IllegalStateException( "Timeout - " +predicate.reasonForFailure() );
            }

            Thread.yield();
        }
    }


    /**
     * Spin up n threads that invoke the step() method of the supplied instance of AssertJob.  This
     * method will not return until all of the threads have completed calling 'step()' a random number
     * of times.<p/>
     *
     * Each thread will invoke the 'step()' method by passing in the 'state' of the thread on each call.
     * The state is always null on the first call, and from then on will be the value returned from
     * the last call to 'step()' from that thread.<p/>
     *
     * The AssertJob itself must be immutable.
     *
     *
     * @param concurrentJob the concurrent work to be carried out from multiple threads
     *
     * @return a list containing the result of each threads final call to 'step()'
     */
    public static <T> List<T> multiThreadedAssert( AssertJob<T> concurrentJob ) {
        int                numThreads                 = Runtime.getRuntime().availableProcessors() * 4;
        Generator<Integer> numStepsPerThreadGenerator = PrimitiveGenerators.integers(0, 100);

        return multiThreadedAssert( numThreads, concurrentJob, numStepsPerThreadGenerator );
    }

    /**
     * Spin up n threads that invoke the step() method of the supplied instance of AssertJob.  This
     * method will not return until all of the threads have completed calling 'step()' a random number
     * of times.<p/>
     *
     * Each thread will invoke the 'step()' method by passing in the 'state' of the thread on each call.
     * The state is always null on the first call, and from then on will be the value returned from
     * the last call to 'step()' from that thread.<p/>
     *
     * The AssertJob itself must be immutable.
     *
     *
     * @param concurrentJob the concurrent work to be carried out from multiple threads
     *
     * @return a list containing the result of each threads final call to 'step()'
     */
    public static <T> List<T> multiThreadedAssert( int numThreads, AssertJob<T> concurrentJob, Generator<Integer> numStepsPerThreadGenerator ) {
        ConcurrentAsserter worker = new ConcurrentAsserter( numThreads, concurrentJob, numStepsPerThreadGenerator );

        return worker.runToCompletion();
    }

}
