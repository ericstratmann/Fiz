package org.fiz;

/**
 * This class is used for testing: it manages a String object in a way
 * that allows changes to be detected.
 */

public class Message {
    protected String value;          // Current contents of string.
    protected int generation = 0;    // Incremented each time value is
                                     // changed.
    public Message() {
        value = new String();
    }

    public synchronized String getValue() {
        return value;
    }
    public synchronized void setValue(String newValue) {
        value = newValue;
        generation++;
        this.notifyAll();
    }
    public synchronized int waitChange(int oldGeneration) {
        while (generation == oldGeneration) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // Don't do anything; just retry.
            }
        }
        return generation;
    }
    public synchronized int getGeneration() {
        return generation;
    }
}
