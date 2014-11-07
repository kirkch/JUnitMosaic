package com.softwaremosaic.junit.quickcheck;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.distribution.Distribution;
import net.java.quickcheck.util.Assert;

import static java.lang.String.format;


/**
 *
 */
public class ShortGenerator implements Generator<Short> {

    private final short min;
    private final short max;
    private final Distribution distribution;

    public ShortGenerator() {
        this(Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public ShortGenerator(short min, short max) {
        this(min, max, Distribution.UNIFORM);
    }

    public ShortGenerator(short min, short max, Distribution dist) {
        Assert.lessOrEqual( max, min, "min" );
        Assert.notNull(dist, "dist");

        this.min = min;
        this.max = max;
        this.distribution = dist;
    }

    @Override
    public Short next() {
        return nextShort();
    }

    public short nextShort() {
        return (short) (this.distribution.nextRandomNumber() * (this.max - this.min) + this.min);
    }

    @Override
    public String toString() {
        return format("%s[min=%s, max=%s, distribution=%s", getClass().getSimpleName(), min, max, distribution);
    }

}