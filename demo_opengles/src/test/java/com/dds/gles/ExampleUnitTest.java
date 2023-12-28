package com.dds.gles;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        String aa = "{\n" +
                "\t\"supportRatios\": [\n" +
                "{\n" +
                "\"ratio\":\"9x16\",\n" +
                "\"width\": 420,\n" +
                "\"height\": 544,\t\n" +
                "\"translateX\": 0.05,\t\n" +
                "\"translateY\": -0.2\t\n" +
                "},\n" +
                "{\n" +
                "\"ratio\":\"default\",\n" +
                "\"width\":420,\n" +
                "\"height\":544,\n" +
                "\"translateX\": -0.237586,\t\n" +
                "\"translateY\": 0.20766\t\n" +
                "}\n" +
                "]\n" +
                "}";

        try {
            String encode = URLEncoder.encode(aa, "utf-8");
            System.out.println(encode);
        } catch (UnsupportedEncodingException e) {

        }
    }
}