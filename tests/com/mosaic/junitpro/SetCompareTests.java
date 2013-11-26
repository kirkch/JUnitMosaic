package com.mosaic.junitpro;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.mosaic.junitpro.Assert.*;


/**
 *
 */
public class SetCompareTests {

    private <T> Set asSet( T...elements ) {
        return new HashSet<T>( Arrays.asList(elements) );
    }

    @Test
    public void compareEmptySetToNullSet_expectEmptyResults() {
        SetComparison r = compare( asSet(), null );

        assertEquals( 0, r.inBothSets.size() );
        assertEquals( 0, r.onlyInSetA.size() );
        assertEquals( 0, r.onlyInSetB.size() );
    }

    @Test
    public void compareNullToEmptySetSet_expectEmptyResults() {
        SetComparison r = compare( null, asSet() );

        assertEquals( 0, r.inBothSets.size() );
        assertEquals( 0, r.onlyInSetA.size() );
        assertEquals( 0, r.onlyInSetB.size() );
    }

    @Test
    public void compare_1Vnull() {
        SetComparison r = compare( asSet(1), null );

        assertEquals( "[1]", r.onlyInSetA.toString() );
        assertEquals( "[]",  r.onlyInSetB.toString() );
        assertEquals( "[]",  r.inBothSets.toString() );
    }

    @Test
    public void compare_1Vempty() {
        SetComparison r = compare( asSet(1), new HashSet() );

        assertEquals( "[1]", r.onlyInSetA.toString() );
        assertEquals( "[]",  r.onlyInSetB.toString() );
        assertEquals( "[]",  r.inBothSets.toString() );
    }

    @Test
    public void compare_1V1() {
        SetComparison r = compare( asSet(1), asSet(1) );

        assertEquals( "[]",  r.onlyInSetA.toString() );
        assertEquals( "[]",  r.onlyInSetB.toString() );
        assertEquals( "[1]", r.inBothSets.toString() );
    }

    @Test
    public void compare_1V0() {
        SetComparison r = compare( asSet(1), asSet(0) );

        assertEquals( "[1]",  r.onlyInSetA.toString() );
        assertEquals( "[0]",  r.onlyInSetB.toString() );
        assertEquals( "[]", r.inBothSets.toString() );
    }

    @Test
    public void compare_12V1() {
        SetComparison r = compare( asSet(1,2), asSet(1) );

        assertEquals( "[2]",  r.onlyInSetA.toString() );
        assertEquals( "[]",  r.onlyInSetB.toString() );
        assertEquals( "[1]", r.inBothSets.toString() );
    }

    @Test
    public void compare_1V12() {
        SetComparison r = compare( asSet(1), asSet(1,2) );

        assertEquals( "[]",  r.onlyInSetA.toString() );
        assertEquals( "[2]",  r.onlyInSetB.toString() );
        assertEquals( "[1]", r.inBothSets.toString() );
    }

}

