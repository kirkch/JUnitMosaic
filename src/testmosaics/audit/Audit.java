package testmosaics.audit;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.softwaremosaic.junit.lang.BooleanFunction1;
import com.softwaremosaic.junit.lang.VoidFunction1;


/**
 * Intercept and record calls to another object.
 *
 * <code>
 *     AuditingProxy audit = new AuditingProxy();
 *
 *     HttpClient client = audit.wrap( new HttpClientImpl() );
 *
 *     // make calls to client
 *
 *     audit.assertExactlyMatches( pxy -> set expectations by calling pxy )
 *
 * </code>
 *
 * Now any call to client will be recorded and queryable within the audit class.
 */
@SuppressWarnings("unchecked")
public class Audit<T> {

    private List<AuditEntry> actualEntries = new ArrayList<>();
    private Class[]          targetInterfaces;

    /**
     * Any audit entry that a predicate in this matches will be skipped before it gets added to
     * actualEntries.
     */
    private List<BooleanFunction1<AuditEntry>> skipPredicates = new ArrayList<>();


    /**
     *
     * @param targetInterfaces Limit interception to the specified interfaces; if not are
     *                         supplied then the target class will be scanned for all interfaces
     */
    public Audit( Class...targetInterfaces ) {
        if ( targetInterfaces.length == 0 ) {
            throw new IllegalArgumentException( "missing required arg 'targetInterfaces'" );
        }

        this.targetInterfaces = targetInterfaces;
    }


    public T wrap( T obj ) {
        return (T) Proxy.newProxyInstance(
            this.getClass().getClassLoader(),
            targetInterfaces,
            new MyAuditingHandler( obj )
        );
    }

    public void dumpAudit() {
        System.out.println( "AuditProxy log:" );
        System.out.println( "--------------" );
        for ( AuditEntry e : actualEntries ) {
            System.out.println( e );
        }
    }

    public void assertExactlyMatches( VoidFunction1<T> assertionScript ) {
        MyAssertingHandler proxyHandler = new MyAssertingHandler();

        T proxy = (T) Proxy.newProxyInstance(
            this.getClass().getClassLoader(),
            targetInterfaces,
            proxyHandler
        );


        assertionScript.invoke( proxy );

        proxyHandler.errorIfAuditWasNotFullyMatched();
    }

    public void spinUntilExactlyMatches( VoidFunction1<T> assertionScript ) {
        spinUntilExactlyMatches(assertionScript,3000);
    }

    public void spinUntilExactlyMatches( VoidFunction1<T> assertionScript, long maxWaitMillis ) {
        long startedAt = System.currentTimeMillis();
        long maxMillis = startedAt + maxWaitMillis;

        Throwable lastException = null;
        while ( System.currentTimeMillis() <= maxMillis ) {
            try {
                assertExactlyMatches( assertionScript );

                return;  // SUCCESS
            } catch ( Exception ex ) {
                lastException = ex;
            } catch ( AssertionError ex ) {
                lastException = ex;
            }

            Backdoor.sleep( 10 );
        }


        dumpAudit();


        if ( lastException == null ) {
            throw new AssertionError( "Time out" );
        } else {
            Backdoor.throwException( lastException );
        }
    }

    public void clear() {
        actualEntries.clear();
    }

    public void assertThatThereWereNoCalls() {
        assertExactlyMatches( new VoidFunction1<T>() {
            public void invoke( T arg ) {

            }
        } );
    }

    public void ignoreMethods( final String methodName ) {
        ignoreMethods( new BooleanFunction1<AuditEntry>() {
            public boolean invoke( AuditEntry v ) {
                return v.getMethodName().equals(methodName);
            }
        } );
    }

    public void ignoreMethods( BooleanFunction1<AuditEntry> predicate ) {
        skipPredicates.add(predicate);
    }


    private class MyAuditingHandler implements InvocationHandler {
        private Object proxiedObject;

        public <T> MyAuditingHandler( T proxiedObject ) {
            this.proxiedObject = proxiedObject;
        }

        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
            AuditEntry entry = new AuditEntry( proxiedObject, method, args );

            appendAuditingEntry( entry );

            try {
                Object result = method.invoke( proxiedObject, args );

                entry.success( result );

                return result;
            } catch ( Throwable ex ) {
                entry.failure( ex );

                throw ex;
            }
        }
    }

    private void appendAuditingEntry( AuditEntry entry ) {
        for ( BooleanFunction1<AuditEntry> p : skipPredicates ) {
            if ( p.invoke( entry ) ) {
                return;
            }
        }

        actualEntries.add( entry );
    }

    private class MyAssertingHandler implements InvocationHandler {
        private int nextExpectationIndex = 0;

        /**
         * As each expectation is registered, this method will be called and checked against
         * the list of actuals that occurred previously.
         */
        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
            args = args == null ? AuditEntry.EMPTY_ARRAY : args;

            if ( nextExpectationIndex >= actualEntries.size() ) {
                String errorMessage = createMoreExpectationsThanActualsErrorMessage( method, args );

                throw new AssertionError( errorMessage );
            }

            AuditEntry actual = actualEntries.get( nextExpectationIndex );

            if ( actual.equalsCall(method, args) ) {
                nextExpectationIndex++;
            } else {
                String errorMessage = createMethodCallComparisonErrorMessage( actual, method, args );

                throw new AssertionError( errorMessage );
            }


            return actual.replayResult();
        }

        private String createMoreExpectationsThanActualsErrorMessage( Method method, Object[] args ) {
            StringBuilder buf = new StringBuilder();

            if ( actualEntries.size() == 0 ) {
                buf.append( "Mismatch at first call, expected " );
                AuditEntry.appendMethodCallSig( buf, method, args );
                buf.append( " but saw no further calls" );
            } else {
                buf.append( "Expected " );
                AuditEntry.appendMethodCallSig( buf, method, args );
                buf.append( " but saw no further calls" );
            }

            return buf.toString();
        }

        private String createMethodCallComparisonErrorMessage( AuditEntry actual, Method method, Object[] args ) {
            StringBuilder buf = new StringBuilder();

            buf.append( "Mismatch at first call, expected " );
            AuditEntry.appendMethodCallSig( buf, method, args );
            buf.append( " but saw " );
            actual.appendMethodSignatureShort( buf );


            return buf.toString();
        }

        public void errorIfAuditWasNotFullyMatched() {
            if ( actualEntries.size() == nextExpectationIndex ) {
                return;
            }


            String errorMessage = createErrorMessage();

            throw new AssertionError( errorMessage );
        }

        private String createErrorMessage() {
            StringBuilder buf = new StringBuilder();

            if ( nextExpectationIndex == 0 ) {
                buf.append( "Expected no calls, but received:\n" );
            } else if ( nextExpectationIndex == 1 ) {
                buf.append( "Matched 1 call, and then expected no further calls, but received:\n" );
            } else {
                buf.append( "Matched "+ nextExpectationIndex +" calls, and then expected no further calls, but received:\n" );
            }

            boolean appendNewLine = false;
            for ( int i= nextExpectationIndex; i< actualEntries.size(); i++ ) {
                AuditEntry entry = actualEntries.get(i);

                if ( appendNewLine ) {
                    buf.append( "\n" );
                } else {
                    appendNewLine = true;
                }

                buf.append( "   ." );
                entry.appendMethodSignatureShort(buf);
            }

            return buf.toString();
        }
    }

}
