package com.softwaremosaic.junit.lang;

/**
 *
 */
public interface Predicate {

    public boolean eval();

    public String reasonForFailure();

}
