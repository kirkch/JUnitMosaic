package com.softwaremosaic.junit.quickcheck;

import net.java.quickcheck.generator.distribution.Distribution;


/**
 *
 */
class GeneratorUtils {

    public static boolean generateBoolean( Distribution d ) {
        double v = d.nextRandomNumber() * 10000;

        return (((int) v) & 0x01) == 1;
    }

    public static int generateInt( Distribution d, int min, int max ) {
        return (int) (d.nextRandomNumber() * (max - min) + min);
    }

}
