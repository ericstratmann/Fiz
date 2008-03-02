package org.fiz;
import java.io.*;

/**
 * This class is an application that measures the performance of the
 * various operations on Strings.
 */
public class PerfString {
    public static void main(String[] argv) throws IOException {
        int count = 100000;
        int code = 0;
        String value = "xyz";
        String value2 = "projectName, procedureName, stepName, propertyName";
        String value3 = "0123456789012345678(";
        String compare = "first value";
        char[] chars = value3.toCharArray();
        StringBuilder s = new StringBuilder();
        StringBuilder builder1 = new StringBuilder("12345");
        StringBuilder builder2 = new StringBuilder("xxx12345aaa");
        char c = 'c';
        int result = 44;

        for (int i= 0; i < 10; i++) {
            value = new String("first " + "value");
            int length = value.length();
            long start = System.nanoTime();
            for (int j= 0; j < count; j++) {
//                code = inc(code);
//                code = value.hashCode();
//                code += (stringEquals(value, compare) ? 1 : 0);
//                code += (value.equals(compare) ? 1 : 0);
//                code += value.compareTo(compare);
//                s.delete(0, s.length());
//                for (int k = 0; k < value3.length(); k++) {
//                     s.append(value3.charAt(k));
//                }
//                code += value.length();
//                code += length;
//                code += Util.split(value2, ',').length;
//                code += value2.substring(10, 20).length();
//                ArrayList<String> array = new ArrayList<String>();
//                for (int k = 0; k < 5; k++) {
//                    array.add(value);
//                }
//                String[] array = new String[100];
//                for (int k = 0; k < 100; k++) {
//                    array[k] = value;
//                }
//                code = 0;
//                for (int k = 0; k < value3.length(); k++) {
//                    if (Character.isUnicodeIdentifierPart(value3.charAt(k))) {
//                        code++;
//                    }
//                }
//                StringBuilder builder3 = new StringBuilder();
//                code = builder1.toString().length();
                result = value3.indexOf("/", 30);
            }
            long finish = System.nanoTime();
            System.out.printf("%.4f us per iteration%n", (finish - start)/(1000.0*count));
        }
        System.out.printf("Result: %s\n", result);
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
