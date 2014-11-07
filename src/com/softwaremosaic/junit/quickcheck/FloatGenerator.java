package com.softwaremosaic.junit.quickcheck;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.distribution.Distribution;
import net.java.quickcheck.util.Assert;

import static java.lang.String.format;


/**
 *
 */
public class FloatGenerator implements Generator<Float> {

    private final float min;
    private final float max;
    private final Distribution distribution;

    public FloatGenerator() {
        this(Float.MIN_VALUE, Float.MAX_VALUE);
    }

    public FloatGenerator(float min, float max) {
        this(min, max, Distribution.UNIFORM);
    }

    public FloatGenerator(float min, float max, Distribution dist) {
        Assert.lessOrEqual( max, min, "min" );
        Assert.notNull(dist, "dist");

        this.min = min;
        this.max = max;
        this.distribution = dist;
    }

    @Override
    public Float next() {
        return nextFloat();
    }

    public float nextFloat() {
        return (float) (this.distribution.nextRandomNumber() * (this.max - this.min) + this.min);
    }

    @Override
    public String toString() {
        return format("%s[min=%s, max=%s, distribution=%s", getClass().getSimpleName(), min, max, distribution);
    }

}

