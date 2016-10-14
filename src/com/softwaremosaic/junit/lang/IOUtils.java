package com.softwaremosaic.junit.lang;

import testmosaics.audit.Backdoor;

import java.io.*;

/**
 *
 */
public class IOUtils {

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepCopy( T orig ) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));

            return (T) in.readObject();
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    public static File makeTempDirectory( String prefix ) {
        return makeTempDirectory( prefix, null );
    }

    public static File makeTempDirectory( String prefix, String postfix ) {
        try {
            File f = File.createTempFile( prefix, postfix );

            f.delete();
            f.mkdir();

            return f;
        } catch ( IOException ex ) {
            throw Backdoor.throwException(ex);
        }
    }

    public static void close( Closeable c ) {
        try {
            c.close();
        } catch ( IOException ex ) {

        }
    }

    public static int deleteAll( File f ) {
        int count = 0;

        File[] children = f.listFiles();
        if ( children != null ) {
            for ( File child : children ) {
                if ( child.isDirectory() ) {
                    count += deleteAll(child);
                } else {
                    child.delete();
                    count += 1;
                }
            }
        }

        if ( f.delete() ) {
            count += 1;
        }

        return count;
    }


}
