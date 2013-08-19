# JUnit JUnitPro

JUnit JUnitPro adds several extensions to JUnit.  It adds a number of configurable
assertions that are useful for validating memory and thread usage, a convenient
micro benchmarking harness and a tool for generating a range of input values which
helps test a wider range of data without having to explicitly code each of them.



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
marked test 100 times.  The arguments a and b are created afresh with different
values each run, from the generators specified in the @Test annotation.

Example:

    /**
     * Calculator with a potential to overflow an int.
     */
    public class Calculator {
        public int sum( int a, int b ) {
          return a + b;
        }

        public int subtract( int a, int b ) {
          return a - b;
        }
    }


    /**
     * Test that will fail due to over flow errors.
     */
    @RunWith(JUnitPro.class)
    public class CalculatorTests {
        private final Generator intGenerator = PrimitiveGenerators.integers();

        private Calculator calc = new Calculator();

        @com.mosaic.junitpro.Test(generators={"intGenerator","intGenerator"})
        public void sumTwoIntegers( int a, int b) {
            int r = calc.sum(a,b);

            assertEquals( a + b, r );
            assertEquals( a, calc.subtract(r,b) );
            assertEquals( b, calc.subtract(r,a) );

            assertTrue( r > a );
            assertTrue( r > b );
        }
    }



    /**
     * Test that restricts its inputs to the valid input range.
     */
    @RunWith(JUnitPro.class)
    public class CalculatorTests2 {
        private static final Generator<Integer> HALF_INT_GENERATOR = PrimitiveGenerators.integers(1, Integer.MAX_VALUE/2, Distribution.POSITIV_NORMAL);

        private Calculator calc = new Calculator();

        @Test(generators={"HALF_INT_GENERATOR","HALF_INT_GENERATOR"})
        public void sumTwoIntegers( int a, int b) {
            int r = calc.sum(a,b)

            assertEquals( a + b, r );
            assertEquals( a, calc.subtract(r,b) );
            assertEquals( b, calc.subtract(r,a) );


            assertTrue( r > a );
            assertTrue( r > b );
        }
    }

    /**
     * Test that restricts its inputs using JUnit's Assume mechanism.
     */
    @RunWith(JUnitPro.class)
    public class CalculatorTests2 {
        private static final Generator<Integer> HALF_INT_GENERATOR = PrimitiveGenerators.integers(1, Integer.MAX_VALUE/2, Distribution.POSITIV_NORMAL);

        private Calculator calc = new Calculator();

        @Test(generators={"HALF_INT_GENERATOR","HALF_INT_GENERATOR"})
        public void sumTwoIntegers( int a, int b) {
            Assume.assumeTrue(a > b);

            if ( a < b ) {
                fail("a is not allowed to be less than b for this test");
            }

            int r = calc.sum(a,b)

            assertEquals( a + b, r );
            assertEquals( a, calc.subtract(r,b) );
            assertEquals( b, calc.subtract(r,a) );


            assertTrue( r > a );
            assertTrue( r > b );
        }
    }



## Micro Benchmarks

When a method is annotated with @Benchmark, JUnitPro will ensure that this is the
only method to be run at that time and will time it over many runs; throwing
away the initial runs to warm up the optimisers and running the Garbage Collector
between each run.  The results of each run can then be stored over time and
compared over time/checkins to visualise trends.


    public class Calculator {
        public int sum( int a, int b ) {
          return a + b;
        }
    }


    @TestRunner(JUnitPro.class)
    public class CalculatorBenchmark1 {
        private Calculator calc = new Calculator();

        /**
         * This method will be called Benchmark.value times (defaults to 100,000)
         * and Benchmark.batchCount times (defaults to 6).  Each batch run will
         * have System.gc() invoked before starting and at the end of the run
         * an average duration of each call will be printed.
         */
        @Benchmark()
        public void sumTwoIntegers() {
            calc.sum(i,i);
        }
    }

    @TestRunner(JUnitPro.class)
    public class CalculatorBenchmark2 {
        private Calculator calc = new Calculator();

        /**
         * Because Java can be too clever for its own good, we may want to
         * calculate a value and return it from the test method.  This prevents
         * the JVM from spotting that the method has no side effects and
         * discards the code as dead code.
         */
        @Benchmark()
        public long sumTwoIntegers() {
            return calc.sum(i,i);
        }
    }


    @TestRunner(JUnitPro.class)
    public class CalculatorBenchmark3 {
        private Calculator calc = new Calculator();

        /**
         * Because Java can be too clever for its own good, we may want to
         * calculate a value and return it from the test method.  This prevents
         * the JVM from spotting that the method has no side effects and
         * discards the code as dead code.
         */
        @Benchmark()
        public long sumTwoIntegers() {
            return calc.sum(i,i);
        }
    }

    @TestRunner(JUnitPro.class)
    public class CalculatorBenchmark4 {
        private Calculator calc = new Calculator();

        /**
         * Because the benchmark framework will invoke the previous methods
         * a default of 100,000 times using reflection.  The overheads of using
         * reflection will be significant.  By adding an int parameter to the
         * method, the framework will pass the iterationCount to the method
         * and then call the method once.  Relying on the method to handle its
         * own iteration.
         */
        @Benchmark()
        public long sumTwoIntegers( int iterationCount ) {
            long sum = 0;

            for ( int i=0; i<iterationCount; i++ ) {
                sum += calc.sum(i,i);
            }

            return sum;
        }
    }


