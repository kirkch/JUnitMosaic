package com.mosaic.hammer.junit;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public class TestExecutionLock {

    private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public static void acquireTestLock() {
        rwLock.readLock().lock();
    }

    public static void releaseTestLock() {
        rwLock.readLock().unlock();
    }

    public static void acquireBenchmarkLock() {
        rwLock.writeLock().lock();
    }

    public static void releaseBenchmarkLock() {
        rwLock.writeLock().lock();
    }

}
