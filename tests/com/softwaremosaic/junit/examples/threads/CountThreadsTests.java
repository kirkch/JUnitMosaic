package com.softwaremosaic.junit.examples.threads;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import static com.softwaremosaic.junit.JUnitMosaic.*;


/**
 * Examples of asserting that a group of threads have started/completed.
 */
public class CountThreadsTests {
    private String threadPrefix = nextName();

    @Test
    public void withNoThreadsStarted_expectZeroThreads() {
        assertEquals( 0, countThreads( threadPrefix ) );
    }

    @Test
    public void startAThread_expectOneThreadFromCount() {
        startChildThread();

        spinUntilTrue(new Callable() {
            public Boolean call() {
                return countThreads(threadPrefix) > 0;
            }
        });

        assertEquals( 1, countThreads( threadPrefix ) );
    }

    @Test
    public void startTwoThreads_expectTwoThreadFromCount() {
        startChildThread();
        startChildThread();

        spinUntilTrue(new Callable() {
            public Boolean call() {
                return countThreads(threadPrefix) > 1;
            }
        });

        assertEquals( 2, countThreads( threadPrefix ) );
    }

    @Test
    public void startTwoThreadsStopOne_expectOneThreadFromCount() {
        startChildThread();
        Thread child = startChildThread();

        spinUntilThreadCountsReaches( threadPrefix, 2 );

        child.interrupt();

        spinUntilThreadCountsReaches( threadPrefix, 1 );
    }

    private static final AtomicLong threadIdCounter = new AtomicLong(0);

    private Thread startChildThread() {
        Thread thread = new Thread( threadPrefix+":"+threadIdCounter.incrementAndGet() ) {
            @Override
            public void run() {
                try {
                    Thread.sleep( 3000 );
                } catch (InterruptedException e) {

                }
            }
        };

        thread.start();

        return thread;
    }
}
