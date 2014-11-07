package com.softwaremosaic.junit.examples.random_input;

import com.softwaremosaic.junit.JUnitMosaicRunner;
import com.softwaremosaic.junit.annotations.Test;
import com.softwaremosaic.junit.quickcheck.FloatGenerator;
import com.softwaremosaic.junit.quickcheck.ShortGenerator;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import net.java.quickcheck.generator.distribution.RandomConfiguration;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


/**
 *
 */
@RunWith(JUnitMosaicRunner.class)
public class QuickCheckTests {

    private static final long INITIAL_SEED = 888889221;


    @Test(seed=INITIAL_SEED)
    public void testDefaultBooleanGenerator(boolean v) {
        boolean expected = fetchExpectedValueFrom( PrimitiveGenerators.booleans() );

        assertEquals(expected, v);
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultBooleanGenerator(Boolean v) {
        boolean expected = fetchExpectedValueFrom( PrimitiveGenerators.booleans() );

        assertEquals(expected, v);
    }


    @Test(seed=INITIAL_SEED)
    public void testDefaultByteGenerator(byte v) {
        byte expected = fetchExpectedValueFrom( PrimitiveGenerators.bytes() );

        assertEquals(expected, v);
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultByteGenerator(Byte v) {
        byte expected = fetchExpectedValueFrom( PrimitiveGenerators.bytes() );

        assertEquals(expected, v.byteValue());
    }


    @Test(seed=INITIAL_SEED)
    public void testDefaultCharGenerator(char v) {
        char expected = fetchExpectedValueFrom( PrimitiveGenerators.characters() );

        assertEquals(expected, v);
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultCharGenerator(Character v) {
        char expected = fetchExpectedValueFrom( PrimitiveGenerators.characters() );

        assertEquals(expected, v.charValue());
    }


    @Test(seed=INITIAL_SEED)
    public void testDefaultShortGenerator(short v) {
        short expected = fetchExpectedValueFrom( new ShortGenerator() );

        assertEquals(expected, v);
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultShortGenerator(Short v) {
        short expected = fetchExpectedValueFrom( new ShortGenerator() );

        assertEquals(expected, v.shortValue());
    }


    @Test(seed=INITIAL_SEED)
    public void testDefaultIntGenerator(int v) {
        long expected = fetchExpectedValueFrom( PrimitiveGenerators.integers() );

        assertEquals(expected, v);
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultIntGenerator(Integer v) {
        long expected = fetchExpectedValueFrom( PrimitiveGenerators.integers() );

        assertEquals(expected, v.intValue());
    }


    @Test(seed=INITIAL_SEED)
    public void testDefaultLongGenerator(long v) {
        long expected = fetchExpectedValueFrom( PrimitiveGenerators.longs() );

        assertEquals(expected, v);
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultLongGenerator2(Long v) {
        long expected = fetchExpectedValueFrom( PrimitiveGenerators.longs() );

        assertEquals(expected, v.longValue());
    }


    @Test(seed=INITIAL_SEED)
    public void testDefaultFloatGenerator(float v) {
        float expected = fetchExpectedValueFrom( new FloatGenerator() );

        assertEquals(expected, v, 1e-4);
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultFloatGenerator(Float v) {
        float expected = fetchExpectedValueFrom( new FloatGenerator() );

        assertEquals(expected, v, 1e-4);
    }


    @Test(seed=INITIAL_SEED)
    public void testDefaultDoubleGenerator(double v) {
        double expected = fetchExpectedValueFrom( PrimitiveGenerators.doubles() );

        assertEquals(expected, v, 1e-4);
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultDoubleGenerator(Double v) {
        double expected = fetchExpectedValueFrom( PrimitiveGenerators.doubles() );

        assertEquals(expected, v, 1e-4);
    }


    @Test(seed=INITIAL_SEED)
    public void testDefaultDoubleGenerator(String v) {
        String expected = fetchExpectedValueFrom( PrimitiveGenerators.strings() );

        assertEquals(expected, v);
    }



    /**
     * The same seed is only expected to create the same value from a generator when run on the
     * same JVM and hardware.  Thus to avoid flaky tests we reset the seed and check that the
     * specified generator returns the same value as the default used by JUnitMosaicRunner.
     */
    private <T> T fetchExpectedValueFrom( Generator<T> g ) {
        RandomConfiguration.setSeed( INITIAL_SEED );

        return g.next();
    }

}
