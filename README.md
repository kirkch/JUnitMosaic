# JUnit Mosaic


JUnitMosaic adds several extensions to JUnit.

 - adds support for method parameters to JUnit test methods
 - detection of memory leaks
 - tools for testing concurrent code
 - support for micro benchmarks




*Project Status:  Nearing First Release*

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

    @RunWith(JUnitMosaicRunner.class)
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

### Random Number Seed

When an error occurs using QuickCheck, the random values that were generated will be printed to
stderr along with the seed that was used at the start of the test.  By default the seed will
be set to System.currentTimeMillis, however this will give different random parameters each time
the test is run.  To make the test repeatable, specify the seed via the test annoation @Test(seed=1234).


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

When a method is annotated with @Benchmark, JUnitMosaic will ensure that this is the
only method to be run at that time and will time it over many runs; throwing
away the initial runs to warm up the runtime optimisers and running the Garbage Collector
between each run.  The results of each run can then be stored over time and
compared with previous runs to visualise trends.



    @RunWith(JUnitMosaicRunner.class)
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
        public long nanoSecondBenchmark_avoidingReflectionOverhead( int maxNumIterations) {
            long sum = 0;

            for ( int i=0; i<maxNumIterations; i++ ) {
                sum += System.nanoTime();
            }

            return sum;
        }

    }

The results of the run are currently printed to the console, assuming that
you did *not* enable assertions:


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


## Testing Concurrent Code

### Spin Locks

Concurrent tests using JUnit often devolve into calls to Thread.sleep.  This has a few problems, most
notably that the tests become both slow and fragile.  Slow because the thread has gone to sleep, and
fragile due to race conditions where the sleep may not be long enough in some circumstances.  Which usually
results in the sleep time being increased, and the test getting slower until the test is run on another
machine (usually the build server) where it starts failing intermittently.  Joy.

For situations where the test will usually complete in a few milliseconds, and no callback exists then
a simple spin lock may be used to wait for the async event to occur.


    JUnitMosaic.assertEventually( new Predicate() {...} );
    JUnitMosaic.spinUntilTrue( new Callbable<Boolean>() {...} );

When waiting for an object to become GC'd, the following helper can be used:

    JUnitMosic.spinUntilReleased( weakRef );

The advantage of this approach is that the moment that the async condition becomes true, then the
test will move on.  This keeps the test fast and responsive.  It should also be noted that if
the condition never happens, then the test will time out (3 seconds by default, but can be specified).
3 seconds is usually plenty for small and fast unit tests; obviously integration and system tests
would require much longer.


# Run N jobs concurrently

JUnitMosaic.runConcurrentlyAndWaitFor is useful when one needs to run N tasks concurrently and
not continue until they have all completed.  The method takes N instances of runnable, starts them
all, waits for them to start as thread creation can take awhile and then unleashes them all at the
same time.  The method does not return until all of the tasks have returned.

The following example spins up two threads which each grab the name of the running thread
and stores it into a collection for assertion later on.


    @Test( threadCheck=true )
    public void runConcurrentlyAndWaitFor() throws MultipleFailureException {
        final List<String> jobThreadNames = new Vector<>();

        Runnable job1 = new Runnable() {
            public void run() {
                jobThreadNames.add( Thread.currentThread().getName() );
            }
        };

        Runnable job2 = new Runnable() {
            public void run() {
                jobThreadNames.add( Thread.currentThread().getName() );
            }
        };

        JUnitMosaic.runConcurrentlyAndWaitFor( job1, job2 );

        Collections.sort( jobThreadNames );

        List<String> expected = Arrays.asList( "ThreadingTests.runConcurrentlyAndWaitFor0", "ThreadingTests.runConcurrentlyAndWaitFor1" );
        Assert.assertEquals( expected, jobThreadNames );
    }



### Concurrent Stochastic Testing

JUnitMosaic.multiThreaded spins up n threads that invokes the same supplied function and
collects the results returned by each call.

The following example shows how a stack could be tested for thread safety.  Each thread adds n
items onto the stack, and then after each thread has completed the stack is inspected to make sure
that each of the items that was expected to be on the stack is in fact there.


    @RunWith(JUnitMosaicRunner.class)
    @SuppressWarnings("ALL")
    public class MultiThreadedTest {

        private static final Generator<String > RND_STRING = PrimitiveGenerators.strings();


        @Test
        public void concurrentPushPopTest() {
            final List<String> stack = Collections.synchronizedList(new ArrayList<String>());

            List<List<String>> itemsPushedByEachThread = JUnitMosaic.multiThreaded( new TakesIntFunction<List<String>>() {
                public List<String> invoke( int numIterations ) {
                    List<String> allGeneratedValues = new ArrayList<String>();

                    for ( int i = 0; i < numIterations; i++ ) {
                        String v = RND_STRING.next();

                        stack.add( v );
                        allGeneratedValues.add( v );
                    }

                    return allGeneratedValues;
                }
            } );


            verifyStack( stack, itemsPushedByEachThread );
        }


        private void verifyStack( List<String> stack, List<List<String>> perThreadResults ) {
            List<String> allPushedItems = flatten( perThreadResults );

            while ( !stack.isEmpty() ) {
                String head = stack.remove(0);

                boolean wasRemoved = allPushedItems.remove(head);
                assertTrue( "stack is not thread safe; stack contained a value that was not reported to have been pushed: '" +head+"'", wasRemoved );
            }

            assertEquals( "stack is not thread safe; the stack has lost the following items: " + allPushedItems, 0, allPushedItems.size() );
        }

        private List<String> flatten(List<List<String>> perThreadResults) {
            List<String> all = new ArrayList<String>();

            for ( List<String> resultsFromOneThread : perThreadResults ) {
                if ( resultsFromOneThread != null ) {
                    all.addAll( resultsFromOneThread );
                }
            }

            return all;
        }

    }

In Java8, we can use lambdas and streams to simplify further.  To verify that the test detects when
the stack is not thread safe, then remove the call to synchronizedList from the example.  The test
will then fail.


    private static final Generator<String > RND_STRING = PrimitiveGenerators.strings();

    @Test
    public void concurrentPushPopTest() {
          List<String> stack = new ArrayList<>();                                    // stack is not thread safe - test will fail
//        List<String> stack = Collections.synchronizedList( new ArrayList<>() );    // replace the above line with this one to fix the test

        List<List<String>> itemsPushedByEachThread = JUnitMosaic.multiThreaded( numIterations -> {
            List<String> allGeneratedValues = new ArrayList<>();

            for ( int i = 0; i < numIterations; i++ ) {
                String v = RND_STRING.next();

                stack.add( v );
                allGeneratedValues.add( v );
            }

            return allGeneratedValues;
        } );


        verifyStack( stack, itemsPushedByEachThread );
    }


    private void verifyStack( List<String> stack, List<List<String>> perThreadResults ) {
        Set<String> expected = perThreadResults.stream().flatMap(List::stream).collect( Collectors.toSet() );
        Set<String> actual   = new HashSet<>( stack );

        assertEquals( expected, actual );
    }


