package com.mosaic.junitpro;

/**
 *
 */
public interface Predicate {

    public boolean eval();

    public String reasonForFailure();

}
