package testmosaics.audit;

import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * Represents a single call to a wrapped object.  It stores the method, arguments and result of
 * that call.
 */
public class AuditEntry {
    static final Object[] EMPTY_ARRAY = new Object[] {};


    private final long     startNanos;
    private final Object   target;
    private final Method interceptedMethod;
    private final Object[] actualArgs;

    private long     durationNanos;
    private boolean  threwException;
    private Object   result;


    public AuditEntry( Object target, Method interceptedMethod, Object[] actualArgs ) {
        this.target            = target;
        this.interceptedMethod = interceptedMethod;
        this.actualArgs        = actualArgs == null ? EMPTY_ARRAY : actualArgs;

        this.startNanos        = System.nanoTime();
    }

    public String getMethodName() {
        return interceptedMethod.getName();
    }

    public void success( Object r ) {
        this.threwException = false;
        this.result         = r;
        this.durationNanos  = System.nanoTime() - this.startNanos;
    }

    public void failure( Throwable ex ) {
        this.threwException = true;
        this.result         = ex;
        this.durationNanos  = System.nanoTime() - this.startNanos;
    }



    public String toString() {
        StringBuilder buf = new StringBuilder();

        appendMethodSignatureShort( buf );

        buf.append( " -returned-> " );
        if ( result != null ) {
            if ( threwException ) {
                buf.append( "<EXCEPTION>\n" );
            }

            buf.append( result );
        } else {
            buf.append("<void>");
        }

        return buf.toString();
    }

    public void appendMethodSignatureShort( StringBuilder buf ) {
        appendMethodCallSig( buf, interceptedMethod, actualArgs );
    }

    public Object replayResult() {
        if ( threwException ) {
            Backdoor.throwException( (Throwable) result );

            return null;
        } else {
            return result;
        }
    }

    public boolean equalsCall( Method method, Object[] args ) {
        return this.interceptedMethod.equals(method) && Arrays.equals( args, actualArgs );
    }

    public static void appendMethodCallSig( StringBuilder buf, Method method, Object[] args ) {
        buf.append( method.getName() );
        buf.append( '(' );

        boolean requiresSeparator = false;
        for ( Object arg : args ) {
            if ( requiresSeparator ) {
                buf.append( ',' );
            }

            buf.append( arg );

            requiresSeparator = true;
        }

        buf.append( ')' );
    }
}
