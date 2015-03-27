package com.softwaremosaic.junit.lang;

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


}
