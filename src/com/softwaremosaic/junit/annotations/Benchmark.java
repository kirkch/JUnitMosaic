package com.softwaremosaic.junit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks the method as being a micro benchmark.  This will change JUnit's behaviour
 * when executing the method:<p/>
 *
 * <ol>
 *   <li>wait for any other tests to complete before starting</li>
 *   <li>prevent any other tests from starting</li>
 *   <li>run methods annotated with @Before</li>
 *   <li>trigger GC then invoke the method 'value' times while timing in nano seconds</li>
 *   <li>repeat GC and invoke 'value()' times again and again up to batchCount() times</li>
 *   <li>report</li>
 *   <li>allow other tests to begin</li>
 * </ol>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Benchmark {

    /**
     * Invoke the test method this many times between
     */
    int value() default 1000000;

    /**
     * How many times to measure the test method.  Each measurement is taken in
     * batches of 'value()' calls.
     */
    int batchCount() default 6;


    /**
     * Multiply the times printed by the benchmark.  Useful if you want
     * to adjust the 'units' of the result to account for how many times
     * an operation was carried out during a test. For example if the test
     * processed 100 lines of text and you wanted the average timing per line
     * of text processed then the multiplier would be 0.01 (or 1.0/100).
     */
    double durationResultMultiplier() default 1.0;

    /**
     * A short description of what is being timed.  Used when printing the
     * results.
     */
    String units() default "call";
}