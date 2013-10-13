package com.mosaic.junitpro;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that declares a test.  Identical in principle to JUnit's annotation
 * of the same name, however it offers support for QuickCheck without the need
 * to add multiple annotations (thus avoiding annotation soup).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Test {

    /**
     * The number of times to repeat a test method that takes parameters.  Methods
     * that take no parameters will be run only once, as is normal with JUnit.
     */
    int repeat() default 10;

    /**
     * Specifies the instance/class fields that hold instances of
     * net.java.quickcheck.Generator.  One per parameter on the test method.
     * The test method must have exactly the same number of generators specified
     * as the number of declared parameters in this annotation.  If the test
     * method has no parameters then JUnit will behave as usual.  If however
     * there are parameters, and matching generators then JUnitExt will tell
     * JUnit to run the test method 'repeat' times; which defaults to 100.
     * On each iteration the methods parameters will be generated from new
     * using quick check.  The JUnitExt runner will support JUnit's assume
     * methods, discarding any run that fails an assumption.
     */
    String[] generators() default {};


    /**
     * Ensures that any threads started during the test are shutdown by
     * the end of the test.  To prevent noise between tests, setting this option
     * to true will result in this test being run by itself and not in parallel
     * with other tests.
     */
    boolean threadCheck() default false;


    /**
     * Checks for memory leaks. It is expected that all objects passed into the
     * test method as parameters, must be garbage collectable by the end of the
     * test method.  If they are not, then the test will fail.<p/>
     *
     * Note that setting this option to true on tests that do not take any parameters
     * is considered an error.  Purely to help communicate that this check is only
     * for those parameters and not for objects instantiated during the test.
     */
    boolean memCheck() default false;


}