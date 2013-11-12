package com.mosaic.junitpro.examples.threads;

import com.mosaic.junitpro.Assert;
import com.mosaic.junitpro.JUnitExt;
import com.mosaic.junitpro.annotations.Test;
import com.mosaic.junitpro.tools.AssertionJob;
import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import static org.junit.Assert.assertEquals;


/**
 *
 */
@RunWith(JUnitExt.class)
@SuppressWarnings("ALL")
public class MultiThreadedTest {

    private static final Generator<Integer> RND_INT    = PrimitiveGenerators.integers(1, 100);
    private static final Generator<String > RND_STRING = PrimitiveGenerators.strings();


    private List<String> stack = Collections.synchronizedList(new ArrayList<String>());
//    private List<String> stack = new ArrayList<String>();



    @Test
    public void concurrentPushPopTest() {
        Assert.multiThreadedAssert( new PushPopAssertionJob() );
    }


    private class PushPopAssertionJob extends AssertionJob {
        private List<String> valuesPushed;
        private List<String> valuesPopped;

        private int          maxNumIterations;
        private int          iterationCount;

        public PushPopAssertionJob() {
            this( new ArrayList<String>(), new ArrayList<String>() );
        }

        public PushPopAssertionJob( List<String> pushedValues, List<String> valuesPopped ) {
            this.valuesPushed = pushedValues;
            this.valuesPopped = valuesPopped;
        }


        public void invoke() {
            iterationCount++;

            String newValue = RND_STRING.next();


            stack.add(newValue);
            valuesPushed.add(newValue);

            if ( shouldPop() ) {
                valuesPopped.add((String) stack.remove(0));
            }

            if ( iterationCount >= maxNumIterations ) {
                complete();
            }
        }

        public AssertionJob merge( AssertionJob o ) {
            PushPopAssertionJob other = (PushPopAssertionJob) o;
            PushPopAssertionJob j = (PushPopAssertionJob) super.merge(other);


            j.valuesPushed = mergeLists( this.valuesPushed, other.valuesPushed );
            j.valuesPopped = mergeLists( this.valuesPopped, other.valuesPopped );


            return j;
        }

        private List<String> mergeLists(List<String> a, List<String> b) {
            List<String> r = new ArrayList<String>( a.size() + b.size() );

            r.addAll(a);
            r.addAll(b);

            return r;
        }

        public void performAsserts() {
            List<String> expectedContents = generateExpectedContentsOfStack();
            List<String> actualContents   = popAll();

            Collections.sort(actualContents);
            Collections.sort(expectedContents);

            assertEquals( actualContents, expectedContents );
        }

        private List<String> generateExpectedContentsOfStack() {
            List<String> expectedContents = new ArrayList<String>();
            expectedContents.addAll( this.valuesPushed );
            for ( String v : this.valuesPopped ) {
                expectedContents.remove(v);
            }

            return expectedContents;
        }

        @Override
        public PushPopAssertionJob clone() {
            PushPopAssertionJob clone = (PushPopAssertionJob) super.clone();

            clone.maxNumIterations = RND_INT.next();
            clone.valuesPopped = new ArrayList<String>();
            clone.valuesPushed = new ArrayList<String>();

            clone.valuesPopped.addAll( this.valuesPopped );
            clone.valuesPushed.addAll( this.valuesPushed );

            return clone;
        }

        private boolean shouldPop() {
            return RND_INT.next() > 80 && stack.size() > 10;
        }

        private List<String> popAll() {
            List<String> contents = new ArrayList<String>();

            while ( stack.size() > 0 ) {
                String remove = (String) stack.remove(0);

                contents.add(remove);
            }

            return contents;
        }
    }


}
