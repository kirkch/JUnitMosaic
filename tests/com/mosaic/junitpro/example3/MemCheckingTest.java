package com.mosaic.junitpro.example3;

import com.mosaic.junitpro.JUnitExt;
import com.mosaic.junitpro.Test;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(JUnitExt.class)
public class MemCheckingTest {
    private final Generator stringGenerator = PrimitiveGenerators.strings();

    public Object name;


    public void vo() {
        System.out.println("name = " + name);
    }

    @Test( memCheck=true, generators={"stringGenerator"} )
    public void f( Object name ) {
        this.name = name;

        this.name = null;
    }

    @Test( memCheck=false )
    public void limitationCase() {
        // current implementation of memCheck can only track params passed into
        // the test (and thus created from an instance of Generator).
        this.name = new String("foo");
        this.name = new MemCheckingTest();
        this.name = null;
    }

}
