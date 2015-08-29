package testmosaics.audit;

import com.softwaremosaic.junit.lang.VoidFunction1;
import org.junit.Test;

import static org.junit.Assert.*;


@SuppressWarnings("unchecked")
public class AuditTest {
// ASSERT EXACTLY MATCHES

    @Test
    public void interceptTwoCalls_andExpectNone_expectException() {
        Audit<Factory<String>> audit = new Audit(Factory.class);

        Factory<String> factory = audit.wrap( new CarFactory() );

        factory.create();
        factory.create();

        try {
            audit.assertExactlyMatches(
                new VoidFunction1<Factory<String>>() {
                    public void invoke( Factory<String> proxy ) {}
                }
            );

            fail( "expected exception" );
        } catch ( AssertionError ex ) {
            String expectedMessage = "Expected no calls, but received:\n" +
                "   .create()\n" +
                "   .create()";

            assertEquals( expectedMessage, ex.getMessage() );
        }
    }

    @Test
    public void interceptTwoCalls_andFullfillFirst_expectExceptionAsSecondWasMissing() {
        Audit<Factory<String>> audit = new Audit(Factory.class);

        Factory<String> factory = audit.wrap( new CarFactory() );

        factory.create();
        factory.create();

        try {
            audit.assertExactlyMatches(
                new VoidFunction1<Factory<String>>() {
                    public void invoke( Factory<String> proxy ) {
                        proxy.create();
                    }
                }
            );

            fail( "expected exception" );
        } catch ( AssertionError ex ) {
            String expectedMessage = "Matched 1 call, and then expected no further calls, but received:\n" +
                "   .create()";

            assertEquals( expectedMessage, ex.getMessage() );
        }
    }

    @Test
    public void interceptThreeCalls_andFullfillFirst_expectExceptionAsLastTwoWereMissing() {
        Audit<Factory<String>> audit = new Audit(Factory.class);

        Factory<String> factory = audit.wrap( new CarFactory() );

        factory.create();
        factory.create();
        factory.create();

        try {
            audit.assertExactlyMatches(
                new VoidFunction1<Factory<String>>() {
                    public void invoke( Factory<String> proxy ) {
                        proxy.create();
                    }
                }
            );

            fail( "expected exception" );
        } catch ( AssertionError ex ) {
            String expectedMessage = "Matched 1 call, and then expected no further calls, but received:\n" +
                "   .create()\n" +
                "   .create()";

            assertEquals( expectedMessage, ex.getMessage() );
        }
    }

    @Test
    public void interceptThreeCalls_andFullfillFirstTwo_expectExceptionAsThirdWasMissing() {
        Audit<Factory<String>> audit = new Audit(Factory.class);

        Factory<String> factory = audit.wrap( new CarFactory() );

        factory.create();
        factory.create();
        factory.create();

        try {
            audit.assertExactlyMatches(
                new VoidFunction1<Factory<String>>() {
                    public void invoke( Factory<String> proxy ) {
                        proxy.create();
                        proxy.create();
                    }
                }
            );

            fail( "expected exception" );
        } catch ( AssertionError ex ) {
            String expectedMessage = "Matched 2 calls, and then expected no further calls, but received:\n" +
                "   .create()";

            assertEquals( expectedMessage, ex.getMessage() );
        }
    }

    @Test
    public void interceptThreeDifferingCalls_andFullfillFirstOne_expectExceptionAsThirdWasMissing() {
        Audit<Factory<String>> audit = new Audit(Factory.class);

        Factory<String> factory = audit.wrap( new CarFactory() );

        factory.create();
        factory.haltProduction();
        factory.create();

        try {
            audit.assertExactlyMatches(
                new VoidFunction1<Factory<String>>() {
                    public void invoke( Factory<String> proxy ) {
                        proxy.create();
                    }
                }
            );

            fail( "expected exception" );
        } catch ( AssertionError ex ) {
            String expectedMessage = "Matched 1 call, and then expected no further calls, but received:\n" +
                "   .haltProduction()\n" +
                "   .create()";

            assertEquals( expectedMessage, ex.getMessage() );
        }
    }

    @Test
    public void interceptThreeDifferingCalls_andFullfillAll_expectSuccess() {
        Audit<Factory<String>> audit = new Audit(Factory.class);

        Factory<String> factory = audit.wrap( new CarFactory() );

        factory.create();
        factory.haltProduction();
        factory.create();

        audit.assertExactlyMatches(
            new VoidFunction1<Factory<String>>() {
                public void invoke( Factory<String> proxy ) {
                    proxy.create();
                    proxy.haltProduction();
                    proxy.create();
                }
            }
        );
    }



    @Test
    public void interceptOneCall_expectDifferentCall_expectException() {
        Audit<Factory<String>> audit = new Audit(Factory.class);

        Factory<String> factory = audit.wrap( new CarFactory() );

        factory.create();

        try {
            audit.assertExactlyMatches(
                new VoidFunction1<Factory<String>>() {
                    public void invoke( Factory<String> proxy ) {
                        proxy.haltProduction();
                    }
                }
            );

            fail( "expected exception" );
        } catch ( AssertionError ex ) {
            String expectedMessage = "Mismatch at first call, expected haltProduction() but saw create()";

            assertEquals( expectedMessage, ex.getMessage() );
        }
    }

    @Test
    public void interceptNoCalls_expectedOneCall_expectException() {
        Audit<Factory<String>> audit = new Audit(Factory.class);

        audit.wrap( new CarFactory() );

        try {
            audit.assertExactlyMatches(
                new VoidFunction1<Factory<String>>() {
                    public void invoke( Factory<String> proxy ) {
                        proxy.haltProduction();
                    }
                }
            );

            fail( "expected exception" );
        } catch ( AssertionError ex ) {
            String expectedMessage = "Mismatch at first call, expected haltProduction() but saw no further calls";

            assertEquals( expectedMessage, ex.getMessage() );
        }
    }

    @Test
    public void interceptOneCall_expectTwoCalls_expectException() {
        Audit<Factory<String>> audit = new Audit(Factory.class);

        Factory<String> factory = audit.wrap( new CarFactory() );

        factory.create();

        try {
            audit.assertExactlyMatches(
                new VoidFunction1<Factory<String>>() {
                    public void invoke( Factory<String> proxy ) {
                        proxy.create();
                        proxy.haltProduction();
                    }
                }
            );

            fail( "expected exception" );
        } catch ( AssertionError ex ) {
            String expectedMessage = "Expected haltProduction() but saw no further calls";

            assertEquals( expectedMessage, ex.getMessage() );
        }
    }




    public static interface Factory<T> {
        public T create();
        public void haltProduction();
    }

    public static class CarFactory implements Factory<String> {
        public String create() {
            return "Car";
        }

        public void haltProduction() {}
    }
}