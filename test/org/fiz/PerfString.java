package org.fiz;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * This class is an application that measures the performance of the
 * various operations on Strings.
 */

public class PerfString {
    protected static Logger logger = Logger.getLogger("org.fiz.Dispatcher");
    public static void main(String[] argv) throws IOException, SQLException {
        int count = 10;
        Dataset d = null;
        int value = 0;
        ArrayList<String> list = new ArrayList<String>();
        long sum = 0;
        Dataset response = null;
        Config.init("test/testData/WEB-INF/config");
        SqlDataManager manager = (SqlDataManager)
                DataManager.getDataManager("sql");
        Dataset row = new Dataset("name", "California",
                "capital", "Sacramento");

        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
                response = manager.findWithSql("SELECT * FROM people;");
            }
            long finish = System.nanoTime();
            System.out.printf("%.4f us per iteration%n",
                    (finish - start)/(1000.0*count));
        }
        // System.out.printf("Response dataset:\n%s", response.toString());
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
