package com.mosaic.junitpro.examples.memoryleaks;

import org.junit.Test;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import static com.mosaic.junitpro.Assert.*;


/**
 * Examples of blocking the current thread until a weak reference has been
 * cleared.
 */
public class ManualGCDetectionTests {

    @Test
    public void detectThatAObjectWasGCd() {
        Object o = new Object();
        Reference ref = new WeakReference( o );

        o = null;

        spinUntilReleased(ref);
    }

    @Test
    public void detectThatAObjectIsNotGCable() {
        Object o = new Object();
        Reference ref = new WeakReference( o );

        try {
            spinUntilReleased(ref);
            fail("object was GC'd after all?");
        } catch ( IllegalStateException e ) {

        }
    }

}
