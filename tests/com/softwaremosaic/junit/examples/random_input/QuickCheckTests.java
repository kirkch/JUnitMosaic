package com.softwaremosaic.junit.examples.random_input;

import com.softwaremosaic.junit.JUnitMosaic;
import com.softwaremosaic.junit.JUnitMosaicRunner;
import com.softwaremosaic.junit.annotations.Test;
import com.softwaremosaic.junit.quickcheck.ArrayGenerator;
import com.softwaremosaic.junit.quickcheck.FloatGenerator;
import com.softwaremosaic.junit.quickcheck.GeneratorFactory;
import com.softwaremosaic.junit.quickcheck.ShortGenerator;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import net.java.quickcheck.generator.distribution.Distribution;
import net.java.quickcheck.generator.distribution.RandomConfiguration;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 *
 */
@RunWith(JUnitMosaicRunner.class)
public class QuickCheckTests {

    private static final long INITIAL_SEED = 888889221;



// BOOLEAN

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
    public void testDefaultBooleanArrayGenerator(boolean[] v) {
        Object expected = generateArray( Boolean.TYPE );

        JUnitMosaic.assertArrayEquals( expected, v );
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultBooleanArrayGenerator(Boolean[] v) {
        Boolean[] expected = (Boolean[]) fetchExpectedValueFrom( new ArrayGenerator<>(0, 10000, Boolean.class, Distribution.UNIFORM, new GeneratorFactory()) );

        assertTrue( Arrays.equals( expected, v ) );
    }


// BYTE

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
    public void testDefaultByteArrayGenerator(byte[] v) {
        Object expected = generateArray( Byte.TYPE );

        JUnitMosaic.assertArrayEquals( expected, v );
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultByteArrayGenerator(Byte[] v) {
        Byte[] expected = (Byte[]) fetchExpectedValueFrom( new ArrayGenerator<>(0, 10000, Byte.class, Distribution.UNIFORM, new GeneratorFactory()) );

        assertTrue( Arrays.equals( expected, v ) );
    }

// CHAR

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
    public void testDefaultCharacterArrayGenerator(char[] v) {
        Object expected = generateArray( Character.TYPE );

        JUnitMosaic.assertArrayEquals( expected, v );
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultCharacterArrayGenerator(Character[] v) {
        Character[] expected = (Character[]) fetchExpectedValueFrom( new ArrayGenerator<>(0, 10000, Character.class, Distribution.UNIFORM, new GeneratorFactory()) );

        assertTrue( Arrays.equals( expected, v ) );
    }


// SHORT

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
    public void testDefaultShortArrayGenerator(short[] v) {
        Object expected = generateArray( Short.TYPE );

        JUnitMosaic.assertArrayEquals( expected, v );
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultShortArrayGenerator(Short[] v) {
        Short[] expected = (Short[]) fetchExpectedValueFrom( new ArrayGenerator<>(0, 10000, Short.class, Distribution.UNIFORM, new GeneratorFactory()) );

        assertTrue( Arrays.equals( expected, v ) );
    }


// INT

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
    public void testDefaultIntegerArrayGenerator(int[] v) {
        Object expected = generateArray( Integer.TYPE );

        JUnitMosaic.assertArrayEquals( expected, v );
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultIntegerArrayGenerator(Integer[] v) {
        Integer[] expected = (Integer[]) fetchExpectedValueFrom( new ArrayGenerator<>(0, 10000, Integer.class, Distribution.UNIFORM, new GeneratorFactory()) );

        assertTrue( Arrays.equals( expected, v ) );
    }


// LONG

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
    public void testDefaultLongArrayGenerator(long[] v) {
        Object expected = generateArray( Long.TYPE );

        JUnitMosaic.assertArrayEquals( expected, v );
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultLongArrayGenerator(Long[] v) {
        Long[] expected = (Long[]) fetchExpectedValueFrom( new ArrayGenerator<>(0, 10000, Long.class, Distribution.UNIFORM, new GeneratorFactory()) );

        assertTrue( Arrays.equals( expected, v ) );
    }


// FLOAT

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
    public void testDefaultFloatArrayGenerator(float[] v) {
        Object expected = generateArray( Float.TYPE );

        JUnitMosaic.assertArrayEquals( expected, v );
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultFloatArrayGenerator(Float[] v) {
        Float[] expected = (Float[]) fetchExpectedValueFrom( new ArrayGenerator<>(0, 10000, Float.class, Distribution.UNIFORM, new GeneratorFactory()) );

        assertTrue( Arrays.equals( expected, v ) );
    }


// DOUBLE

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

    @Test(seed=INITIAL_SEED)
    public void testDefaultDoubleArrayGenerator(double[] v) {
        Object expected = generateArray( Double.TYPE );

        JUnitMosaic.assertArrayEquals( expected, v );
    }

    @Test(seed=INITIAL_SEED)
    public void testDefaultDoubleArrayGenerator(Double[] v) {
        Double[] expected = (Double[]) fetchExpectedValueFrom( new ArrayGenerator<>(0, 10000, Double.class, Distribution.UNIFORM, new GeneratorFactory()) );

        assertTrue( Arrays.equals( expected, v ) );
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

    private Object generateArray( Class<?> type ) {
        return fetchExpectedValueFrom( new ArrayGenerator<>(0, 10000, type, Distribution.UNIFORM, new GeneratorFactory()) );
    }

}
