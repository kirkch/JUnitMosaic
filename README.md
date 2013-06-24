# JUnit Hammer

JUnit Hammer adds two extensions to JUnit.  Firstly a runner for Java QuickCheck
and secondly a micro benchmarking harness.


*Project Status:  Conceptual/Exploratory.  Started 10th June 2013.*


## QuickCheck integration

[QuickCheck](https://java.net/projects/quickcheck/pages/Home) is a combinator generator
library.  In combination with JUnit as a test harness, QuickCheck allows the
reuse of declared boundary conditions for method inputs, reducing the amount of
test code that has to be written to cover the same number of tests; while also increasing
readability.  A rare productivity win/win.


### QuickCheck Runner

The following example is runnable from any JUnit environment, and it runs the
marked test 100 times.  The arguments a and b are created afresh each run from
the generators specified in the @Test annotation.

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
    @RunWith(Hammer.class)
    public class CalculatorTests {
        private Calculator calc = new Calculator();

        @Test(IntGenerator,IntGenerator)
        public void sumTwoIntegers( int a, int b) {
            int r = calc.sum(a,b)

            assertEquals( a + b, r );
            assertEquals( a, calc.subtract(r,b) );
            assertEquals( b, calc.subtract(r,a) );
        }
    }



    /**
     * Test that restricts its inputs to the valid input range.
     */
    @RunWith(Hammer.class)
    public class CalculatorTests2 {
        private static final Generator<Integer> HALF_INT_GENERATOR = ...;

        private Calculator calc = new Calculator();

        @Test(HALF_INT_GENERATOR,HALF_INT_GENERATOR)
        public void sumTwoIntegers( int a, int b) {
            int r = calc.sum(a,b)

            assertEquals( a + b, r );
            assertEquals( a, calc.subtract(r,b) );
            assertEquals( b, calc.subtract(r,a) );
        }
    }


### Extra Generators

QuickCheck only comes with a set of base generators used to build up more powerful
generators.  Such as one for most of the primitive types, and collections.  Here
we add to this a set of common scenarios such as URLs, peoples names, email addresses
and so forth.



## Micro Benchmarks

When a method is annotated with @Benchmark, Hammer will ensure that this is the
only method to be run at that time and will time it over many runs; throwing
away the initial runs to warm up the optimisers and running the Garbage Collector
between each run.  The results of each run can then be stored over time and
compared over time/checkins to visualise trends.


    public class Calculator {
        public int sum( int a, int b ) {
          return a + b;
        }
    }


    @TestRunner(Hammer.class)
    public class CalculatorTests {
        private Calculator calc = new Calculator();

        @Benchmark(InMemory)
        public long sumTwoIntegers( int iterationNumber ) {
            return calc.sum(i,i);
        }
    }

