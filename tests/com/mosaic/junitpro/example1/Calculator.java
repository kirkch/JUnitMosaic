package com.mosaic.junitpro.example1;

/**
 * Calculator with a potential to overflow an int.
 */
public class Calculator {
    public int sum( int a, int b ) {
        return a + b;
    }

    public int subtract( int a, int b ) {
        return a - b;
    }
}
