package com.softwaremosaic.junit.quickcheck;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.distribution.Distribution;
import net.java.quickcheck.util.Assert;

import java.lang.reflect.Array;

import static java.lang.String.format;


/**
 *
 */
public class ArrayGenerator<T> implements Generator {

    private final int              minLength;
    private final int              maxLength;
    private final Distribution     distribution;
    private final Class<T>         type;
    private final GeneratorFactory childGeneratorsFactory;


//    public ArrayGenerator() {
//        this(0, 10000);
//    }
//
//    public ArrayGenerator( int minLength, int maxLength ) {
//        this( minLength, maxLength, Distribution.UNIFORM);
//    }

    public ArrayGenerator( int minLength, int maxLength, Class type, Distribution dist, GeneratorFactory factory ) {
        Assert.lessOrEqual( maxLength, minLength, "min" );
        Assert.notNull(dist, "dist");

        this.minLength              = minLength;
        this.maxLength              = maxLength;
        this.distribution           = dist;
        this.type                   = type;
        this.childGeneratorsFactory = factory;
    }

    @SuppressWarnings("unchecked")
    public Object next() {
        int    len   = GeneratorUtils.generateInt( distribution, minLength, maxLength );
        Object array = Array.newInstance( type, len );

        Generator<T> generator = childGeneratorsFactory.newGeneratorFor( type );

        for ( int i=0; i<len; i++ ) {
            Array.set( array, i, generator.next() );
        }

        return array;
    }

    public String toString() {
        return format("%s[minLen=%s, maxLen=%s, distribution=%s", getClass().getSimpleName(), minLength, maxLength, distribution);
    }

}
