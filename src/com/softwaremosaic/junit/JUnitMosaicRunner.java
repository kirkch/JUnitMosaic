package com.softwaremosaic.junit;

import com.softwaremosaic.junit.annotations.Benchmark;
import com.softwaremosaic.junit.annotations.Test;
import com.softwaremosaic.junit.lang.TestExecutionLock;
import com.softwaremosaic.junit.quickcheck.GeneratorFactory;
import com.softwaremosaic.junit.tools.MemChecker;
import com.softwaremosaic.junit.tools.ThreadChecker;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.distribution.RandomConfiguration;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.MultipleFailureException;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
@SuppressWarnings("unchecked")
public class JUnitMosaicRunner extends BlockJUnit4ClassRunner {

    private List<FrameworkMethod> list = null;

    public JUnitMosaicRunner(Class<?> klass) throws InitializationError {
        super( klass );
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        if ( list == null ) {
            List<FrameworkMethod> annotatedMethods1 = getTestClass().getAnnotatedMethods(Test.class);
            List<FrameworkMethod> annotatedMethods2 = getTestClass().getAnnotatedMethods(org.junit.Test.class);
            List<FrameworkMethod> annotatedMethods3 = getTestClass().getAnnotatedMethods(Benchmark.class);

            list = new ArrayList(annotatedMethods1.size()+annotatedMethods2.size()+annotatedMethods3.size());
            list.addAll(annotatedMethods1);
            list.addAll(annotatedMethods2);
            list.addAll(annotatedMethods3);
        }

        return list;
    }

    @Override
    protected Statement withBefores(final FrameworkMethod method, Object target,
                                    Statement statement) {
        final Statement withBefores = super.withBefores( method, target, statement );

        return new Statement() {
            public void evaluate() throws Throwable {
                final ExceptionCollector exceptionCollector = new ExceptionCollector();
                final Test               testAnnotation = method.getAnnotation(Test.class);

                if  ( testAnnotation != null ) {
                    exceptionCollector.invokeAndCaptureException( new FunctionS0() {
                        @Override
                        public void invoke() throws Throwable {
                            TestExecutionLock.acquireTestLock( testAnnotation );
                        }
                    });

                    exceptionCollector.invokeAndCaptureException( new FunctionS0() {
                        @Override
                        public void invoke() throws Throwable {
                            ThreadChecker.testAboutToStart( testAnnotation );
                        }
                    });
                }

                exceptionCollector.throwIfAnyExceptionsWereCollected();
                withBefores.evaluate();
            }
        };
    }

    @Override
    protected Statement withAfters(final FrameworkMethod method, Object target,
                                   Statement statement) {
        final Statement withAfters = super.withAfters( method, target, statement );

        return new Statement() {
            public void evaluate() throws Throwable {
                final ExceptionCollector exceptionCollector = new ExceptionCollector();
                final Test               testAnnotation = method.getAnnotation(Test.class);

                exceptionCollector.invokeAndCaptureException( new FunctionS0() {
                    @Override
                    public void invoke() throws Throwable {
                        withAfters.evaluate();
                    }
                } );

                if ( testAnnotation != null ) {
                    exceptionCollector.invokeAndCaptureException( new FunctionS0() {
                        @Override
                        public void invoke() throws Throwable {
                            ThreadChecker.testHasFinished( testAnnotation );
                        }
                    } );

                    exceptionCollector.invokeAndCaptureException( new FunctionS0() {
                        @Override
                        public void invoke() throws Throwable {
                            TestExecutionLock.releaseTestLock( testAnnotation );
                        }
                    } );
                }

                exceptionCollector.throwIfAnyExceptionsWereCollected();
            }
        };
    }

    private static interface FunctionS0 {
        public void invoke() throws Throwable;
    }

    private static class ExceptionCollector {
        private List<Throwable> errors = new ArrayList<Throwable>();

        public void invokeAndCaptureException( FunctionS0 f ) {
            try {
                f.invoke();
            } catch ( Throwable ex ) {
                errors.add(ex);
            }
        }

        public void throwIfAnyExceptionsWereCollected() throws Throwable {
            MultipleFailureException.assertEmpty( errors );
        }
    }


    @Override
    protected void validateTestMethods(List<Throwable> errors) {}

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Benchmark benchmarkAnnotation = method.getAnnotation(Benchmark.class);

