package com.softwaremosaic.junit.quickcheck;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.distribution.Distribution;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 * Keeps iterating over each ofthe enum values starting from a random position.
 */
public class EnumGenerator<T extends Enum<T>> implements Generator<T> {

    private String       name;
    private int          pos;
    private List<T>      allValues;


    public EnumGenerator(Class<T> enumType, Distribution dist) {
        this.name      = enumType.getSimpleName();
        this.allValues = new ArrayList<>(EnumSet.allOf(enumType));
    }


    @Override
    public T next() {
        T v = allValues.get(pos);

        setPos( pos+1 );

        return v;
    }

    @Override
    public String toString() {
        return name;
    }

    private void setPos( int newPos ) {
        this.pos = newPos % allValues.size();
    }
}
