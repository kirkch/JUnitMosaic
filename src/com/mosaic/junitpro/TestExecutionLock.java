package com.mosaic.junitpro;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Permits multi executions of @Test methods, but only one @Benchmark test at
 * a time.
 */
public class TestExecutionLock {

    private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public static void acquireTestLock( Test testAnnotation ) {
        Lock lock = selectTestLock( testAnnotation );

        lock.lock();
    }

    public static void releaseTestLock( Test testAnnotation ) {
        Lock lock = selectTestLock( testAnnotation );

        lock.unlock();
    }

    public static void acquireBenchmarkLock() {
        rwLock.writeLock().lock();
    }

    public static void releaseBenchmarkLock() {
        rwLock.writeLock().lock();
    }

    private static Lock selectTestLock(Test testAnnotation) {
        if ( testAnnotation.threadCheck() ) {
            return rwLock.writeLock();
        } else {
            return rwLock.readLock();
        }
    }

}
