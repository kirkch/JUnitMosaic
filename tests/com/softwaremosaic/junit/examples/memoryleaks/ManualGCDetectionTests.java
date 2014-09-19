package com.softwaremosaic.junit.examples.memoryleaks;

import org.junit.Test;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import static com.softwaremosaic.junit.JUnitMosaic.*;


/**
 * Examples of blocking the current thread until a weak reference has been
 * cleared.
 */
public class ManualGCDetectionTests {

    @Test
    public void detectThatAObjectWasGCd() {
        Object o = new Object();
        Reference<Object> ref = new WeakReference<Object>( o );

        o = null;

        spinUntilReleased(ref);
    }

    @Test
    public void detectThatAObjectIsNotGCable() {
        Object o = new Object();
        Reference<Object> ref = new WeakReference<Object>( o );

        try {
            spinUntilReleased(ref);
            fail("object was GC'd after all?");
        } catch ( IllegalStateException e ) {

        }
    }

}
