package com.mosaic.junitpro.lang;

/**
 *
 */
public interface Predicate {

    public boolean eval();

    public String reasonForFailure();

}
