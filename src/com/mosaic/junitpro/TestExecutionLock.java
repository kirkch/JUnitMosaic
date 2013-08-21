package com.mosaic.junitpro;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Permits multi executions of @Test methods, but only one @Benchmark test at
 * a time.
 */
public class TestExecutionLock {

    private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public static void acquireTestLock( boolean memCheckEnabled ) {
        if ( memCheckEnabled ) {
            rwLock.writeLock().lock();
        } else {
            rwLock.readLock().lock();
        }
    }

    public static void releaseTestLock( boolean memCheckEnabled ) {
        if ( memCheckEnabled ) {
            rwLock.writeLock().unlock();
        } else {
            rwLock.readLock().unlock();
        }
    }

    public static void acquireBenchmarkLock() {
        rwLock.writeLock().lock();
    }

    public static void releaseBenchmarkLock() {
        rwLock.writeLock().lock();
    }

}
