package org.fiz;

import java.util.*;

/**
 * Junit tests for the Timer class.
 */
public class TimerTest extends junit.framework.TestCase {
    public void test_start() {
        Timer timer = new Timer();
        long first = System.nanoTime();
        timer.start();
        long second = System.nanoTime();
        if ((timer.startTime < first) || (timer.startTime > second)) {
            fail(String.format("Expected startTime between %d and %d, got %d",
                    first, second, timer.startTime));
        }
    }

    public void test_start_withTime() {
        Timer timer = new Timer();
        timer.start(417);
        assertEquals("startTime", 417, timer.startTime);
    }

    public void test_stop() {
        Timer timer = new Timer();
        long first = System.nanoTime();
        timer.start();
        timer.stop();
        long interval = System.nanoTime() - first;
        if ((timer.minInterval < 0) || (timer.minInterval > interval)) {
            fail(String.format("Expected minInterval between 0 and %d, got %d",
                    interval, timer.minInterval));
        }
    }

    public void test_stop_withTime() {
        Timer timer = new Timer();

        // First interval.
        timer.start(100);
        timer.stop(200);
        assertEquals("minInterval", 100, timer.minInterval);
        assertEquals("maxInterval", 100, timer.maxInterval);
        assertEquals("totalNs", 100.0, timer.totalNs);
        assertEquals("totalSquaredNs", 10000.0, timer.totalSquaredNs);
        assertEquals("numIntervals", 1, timer.numIntervals);

        // Second interval is a new minimum.
        timer.start(100);
        timer.stop(150);
        assertEquals("minInterval", 50, timer.minInterval);
        assertEquals("maxInterval", 100, timer.maxInterval);
        assertEquals("totalNs", 150.0, timer.totalNs);
        assertEquals("totalSquaredNs", 12500.0, timer.totalSquaredNs);
        assertEquals("numIntervals", 2, timer.numIntervals);

        // Third interval is a new maximum.
        timer.start(100);
        timer.stop(201);
        assertEquals("minInterval", 50, timer.minInterval);
        assertEquals("maxInterval", 101, timer.maxInterval);
    }

    public void test_reset() {
        Timer timer = new Timer();
        timer.start(100);
        timer.stop(200);
        timer.start(300);
        timer.start(500);
        timer.reset();
        timer.start(600);
        timer.stop(750);
        assertEquals("minInterval", 150, timer.minInterval);
        assertEquals("maxInterval", 150, timer.maxInterval);
        assertEquals("totalNs", 150.0, timer.totalNs);
        assertEquals("totalSquaredNs", 22500.0, timer.totalSquaredNs);
        assertEquals("numIntervals", 1, timer.numIntervals);
    }

    // The following test tests all of the getters for Timers, from
    // getAverage to getStdDeviation.

    public void test_getters() {
        Timer timer = new Timer();
        timer.start(100);
        timer.stop(103);
        timer.start(300);
        timer.stop(304);
        assertEquals("getAverage", 3.5, timer.getAverage());
        assertEquals("getCount", 2, timer.getCount());
        assertEquals("getLongestInterval", 4, timer.getLongestInterval());
        assertEquals("getShortestInterval", 3, timer.getShortestInterval());
        assertEquals("standard deviation", 0.5, timer.getStdDeviation());
    }

    public void test_getNamedTimer() {
        Timer.forgetNamedTimers();
        Timer timer1 = Timer.getNamedTimer("first");
        Timer timer2 = Timer.getNamedTimer("second");
        Timer timer3 = Timer.getNamedTimer("first");
        assertSame("same timer", timer1, timer3);
        assertNotSame("different timers", timer1, timer2);
        assertEquals("total number of timers", 2, Timer.namedTimers.size());
    }

    public void test_resetAll() {
        Timer timer1 = Timer.getNamedTimer("first");
        Timer timer2 = Timer.getNamedTimer("second");
        timer1.start(10);
        timer1.stop(20);
        timer2.start(10);
        timer2.stop(20);
        Timer.resetAll();
        assertEquals("first timer reset", 0, timer1.numIntervals);
        assertEquals("second timer reset", 0, timer2.numIntervals);
    }

    public void test_getStatistics() {
        Timer.forgetNamedTimers();
        Timer timer1 = Timer.getNamedTimer("first");
        Timer timer2 = Timer.getNamedTimer("second");
        timer1.start(100);        timer1.stop(103);
        timer1.start(300);        timer1.stop(304);
        timer2.start(500);        timer2.stop(600);
        ArrayList<Dataset> children = Timer.getStatistics(10.0, "%.2f");
        Collections.sort(children, new DatasetComparator("name",
                DatasetComparator.Type.STRING,
                DatasetComparator.Order.INCREASING));
        assertEquals("number of datasets", 2, children.size());
        assertEquals("info about timer1",
                "average:           0.35\n" +
                "intervals:         2\n" +
                "maximum:           0.40\n" +
                "minimum:           0.30\n" +
                "name:              first\n" +
                "standardDeviation: 0.05\n",
                children.get(0).toString());
        assertEquals("info about timer2",
                "average:           10.00\n" +
                "intervals:         1\n" +
                "maximum:           10.00\n" +
                "minimum:           10.00\n" +
                "name:              second\n" +
                "standardDeviation: 0.00\n",
                children.get(1).toString());
    }
    public void test_getStatistics_ignoreIdleTimers() {
        Timer.forgetNamedTimers();
        Timer timer1 = Timer.getNamedTimer("first");
        Timer timer2 = Timer.getNamedTimer("second");
        ArrayList<Dataset> children = Timer.getStatistics(10.0, "%.2f");
        assertEquals("number of datasets returned", 0, children.size());
    }

    public void test_clear() {
        Timer.forgetNamedTimers();
        Timer timer1 = Timer.getNamedTimer("first");
        Timer timer2 = Timer.getNamedTimer("second");
        assertEquals("size of namedTimers", 2, Timer.namedTimers.size());
        Timer.forgetNamedTimers();
        assertEquals("size of namedTimers", 0, Timer.namedTimers.size());
    }
}
