package testmosaics.audit;

import sun.misc.Unsafe;

import java.lang.reflect.Field;


public class Backdoor {
    private static final Unsafe unsafe = fetchUnsafe();


    public static <T> T throwException( Throwable ex ) {
        if ( ex == null ) {
            throw new NullPointerException();
        }

        unsafe.throwException( ex );

        // returns null so that callers can write 'return Backdoor.throwException(ex)'
        // all to avoid compiler errors, bah.
        return null;
    }

    public static void sleep( long millis ) {
        assert millis >= 0;

        try {
            Thread.sleep( millis );
        } catch ( InterruptedException ex ) {
            throwException( ex );
        }
    }



    private static Unsafe fetchUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField( "theUnsafe" );

            field.setAccessible(true);

            return (Unsafe) field.get(null);
        } catch ( Throwable e ) {
            throw new RuntimeException(e);
        }
    }
}
