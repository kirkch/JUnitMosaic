# JUnit JUnitExt


JUnitExt adds several extensions to JUnit.

 - adds support for parameters to JUnit test methods
 - detection of memory leaks
 - detection of threads that are started during a test but not shut down
 - support for quick and dirty micro benchmarks




*Project Status:  Exploratory.  The basics work, feedback to help improve the project welcome.*

For now this project is not in a Maven repository; to use checkout the project
and compile using maven.


## QuickCheck integration

[QuickCheck](https://java.net/projects/quickcheck/pages/Home) is a combinator generator
library.  In combination with JUnit as a test harness, QuickCheck allows the
reuse of declared boundary conditions for method inputs, reducing the amount of
test code that has to be written to cover the same number of tests; while also increasing
readability.  A rare productivity win/win.


### QuickCheck Runner

The following example is runnable from any JUnit environment, and it runs the
marked test 10 times.  The arguments a and b are created afresh with different
values each run, from the generators specified in the @Test annotation.


Example:

    public class Stack {

        private Object[] stack  = new Object[10];
        private int      offset = 0;


        public void push( Object o ) {
            stack[offset++] = o;
        }

        public Object pop() {
            return stack[--offset];
        }

        public int size() {
            return offset;
        }

    }

Traditional JUnit Test:

    public class StackTests {

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

        // etc etc etc
    }

The traditional JUnit tests still have value as they make the testing of key
scenarios explicit, however there are a lot of scenarios if one is to cover all
edge cases.  Repeating a test to scan a wide range of edge cases can be automated.
Consider the following code which will fail when run.  It will detect that
the stack cannot cope with more than ten items.


    private final Generator arraySizeGenerator = PrimitiveGenerators.integers(1,100);
    private final Generator stringsGenerator   = CombinedGenerators.arrays(PrimitiveGenerators.strings(), arraySizeGenerator, String.class);


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
Java uses a Generational Garbage Collector, which means that a different
algorithm with different performance characteristics is used depending on the
age of the java objects.  The Stack example above does not clear out references
that it is holding, which means it can lead at worst to a memory leak or more
subtly the JVM could use the slower GC algorithm in cases that could have
avoided it.

The following unit test will detect this memory leak.


    @Test(memCheck=true, generators={"stringsGenerator"})
    public void verifyAssumeBehaviour( String[] values ) {
        Assume.assumeTrue( values.length < 10 );

        pushAll(values);

        popAndAssertAllValues(values);
    }


MemCheck only works for values passed in to the unit test as method parameters. After
the test has completed the unit test framework checks to see if the values can be
garbage collected, and errors if they cannot be.



## Micro Benchmarks

When a method is annotated with @Benchmark, JUnitExt will ensure that this is the
only method to be run at that time and will time it over many runs; throwing
away the initial runs to warm up the optimisers and running the Garbage Collector
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