        if ( benchmarkAnnotation == null ) {
            Test testAnnotation = method.getAnnotation(Test.class);

            return new InvokeTestMethod( method, test, testAnnotation );
        } else {
            return new InvokeBenchmarkMethod( method, test, benchmarkAnnotation );
        }
    }

    private static final boolean areAssertionsEnabled = detectWhetherAssertionsAreEnabled();

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Benchmark benchmarkAnnotation = method.getAnnotation(Benchmark.class);

        if ( benchmarkAnnotation == null || !areAssertionsEnabled ) {
            super.runChild( method, notifier );
        } else {
            Description description = Description.createTestDescription(getTestClass().getJavaClass(),
                testName(method),
                method.getAnnotations());


            System.out.println(description.getDisplayName()+" benchmark skipped because assertions are enabled, remove the -ea flag from the java process");
            notifier.fireTestIgnored( description );
        }
    }

    @SuppressWarnings({"ConstantConditions", "AssertWithSideEffects", "UnusedAssignment"})
    private static boolean detectWhetherAssertionsAreEnabled() {
        boolean flag = false;

        assert (flag = true);

        return flag;
    }
}


@SuppressWarnings("unchecked")
class InvokeTestMethod extends Statement {

    private final FrameworkMethod fTestMethod;
    private final Object          fTarget;
    private final Test            testAnnotation;



    public InvokeTestMethod( FrameworkMethod testMethod, Object target, Test testAnnotation ) {
        fTestMethod         = testMethod;
        fTarget             = target;
        this.testAnnotation = testAnnotation;
    }

    @Override
    public void evaluate() throws Throwable {
        Generator[] generators        = fetchGenerators();
        int         numRuns           = calculateNumberOfRuns(generators);
        boolean     isMemCheckEnabled = isMemCheckEnabled();


        MemChecker memChecker = new MemChecker();

        try {
            int successCount = 0;

            for ( int i=0; i<numRuns; i++ ) {
                memChecker.startMemCheckRegion( isMemCheckEnabled );

                long seed = selectSeed();
                RandomConfiguration.setSeed(seed);


                Object[] paramValues = generateParameterValues( generators );

                if ( isMemCheckEnabled && paramValues.length == 0 ) {
                    throw new UnsupportedOperationException("@Test(memCheck=true) requires a test method that takes parameters");
                }

                memChecker.watchValue( paramValues );

                try {
                    fTestMethod.invokeExplosively(fTarget, paramValues);

                    successCount++;
                } catch (AssumptionViolatedException e) {
                    // ignore failed assumptions
                } catch (Throwable ex) {
                    if ( generators.length != 0 ) {
                        System.err.println( "Test failed with: " + ex.getClass().getName() + "  " + ex.getMessage() );
                        System.err.println( "To repeat the test with the same values from the random value generators, specify @Test(seed="+seed+"L)" );

                        System.err.println( "The generated values used for this test run were:" );
                        for ( int j=0; j<paramValues.length; j++ ) {
                            System.err.println( "  " + j + ": '"+paramValues[j]+"'" );
                        }
                    }

                    rethrowException(ex);
                } finally {
                    //noinspection UnusedAssignment
                    paramValues = null;  // makes the param values GC'able

                    memChecker.endMemCheckRegion( successCount == numRuns );
                }
            }
        } finally {

        }
    }

    private long selectSeed() {
        if ( testAnnotation == null || testAnnotation.seed() == Long.MIN_VALUE ) {
            return System.currentTimeMillis();
        } else {
            return testAnnotation.seed();
        }
    }

    private void rethrowException( Throwable ex ) {
        Unsafe unsafe = fetchUnsafe();

        unsafe.throwException( ex );
    }

    private static Unsafe fetchUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField( "theUnsafe" );

            field.setAccessible(true);

