package com.dds.gles.utils;

import android.content.Context;
import android.opengl.Matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OpenGLUtils {

    public static String readRawTextFile(Context context, int rawId) {
        InputStream is = context.getResources().openRawResource(rawId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Scale operations on matrices
     *
     * @param matrix matrix array
     * @param x Scale along the x-axis
     * @param y Scale along the y-axis
     */
    public static void scaleMatrix(float[] matrix, int x, int y) {
        Matrix.translateM(matrix, 0, 0.5f, 0.5f, 0);
        Matrix.scaleM(matrix, 0, x, y, 1);
        Matrix.translateM(matrix, 0, -0.5f, -0.5f, 0);
    }

    /**
     * Rotate the matrix
     *
     * @param matrix matrix array
     * @param degree Rotation angle
     */
    public static void rotateMatrix(float[] matrix, int degree) {
        Matrix.translateM(matrix, 0, 0.5f, 0.5f, 0);
        Matrix.rotateM(matrix, 0, degree, 0, 0, 1);
        Matrix.translateM(matrix, 0, -0.5f, -0.5f, 0);
    }

}
