package com.mosaic.junitpro.example3;

import com.mosaic.junitpro.JUnitPro;
import com.mosaic.junitpro.Test;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(JUnitPro.class)
public class MemCheckingTest {
    private final Generator stringGenerator = PrimitiveGenerators.strings();

    private Object name;

    @Test( memCheck = true, generators = {"stringGenerator"})
    public void f( Object name ) {
//        System.out.println("name = " + name);
//        this.name = name;
    }

}
