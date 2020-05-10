package com.example.androidopenglesdemo.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {


    public static byte[] loadAsBuffer(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int len = -1;
            byte[] buf = new byte[64];
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            return baos.toByteArray();
        } catch (Exception ignored) {

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
    public static String loadAsString(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int len = -1;
            byte[] buf = new byte[64];
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            return baos.toString();
        } catch (Exception ignored) {

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
