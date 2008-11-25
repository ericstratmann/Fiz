package org.fiz;

import java.io.*;
import java.lang.management.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import org.apache.log4j.*;

/**
 * This class is an application that measures the performance of the
 * various operations on Strings.
 */

public class PerfString {
    protected static Logger logger = Logger.getLogger("org.fiz.Dispatcher");
    public static void main(String[] argv)
            throws IOException, NoSuchAlgorithmException,
            InvalidKeyException {
        int count = 10000;
        int value = 0;
        ArrayList<String> list = new ArrayList<String>();
        long sum = 0;
        Dataset response = null;
        Dataset d = null;
        Dataset properties = new Dataset("name", "Alice", "id", "id44");
        String rowId = "tree14_1";
        String edgeStyle = "solid";
        boolean expandable = true;
        String s = "3";
        Config.init("test/testData/WEB-INF/config");
        ClientRequest cr = new ClientRequestFixture();
        StringBuilder out = new StringBuilder();
        List<GarbageCollectorMXBean> beans =
                ManagementFactory.getGarbageCollectorMXBeans();
        long startNs = System.nanoTime();
        long startGcTime = 0;
        for (GarbageCollectorMXBean bean: beans) {
            startGcTime += bean.getCollectionTime();
        }
        Timer timer = new Timer();

        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
                out.setLength(0);
                StringBuilder ajax = Ajax.invoke(cr, "ajaxTreeExpand", null,
                        properties.get("id"), rowId);
                timer.start();
                Template.expand(" onclick=\"@1\"><img src=" +
                        "\"/fizlib/images/@2-@3.gif\"></td>\n",
                        out, ajax, edgeStyle,
                        (expandable ? "plus": "leaf"));
                timer.stop();
            }
            long finish = System.nanoTime();
            System.out.printf("%.4f us per iteration%n",
                    (finish - start)/(1000.0*count));
            printTimer(timer, "Template.expand");
            timer.reset();
            Timer t2 = Timer.getNamedTimer ("expandAtSign");
            printTimer(t2, "expandAtSign");
            t2.reset();
        }
        System.out.printf("String length: %d\n", out.length());

        long stopNs = System.nanoTime();
        long stopGcTime = 0;
        for (GarbageCollectorMXBean bean: beans) {
            stopGcTime += bean.getCollectionTime();
        }
        long gcTime = stopGcTime - startGcTime;
        System.out.printf("Garbage collection time: %dms (%.1f%%)\n",
                gcTime, 100.0*(gcTime*1000000.0)/(stopNs - startNs));
    }

    protected static void printTimer(Timer timer, String name) {
        String prefix = (name != null) ? name : "unknown";
            System.out.printf("%s timer avg: %.1fus, min: %.1fus, " +
                    "max: %.1fus, dev: %.1fus\n",
                    prefix,
                    timer.getAverage()/1000.0,
                    timer.getShortestInterval()/1000.0,
                    timer.getLongestInterval()/1000.0,
                    timer.getStdDeviation()/1000.0);
    }

    protected static int inc(int i) {
        return i+1;
    }
    protected static boolean stringEquals(String first, String second) {
        int length1 = first.length();
        int length2 = second.length();
        if (length1 != length2) {
            return false;
        }
        for (int i = 0; i <length1; i++) {
            if (first.charAt(i) != second.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    protected static int getInt(String s) {
        int result = 0;
        int count = s.length();
        for (int i = 0; i < count; i++) {
            int digit = s.charAt(i) - '0';
            if ((digit < 0) || (digit > 9)) {
                break;
            }
            result = result*10 + digit;
        }
        return result;
    }

    protected static void perfMac() throws NoSuchAlgorithmException,
            InvalidKeyException {
        int count = 10000;

        // Generate secret key for HMAC-MD5
        KeyGenerator kg = KeyGenerator.getInstance("HmacSHA256");
        SecretKey sk = kg.generateKey();
        // Get instance of Mac object implementing HMAC-MD5, and
        // initialize it with the above secret key
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(sk);
        byte[] input = ("123456789a123456789b").getBytes();
        byte[] result = null;

        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
                result = mac.doFinal(input);
            }
            long finish = System.nanoTime();
            System.out.printf("%.4f us per iteration%n",
                    (finish - start)/(1000.0*count));
        }
        System.out.printf("Input size: %d, result bytes: %d\n",
                input.length, result.length);
    }
}