            return (Unsafe) field.get(null);
        } catch ( Throwable e ) {
            throw new RuntimeException(e);
        }
    }

    private int calculateNumberOfRuns(Generator[] generators) {
        if ( generators.length > 0 ) {
            return testAnnotation.repeat();
        }

        return 1;
    }

    private boolean isMemCheckEnabled() {
        return testAnnotation != null && testAnnotation.memCheck();
    }

    private Generator[] fetchGenerators() {
        if ( testAnnotation == null )  {
            return new Generator[] {};
        }

        String[]    generatorFieldNames = testAnnotation.generators();
        Class[]     paramTypes          = fTestMethod.getMethod().getParameterTypes();
        int         numGenerators       = generatorFieldNames.length;
        int         numParams           = paramTypes.length;
        Generator[] generators          = new Generator[numParams];

        for ( int i=0; i<numParams; i++ ) {
            Class  paramType             = paramTypes[i];
            String generatorFieldNameNbl = numGenerators > i ? generatorFieldNames[i] : null;

            generators[i] = selectGeneratorFor( paramType, generatorFieldNameNbl );
        }

        return generators;
    }

    private Generator selectGeneratorFor( Class paramType, String generatorFieldNameNbl ) {
        if ( generatorFieldNameNbl == null ) {
            return fetchDefaultGeneratorForType( paramType );
        } else {
            return fetchGeneratorByFieldName( generatorFieldNameNbl );
        }
    }





    private Generator fetchDefaultGeneratorForType( Class paramType ) {
        Generator g = new GeneratorFactory().newGeneratorFor( paramType );

        if ( g == null ) {
            throw new IllegalStateException( "No generator found for parameter type '"+paramType.getName()+"'.  Generators can be declared as fields on the test class, and referenced via @Test(generators={\"fieldName\")." );
        }

        return g;
    }

    private Generator fetchGeneratorByFieldName( String generatorFieldName ) {
        try {
            Field field = locateMandatoryField(generatorFieldName);
            field.setAccessible(true);

            return (Generator) field.get(fTarget);
        } catch (Exception e) {
            throw new IllegalArgumentException( "Unable to retrieve generator from field '"+generatorFieldName+"'", e );
        }
    }

    private Object[] generateParameterValues(Generator[] generators) {
        int      numGenerators = generators.length;
        Object[] values        = new Object[numGenerators];

        for ( int i=0; i<numGenerators; i++ ) {
            values[i] = generators[i].next();
        }

        return values;
    }

    private Field locateMandatoryField( String fieldName ) {
        for ( Class c : getSuperClasses() ) {
            try {
                return c.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // carry on looking
            }
        }

        throw new IllegalArgumentException( "Unable to locate generator in field '"+fieldName+"'");
    }

    private List<Class<?>> getSuperClasses() {
        ArrayList results = new ArrayList();
        Class     current = fTarget.getClass();

        while (current != null) {
            results.add(current);
            current = current.getSuperclass();
        }

        return results;
    }
}


@SuppressWarnings("unchecked")
class InvokeBenchmarkMethod extends Statement {

    private final FrameworkMethod fTestMethod;
    private final Object          fTarget;
    private final Benchmark       annotation;

    public InvokeBenchmarkMethod( FrameworkMethod testMethod, Object target, Benchmark annotation ) {
        fTestMethod     = testMethod;
        fTarget         = target;
        this.annotation = annotation;
    }

    @Override
    public void evaluate() throws Throwable {
//        verifyParameters();

        TestExecutionLock.acquireBenchmarkLock();

        try {
            System.out.println( "Benchmark results for: " );
            System.out.println( "Invoking "+fTestMethod.getMethod().getDeclaringClass().getSimpleName()+"."+fTestMethod.getName() + " (batchCount=" + annotation.batchCount() + ", iterationCount=" + annotation.value() + ", timingMultipler=" + annotation.durationResultMultiplier() + ")" );
            System.out.println( "    " );

            for ( int i=0; i<annotation.batchCount()+1; i++ ) {
                invokeAndReportBatch(i);
            }
        } finally {
            TestExecutionLock.releaseBenchmarkLock();
        }
    }

    private void invokeAndReportBatch( int runCount ) throws Throwable {
        long   durationNanosRaw = invokeBatch();
        double durationNanos    = durationNanosRaw * annotation.durationResultMultiplier();

        double perCallNanos  = durationNanos / annotation.value();
        double perCallMillis = perCallNanos  / 1000000.0;

        if ( runCount > 0 ) {
            System.out.print( "    " );

            if ( perCallMillis < 1 ) {
                System.out.print( String.format("%.2f",perCallNanos)  );
                System.out.println( "ns per " + annotation.units() );
            } else {
                System.out.print( String.format("%.2f",perCallMillis)  );
                System.out.println( "ms per " + annotation.units() );
            }
        }
    }

    private long invokeBatch() throws Throwable {
        System.gc();

        int    numIterations = annotation.value();
        Method method        = fTestMethod.getMethod();

        long startNanos = System.nanoTime();
        if ( method.getParameterTypes().length == 1 ) {
            method.invoke(fTarget, annotation.value());
        } else {
            for ( int i=0; i<numIterations; i++ ) {
                method.invoke(fTarget);
            }
        }

        return System.nanoTime() - startNanos;
    }

//    private void verifyParameters() {
//        int numParams     = fTestMethod.getMethod().getParameterTypes().length;
//        int numGenerators = testAnnotation == null ? 0 : testAnnotation.generators().length;
//
//        if ( numGenerators != numParams ) {
//            String msg = String.format("Test method has %s parameters declared, and %s generators.  There should be exactly one generator per parameter.", numParams, numGenerators);
//
//            throw new IllegalArgumentException(msg);
//        }
//    }

}