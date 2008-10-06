package org.fiz;

import java.io.*;
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
        Dataset d = null;
        int value = 0;
        ArrayList<String> list = new ArrayList<String>();
        long sum = 0;
        Dataset response = null;
        Config.init("test/testData/WEB-INF/config");

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
}
