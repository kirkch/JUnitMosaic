package com.softwaremosaic.junit.quickcheck;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.distribution.Distribution;
import net.java.quickcheck.util.Assert;

import static java.lang.String.format;


/**
 *
 */
public class PrimitiveBooleanArrayGenerator implements Generator<boolean[]> {

    private final int          minLength;
    private final int          maxLength;
    private final Distribution distribution;


    public PrimitiveBooleanArrayGenerator() {
        this(0, 10000);
    }

    public PrimitiveBooleanArrayGenerator( int minLength, int maxLength ) {
        this( minLength, maxLength, Distribution.UNIFORM);
    }

    public PrimitiveBooleanArrayGenerator( int minLength, int maxLength, Distribution dist ) {
        Assert.lessOrEqual( maxLength, minLength, "min" );
        Assert.notNull(dist, "dist");

        this.minLength    = minLength;
        this.maxLength    = maxLength;
        this.distribution = dist;
    }

    public boolean[] next() {
        return nextArray();
    }

    public boolean[] nextArray() {
        int       len   = GeneratorUtils.generateInt( distribution, minLength, maxLength );
        boolean[] array = new boolean[len];

        for ( int i=0; i<len; i++ ) {
            array[i] = GeneratorUtils.generateBoolean( distribution );
        }

        return array;
    }

    public String toString() {
        return format("%s[minLen=%s, maxLen=%s, distribution=%s", getClass().getSimpleName(), minLength, maxLength, distribution);
    }

}
