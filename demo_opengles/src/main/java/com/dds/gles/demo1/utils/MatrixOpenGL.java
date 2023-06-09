package com.dds.gles.demo1.utils;

import android.opengl.Matrix;

public class MatrixOpenGL {

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
