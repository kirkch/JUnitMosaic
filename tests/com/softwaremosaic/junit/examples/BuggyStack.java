package com.softwaremosaic.junit.examples;

/**
 * Stack with a potential to overflow and does not clear out old values
 * which will cause the potential for premature tenuring of objects.
 */
public class BuggyStack {

    private Object[] stack  = new Object[10];
    private int      offset = 0;


    public void push( Object o ) {
        stack[offset++] = o;
    }

    public Object pop() {
        return stack[--offset];
    }

    public int size() {
        return offset;
    }

}
