package com.mosaic.junitpro;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks the method as being a micro benchmark.  This will change JUnit's behaviour
 * when executing the method:<p/>
 *
 * - wait for any other tests to complete before starting<p/>
 * - prevent any other tests from starting<p/>
 * - run methods annotated with @Before<p/>
 * - trigger GC then invoke the method 'value' times while timing in nano seconds<p/>
 * - repeat GC and invoke 'value()' times again and again up to batchCount() times<p/>
 * - report<p/>
 * - allow other tests to begin<p/>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Benchmark {

    /**
     * Invoke the test method this many times between
     */
    int value() default 100000;

    /**
     * How many times to measure the test method.  Each measurement is taken in
     * batches of 'value()' calls.
     */
    int batchCount() default 6;


    /**
     * Multiply the times printed after the benchmark.  Useful if you want
     * the time to process a single line of text and each call processed 100
     * lines.  In that example the multiplier would be 0.01.
     */
    double durationResultMultiplier() default 1.0;

}