package com.mosaic.junitpro.tools;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@SuppressWarnings({"EmptyCatchBlock", "unchecked"})
public class MemChecker {

    private static volatile ReferenceQueue  referenceQueue;
    private static volatile Set<Reference>  memCheckReferences;


    public static void startMemCheckRegion( boolean memCheckEnabled ) {
        assert referenceQueue == null : "a new memcheck region should not be started while another is still running";

        if ( !memCheckEnabled ) {
            return;
        }

        memCheckReferences = new HashSet<Reference>();
        referenceQueue     = new ReferenceQueue();
    }

    public static void watchValue( Object v ) {
        if ( referenceQueue == null || v == null ) {
            return;
        }

        if ( v.getClass().isArray() ) {
            int arrayLength = Array.getLength(v);

            for ( int i=0; i<arrayLength; i++ ) {
                watchValue( Array.get(v, i) );
            }
        } else if ( v instanceof Iterable ) {
            for ( Object child : (Iterable) v ) {
                watchValue(child);
            }
        }

        appendObjectToWatchList(v);
    }

    public static void endMemCheckRegion() {
        if ( referenceQueue == null ) {
            return;
        }

        ReferenceQueue rq         = referenceQueue;
        Set<Reference> references = memCheckReferences;

        referenceQueue     = null;  // swaps queue out to prevent any new references from being added concurrently
        memCheckReferences = null;



        while ( references.size() > 0 ) {
            System.gc();

            try {
                Reference ref = rq.remove(5000);
                if ( ref == null ) {
                    System.out.println( "The following objects have not been GC'd:" );
                    for ( Reference r : references ) {
                        Object o = r.get();

                        System.out.println("      " + (o == null ? null : o.getClass().getName()) + ": " + o);
                    }

                    throw new IllegalStateException( "GC not complete within 5 seconds" );
                }

                references.remove(ref);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void appendObjectToWatchList( Object v ) {
        memCheckReferences.add( new WeakReference(v,referenceQueue) );
    }

}
