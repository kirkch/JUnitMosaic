package com.softwaremosaic.junit.tools;


/**
 * Infers system freezes by measuring delays in thread scheduling.  Small delays are normal, however
 * when the system locks up due to GC or swap usage then thread scheduling will be impacted quite
 * badly.  This class schedules a thread to wake up every millisecond.  It tracks
 * how long after its expected wake up time it actually gets woken up and records the delay.<p/>
 *
 * This class starts tracking delays from the moment that this class is loaded into the JVM.<p/>
 *
 * Example:
 *
 * <pre>
 *     long delay0                 = getTotalDelaySoFarMillis();
 *
 *     // do work
 *
 *     long delay1                 = getTotalDelaySoFarMillis();
 *     long delayExperiencedMillis = delay1 - delay0;
 * </pre>
 */
public class SystemStallDetector {

    private volatile static long totalDelaySoFarMillis = 0;

    public static long getTotalDelaySoFarMillis() {
        return totalDelaySoFarMillis;
    }

    static {
        new Thread("SystemStallDetector") {
            {
                setDaemon( true );
            }

            public void run() {
                while (true) {
                    long t0 = System.nanoTime();

                    try {
                        sleep( 1 );

                        long t1         = System.nanoTime();
                        long delayMillis = (t1-t0)/1000000 - 1;

                        totalDelaySoFarMillis = Math.max(0, totalDelaySoFarMillis+delayMillis);
                    } catch ( InterruptedException e ) {
                        // ignore and try again
                    }
                }
            }
        }.start();
    }

    public static void main(String[] args) throws InterruptedException {
        while (true) {
            Thread.sleep(1000);

            System.out.println( "totalDelaySoFarMillis = " + getTotalDelaySoFarMillis() );
        }
    }

    /**
     * Starts SystemStallDetector by causing the JVM to load the class.
     */
    public static void init() {}
}
