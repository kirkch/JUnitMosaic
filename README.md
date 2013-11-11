# JUnit JUnitExt


JUnitExt adds several extensions to JUnit.

 - adds support for method parameters to JUnit test methods
 - detection of memory leaks
 - detection of threads that are started during a test but not shut down
 - support for micro benchmarks




*Project Status:  Exploratory.  The basics work, feedback to help improve the project welcome.*

For now this project is not in a Maven repository; to use checkout the project
and compile using maven.



## QuickCheck integration

[QuickCheck](https://java.net/projects/quickcheck/pages/Home) is used to generate random values
on demand.  This supports schotastic testing of a rule that should always hold no matter what the
input is.  Especially useful for testing a range of boundary conditions quickly, and with less code.


### QuickCheck Runner

The following example is runnable from any JUnit environment, and it runs the
marked test 10 times.  The arguments a and b are created afresh with different
values each run, from the generators specified in the @Test annotation.


Example:

@RunWith(JUnitExt.class)
public class StackTest {
    private final Generator arraySizeGenerator = PrimitiveGenerators.integers(1,100);
    private final Generator stringsGenerator   = CombinedGenerators.arrays(PrimitiveGenerators.strings(), arraySizeGenerator, String.class);

    private Stack stack = new Stack();


    @Test(generators={"stringsGenerator"})
    public void verifyAssumeBehaviour( String[] values ) {
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

For situations where the generators create a scenario that is invalid then
either new generators can be created that avoid it or JUnit's assumeThat mechanism
can be used to skip that test.  The following example claims that the stack
will never need to store more than 10 values and so this test will pass.


    @Test(generators={"stringsGenerator"})
    public void verifyAssumeBehaviour( String[] values ) {
        Assume.assumeTrue( values.length < 10 );

        pushAll(values);

        popAndAssertAllValues(values);
    }



## Mem Check

Memory leaks can occur in Java, even with the existence of a Garbage Collector.
A Stack that did not clear out references to objects popped from it could cause significantly slow
the Garbage Collector as objects that should have been collected become tenured instead.

The following unit test will detect this memory leak.


    @Test(memCheck=true, generators={"stringsGenerator"})
    public void verifyAssumeBehaviour( String[] values ) {
        pushAll(values);

        popAndAssertAllValues(values);
    }


MemCheck only works for values passed in to the unit test as method parameters. After
the test has completed the unit test framework checks to see if the values can be
garbage collected, and errors if they cannot be.



## Micro Benchmarks

When a method is annotated with @Benchmark, JUnitExt will ensure that this is the
only method to be run at that time and will time it over many runs; throwing
away the initial runs to warm up the runtime optimisers and running the Garbage Collector
between each run.  The results of each run can then be stored over time and
compared with previous runs to visualise trends.



    @RunWith(JUnitExt.class)
    public class SystemTimeBenchmark {

        @Benchmark
        public void nanoTimeBenchmark() {
            System.nanoTime();
        }

        @Benchmark( durationResultMultiplier=1.0/2 )  // divide result by two because we invoke the target method twice per run
        public void currentTimeMillisBenchmark() {
            System.currentTimeMillis();
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
        @Benchmark( batchCount=10, units="call to nanoTime()" )
        public long nanoSecondBenchmark_avoidingReflectionOverhead( int numIterations) {
            long sum = 0;

            for ( int i=0; i<numIterations; i++ ) {
                sum += System.nanoTime();
            }

            return sum;
        }

    }

The results of the run are currently printed to the console, which is useful for point
comparisons during the development cycle.


    Benchmark results for:
    Invoking SystemTimeBenchmark.nanoTimeBenchmark (batchCount=6, iterationCount=1000000, timingMultipler=1.0)

        47.01ns per call to nanoTime()
        49.94ns per call to nanoTime()
        48.08ns per call to nanoTime()
        47.57ns per call to nanoTime()
        50.44ns per call to nanoTime()
        47.91ns per call to nanoTime()

One timing is printed per batch of calls. Each batch consists of iterationCount number
of iterations.  The time printed is thus:  (durationOfCallingMethodIterationCountTimes/iterationCount)*timingMultipler.
Timing multiplier is used to adjust the time displayed to match the units that you expect the result to be in. For example,
if during your test you read in 100 lines of text then you may multiply the result by 1.0/100 to get the average duration
per line of text processed.

