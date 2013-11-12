package com.mosaic.junitpro.tools;

import com.mosaic.junitpro.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AssertionJob implements Cloneable {

    private boolean   isCompleted;
    private Throwable exception;


    protected void complete() {
        this.isCompleted = true;
    }

    public boolean isComplete() {
        return isCompleted;
    }

    public abstract void invoke();

    public AssertionJob clone() {
        try {
            return (AssertionJob) super.clone();
        } catch (CloneNotSupportedException e) {
            Assert.fail(this.getClass().getName() + " does not implement Cloneable");

            return null;
        }
    }

    public AssertionJob merge( AssertionJob other ) {
        AssertionJob j = clone();

        if ( j.exception != null ) {
            j.exception = other.exception;
        }

        return this;
    }

    public abstract void performAsserts();







    void failed( Throwable e ) {
        this.exception = e;
    }

    boolean didFail() {
        return exception != null;
    }

    Throwable getException() {
        return exception;
    }
}
